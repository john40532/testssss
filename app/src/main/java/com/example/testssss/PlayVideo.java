package com.example.testssss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.File;

public class PlayVideo extends AppCompatActivity {

    private VideoCapture vc;
    private File videoFile = new File("/storage/3638-6538/Download/video1.avi");
    private Cvfun cvfun;
    private PlaySound ps;


    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:{
//                    cameraBridgeViewBase.enableView();
                    cvfun = new Cvfun();

                }
                default:{
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(PlayVideo.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        setContentView(R.layout.activity_play_video);
        ps = new PlaySound(this);

        initViews();

        setListensers();

        handler.post(runnableCode);

    }


    private ImageView tv1;
    private Button colorMask;
    private Button roi;
    private Button arrowMask;
    private Button full;
    private Button mog;
    private Button canny;


    private void initViews()
    {
        tv1 = (ImageView) findViewById(R.id.videoView2);
        colorMask = (Button) findViewById(R.id.colorMask);
        roi = (Button) findViewById(R.id.roi);
        arrowMask = (Button) findViewById(R.id.arrowMask);
        full = (Button) findViewById(R.id.full);
        mog = (Button) findViewById(R.id.mog);
        canny = (Button) findViewById(R.id.canny);
    }

    private void setListensers()
    {
        tv1.setOnClickListener(tv1NextFrame);
        colorMask.setOnClickListener(colorMaskListener);
        roi.setOnClickListener(roiListener);
        arrowMask.setOnClickListener(arrowMaskListener);
        full.setOnClickListener(fullListener);
        mog.setOnClickListener(mogListener);
        canny.setOnClickListener(cannyListener);
    }

    // Create the Handler object (on the main thread by default)
    Handler handler = new Handler();
    // Define the code block to be executed
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            if(nextFrame()) {
                handler.postDelayed(this, 33);
            }
            else {
                handler.removeMessages(0);
            }
        }
    };
    // Start the initial runnable task by posting through the handler

    private View.OnClickListener tv1NextFrame = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };

    private View.OnClickListener colorMaskListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cvfun.setDebugOutput(Cvfun.DebugIndex.COLORMASK);
        }
    };
    private View.OnClickListener roiListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cvfun.setDebugOutput(Cvfun.DebugIndex.ROI);
        }
    };
    private View.OnClickListener arrowMaskListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cvfun.setDebugOutput(Cvfun.DebugIndex.ARROWMASK);
        }
    };
    private View.OnClickListener fullListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cvfun.setDebugOutput(Cvfun.DebugIndex.FULL);
        }
    };
    private View.OnClickListener mogListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cvfun.setDebugOutput(Cvfun.DebugIndex.MOG);
        }
    };
    private View.OnClickListener cannyListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cvfun.setDebugOutput(Cvfun.DebugIndex.CANNY);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if request is denied, this will return an empty array
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
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
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }

        openVideo();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (handler != null) {
            handler.removeMessages(0);
        }
    }

    private boolean openVideo() {

        if (videoFile.exists()) {
            vc = new VideoCapture();

            String absPath = videoFile.getAbsolutePath();
            try {
                vc.open("/storage/3638-6538/Download/video1.avi");
            } catch (Exception e) {
                return false;
            }

            if (!vc.isOpened()) {
                Log.v("VideoCapture", "failed");
            }
            else {
                Log.v("VideoCapture", "opened");
            }
        }
        return true;
    }

    protected int frame_number = 0;
    private boolean nextFrame() {
        Mat mFrame = new Mat();
        if (vc.read(mFrame)) {
            frame_number++;
            Imgproc.cvtColor(mFrame, mFrame, Imgproc.COLOR_RGB2BGR);
            mFrame = cvfun.img_proc(mFrame, ps);
            Imgproc.putText(mFrame, String.valueOf(frame_number), new Point(30,150), Imgproc.FONT_HERSHEY_COMPLEX, 2, new Scalar(0, 255, 255), 1);
            Imgproc.cvtColor(mFrame, mFrame, Imgproc.COLOR_BGR2RGB);

            Bitmap mBitmap = Bitmap.createBitmap(mFrame.cols(), mFrame.rows(), Bitmap.Config.ARGB_8888);;
            Utils.matToBitmap(mFrame, mBitmap);
            tv1.setImageBitmap(mBitmap);
            Log.d("degg", "frame"+frame_number);
        }
        else {
            return false;
        }

        return true;
    }
}