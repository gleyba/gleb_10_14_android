package test.gleb_10_14_android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.ViewGroup;

import android.graphics.Canvas;
import android.widget.TextView;

public class GraphView extends ViewGroup {

    private final static int Size = 64;

    private float graphArray[] = new float[Size];
    private float maxY = -1;
    private float minY = 1;

    private int startPos = 0;
    private int endPos = 0;

    private Paint paint;
    private Path path;

    public GraphView(Context context) {
        super(context);
        init();
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);

        path = new Path();

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

    }

    public void addValue(float value) {
        graphArray[endPos] = value;

        if (++endPos == Size) {
            endPos = 0;
        }
        if (endPos == startPos) {
            if (++startPos == Size) {
                startPos = 0;
            }
        }

        if (maxY < value) {
            maxY = value;
        }
        if (minY > value) {
            minY = value;
        }
    }

    public void flush() {
        endPos = 0;
        startPos = 0;
        minY = Integer.MAX_VALUE;
        maxY = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (startPos == endPos || startPos == endPos -1) {
            return;
        }

        path.reset();

        int width = getWidth();
        int height = getHeight();

        float factorX = width / (float)Size;
        float factorY = height / maxY - minY;

        int curPos = startPos;

        for(int i = 0; i < Size; ++i) {
            int sx = (int)(i * factorX);
            int sy = getHeight() - (int)((graphArray[curPos] - minY) * factorY);

            if (i != 0) {
                path.lineTo(sx,sy);
            }
            path.moveTo(sx,sy);

            if (++curPos == Size) {
                curPos = 0;
            }
            if (curPos == endPos) {
                break;
            }
        }

        canvas.drawPath(path, paint);
    }
}