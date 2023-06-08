package com.rokudo.xpense.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.rokudo.xpense.R;

public class ShimmerUtils {
    public static void transitionShimmerLayoutToFinalView(ShimmerFrameLayout shimmerView, View finalView, Context context) {
        AlphaAnimation shimmerLayoutFadeAnimation = new AlphaAnimation(1.0f, 0.0f);
        shimmerLayoutFadeAnimation.setDuration(500);
        shimmerLayoutFadeAnimation.setRepeatCount(0);
        shimmerLayoutFadeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                shimmerView.hideShimmer();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                shimmerView.stopShimmer();
                shimmerView.setVisibility(View.INVISIBLE);
                finalView.setVisibility(View.VISIBLE);
                finalView.startAnimation(
                        AnimationUtils.loadAnimation(context, R.anim.item_animation_fade_in)
                );
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        shimmerView.startAnimation(shimmerLayoutFadeAnimation);
    }
}
