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
            startActivity(new Intent(MainActivity.this, OpencvCameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    };

    private View.OnClickListener loadSampleVideo = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            startActivity(new Intent(MainActivity.this, PlayVideoActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
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
//        // ??????????????????????????????
//        private PointF startPoint = new PointF();
//        // ??????????????????????????????????????????
//        private PointF midPoint = new PointF();
//        // ????????????????????????????????????????????????
//        private float oriDis = 1f;
//        // ????????????????????????????????????
//        private float distance(MotionEvent event) {
//            float x = event.getX(0) - event.getX(1);
//            float y = event.getY(0) - event.getY(1);
//            return (float) Math.sqrt(x * x + y * y);
//        }
//
//        // ??????????????????????????????
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
//            // ??????????????????????????????????????????
//            switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                case MotionEvent.ACTION_DOWN:
//                    Log.d("touch", "ACTION_DOWN");
//
//                    // ???????????????????????????
//                    matrix.set(view.getImageMatrix());
//                    savedMatrix.set(matrix);
//                    startPoint.set(event.getX(), event.getY());
//                    mode = DRAG;
//                    break;
//                case MotionEvent.ACTION_POINTER_DOWN:
//                    // ???????????????????????????
//                    oriDis = distance(event);
//                    // ????????????????????????????????????
//                    if (oriDis > 10f) {
//                        savedMatrix.set(matrix);
//                        midPoint = middle(event);
//                        mode = ZOOM;
//                    }
//                    break;
//                case MotionEvent.ACTION_UP:
//                case MotionEvent.ACTION_POINTER_UP:
//                    // ??????????????????
//                    mode = NONE;
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    // ??????????????????
//                    if (mode == DRAG) {
//                        // ?????????????????????
//                        matrix.set(savedMatrix);
//                        matrix.postTranslate(event.getX() - startPoint.x, event.getY() - startPoint.y);
//                    } else if (mode == ZOOM) {
//                        // ??????????????????
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
//            // ??????ImageView???Matrix
//            view.setImageMatrix(matrix);
//            return true;
//        }
//    };




}
