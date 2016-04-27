package me.yourbay.basic.vividpath;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ram on 16/4/26.
 */
public class PathParseHelper {
    private Path mRawPath;
    private PathMeasure mPathMeasure;
    private List<Pair<Path, PathMeasure>> mList;

    public PathParseHelper() {
        mList = new ArrayList<>();
        mPathMeasure = new PathMeasure();
    }

    public Path getRawPath() {
        return mRawPath;
    }

    public PathParseHelper setRawPath(Path rawPath) {
        this.mRawPath = rawPath;
        if (mPathMeasure != null) {
            mPathMeasure.setPath(rawPath, true);
        }
        extract();
        return this;
    }

    public PathParseHelper setPathMeasure(PathMeasure pathMeasure) {
        this.mPathMeasure = pathMeasure;
        if (pathMeasure != null && mRawPath != null) {
            pathMeasure.setPath(mRawPath, true);
        }
        return this;
    }

    public List<Pair<Path, PathMeasure>> extract() {
        if (mPathMeasure == null || mRawPath == null) {
            return null;
        }
        mList.clear();
        do {
            final float len = mPathMeasure.getLength();
            Path path = new Path();
            mPathMeasure.getSegment(0, len, path, true);
            path.close();
            //
            mList.add(new Pair<>(path, new PathMeasure(path, true)));
        } while (mPathMeasure.nextContour());
        return mList;
    }

    public List<Pair<Path, PathMeasure>> getList() {
        return mList;
    }
}
