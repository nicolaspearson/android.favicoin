package com.lupinemoon.favicoin.presentation.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

public class FileUtils {

    public static String getJsonFileFromAssets(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream;
        String json = null;
        try {
            inputStream = assetManager.open(fileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            Timber.d("Error loading " + fileName + " from assets: " + e);
        }
        return json;
    }

}
