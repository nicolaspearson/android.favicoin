package com.lupinemoon.favicoin.data.storage.interfaces;

import android.graphics.Bitmap;

@SuppressWarnings("unused")
public interface BitmapCache {

    void putBitmap(Bitmap bitmap);

    Bitmap getBitmap();

    void delBitmap();

}
