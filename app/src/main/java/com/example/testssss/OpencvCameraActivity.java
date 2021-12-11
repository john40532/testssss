package com.example.testssss;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class OpencvCameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "MainActivity";
    Mat mRGBA;
    Mat mRGBAT;
    Cvfun cvfun;
    CameraBridgeViewBase cameraBridgeViewBase;
    int image_height, image_width;
    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:{
                    Log.i(TAG, "onManagerConnected: Opencv loaded");
                    cameraBridgeViewBase.enableView();
                    cvfun = new Cvfun();
                }
                default:{
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    private Button open_camera;
    private DrawView view3;

    private void initViews()
    {
        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.camera_surface);
        view3 = findViewById(R.id.view3);
    }

    private void setListensers()
    {
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(OpencvCameraActivity.this, new String[]{Manifest.permission.CAMERA},1);
        setContentView(R.layout.activity_opencv_camera);
        initViews();

        setListensers();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if request is denied, this will return an empty array
        switch(requestCode){
            case 1:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    cameraBridgeViewBase.setCameraPermissionGranted();
                }
                else{
                    //permisiion denied
                }
                return;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()){
//            if load success
            Log.d(TAG, "onResume: Opencv initialized");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            Log.d(TAG, "onResume: Opencv not initialized");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }

//        view3.setLayoutParams(new );
        ViewGroup.LayoutParams params = view3.getLayoutParams();

        params.height = (int) (cameraBridgeViewBase.getImageWidth() * 1.5);
        params.width = (int) (cameraBridgeViewBase.getImageHeight() * 1.5);
        view3.requestLayout();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase !=null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraBridgeViewBase !=null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    public void onCameraViewStopped() {
        mRGBA.release();
        mRGBAT.release();

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRGBA = new Mat();
        mRGBAT = new Mat(height, width, CvType.CV_8UC1);

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRGBA = inputFrame.rgba();

//        mRGBAT = inputFrame.gray();

        Imgproc.cvtColor(mRGBA, mRGBA, Imgproc.COLOR_RGB2BGR);
        if (view3.zoom) {
            Point roiRec = view3.zoomInRec;
            int roiWidth = (int) view3.zoomInWidth;
            int roiHeight = (int) view3.zoomInHeight;

            Rect roi = new Rect((int)roiRec.x, (int)roiRec.y, roiHeight, roiWidth);
            Mat mRGBA_roi = new Mat(mRGBA, roi);

            mRGBA_roi = cvfun.img_proc(mRGBA_roi, null);
            mRGBA_roi.copyTo(new Mat(mRGBA, roi));
        }
        else {
            cvfun.reset();
        }

        return mRGBA;
    }

    public void screen_tapped(View view) {

    }

}