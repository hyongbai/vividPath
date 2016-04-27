package me.yourbay.vividpath;


import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParserFactory;

public class PathParser {
    static final String TAG = "SVG";
    static float DPI = 72.0f;   // Should be settable

    /**
     * Parses a single SVG path and returns it as a <code>android.graphics.Path</code> object.
     * An example path is <code>M250,150L150,350L350,350Z</code>, which draws a triangle.
     *
     * @param pathString the SVG path, see the specification <a href="http://www.w3.org/TR/SVG/paths.html">here</a>.
     */
    public static Path parsePath(String pathString) {
        return doPath(pathString);
    }

    private static final SAXParserFactory mFactory = SAXParserFactory.newInstance();

    public static String escape(String s) {
        return s.replaceAll("\"", "&quot;")
                .replaceAll("'", "&apos")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("&", "&amp;");
    }

    private static NumberParse parseNumbers(String s) {
        //Util.debug("Parsing numbers from: '" + s + "'");
        int n = s.length();
        int p = 0;
        ArrayList<Float> numbers = new ArrayList<Float>();
        boolean skipChar = false;
        for (int i = 1; i < n; i++) {
            if (skipChar) {
                skipChar = false;
                continue;
            }
            char c = s.charAt(i);
            switch (c) {
                // This ends the parsing, as we are on the next element
                case 'M':
                case 'm':
                case 'Z':
                case 'z':
                case 'L':
                case 'l':
                case 'H':
                case 'h':
                case 'V':
                case 'v':
                case 'C':
                case 'c':
                case 'S':
                case 's':
                case 'Q':
                case 'q':
                case 'T':
                case 't':
                case 'a':
                case 'A':
                case ')': {
                    String str = s.substring(p, i);
                    if (str.trim().length() > 0) {
                        //Util.debug("  Last: " + str);
                        float f = Float.parseFloat(str);
                        numbers.add(f);
                    }
                    p = i;
                    return new NumberParse(numbers, p);
                }
                case '\n':
                case '\t':
                case ' ':
                case ',':
                case '-': {
                    String str = s.substring(p, i);
                    // Just keep moving if multiple whitespace
                    if (str.trim().length() > 0) {
                        //Util.debug("  Next: " + str);
                        float f = Float.parseFloat(str);
                        numbers.add(f);
                        if (c == '-') {
                            p = i;
                        } else {
                            p = i + 1;
                            skipChar = true;
                        }
                    } else {
                        p++;
                    }
                    break;
                }
            }
        }
        String last = s.substring(p);
        if (last.length() > 0) {
            //Util.debug("  Last: " + last);
            try {
                numbers.add(Float.parseFloat(last));
            } catch (NumberFormatException nfe) {
                // Just white-space, forget it
            }
            p = s.length();
        }
        return new NumberParse(numbers, p);
    }

    // Process a list of transforms
    // foo(n,n,n...) bar(n,n,n..._ ...)
    // delims are whitespace or ,'s

    private static Matrix parseTransform(String s) {
        //Log.d(TAG, s);
        Matrix matrix = new Matrix();
        while (true) {
            parseTransformItem(s, matrix);
            // Log.i(TAG, "Transformed: (" + s + ") " + matrix);
            int rparen = s.indexOf(")");
            if (rparen > 0 && s.length() > rparen + 1) {
                s = s.substring(rparen + 1).replaceFirst("[\\s,]*", "");
            } else {
                break;
            }
        }
        //Log.d(TAG, matrix.toShortString());
        return matrix;
    }

    private static Matrix parseTransformItem(String s, Matrix matrix) {
        if (s.startsWith("matrix(")) {
            NumberParse np = parseNumbers(s.substring("matrix(".length()));
            if (np.numbers.size() == 6) {
                Matrix mat = new Matrix();
                mat.setValues(new float[]{
                        // Row 1
                        np.numbers.get(0),
                        np.numbers.get(2),
                        np.numbers.get(4),
                        // Row 2
                        np.numbers.get(1),
                        np.numbers.get(3),
                        np.numbers.get(5),
                        // Row 3
                        0,
                        0,
                        1,
                });
                matrix.preConcat(mat);
            }
        } else if (s.startsWith("translate(")) {
            NumberParse np = parseNumbers(s.substring("translate(".length()));
            if (np.numbers.size() > 0) {
                float tx = np.numbers.get(0);
                float ty = 0;
                if (np.numbers.size() > 1) {
                    ty = np.numbers.get(1);
                }
                matrix.preTranslate(tx, ty);
            }
        } else if (s.startsWith("scale(")) {
            NumberParse np = parseNumbers(s.substring("scale(".length()));
            if (np.numbers.size() > 0) {
                float sx = np.numbers.get(0);
                float sy = sx;
                if (np.numbers.size() > 1) {
                    sy = np.numbers.get(1);
                }
                matrix.preScale(sx, sy);
            }
        } else if (s.startsWith("skewX(")) {
            NumberParse np = parseNumbers(s.substring("skewX(".length()));
            if (np.numbers.size() > 0) {
                float angle = np.numbers.get(0);
                matrix.preSkew((float) Math.tan(angle), 0);
            }
        } else if (s.startsWith("skewY(")) {
            NumberParse np = parseNumbers(s.substring("skewY(".length()));
            if (np.numbers.size() > 0) {
                float angle = np.numbers.get(0);
                matrix.preSkew(0, (float) Math.tan(angle));
            }
        } else if (s.startsWith("rotate(")) {
            NumberParse np = parseNumbers(s.substring("rotate(".length()));
            if (np.numbers.size() > 0) {
                float angle = np.numbers.get(0);
                float cx = 0;
                float cy = 0;
                if (np.numbers.size() > 2) {
                    cx = np.numbers.get(1);
                    cy = np.numbers.get(2);
                }
                matrix.preTranslate(cx, cy);
                matrix.preRotate(angle);
                matrix.preTranslate(-cx, -cy);
            }
        } else {
            Log.w(TAG, "Invalid transform (" + s + ")");
        }
        return matrix;
    }

    private static RectF parseViewBox(String s) {
        final NumberParse np = parseNumbers(s);
        if (np.numbers.size() >= 4)
            return new RectF(np.numbers.get(0), np.numbers.get(1), np.numbers.get(2), np.numbers.get(3));
        return null;
    }

    /**
     * This is where the hard-from-parse paths are handled.
     * Uppercase rules are absolute positions, lowercase are relative.
     * Types of path rules:
     * <p/>
     * <ol>
     * <li>M/m - (x y)+ - Move from (without drawing)
     * <li>Z/z - (no params) - Close path (back from starting point)
     * <li>L/l - (x y)+ - Line from
     * <li>H/h - x+ - Horizontal ine from
     * <li>V/v - y+ - Vertical line from
     * <li>C/c - (x1 y1 x2 y2 x y)+ - Cubic bezier from
     * <li>S/s - (x2 y2 x y)+ - Smooth cubic bezier from (shorthand that assumes the x2, y2 from previous C/S is the x1, y1 of this bezier)
     * <li>Q/q - (x1 y1 x y)+ - Quadratic bezier from
     * <li>T/t - (x y)+ - Smooth quadratic bezier from (assumes previous control point is "reflection" of last one w.r.t. from current point)
     * </ol>
     * <p/>
     * Numbers are separate by whitespace, comma or nothing at all (!) if they are self-delimiting, (ie. begin with a - sign)
     *
     * @param s the path string from the XML
     */
    private static Path doPath(String s) {
        int n = s.length();
        ParserHelper ph = new ParserHelper(s, 0);
        ph.skipWhitespace();
        Path p = new Path();
        float lastX = 0;
        float lastY = 0;
        float lastX1 = 0;
        float lastY1 = 0;
        float contourInitialX = 0;
        float contourInitialY = 0;
        RectF r = new RectF();
        char cmd = 'x';
        while (ph.pos < n) {
            char next = s.charAt(ph.pos);
            if (!Character.isDigit(next) && !(next == '.') && !(next == '-')) {
                cmd = next;
                ph.advance();
            } else if (cmd == 'M') { // implied command
                cmd = 'L';
            } else if (cmd == 'm') { // implied command
                cmd = 'l';
            } else { // implied command
                // Log.d(TAG, "Implied command: " + cmd);
            }
            p.computeBounds(r, true);
            // Log.d(TAG, "  " + cmd + " " + r);
            // Util.debug("* Commands remaining: '" + path + "'.");
            boolean wasCurve = false;
            switch (cmd) {
                case 'M':
                case 'm': {
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 'm') {
                        p.rMoveTo(x, y);
                        lastX += x;
                        lastY += y;
                    } else {
                        p.moveTo(x, y);
                        lastX = x;
                        lastY = y;
                    }
                    contourInitialX = lastX;
                    contourInitialY = lastY;
                    break;
                }
                case 'Z':
                case 'z': {
                    /// p.lineTo(contourInitialX, contourInitialY);
                    p.close();
                    lastX = contourInitialX;
                    lastY = contourInitialY;
                    break;
                }
                case 'L':
                case 'l': {
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 'l') {
                        p.rLineTo(x, y);
                        lastX += x;
                        lastY += y;
                    } else {
                        p.lineTo(x, y);
                        lastX = x;
                        lastY = y;
                    }
                    break;
                }
                case 'H':
                case 'h': {
                    float x = ph.nextFloat();
                    if (cmd == 'h') {
                        p.rLineTo(x, 0);
                        lastX += x;
                    } else {
                        p.lineTo(x, lastY);
                        lastX = x;
                    }
                    break;
                }
                case 'V':
                case 'v': {
                    float y = ph.nextFloat();
                    if (cmd == 'v') {
                        p.rLineTo(0, y);
                        lastY += y;
                    } else {
                        p.lineTo(lastX, y);
                        lastY = y;
                    }
                    break;
                }
                case 'C':
                case 'c': {
                    wasCurve = true;
                    float x1 = ph.nextFloat();
                    float y1 = ph.nextFloat();
                    float x2 = ph.nextFloat();
                    float y2 = ph.nextFloat();
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 'c') {
                        x1 += lastX;
                        x2 += lastX;
                        x += lastX;
                        y1 += lastY;
                        y2 += lastY;
                        y += lastY;
                    }
                    p.cubicTo(x1, y1, x2, y2, x, y);
                    lastX1 = x2;
                    lastY1 = y2;
                    lastX = x;
                    lastY = y;
                    break;
                }
                case 'S':
                case 's': {
                    wasCurve = true;
                    float x2 = ph.nextFloat();
                    float y2 = ph.nextFloat();
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 's') {
                        x2 += lastX;
                        x += lastX;
                        y2 += lastY;
                        y += lastY;
                    }
                    float x1 = 2 * lastX - lastX1;
                    float y1 = 2 * lastY - lastY1;
                    p.cubicTo(x1, y1, x2, y2, x, y);
                    lastX1 = x2;
                    lastY1 = y2;
                    lastX = x;
                    lastY = y;
                    break;
                }
                case 'A':
                case 'a': {
                    float rx = ph.nextFloat();
                    float ry = ph.nextFloat();
                    float theta = ph.nextFloat();
                    int largeArc = (int) ph.nextFloat();
                    int sweepArc = (int) ph.nextFloat();
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 'a') {
                        x += lastX;
                        y += lastY;
                    }
                    drawArc(p, lastX, lastY, x, y, rx, ry, theta, largeArc == 1, sweepArc == 1);
                    lastX = x;
                    lastY = y;
                    break;
                }
                case 'T':
                case 't': {
                    wasCurve = true;
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 't') {
                        x += lastX;
                        y += lastY;
                    }
                    float x1 = 2 * lastX - lastX1;
                    float y1 = 2 * lastY - lastY1;
                    p.cubicTo(lastX, lastY, x1, y1, x, y);
                    lastX = x;
                    lastY = y;
                    lastX1 = x1;
                    lastY1 = y1;
                    break;
                }
                case 'Q':
                case 'q': {
                    wasCurve = true;
                    float x1 = ph.nextFloat();
                    float y1 = ph.nextFloat();
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 'q') {
                        x += lastX;
                        y += lastY;
                        x1 += lastX;
                        y1 += lastY;
                    }
                    p.cubicTo(lastX, lastY, x1, y1, x, y);
                    lastX1 = x1;
                    lastY1 = y1;
                    lastX = x;
                    lastY = y;
                    break;
                }
                default:
                    Log.w(TAG, "Invalid path command: " + cmd);
                    ph.advance();
            }
            if (!wasCurve) {
                lastX1 = lastX;
                lastY1 = lastY;
            }
            ph.skipWhitespace();
        }
        return p;
    }

    /**
     * Elliptical arc implementation based on the SVG specification notes
     * Adapted from the Batik library (Apache-2 license) by SAU
     */

    private static void drawArc(Path path, double x0, double y0, double x, double y, double rx,
                                double ry, double angle, boolean largeArcFlag, boolean sweepFlag) {
        double dx2 = (x0 - x) / 2.0;
        double dy2 = (y0 - y) / 2.0;
        angle = Math.toRadians(angle % 360.0);
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);

        double x1 = (cosAngle * dx2 + sinAngle * dy2);
        double y1 = (-sinAngle * dx2 + cosAngle * dy2);
        rx = Math.abs(rx);
        ry = Math.abs(ry);

        double Prx = rx * rx;
        double Pry = ry * ry;
        double Px1 = x1 * x1;
        double Py1 = y1 * y1;

        // check that radii are large enough
        double radiiCheck = Px1 / Prx + Py1 / Pry;
        if (radiiCheck > 1) {
            rx = Math.sqrt(radiiCheck) * rx;
            ry = Math.sqrt(radiiCheck) * ry;
            Prx = rx * rx;
            Pry = ry * ry;
        }

        // Step 2 : Compute (cx1, cy1)
        double sign = (largeArcFlag == sweepFlag) ? -1 : 1;
        double sq = ((Prx * Pry) - (Prx * Py1) - (Pry * Px1))
                / ((Prx * Py1) + (Pry * Px1));
        sq = (sq < 0) ? 0 : sq;
        double coef = (sign * Math.sqrt(sq));
        double cx1 = coef * ((rx * y1) / ry);
        double cy1 = coef * -((ry * x1) / rx);

        double sx2 = (x0 + x) / 2.0;
        double sy2 = (y0 + y) / 2.0;
        double cx = sx2 + (cosAngle * cx1 - sinAngle * cy1);
        double cy = sy2 + (sinAngle * cx1 + cosAngle * cy1);

        // Step 4 : Compute the angleStart (angle1) and the angleExtent (dangle)
        double ux = (x1 - cx1) / rx;
        double uy = (y1 - cy1) / ry;
        double vx = (-x1 - cx1) / rx;
        double vy = (-y1 - cy1) / ry;
        double p, n;

        // Compute the angle start
        n = Math.sqrt((ux * ux) + (uy * uy));
        p = ux; // (1 * ux) + (0 * uy)
        sign = (uy < 0) ? -1.0 : 1.0;
        double angleStart = Math.toDegrees(sign * Math.acos(p / n));

        // Compute the angle extent
        n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
        p = ux * vx + uy * vy;
        sign = (ux * vy - uy * vx < 0) ? -1.0 : 1.0;
        double angleExtent = Math.toDegrees(sign * Math.acos(p / n));
        if (!sweepFlag && angleExtent > 0) {
            angleExtent -= 360f;
        } else if (sweepFlag && angleExtent < 0) {
            angleExtent += 360f;
        }
        angleExtent %= 360f;
        angleStart %= 360f;

        RectF oval = new RectF((float) (cx - rx), (float) (cy - ry), (float) (cx + rx), (float) (cy + ry));
        path.addArc(oval, (float) angleStart, (float) angleExtent);
    }

    private static NumberParse getNumberParseAttr(String name, Attributes attributes) {
        int n = attributes.getLength();
        for (int i = 0; i < n; i++) {
            if (attributes.getLocalName(i).equals(name)) {
                return parseNumbers(attributes.getValue(i));
            }
        }
        return null;
    }

    private static String getStringAttr(String name, Attributes attributes) {
        int n = attributes.getLength();
        for (int i = 0; i < n; i++) {
            if (attributes.getLocalName(i).equals(name)) {
                return attributes.getValue(i);
            }
        }
        return null;
    }

    private static class NumberParse {
        private ArrayList<Float> numbers;
        private int nextCmd;

        public NumberParse(ArrayList<Float> numbers, int nextCmd) {
            this.numbers = numbers;
            this.nextCmd = nextCmd;
        }

        @SuppressWarnings("unused")
        public int getNextCmd() {
            return nextCmd;
        }

        @SuppressWarnings("unused")
        public float getNumber(int index) {
            return numbers.get(index);
        }

    }

    private static class Gradient {
        String id;
        String xlink;
        boolean isLinear;
        float x1, y1, x2, y2;
        float x, y, radius;
        ArrayList<Float> positions = new ArrayList<Float>();
        ArrayList<Integer> colors = new ArrayList<Integer>();
        Matrix matrix = null;
        Shader shader = null;

        public Gradient createChild(Gradient g) {
            Gradient child = new Gradient();
            child.id = g.id;
            child.xlink = id;
            child.isLinear = g.isLinear;
            child.x1 = g.x1;
            child.x2 = g.x2;
            child.y1 = g.y1;
            child.y2 = g.y2;
            child.x = g.x;
            child.y = g.y;
            child.radius = g.radius;
            child.positions = positions;
            child.colors = colors;
            child.matrix = matrix;
            if (g.matrix != null) {
                if (matrix == null) {
                    child.matrix = g.matrix;
                } else {
                    Matrix m = new Matrix(matrix);
                    m.preConcat(g.matrix);
                    child.matrix = m;
                }
            }
            return child;
        }

        public Shader createShader() {
            if (shader != null)
                return shader;

            int[] clr = new int[colors.size()];
            for (int i = 0; i < clr.length; i++) {
                clr[i] = colors.get(i);
            }

            float[] pos = new float[positions.size()];
            for (int i = 0; i < pos.length; i++) {
                pos[i] = positions.get(i);
            }

            if (isLinear)
                shader = new LinearGradient(x1, y1, x2, y2, clr, pos, Shader.TileMode.CLAMP);
            else
                shader = new RadialGradient(x, y, radius, clr, pos, Shader.TileMode.CLAMP);
            if (matrix != null) {
                shader.setLocalMatrix(matrix);
            }
            return shader;
        }
    }

    private static class StyleSet {
        HashMap<String, String> styleMap = new HashMap<String, String>();

        private StyleSet(String string) {
            String[] styles = string.split(";");
            for (String s : styles) {
                String[] style = s.split(":");
                if (style.length == 2) {
                    styleMap.put(style[0], style[1]);
                }
            }
        }

        public String getStyle(String name) {
            return styleMap.get(name);
        }
    }

    private static class Properties {
        StyleSet styles = null;
        Attributes atts;

        private Properties(Attributes atts) {
            this.atts = atts;
            String styleAttr = getStringAttr("style", atts);
            if (styleAttr != null) {
                styles = new StyleSet(styleAttr);
            }
        }

        public String getAttr(String name) {
            String v = null;
            if (styles != null) {
                v = styles.getStyle(name);
            }
            if (v == null) {
                v = getStringAttr(name, atts);
            }
            return v;
        }

        public String getString(String name) {
            return getAttr(name);
        }

        public Integer getColorValue(String name) {
            String v = getAttr(name);
            if (v == null) {
                return null;
            } else if (v.startsWith("#") && (v.length() == 4 || v.length() == 7)) {
                try {
                    int result = Integer.parseInt(v.substring(1), 16);
                    return v.length() == 4 ? hex3Tohex6(result) : result;
                } catch (NumberFormatException nfe) {
                    return null;
                }
            } else {
                try {
                    return Color.parseColor(v);
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "unknown color: " + v);
                    return null;
                }
            }
        }

        // convert 0xRGB into 0xRRGGBB
        private int hex3Tohex6(int x) {
            return (x & 0xF00) << 8 | (x & 0xF00) << 12 |
                    (x & 0xF0) << 4 | (x & 0xF0) << 8 |
                    (x & 0xF) << 4 | (x & 0xF);
        }

        public Float getFloat(String name) {
            String v = getAttr(name);
            if (v == null) {
                return null;
            } else {
                try {
                    return Float.parseFloat(v);
                } catch (NumberFormatException nfe) {
                    return null;
                }
            }
        }
    }

}