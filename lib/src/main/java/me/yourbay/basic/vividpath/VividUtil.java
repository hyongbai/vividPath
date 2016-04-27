package me.yourbay.basic.vividpath;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by ram on 16/4/28.
 */
public class VividUtil {
    public static float getProperScaleWithStroke(Rect canvasRect, RectF rect, Paint paint) {
        if (canvasRect == null || rect == null || rect.isEmpty()) {
            return 0;
        }
        final float stroke = (paint != null) ? paint.getStrokeWidth() : 0;
        final float scaleWidth = canvasRect.width() / (rect.width() + stroke);
        final float scaleHeight = canvasRect.height() / (rect.height() + stroke);
        if (canvasRect.width() > 0 && canvasRect.height() > 0) {
            return Math.min(scaleWidth, scaleHeight);
        } else if (canvasRect.width() > 0) {
            return scaleWidth;
        } else if (canvasRect.height() > 0) {
            return scaleHeight;
        }
        return 0;
    }
}
