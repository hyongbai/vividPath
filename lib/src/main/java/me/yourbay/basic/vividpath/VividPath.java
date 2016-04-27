package me.yourbay.basic.vividpath;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Pair;

/**
 * Created by ram on 16/4/4.
 */
public class VividPath {
    //
    private Matrix mMatrix;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //
    private RectF mPathBound = new RectF();
    private int[] colors = {0x44FFFFFF, 0x88FFFFFF};
    private PathParseHelper mParseHelper = new PathParseHelper();

    public VividPath(Path path) {
        path.computeBounds(mPathBound, false);
        //
        mParseHelper.setRawPath(path);
        //
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(Resources.getSystem().getDisplayMetrics().density * 2);
    }

    public VividPath setPath(Path path) {
        mParseHelper.setRawPath(path);
        if (path != null) {
            path.computeBounds(mPathBound, false);
        } else {
            mPathBound.setEmpty();
        }
        return this;
    }

    public VividPath setPaint(Paint paint) {
        this.mPaint = paint;
        return this;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public RectF getPathRect() {
        return mPathBound;
    }

    public VividPath updateSize(Rect bounds) {
        if (mMatrix == null) {
            mMatrix = new Matrix();
        }
        //
        final float scale = VividUtil.getProperScaleWithStroke(bounds, mPathBound, mPaint);
        //
        final int transY = (int) (bounds.centerY() - mPathBound.centerY() * scale);
        final int transX = (int) (bounds.centerX() - mPathBound.centerX() * scale);
        mMatrix.reset();
        mMatrix.setScale(scale, scale);
        mMatrix.postTranslate(transX, transY);
        return this;
    }

    public void draw(Canvas canvas, float progress, boolean showEntire) {
        if (canvas == null) {
            return;
        }
        if (mParseHelper.getList() == null) {
            return;
        }
        if (mParseHelper.getList().isEmpty()) {
            return;
        }
        for (Pair<Path, PathMeasure> pair : mParseHelper.getList()) {
            final Path path = pair.first;
            final PathMeasure pm = pair.second;
            //
            final float p = progress;
            final float l = pm.getLength();
            final float fragmentLen = 0.05f;
            //
            if (showEntire) {
                path.rewind();
                pm.getSegment(0, l, path, true);
                path.rLineTo(0.0f, 0.0f);
                if (mMatrix != null) {
                    path.transform(mMatrix);
                }
                mPaint.setColor(colors[0]);
                canvas.drawPath(path, mPaint);
            }
            //
            path.rewind();
            SegmentCalculator.setSegment(pm, path, p, p + fragmentLen);
            path.rLineTo(0.0f, 0.0f);
            if (mMatrix != null) {
                path.transform(mMatrix);
            }
            mPaint.setColor(colors[1]);
            canvas.drawPath(path, mPaint);
        }
    }
}
