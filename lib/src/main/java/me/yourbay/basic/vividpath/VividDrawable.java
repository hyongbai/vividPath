package me.yourbay.basic.vividpath;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by ram on 16/4/26.
 */
public class VividDrawable extends Drawable implements VividListener {
    private VividPath mVividPath;
    //
    private float mProgress;
    private Animator mAnimator;
    private TimeInterpolator mInterpolator = new AccelerateDecelerateInterpolator();

    @Override
    public void draw(Canvas canvas) {
        mVividPath.draw(canvas, mProgress, true);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mVividPath.updateSize(bounds);
        start();
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return 0;
    }

    public VividDrawable(Path path) {
        this.mVividPath = new VividPath(path);
    }

    public VividPath getVividPath() {
        return mVividPath;
    }

    public VividDrawable setPath(Path path) {
        mVividPath.setPath(path);
        mProgress = 0;
        start();
        return this;
    }

    @Override
    public void destroy() {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
    }

    @Override
    public Animator start() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        //
        final float lastProgress = (mProgress > 0 && mProgress < 1) ? mProgress : 0;
        //
        final ValueAnimator animator = ValueAnimator.ofFloat(0, 1.0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final Object value = animation.getAnimatedValue();
                if (value instanceof Float) {
                    mProgress = ((Float) value).floatValue();
                    invalidateSelf();
                }
            }
        });
        animator.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float input) {
                input += lastProgress;
                if (input > 1) {
                    input = input - 1;
                }
                return (mInterpolator != null) ? mInterpolator.getInterpolation(input) : input;
            }
        });
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setRepeatCount(-1);
        animator.setDuration(500);
        animator.start();
        //
        return mAnimator = animator;
    }

}
