package com.example.testssss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import org.opencv.core.Point;

public class DrawView extends View {
    Canvas canvas;
    Paint paint;
    Bitmap bitmap;
    Context context;
    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(3);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);;
    }

//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//        //初始化空畫布
//        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//        canvas = new Canvas(bitmap);
//    }
//
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("draw", ""+currentPoint.x+""+currentPoint.y);
        if (zoom==true) {
            canvas.drawRect(startPoint.x, startPoint.y, currentPoint.x, currentPoint.y, paint);
        }
        else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
    }

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    // 第一個按下的手指的點
    public PointF startPoint = new PointF(0,0);
    public PointF currentPoint = new PointF(0,0);
    // 兩個按下的手指的觸控點的中點
    private PointF midPoint = new PointF();
    // 初始的兩個手指按下的觸控點的距離
    private float oriDis = 1f;
    // 計算兩個觸控點之間的距離
    public boolean zoom = false;
    Point zoomInRec;
    float zoomInWidth;
    float zoomInHeight;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("draw", "onDrawTouch");

        zoomInRec = new Point(Math.min(startPoint.y, currentPoint.y)/1.5,
                (720-Math.max(startPoint.x, currentPoint.x))/1.5);
        zoomInWidth = Math.abs((float)((currentPoint.x - startPoint.x)/1.5));
        zoomInHeight = Math.abs((float)((currentPoint.y - startPoint.y)/1.5));

        // 進行與操作是為了判斷多點觸控
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Log.d("touch", "ACTION_DOWN");

                // 第一個手指按下事件
                startPoint.set(event.getX(), event.getY());
                currentPoint.set(event.getX(), event.getY());
                mode = DRAG;
                zoom = false;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // 第二個手指按下事件
//                oriDis = distance(event);
//                // 防止一個手指上出現兩個繭
//                if (oriDis > 10f) {
//                    savedMatrix.set(matrix);
//                    midPoint = middle(event);
//                    mode = ZOOM;
//                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                // 手指放開事件
                mode = NONE;

                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("touch", "ACTION_MOVE");

                // 手指滑動事件
                if (mode == DRAG) {
                    // 是一個手指拖動
                    currentPoint.set(event.getX(), event.getY());
                    if (zoomInWidth>20 && zoomInHeight>20){
                        zoom = true;
                    }
                } else if (mode == ZOOM) {
                    // 兩個手指滑動
//                    float newDist = distance(event);
//                    if (newDist > 10f) {
//                        matrix.set(savedMatrix);
//                        float scale = newDist / oriDis;
//                        matrix.postScale(scale, scale, midPoint.x, midPoint.y);
//                    }
                }
                break;
        }

        // 設定ImageView的Matrix
        invalidate();
        return true;
    }
}
