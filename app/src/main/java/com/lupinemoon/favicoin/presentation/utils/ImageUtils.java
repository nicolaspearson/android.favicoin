package com.lupinemoon.favicoin.presentation.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
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

    // Returns a circular bitmap
    public static Bitmap getBitmapClippedCircle(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(
                bitmap.getWidth(),
                bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(
                bitmap.getWidth() / 2,
                bitmap.getHeight() / 2,
                bitmap.getWidth() / 2,
                paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

}
