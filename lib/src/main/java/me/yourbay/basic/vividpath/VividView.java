package me.yourbay.basic.vividpath;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by ram on 16/4/26.
 */
public class VividView extends View implements VividListener {

    private VividPath mVividPath;
    private Rect mViewBound = new Rect();
    //
    private float mProgress;
    private Animator mAnimator;
    private TimeInterpolator mInterpolator = new AccelerateDecelerateInterpolator();

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int l = getPaddingLeft();
        final int t = getPaddingTop();
        final int r = getPaddingRight();
        final int b = getPaddingBottom();
        mViewBound.set(l, t, getMeasuredWidth() - l - r, getMeasuredHeight() - t - b);
        if (mVividPath == null || mVividPath.getPathRect().isEmpty()) {
            return;
        }
        final Paint paint = mVividPath.getPaint();
        final RectF rectF = mVividPath.getPathRect();
        final float stroke = (paint != null) ? paint.getStrokeWidth() : 0;
        final float scale = VividUtil.getProperScaleWithStroke(mViewBound, rectF, paint);
        setMeasuredDimension((int) (scale * (rectF.width() + stroke)) + l + r, (int) (scale * (rectF.height() + stroke)) + t + b);
        //
        mViewBound.set(l, t, getMeasuredWidth() - l - r, getMeasuredHeight() - t - b);
        mVividPath.updateSize(mViewBound);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mAnimator == null) {
            start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mVividPath != null) {
            mVividPath.draw(canvas, mProgress, true);
        }
    }

    public VividView(Context context) {
        super(context);
    }

    public VividView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VividView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPath(Path path) {
        if (mVividPath == null) {
            mVividPath = new VividPath(path);
        } else {
            mVividPath.setPath(path);
        }
        requestLayout();
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
        if (mVividPath == null) {
            return null;
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
                    postInvalidate();
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
