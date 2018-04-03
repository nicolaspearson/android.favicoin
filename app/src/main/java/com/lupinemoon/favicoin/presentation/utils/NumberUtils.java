package com.lupinemoon.favicoin.presentation.utils;

import android.text.TextUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import timber.log.Timber;

public class NumberUtils {

    private static final String DECIMAL_FORMAT = "###,###.######";

    public static String formatNumberWithSpaces(String value) {
        if (!TextUtils.isEmpty(value)) {
            try {
                DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.getDefault());
                formatSymbols.setDecimalSeparator('.');
                formatSymbols.setGroupingSeparator(' ');
                DecimalFormat formatter = new DecimalFormat(DECIMAL_FORMAT, formatSymbols);
                return formatter.format(Double.parseDouble(value));
            } catch (Exception e) {
                Timber.e(e, "Formaating Number Failed");
            }
        }
        return value;
    }
}
