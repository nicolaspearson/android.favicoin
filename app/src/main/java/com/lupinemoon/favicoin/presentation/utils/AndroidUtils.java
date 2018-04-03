package com.lupinemoon.favicoin.presentation.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import timber.log.Timber;

public class AndroidUtils {

    public static void hideKeyboard(Activity activity) {
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static void getDeviceScreenDensity(int density) {
        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
                Timber.d("Device Screen Density: LDPI (%d)", density);
                break;

            case DisplayMetrics.DENSITY_MEDIUM:
                Timber.d("Device Screen Density: MDPI (%d)", density);
                break;

            case DisplayMetrics.DENSITY_HIGH:
                Timber.d("Device Screen Density: HDPI (%d)", density);
                break;

            case DisplayMetrics.DENSITY_XHIGH:
                Timber.d("Device Screen Density: XHDPI (%d)", density);
                break;

            case DisplayMetrics.DENSITY_XXHIGH:
                Timber.d("Device Screen Density: XXHDPI (%d)", density);
                break;

            case DisplayMetrics.DENSITY_XXXHIGH:
                Timber.d("Device Screen Density: XXXHDPI (%d)", density);
                break;

            case DisplayMetrics.DENSITY_560:

            default:
                Timber.d("Device Screen Density: Unknown (%d)", density);
                break;
        }
    }
}
