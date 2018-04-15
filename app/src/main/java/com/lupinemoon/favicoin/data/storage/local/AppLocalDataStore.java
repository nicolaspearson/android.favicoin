package com.lupinemoon.favicoin.data.storage.local;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.lupinemoon.favicoin.BuildConfig;
import com.lupinemoon.favicoin.data.models.AuthToken;
import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.data.models.Coins;
import com.lupinemoon.favicoin.data.models.CryptoCompareCoin;
import com.lupinemoon.favicoin.data.models.NetworkHeader;
import com.lupinemoon.favicoin.data.models.NetworkMediaType;
import com.lupinemoon.favicoin.data.models.NetworkRequest;
import com.lupinemoon.favicoin.data.models.NetworkRequestBody;
import com.lupinemoon.favicoin.data.storage.interfaces.AppDataStore;
import com.lupinemoon.favicoin.presentation.services.rxbus.RxBus;
import com.lupinemoon.favicoin.presentation.services.rxbus.events.UpdatedCoinsEvent;
import com.lupinemoon.favicoin.presentation.utils.Constants;
import com.lupinemoon.favicoin.presentation.utils.FileUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import timber.log.Timber;

public class AppLocalDataStore implements AppDataStore {

    private static AppLocalDataStore appLocalDataStore = new AppLocalDataStore();

    private AppLocalDataStore() {
        // Empty Constructor
    }

    public static synchronized AppLocalDataStore getInstance() {
        if (appLocalDataStore == null) {
            appLocalDataStore = new AppLocalDataStore();
        }
        return appLocalDataStore;
    }

    public static RealmConfiguration getRealmConfiguration() {
        return new RealmConfiguration
                .Builder()
                .name(String.format(Locale.getDefault(), "%s_db.realm", BuildConfig.FLAVOR))
                .deleteRealmIfMigrationNeeded() // https://realm.io/docs/java/latest/#migrations
                .build();
    }

    private Realm getRealm() {
        Timber.d("Open Realm: Thread: %s", Thread.currentThread().getName());
        try {
            return Realm.getInstance(getRealmConfiguration());
        } catch (Exception e) {
            Timber.e(e, "getRealm() Failed");
        }
        return Realm.getDefaultInstance();
    }

    private void closeRealm(Realm realm) {
        Timber.d("Close Realm: Thread: %s", Thread.currentThread().getName());
        try {
            if (realm != null) {
                realm.close();
            }
        } catch (Exception e) {
            Timber.e(e, "closeRealm(Realm realm) Failed");
        }
    }

    public void clearRealmDatabase() {
        Timber.w("Clearing Realm...");
        Realm iRealm = getRealm();
        try {
            iRealm.executeTransaction(realm -> realm.deleteAll());
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            closeRealm(iRealm);
        }
    }

    @Override
    public Flowable<AuthToken> doLogin(
            Context context, String username, String password) {
        // Never done locally
        return null;
    }

    public Flowable<File> getRealmDatabaseFile(File tempDirectory) {
        Realm iRealm = getRealm();
        // Get or create an "export.realm" file
        File exportRealmFile = new File(tempDirectory, "export.realm");
        try {
            // If "export.realm" already exists, delete it
            boolean deleted = exportRealmFile.delete();
            if (deleted) {
                // Copy the current realm database to "export.realm"
                iRealm.writeCopyTo(exportRealmFile);
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            closeRealm(iRealm);
        }
        return Flowable.just(exportRealmFile);
    }

    public Flowable<List<NetworkRequest>> getNetworkRequests() {
        Realm iRealm = getRealm();
        List<NetworkRequest> networkRequests = new ArrayList<>();
        try {
            RealmResults<NetworkRequest> result = iRealm.where(NetworkRequest.class).findAll();
            if (result != null) {
                // Get detached object from realm
                Collection<NetworkRequest> detachedRequests = iRealm.copyFromRealm(result);
                Timber.d("getNetworkRequests: %s", detachedRequests.size());
                networkRequests.addAll(detachedRequests);
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            closeRealm(iRealm);
        }
        return Flowable.just(networkRequests);
    }

    public void saveNetworkRequest(final NetworkRequest networkRequest) {
        Realm iRealm = getRealm();
        Timber.d("%s: saveNetworkRequest %s", this.getClass().getSimpleName(), networkRequest);
        try {
            iRealm.executeTransaction(realm -> realm.copyToRealmOrUpdate(networkRequest));
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            closeRealm(iRealm);
        }
    }

    private void removeNetworkHeader(NetworkHeader networkHeader) {
        Realm iRealm = getRealm();
        Timber.d("removeNetworkHeader");
        try {
            final RealmResults<NetworkHeader> result = iRealm
                    .where(NetworkHeader.class)
                    .equalTo("primaryKey", networkHeader.getPrimaryKey())
                    .findAll();
            if (result != null) {
                iRealm.executeTransaction(realm -> result.deleteAllFromRealm());
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            closeRealm(iRealm);
        }
    }

    private void removeNetworkMediaType(NetworkMediaType networkMediaType) {
        Realm iRealm = getRealm();
        Timber.d("removeNetworkMediaType");
        try {
            final RealmResults<NetworkMediaType> result = iRealm
                    .where(NetworkMediaType.class)
                    .equalTo("primaryKey", networkMediaType.getPrimaryKey())
                    .findAll();
            if (result != null) {
                iRealm.executeTransaction(realm -> result.deleteAllFromRealm());
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            closeRealm(iRealm);
        }
    }

    public void removeNetworkRequest(NetworkRequest networkRequest) {
        Realm iRealm = getRealm();
        Timber.d("removeNetworkRequest");
        try {
            final RealmResults<NetworkRequest> result = iRealm
                    .where(NetworkRequest.class)
                    .equalTo("primaryKey", networkRequest.getPrimaryKey())
                    .findAll();
            if (result != null) {
                iRealm.executeTransaction(realm -> result.deleteAllFromRealm());
            }
            removeNetworkHeader(networkRequest.getNetworkHeader());
            removeNetworkMediaType(networkRequest.getNetworkRequestBody().getNetworkMediaType());
            removeNetworkRequestBody(networkRequest.getNetworkRequestBody());
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            closeRealm(iRealm);
        }
    }

    private void removeNetworkRequestBody(NetworkRequestBody networkRequestBody) {
        Realm iRealm = getRealm();
        Timber.d("removeNetworkRequestBody");
        try {
            final RealmResults<NetworkRequestBody> result = iRealm
                    .where(NetworkRequestBody.class)
                    .equalTo("primaryKey", networkRequestBody.getPrimaryKey())
                    .findAll();
            if (result != null) {
                iRealm.executeTransaction(realm -> result.deleteAllFromRealm());
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            closeRealm(iRealm);
        }
    }

    // region Coins API
    @Override
    public Flowable<Coins> getCoins(Context context, int start, int limit) {
        Realm iRealm = getRealm();
        Coins coins = new Coins();
        coins.setSource(Constants.SOURCE_REALM);
        RealmList<CoinItem> coinItems = new RealmList<>();
        try {
            RealmResults<CoinItem> result = iRealm.where(CoinItem.class).findAll();
            if (result != null) {
                // Get detached object from realm
                Collection<CoinItem> detachedCoinItems = iRealm.copyFromRealm(result);
                List<CoinItem> paginated = new ArrayList<>();
                try {
                    coinItems.addAll(detachedCoinItems);
                    if (start > 1) {
                        paginated = coinItems.subList(start - 1, limit - 1);
                    } else {
                        paginated.addAll(detachedCoinItems);
                    }
                } catch (Exception e) {
                    paginated.clear();
                    paginated.addAll(detachedCoinItems);
                }
                Timber.d("getCoins: %s", paginated);
                coinItems.clear();
                coinItems.addAll(paginated);
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            closeRealm(iRealm);
        }
        coins.setCoinItems(coinItems);
        return Flowable.just(coins);
    }

    public Coins saveCoins(final List<CoinItem> coinItems, boolean postEvent) {
        Realm iRealm = getRealm();
        Timber.d("saveCoins: %s", coinItems);
        try {
            for (final CoinItem item : coinItems) {
                CryptoCompareCoin cryptoCompareCoin = this.fetchCryptoCompareCoin(
                        item.getSymbol(),
                        item.getName());
                if (cryptoCompareCoin != null) {
                    item.setCryptoCompareCoin(cryptoCompareCoin);
                }
                CoinItem existingCoinItem = this.fetchCoin(item.getId());
                if (existingCoinItem != null) {
                    item.setFavourite(existingCoinItem.isFavourite());
                }
            }
            iRealm.executeTransaction(realm -> realm.copyToRealmOrUpdate(coinItems));
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            closeRealm(iRealm);
        }

        Coins coins = new Coins();
        coins.setSource(Constants.SOURCE_REALM);
        RealmList<CoinItem> updatedCoinItems = new RealmList<>();
        updatedCoinItems.addAll(coinItems);
        coins.setCoinItems(updatedCoinItems);
        if (postEvent) {
            RxBus.getDefault().post(new UpdatedCoinsEvent(coins));
        }
        return coins;
    }

    private CoinItem fetchCoin(String id) {
        Realm iRealm = getRealm();
        try {
            CoinItem coinItem = iRealm
                    .where(CoinItem.class)
                    .equalTo("id", id)
                    .findFirst();
            if (coinItem != null) {
                // Get detached object from realm
                CoinItem detachedValue = iRealm.copyFromRealm(coinItem);
                Timber.d("fetchCoin: %s", detachedValue);
                if (detachedValue != null) {
                    return detachedValue;
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            closeRealm(iRealm);
        }
        Timber.d("fetchCoin: ID: %s", id);
        return null;
    }
    //endregion

    // region Favourites API
    @Override
    public Flowable<Coins> getFavourites() {
        Realm iRealm = getRealm();
        Coins coins = new Coins();
        coins.setSource(Constants.SOURCE_REALM);
        RealmList<CoinItem> coinItems = new RealmList<>();
        try {
            RealmResults<CoinItem> result = iRealm.where(CoinItem.class).equalTo(
                    "isFavourite",
                    true).findAll();
            if (result != null) {
                // Get detached object from realm
                Collection<CoinItem> detachedCoinItems = iRealm.copyFromRealm(result);
                Timber.d("getFavourites: %s", detachedCoinItems);
                coinItems.addAll(detachedCoinItems);
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            closeRealm(iRealm);
        }
        coins.setCoinItems(coinItems);
        return Flowable.just(coins);
    }

    @Override
    public Flowable<CoinItem> toggleFavourite(final CoinItem coinItem, boolean isFavourite) {
        Realm iRealm = getRealm();
        coinItem.setFavourite(isFavourite);
        Timber.d("saveFavourite: %s", coinItem);
        try {
            iRealm.executeTransaction(realm -> realm.copyToRealmOrUpdate(coinItem));
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            closeRealm(iRealm);
        }

        return Flowable.just(coinItem);
    }
    // endregion

    // region Crypto Compare API
    @Override
    public Completable loadCryptoCompareCoins(Context context) {
        if (getCryptoCompareCoins().size() < 1) {
            Realm iRealm = getRealm();
            try {
                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = (JsonObject) jsonParser.parse(FileUtils.getJsonFileFromAssets(
                        context,
                        BuildConfig.CRYPTO_COMPARE_FILENAME));
                JsonArray jsonArray = jsonObject.getAsJsonArray("Data");
                Type listType = new TypeToken<List<CryptoCompareCoin>>() {
                }.getType();
                final List<CryptoCompareCoin> cryptoCompareCoins = new Gson().fromJson(
                        jsonArray,
                        listType);
                iRealm.executeTransaction(realm -> realm.copyToRealmOrUpdate(cryptoCompareCoins));
            } catch (Exception e) {
                Timber.e(e);
                return Completable.error(e);
            } finally {
                closeRealm(iRealm);
            }
        }
        return Completable.complete();
    }

    private RealmList<CryptoCompareCoin> getCryptoCompareCoins() {
        Realm iRealm = getRealm();
        RealmList<CryptoCompareCoin> coins = new RealmList<>();
        try {
            RealmResults<CryptoCompareCoin> result = iRealm.where(CryptoCompareCoin.class).findAll();
            if (result != null) {
                // Get detached object from realm
                Collection<CryptoCompareCoin> detachedCoins = iRealm.copyFromRealm(result);
                Timber.d("getCoins: %s", detachedCoins);
                coins.addAll(detachedCoins);
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            closeRealm(iRealm);
        }
        return coins;
    }

    private CryptoCompareCoin fetchCryptoCompareCoin(String symbol, String name) {
        Realm iRealm = getRealm();
        try {
            CryptoCompareCoin cryptoCompareCoin = iRealm
                    .where(CryptoCompareCoin.class)
                    .beginGroup()
                    .equalTo("symbol", symbol)
                    .or()
                    .equalTo("coinName", name)
                    .endGroup()
                    .findFirst();
            if (cryptoCompareCoin != null) {
                // Get detached object from realm
                CryptoCompareCoin detachedValue = iRealm.copyFromRealm(cryptoCompareCoin);
                Timber.d("fetchCryptoCompareCoin: %s", detachedValue);
                if (detachedValue != null) {
                    return detachedValue;
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            closeRealm(iRealm);
        }
        Timber.d("fetchCryptoCompareCoin: MISSED - SYM: %s | NAME: %s", symbol, name);
        return null;
    }
    // endregion
}
