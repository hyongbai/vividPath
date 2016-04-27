package me.yourbay.basic.vividpath;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ram on 16/4/26.
 */
public class VividView extends View implements VividListener {

    private Rect mViewBound = new Rect();
    //
    private VividDrawable mVividDrawable;

    @Override
    protected boolean verifyDrawable(Drawable who) {
        // when Drawable.invalidateSelf is invoked, view need to check if the drawable is valid, make it valid
        return who == mVividDrawable || super.verifyDrawable(who);
    }

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
        if (mVividDrawable == null) {
            return;
        }
        final VividPath viviPath = mVividDrawable.getVividPath();
        if (viviPath == null || viviPath.getPathRect().isEmpty()) {
            return;
        }
        final Paint paint = viviPath.getPaint();
        final RectF rectF = viviPath.getPathRect();
        final float stroke = (paint != null) ? paint.getStrokeWidth() : 0;
        final float scale = VividUtil.getProperScaleWithStroke(mViewBound, rectF, paint);
        setMeasuredDimension((int) (scale * (rectF.width() + stroke)) + l + r, (int) (scale * (rectF.height() + stroke)) + t + b);
        //
        mViewBound.set(l, t, getMeasuredWidth() - l - r, getMeasuredHeight() - t - b);
        mVividDrawable.setBounds(mViewBound);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mVividDrawable != null) {
            mVividDrawable.draw(canvas);
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
        if (mVividDrawable == null) {
            mVividDrawable = new VividDrawable(path);
            // this must be invoked, or Drawable.invalidSelf will not work
            mVividDrawable.setCallback(this);
        } else {
            mVividDrawable.setPath(path);
        }
        requestLayout();
    }

    @Override
    public void destroy() {
        if (mVividDrawable != null) {
            mVividDrawable.destroy();
        }
    }

    @Override
    public Animator start() {
        if (mVividDrawable != null) {
            mVividDrawable.start();
        }
        return null;
    }
}
