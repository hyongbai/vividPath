package me.yourbay.basic.vividpath;

import android.graphics.Path;
import android.graphics.PathMeasure;

/**
 * Created by ram on 16/4/26.
 */
public class SegmentCalculator {
    public static void setSegment(PathMeasure pm, Path p, float start, float end) {
        final float totalLen = pm.getLength();
        float len = end - start;
        // 长度超过1没有意义
        if (Math.abs(len) > 1) {
            len = len > 0 ? 1 : -1;
        }
        // 起始点在-1和1之间
        while (Math.abs(start) > 1) {
            //变成(-1,1)
            start = start + (start > 0 ? -1 : 1);
        }
        end = start + len;
        //
        start = Math.min(start, end);
        end = start + Math.abs(len);
        //
        if (start < 0) {
            if (end < 0) {
                pm.getSegment((1 + start) * totalLen, (1 + end) * totalLen, p, true);
                return;
            }
            pm.getSegment((1 + start) * totalLen, totalLen, p, true);
            start = 0;
        }
        if (end > 1) {
            pm.getSegment(0, (end - 1) * totalLen, p, true);
            end = 1;
        }
        pm.getSegment(start * totalLen, end * totalLen, p, true);
    }
}
