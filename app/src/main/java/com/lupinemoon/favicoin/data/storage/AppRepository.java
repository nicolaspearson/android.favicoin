package com.lupinemoon.favicoin.data.storage;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.lupinemoon.favicoin.BuildConfig;
import com.lupinemoon.favicoin.MainApplication;
import com.lupinemoon.favicoin.data.models.AuthToken;
import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.data.models.Coins;
import com.lupinemoon.favicoin.data.models.NetworkRequest;
import com.lupinemoon.favicoin.data.network.rest.ServiceGenerator;
import com.lupinemoon.favicoin.data.storage.interfaces.AppDataStore;
import com.lupinemoon.favicoin.data.storage.local.AppLocalDataStore;
import com.lupinemoon.favicoin.data.storage.remote.AppRemoteDataStore;
import com.lupinemoon.favicoin.presentation.services.rxbus.RxBus;
import com.lupinemoon.favicoin.presentation.services.rxbus.events.PostRequestFailedEvent;
import com.lupinemoon.favicoin.presentation.services.rxbus.events.QueueProcessingComplete;
import com.lupinemoon.favicoin.presentation.services.rxbus.events.QueueProcessingStarted;
import com.lupinemoon.favicoin.presentation.utils.NetworkUtils;

import org.reactivestreams.Publisher;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import timber.log.Timber;

import static io.reactivex.Flowable.combineLatest;
import static io.reactivex.Flowable.mergeDelayError;

public class AppRepository implements AppDataStore {

    private static AppRepository appRepository = new AppRepository();

    private AppLocalDataStore appLocalDataStore;
    private AppRemoteDataStore appRemoteDataStore;

    private boolean processingQueue;

    private final long queueInterval = 5000;

    private CompositeDisposable retryDisposables = new CompositeDisposable();

    private CompositeDisposable rxBusEvents;

    private AppRepository() {
        appLocalDataStore = AppLocalDataStore.getInstance();
        appRemoteDataStore = AppRemoteDataStore.getInstance(this);
        processingQueue = false;
        createRxBusSubscriptions();
    }

    public static synchronized AppRepository getInstance() {
        if (appRepository == null) {
            appRepository = new AppRepository();
        }
        return appRepository;
    }

    /**
     * Receives the events from the RxBus
     * Extracts the request properties and persists to database.
     * The NetworkRequest.class is used to compose the data structure for query operations
     */
    private void createRxBusSubscriptions() {
        rxBusEvents = new CompositeDisposable();
        rxBusEvents.add(
                RxBus.getDefault().observeEvents(PostRequestFailedEvent.class)
                        .observeOn(Schedulers.io())
                        .subscribe(new Consumer<PostRequestFailedEvent>() {
                            @Override
                            public void accept(@NonNull PostRequestFailedEvent postRequestFailedEvent) {
                                Timber.w("POST Request Failed");
                                Request request = postRequestFailedEvent.getRequest();
                                NetworkRequest networkRequest = new NetworkRequest(
                                        request.method(),
                                        request.url().toString(),
                                        request.headers(),
                                        request.body()
                                );
                                appLocalDataStore.saveNetworkRequest(networkRequest);
                            }
                        }));

    }

    public void onTerminate() {
        if (rxBusEvents != null) {
            rxBusEvents.clear();
            rxBusEvents = null;
        }
    }

    public Flowable<File> getRealmDatabaseFile(File tempDirectory) {
        return appLocalDataStore.getRealmDatabaseFile(tempDirectory);
    }

    public void clearRealmDatabase() {
        appLocalDataStore.clearRealmDatabase();
    }

    public void retryNetworkRequests(final Context context) {
        if (!processingQueue) {
            processingQueue = true;
            retryDisposables.add(
                    appLocalDataStore.getNetworkRequests()
                            .concatMapIterable(new Function<List<NetworkRequest>, Iterable<NetworkRequest>>() {
                                @Override
                                public Iterable<NetworkRequest> apply(@NonNull List<NetworkRequest> networkRequests) {
                                    return networkRequests;
                                }
                            })
                            .concatMap(new Function<NetworkRequest, Publisher<NetworkRequest>>() {
                                @Override
                                public Publisher<NetworkRequest> apply(@NonNull NetworkRequest networkRequest) {
                                    return Flowable.just(networkRequest).delay(
                                            queueInterval,
                                            TimeUnit.MILLISECONDS);
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .subscribe(new Consumer<NetworkRequest>() {
                                @Override
                                public void accept(@NonNull final NetworkRequest networkRequest) {
                                    try {
                                        Timber.d(
                                                "%s: Retrying request queue.",
                                                this.getClass().getSimpleName());

                                        Timber.d("Queue Exists: Posting Queue Started Event");
                                        RxBus.getDefault().post(new QueueProcessingStarted());

                                        Call call = ServiceGenerator.retryRequest(
                                                context,
                                                networkRequest.getMethod(),
                                                networkRequest.getUrl(),
                                                networkRequest.getNetworkHeader().getHeaders(),
                                                networkRequest.getNetworkRequestBody().getSourceRequestBody());
                                        if (call != null) {
                                            call.enqueue(new Callback() {
                                                @Override
                                                public void onResponse(
                                                        @NonNull Call call,
                                                        @NonNull okhttp3.Response response) {
                                                    // We can remove the request from the queue if it
                                                    // was sent, we might not get a successful result
                                                    // i.e. 400 on some requests if already true on API
                                                    if (NetworkUtils.hasActiveNetworkConnection(
                                                            context)) {
                                                        Timber.d(
                                                                "%s: Retry queue %s item successfully called.",
                                                                this.getClass().getSimpleName(),
                                                                call);
                                                        appLocalDataStore.removeNetworkRequest(
                                                                networkRequest);
                                                        try {
                                                            // This is used to perform post processing
                                                            // of certain API calls, e.g. sendMessage
                                                            if (response.isSuccessful()
                                                                    && response.body() != null
                                                                    && response.request() != null
                                                                    && response.request().url() != null
                                                                    && !TextUtils.isEmpty(response.request().url().toString())) {

                                                                String url = response.request().url().toString();
                                                                Timber.d("Retry url: %s", url);
                                                            }
                                                        } catch (Exception e) {
                                                            Timber.w(
                                                                    e,
                                                                    "Post Processing Request Failed");
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onFailure(
                                                        @NonNull Call call,
                                                        @NonNull IOException e) {
                                                    Timber.w(e, "Process Request Failed");
                                                }
                                            });
                                        }
                                    } catch (Exception e) {
                                        Timber.w(e, "Process Request Failed");
                                    }
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(@NonNull Throwable throwable) {
                                    Timber.w(throwable, "Process Request Failed");
                                    RxBus.getDefault().post(new QueueProcessingComplete());
                                    processingQueue = false;
                                }
                            }, new Action() {
                                @Override
                                public void run() {
                                    Timber.d("Process Request Complete");
                                    RxBus.getDefault().post(new QueueProcessingComplete());
                                    processingQueue = false;
                                }
                            }));
        }
    }


    // region Auth API
    public String getAuthString(Context context) {
        AuthToken authToken = (AuthToken) MainApplication.getStorage(context).getObject(
                com.lupinemoon.favicoin.presentation.utils.Constants.KEY_AUTH_TOKEN,
                AuthToken.class);
        if (authToken != null) {
            return String.format("Bearer %s", authToken.getToken());
        } else {
            return com.lupinemoon.favicoin.presentation.utils.Constants.KEY_NO_AUTH_TOKEN;
        }
    }

    public void clearAuthString(Context context) {
        MainApplication.getStorage(context).del(com.lupinemoon.favicoin.presentation.utils.Constants.KEY_AUTH_TOKEN);
    }

    @Override
    public Flowable<AuthToken> doLogin(
            Context context, String username, String password) {
        // Never done locally
        return appRemoteDataStore.doLogin(context, username, password);
    }
    // endregion

    // region Coins API
    @Override
    public Flowable<Coins> getCoins(Context context, int start, int limit) {
        if (!NetworkUtils.isConnected(context)) {
            // NO CONNECTION
            // Return local result only
            return appLocalDataStore.getCoins(context, start, limit)
                    .subscribeOn(Schedulers.io())
                    .delay(BuildConfig.LOCAL_REPO_SOURCE_DELAY, TimeUnit.MILLISECONDS)
                    .filter(new Predicate<Coins>() {
                        @Override
                        public boolean test(@NonNull Coins coins) throws Exception {
                            if (coins.getCoinItems() != null && coins.getCoinItems().size() > 0) {
                                return true;
                            } else {
                                throw new Exception();
                            }
                        }
                    });
        } else if (BuildConfig.LOCAL_REPO_COMBINE_SOURCES && NetworkUtils.isConnectedFast(context)) {
            // FAST CONNECTION (3G / 4G / WIFI)
            // Fire both calls in sequence and combine the results
            // removing duplicates before submitting the merged result
            return combineLatest(
                    appLocalDataStore.getCoins(context, start, limit)
                            .subscribeOn(Schedulers.io()),
                    appRemoteDataStore.getCoins(context, start, limit)
                            .subscribeOn(Schedulers.io()),
                    new BiFunction<Coins, Coins, Coins>() {
                        @Override
                        public Coins apply(
                                @NonNull Coins localCoins,
                                @NonNull Coins networkCoins) {
                            RealmList<CoinItem> resultCoinItemList = new RealmList<>();
                            Coins savedCoins = appLocalDataStore.saveCoins(networkCoins.getCoinItems(), false);
                            try {
                                if (savedCoins.getCoinItems() != null && savedCoins.getCoinItems().size() > 0) {
                                    Timber.d("savedCoinItems: %s", savedCoins.getCoinItems());
                                    // Prefer saved coins
                                    return savedCoins;
                                } else if (localCoins.getCoinItems() != null && localCoins.getCoinItems().size() > 0) {
                                    // Fail over to local coins
                                    Timber.d("localCoinItems: %s", localCoins.getCoinItems());
                                    return localCoins;
                                }
                            } catch (Exception e) {
                                Timber.e(e, "Coin List Combine Latest Failed");
                            }

                            // No result
                            Coins coins = new Coins();
                            coins.setCoinItems(resultCoinItemList);
                            return coins;
                        }
                    });
        } else {
            // SLOW CONNECTION
            // Fire both calls simultaneously on separate threads
            // Return first result, refresh on subsequent result
            return mergeDelayError(
                    appLocalDataStore.getCoins(context, start, limit)
                            .subscribeOn(Schedulers.io()),
                    appRemoteDataStore.getCoins(context, start, limit)
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .doOnNext(new Consumer<Coins>() {
                                @Override
                                public void accept(Coins coins) {
                                    // Save locally
                                    appLocalDataStore.saveCoins(coins.getCoinItems(), true);
                                }
                            }));
        }
    }
    // endregion

    // region Favourites API
    @Override
    public Flowable<Coins> getFavourites() {
        // Return local result
        return appLocalDataStore.getFavourites()
                .subscribeOn(Schedulers.io())
                .delay(BuildConfig.LOCAL_REPO_SOURCE_DELAY, TimeUnit.MILLISECONDS)
                .filter(new Predicate<Coins>() {
                    @Override
                    public boolean test(@NonNull Coins coins) throws Exception {
                        if (coins.getCoinItems() != null && coins.getCoinItems().size() > 0) {
                            return true;
                        } else {
                            throw new Exception();
                        }
                    }
                });
    }

    @Override
    public Flowable<CoinItem> toggleFavourite(CoinItem coinItem, boolean isFavourite) {
        // Return local result
        return appLocalDataStore.toggleFavourite(coinItem, isFavourite)
                .subscribeOn(Schedulers.io())
                .delay(BuildConfig.LOCAL_REPO_SOURCE_DELAY, TimeUnit.MILLISECONDS);
    }
    // endregion

    // region Crypto Compare API
    @Override
    public Completable loadCryptoCompareCoins(Context context) {
        return appLocalDataStore.loadCryptoCompareCoins(context).subscribeOn(
                Schedulers.io());
    }
    // endregion

}
