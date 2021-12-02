package com.example.testssss;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    //    static
//    {
//        if(OpenCVLoader.initDebug())
//        {
//            Log.d(TAG, "Opencv installed successfully");
//        }
//        else{
//            Log.d(TAG, "opencv not installed");
//        }
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        setListensers();
    }

    private Button open_camera;
    private Button load_video;
    private ImageView mImageView;


    private void initViews()
    {
        open_camera = findViewById(R.id.open_camera);
        load_video = findViewById(R.id.loadVideo);
//        mImageView = findViewById(R.id.imageView);
    }

    private void setListensers()
    {
        open_camera.setOnClickListener(turnOnCamera);
        load_video.setOnClickListener(loadSampleVideo);
//        mImageView.setOnTouchListener(touchImage);
    }



    private View.OnClickListener turnOnCamera = new View.OnClickListener() {
        boolean flag = true;
        @Override
        public void onClick(View view) {
            if(flag==true) {
                flag = false;
                open_camera.setText("blasdf");
            }
            else {
                flag = true;
                open_camera.setText("camera");
            }
            startActivity(new Intent(MainActivity.this, OpencvCamera.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    };

    private View.OnClickListener loadSampleVideo = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            startActivity(new Intent(MainActivity.this, PlayVideo.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    };

//    private View.OnTouchListener touchImage = new View.OnTouchListener() {
//        private Matrix matrix = new Matrix();
//        private Matrix savedMatrix = new Matrix();
//
//        private static final int NONE = 0;
//        private static final int DRAG = 1;
//        private static final int ZOOM = 2;
//        private int mode = NONE;
//        // 第一個按下的手指的點
//        private PointF startPoint = new PointF();
//        // 兩個按下的手指的觸控點的中點
//        private PointF midPoint = new PointF();
//        // 初始的兩個手指按下的觸控點的距離
//        private float oriDis = 1f;
//        // 計算兩個觸控點之間的距離
//        private float distance(MotionEvent event) {
//            float x = event.getX(0) - event.getX(1);
//            float y = event.getY(0) - event.getY(1);
//            return (float) Math.sqrt(x * x + y * y);
//        }
//
//        // 計算兩個觸控點的中點
//        private PointF middle(MotionEvent event) {
//            float x = event.getX(0) + event.getX(1);
//            float y = event.getY(0) + event.getY(1);
//            return new PointF(x / 2, y / 2);
//        }
//
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            ImageView view = (ImageView) v;
//            Log.d("touch", ""+event.getAction());
//
//            // 進行與操作是為了判斷多點觸控
//            switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                case MotionEvent.ACTION_DOWN:
//                    Log.d("touch", "ACTION_DOWN");
//
//                    // 第一個手指按下事件
//                    matrix.set(view.getImageMatrix());
//                    savedMatrix.set(matrix);
//                    startPoint.set(event.getX(), event.getY());
//                    mode = DRAG;
//                    break;
//                case MotionEvent.ACTION_POINTER_DOWN:
//                    // 第二個手指按下事件
//                    oriDis = distance(event);
//                    // 防止一個手指上出現兩個繭
//                    if (oriDis > 10f) {
//                        savedMatrix.set(matrix);
//                        midPoint = middle(event);
//                        mode = ZOOM;
//                    }
//                    break;
//                case MotionEvent.ACTION_UP:
//                case MotionEvent.ACTION_POINTER_UP:
//                    // 手指放開事件
//                    mode = NONE;
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    // 手指滑動事件
//                    if (mode == DRAG) {
//                        // 是一個手指拖動
//                        matrix.set(savedMatrix);
//                        matrix.postTranslate(event.getX() - startPoint.x, event.getY() - startPoint.y);
//                    } else if (mode == ZOOM) {
//                        // 兩個手指滑動
//                        float newDist = distance(event);
//                        if (newDist > 10f) {
//                            matrix.set(savedMatrix);
//                            float scale = newDist / oriDis;
//                            matrix.postScale(scale, scale, midPoint.x, midPoint.y);
//                        }
//                    }
//                    break;
//            }
//
//            // 設定ImageView的Matrix
//            view.setImageMatrix(matrix);
//            return true;
//        }
//    };




}
