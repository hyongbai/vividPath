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
    public enum Mode {
        Piece,
        Increase,
    }

    private Mode mMode = Mode.Piece;
    //
    private Matrix mMatrix;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //
    private RectF mPathBound = new RectF();
    private int[] mColors = {0x11000000, 0xFFFFFFFF};
    private PathParseHelper mParseHelper = new PathParseHelper();

    public VividPath(Path path) {
        path.computeBounds(mPathBound, false);
        //
        mParseHelper.setRawPath(path);
        //
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(Resources.getSystem().getDisplayMetrics().density * 2);
    }

    /**
     * @param colors colors[0] is the entire path color, colors[1] is the color of current piece.
     * @return
     */
    public VividPath setColors(int[] colors) {
        final int len = (colors != null) ? colors.length : 0;
        if (len == 0) {
            return this;
        }
        if (len == 1) {
            mColors = new int[2];
            mColors[0] = mColors[1] = colors[0];
        } else {
            mColors = colors;
        }
        return this;
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

    public VividPath setMode(Mode mode) {
        mMode = mode;
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
            if (showEntire && ((mColors[0] & 0xff000000) > 0)) {
                path.rewind();
                pm.getSegment(0, l, path, true);
                path.rLineTo(0.0f, 0.0f);
                if (mMatrix != null) {
                    path.transform(mMatrix);
                }
                mPaint.setColor(mColors[0]);
                canvas.drawPath(path, mPaint);
            }
            //
            path.rewind();
            final float startD, endD;
            if (mMode == Mode.Increase) {
                startD = 0f;
                endD = p;
            } else if (mMode == Mode.Piece) {
                startD = p;
                endD = p + fragmentLen;
            } else {
                startD = 0f;
                endD = p;
            }
            SegmentCalculator.setSegment(pm, path, startD, endD);
            path.rLineTo(0.0f, 0.0f);
            if (mMatrix != null) {
                path.transform(mMatrix);
            }
            mPaint.setColor(mColors[1]);
            canvas.drawPath(path, mPaint);
        }
    }
}
