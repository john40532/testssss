package com.example.testssss;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class PlayVideo extends SurfaceView implements SurfaceHolder.Callback {

    public VideoCapture vc;
    public Cvfun cvfun;
    public PlaySound ps;



    SurfaceHolder mSurfaceHolder;
    DrawingThread mThread;

    Paint mPaint = new Paint();


    public PlayVideo(Context context) {
        super(context);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mThread = new DrawingThread();
        mPaint.setColor(0xFFFFFFFF);
        mPaint.setStyle(Paint.Style.FILL);
    }

    public PlayVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mThread = new DrawingThread();
        mPaint.setColor(0xFFFFFFFF);
        mPaint.setStyle(Paint.Style.FILL);
    }

    protected int frame_number = 0;
    private boolean nextFrame(Canvas canvas) {
        Mat mFrame = new Mat();
        if (vc.read(mFrame)) {
            frame_number++;
            Imgproc.cvtColor(mFrame, mFrame, Imgproc.COLOR_RGB2BGR);
            mFrame = cvfun.img_proc(mFrame, ps);
            Imgproc.putText(mFrame, String.valueOf(frame_number), new Point(30,150), Imgproc.FONT_HERSHEY_COMPLEX, 2, new Scalar(0, 255, 255), 1);
            Imgproc.cvtColor(mFrame, mFrame, Imgproc.COLOR_BGR2RGB);

            Bitmap mBitmap = Bitmap.createBitmap(mFrame.cols(), mFrame.rows(), Bitmap.Config.ARGB_8888);;
            Utils.matToBitmap(mFrame, mBitmap);
            Matrix matrix = new Matrix();
            matrix.preTranslate((canvas.getWidth() - mBitmap.getWidth()) / 2, (canvas.getHeight() - mBitmap.getHeight()) / 2);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            canvas.drawBitmap(mBitmap,matrix,mPaint);
        }
        else {
            return false;
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                synchronized (mSurfaceHolder) {
                }
                return true;
        }
        return super.onTouchEvent(event);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mThread.keepRunning = true;
        mThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mThread.keepRunning = false;
        boolean retry = true;
        while (retry) {
            try {
                mThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    public void setActivityHandle(VideoCapture vc, Cvfun cvfun, PlaySound ps) {
        this.vc = vc;
        this.cvfun = cvfun;
        this.ps = ps;
    }

    private class DrawingThread extends Thread {
        boolean keepRunning = true;

        @Override
        public void run() {
            Canvas c;
            while (keepRunning) {
                c = null;

                try {
                    c = mSurfaceHolder.lockCanvas();
                    synchronized (mSurfaceHolder) {
                        nextFrame(c);
                    }
                } finally {
                    if (c != null)
                        mSurfaceHolder.unlockCanvasAndPost(c);
                }

                // Run the draw loop at 50FPS
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}