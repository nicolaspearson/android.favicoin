package com.lupinemoon.favicoin.presentation.utils;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.ImageView;

import com.lupinemoon.favicoin.BuildConfig;

import timber.log.Timber;

public class ImageUtils {

    public static String getFullCoinUrl(String imageUrl) {
        if (!TextUtils.isEmpty(imageUrl)) {
            if (imageUrl.contains("http")) {
                return imageUrl;
            }

            String newImageUrl = String.format(
                    "%s%s",
                    BuildConfig.HTTP_IMAGE_URL_ENDPOINT,
                    !TextUtils.isEmpty(imageUrl) ? imageUrl.replace("\\", "") : "");
            Timber.d("Image URL: %s", newImageUrl);
            return newImageUrl;
        }
        return "None";
    }

    // Used to check if an image view has an image set
    public static boolean hasImage(@NonNull ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable) drawable).getBitmap() != null;
        }

        return hasImage;
    }

}
