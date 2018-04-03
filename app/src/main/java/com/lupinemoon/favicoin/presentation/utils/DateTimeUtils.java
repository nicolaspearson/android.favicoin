package com.lupinemoon.favicoin.presentation.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import timber.log.Timber;

public class DateTimeUtils {

    public static String epochToDateTimeString(String epoch) throws NumberFormatException {
        try {
            Timber.d("Epoch: %s", epoch);
            Calendar calendar = Calendar.getInstance();
            TimeZone calendarTimeZone = calendar.getTimeZone();
            Date date = new Date(Long.parseLong(epoch) * 1000);
            DateFormat simpleDateFormat = new SimpleDateFormat(
                    "yyyy/MM/dd HH:mm:ss",
                    Locale.getDefault());
            simpleDateFormat.setTimeZone(calendarTimeZone);
            return simpleDateFormat.format(date);
        } catch (NumberFormatException e) {
            throw e;
        }
    }

    public static String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
