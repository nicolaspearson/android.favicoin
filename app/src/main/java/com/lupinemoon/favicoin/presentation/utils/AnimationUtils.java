package com.lupinemoon.favicoin.presentation.utils;

import android.app.Activity;

public class AnimationUtils {

    public static void animateTransitionStartActivity(Activity activity, int animEnter){
      activity.overridePendingTransition(animEnter, 0);
    }

    public static void animateTransitionFinishActivity(Activity activity, int animExit){
        activity.overridePendingTransition(0, animExit);
    }

}
