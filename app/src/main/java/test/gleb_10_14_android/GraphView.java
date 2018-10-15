package test.gleb_10_14_android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import android.graphics.Canvas;

public class GraphView extends View {

    private final static int Size = 64;

    private int graphArray[] = new int[Size];
    private int maxY = 0;

    private int startPos = 0;
    private int endPos = 0;

    private Paint paint;

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
        paint.setStrokeWidth(10);
    }

//    public void setGraphArray(int Xi_graphArray[], int Xi_maxY)
//    {
//        m_graphArray = Xi_graphArray;
//        m_maxY = Xi_maxY;
//    }
//
//    public void setGraphArray(int Xi_graphArray[])
//    {
//        int maxY = 0;
//        for(int i = 0; i < Xi_graphArray.length; ++i)
//        {
//            if(Xi_graphArray[i] > maxY)
//            {
//                maxY = Xi_graphArray[i];
//            }
//        }
//        setGraphArray(Xi_graphArray, maxY);
//    }

    private void iterate(
        Canvas canvas,
        int i,
        int curPos,
        int prevPos,
        float factorX,
        float factorY
    ) {
        int x0 = i - 1;
        int y0 = graphArray[curPos];
        int x1 = i;
        int y1 = graphArray[prevPos];

        int sx = (int)(x0 * factorX);
        int sy = getHeight() - (int)(y0* factorY);
        int ex = (int)(x1*factorX);
        int ey = getHeight() - (int)(y1* factorY);
        canvas.drawLine(sx, sy, ex, ey, paint);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(Math.abs(startPos - endPos) < 2) {
            return;
        }

        float factorX = getWidth() / (float)Size;
        float factorY = getHeight() / (float)maxY;

        int prevPos = startPos;
        int curPos = startPos + 1;

        for(int i = 1; i < Size; ++i) {
            iterate(
                canvas,
                i,
                curPos,
                prevPos,
                factorX,
                factorY
            );
        }
    }
}