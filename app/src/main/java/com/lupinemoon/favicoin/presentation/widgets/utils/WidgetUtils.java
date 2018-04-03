package com.lupinemoon.favicoin.presentation.widgets.utils;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.lupinemoon.favicoin.R;

public class WidgetUtils {

    public static int[] getImageLargeSize() {
        return new int[]{900, 900};
    }

    public static int[] getImageStandardSize() {
        return new int[]{450, 450};
    }

    public static int[] getImageThumbnailSize() {
        return new int[]{200, 200};
    }

    public static Drawable tint9PatchDrawableFrame(
            @NonNull Context context,
            @ColorInt int tintColor) {
        final NinePatchDrawable toastDrawable = (NinePatchDrawable) getDrawable(
                context,
                R.drawable.toast_frame);
        toastDrawable.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));
        return toastDrawable;
    }

    public static void setBackground(@NonNull View view, Drawable drawable) {
        view.setBackground(drawable);
    }

    public static Drawable getDrawable(@NonNull Context context, @DrawableRes int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(id);
        } else {
            return context.getResources().getDrawable(id);
        }
    }
}
