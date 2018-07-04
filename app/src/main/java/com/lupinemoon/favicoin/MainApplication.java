package com.lupinemoon.favicoin;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.lupinemoon.favicoin.data.analytics.AnalyticsService;
import com.lupinemoon.favicoin.data.storage.AppRepository;
import com.lupinemoon.favicoin.data.storage.interfaces.BitmapCache;
import com.lupinemoon.favicoin.data.storage.interfaces.Storage;
import com.lupinemoon.favicoin.data.storage.local.AppLocalDataStore;
import com.lupinemoon.favicoin.data.storage.prefs.SharedPreferencesStorage;
import com.lupinemoon.favicoin.data.storage.prefs.TransitionBitmapCache;
import com.lupinemoon.favicoin.presentation.utils.Constants;
import com.lupinemoon.favicoin.presentation.widgets.AppLifecycleHandler;
import com.squareup.leakcanary.LeakCanary;

import io.fabric.sdk.android.Fabric;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.realm.Realm;
import timber.log.Timber;

public class MainApplication extends Application {

    private static Storage storage;

    private static BitmapCache bitmapCache;

    private static boolean loggedIn = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Realm
        configureRealm();

        // Initialize Fabric
        configureFabric();

        // Turn on strict mode after realm and fabric have been initialized
        turnOnStrictMode();

        // Initialize Leak Canary
        configureLeakCanary();

        // Initialize Calligraphy
        configureCalligraphy();

        registerActivityLifecycleCallbacks(new AppLifecycleHandler());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashlyticsLogTree());
        }

        // Initialize Storage
        getStorage(this);

        // Increment the launch count
        int launchCount = getStorage(getApplicationContext()).getInt(Constants.KEY_LAUNCH_COUNT, 0);
        launchCount++;
        Timber.d("App Launch Count: %d", launchCount);
        getStorage(getApplicationContext()).putInt(Constants.KEY_LAUNCH_COUNT, launchCount);

        analyticsReportPlayServicesVersion();
        analyticsReportAndroidOsVersion();
    }

    @Override
    public void onTerminate() {
        if (Realm.getDefaultInstance() != null) {
            Realm.getDefaultInstance().close();
            AppRepository.getInstance().onTerminate();
        }
        super.onTerminate();
    }

    public static Storage getStorage(Context context) {
        if (storage == null) {
            storage = new SharedPreferencesStorage(context);
        }
        return storage;
    }

    public static BitmapCache getBitmapCache() {
        if (bitmapCache == null) {
            bitmapCache = new TransitionBitmapCache(storage);
        }
        return bitmapCache;
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    public static void setLoggedIn(boolean loggedIn) {
        MainApplication.loggedIn = loggedIn;
    }

    public void analyticsReportPlayServicesVersion() {
        try {
            String systemPlayServicesVersion = getPackageManager().getPackageInfo(
                    "com.google.android.gms",
                    0).versionName;

            if (storage.contains(Constants.KEY_PLAY_SERVICES_VERSION)) {
                // We have stored the version, compare and update if it has changed
                String storedPlayServicesVersion = storage.getString(Constants.KEY_PLAY_SERVICES_VERSION);
                if (!TextUtils.isEmpty(storedPlayServicesVersion) && !storedPlayServicesVersion.equalsIgnoreCase(
                        systemPlayServicesVersion)) {
                    storage.putString(
                            Constants.KEY_PLAY_SERVICES_VERSION,
                            systemPlayServicesVersion);
                    reportAnalyticsPlayServicesVersion(systemPlayServicesVersion);
                }
            } else {
                // Doesn't exist, store and send an event
                storage.putString(Constants.KEY_PLAY_SERVICES_VERSION, systemPlayServicesVersion);
                reportAnalyticsPlayServicesVersion(systemPlayServicesVersion);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e("Package Name not found: %s", e.toString());
        }
    }

    private void reportAnalyticsPlayServicesVersion(String playServicesVersion) {
        Timber.d("Analytics: Play services version: %s", playServicesVersion);
        AnalyticsService.getInstance().reportPlayServicesVersion(!TextUtils.isEmpty(
                playServicesVersion) ? playServicesVersion : "Not Installed");
    }

    private void analyticsReportAndroidOsVersion() {
        try {
            String androidOsVersion = Build.VERSION.RELEASE;
            Timber.d("Installed Android OS Version: %s", androidOsVersion);

            if (storage.contains(Constants.KEY_ANDROID_OS_VERSION)) {
                // We have stored the version, compare and and update if it has changed
                String storedAndroidOsVersion = storage.getString(Constants.KEY_ANDROID_OS_VERSION);
                if (!TextUtils.isEmpty(storedAndroidOsVersion) && !storedAndroidOsVersion.equalsIgnoreCase(
                        androidOsVersion)) {
                    storage.putString(
                            Constants.KEY_ANDROID_OS_VERSION,
                            androidOsVersion);
                    reportAnalyticsAndroidOsVersion(androidOsVersion);
                }
            } else {
                // Doesn't exist, store and send event
                storage.putString(Constants.KEY_ANDROID_OS_VERSION, androidOsVersion);
                reportAnalyticsAndroidOsVersion(androidOsVersion);
            }
        } catch (Exception e) {
            Timber.e("Android OS Version Exception: %s", e.toString());
        }
    }

    private void reportAnalyticsAndroidOsVersion(String osVersion) {
        Timber.d("Analytics: OS version: %s", osVersion);
        AnalyticsService.getInstance().reportAndroidOsVersion(osVersion);
    }

    private void turnOnStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
    }

    private void configureFabric() {
        Fabric.Builder builder =
                new Fabric.Builder(this).kits(
                        new Crashlytics.Builder().core(
                                new CrashlyticsCore.Builder()
                                        .disabled(false)
                                        .build()
                        ).build()
                ).appIdentifier(BuildConfig.APPLICATION_ID);
        Fabric.with(builder.build());
    }

    private void configureRealm() {
        Realm.init(this);
        Realm.setDefaultConfiguration(AppLocalDataStore.getRealmConfiguration());
        Timber.d("Realm File Path: %s", Realm.getDefaultInstance().getPath());
    }

    private void configureLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    private void configureCalligraphy() {
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .setFontMapper(font -> font)
                                .build()))
                .build());
    }

    private class CrashlyticsLogTree extends Timber.DebugTree {
        @Override
        protected void log(int priority, String tag, @NonNull String message, Throwable throwable) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO || priority == Log.WARN) {
                return;
            }

            Crashlytics.setInt(Constants.CRASHLYTICS_KEY_PRIORITY, priority);
            Crashlytics.setString(Constants.CRASHLYTICS_KEY_TAG, tag);
            Crashlytics.setString(Constants.CRASHLYTICS_KEY_MESSAGE, message);
            if (throwable == null && !TextUtils.isEmpty(message)) {
                Crashlytics.logException(new Exception(message));
            } else if (throwable != null) {
                if (!TextUtils.isEmpty(message)) {
                    Throwable throwableWithMessage = new Throwable(message, throwable);
                    Crashlytics.logException(throwableWithMessage);
                } else {
                    Crashlytics.logException(throwable);
                }

            }
        }
    }
}
