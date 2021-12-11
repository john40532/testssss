package com.example.testssss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.hss01248.lib.MyItemDialogListener;
import com.hss01248.lib.StytledDialog;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.util.ArrayList;

public class PlayVideoActivity extends AppCompatActivity {

    private String videoFile = Environment.getExternalStorageDirectory().toString()+"/Download/video1.avi";
    private VideoCapture vc;
    private Cvfun cvfun;
    private PlaySound ps;
    private ArrayList<String> aviFilesDir = new ArrayList<>();
    private ArrayList<String> aviFilesName = new ArrayList<>();


    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {

                }
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(PlayVideoActivity.this, new String(Manifest.permission.READ_EXTERNAL_STORAGE)) ==
            PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(PlayVideoActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        setContentView(R.layout.activity_play_video);
        ps = new PlaySound(this);

        initViews();

        setListensers();

        searchVideos();
    }


    private PlayVideo tv1;
    private Button colorMask;
    private Button roi;
    private Button arrowMask;
    private Button full;
    private Button mog;
    private Button canny;
    private Button changeVideo;


    private void initViews() {
        tv1 = (PlayVideo) findViewById(R.id.videoView2);
        colorMask = (Button) findViewById(R.id.colorMask);
        roi = (Button) findViewById(R.id.roi);
        arrowMask = (Button) findViewById(R.id.arrowMask);
        full = (Button) findViewById(R.id.full);
        mog = (Button) findViewById(R.id.mog);
        canny = (Button) findViewById(R.id.canny);
        changeVideo = (Button) findViewById(R.id.changeVideo);
    }

    private void setListensers() {
//        tv1.setOnClickListener(tv1NextFrame);
        colorMask.setOnClickListener(colorMaskListener);
        roi.setOnClickListener(roiListener);
        arrowMask.setOnClickListener(arrowMaskListener);
        full.setOnClickListener(fullListener);
        mog.setOnClickListener(mogListener);
        canny.setOnClickListener(cannyListener);
        changeVideo.setOnClickListener(changeVideoListener);
    }

    private void searchVideos() {
        String path = Environment.getExternalStorageDirectory().toString()+"/Download";
        File directory = new File(path);
        File[] files = directory.listFiles();
        String fileExtension = ".avi";
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().contains(fileExtension)) {
                aviFilesDir.add(files[i].getAbsolutePath());
                aviFilesName.add(files[i].getName());
                Log.v("Files", files[i].getAbsolutePath());
            }
        }
    }

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
    private View.OnClickListener changeVideoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final ArrayList<String> strings = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                strings.add(Integer.toString(i));
            }

            StytledDialog.showBottomItemDialog(PlayVideoActivity.this, aviFilesName, "cancle", true, true, new MyItemDialogListener() {
                @Override
                public void onItemClick(String text, int position) {
                    Log.v("Files", "Open file "+text);
                    videoFile = aviFilesDir.get(position);
                    openVideo(videoFile);
                    Log.v("Files", "Open file "+aviFilesDir.get(position));

                }

                @Override
                public void onBottomBtnClick() {
//                    showToast("onItemClick");
                }
            });
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
        if (OpenCVLoader.initDebug()) {
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }

        if (cvfun == null)
            cvfun = new Cvfun();
        openVideo(videoFile);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void openVideo(String directory) {

        if (new File(directory).exists()) {
            if (vc == null) {
                vc = new VideoCapture();
                try {
                    vc.open(videoFile);
                } catch (Exception e) {

                }
                tv1.setActivityHandle(vc, cvfun, ps);
            }
            else {
                vc.release();
                vc.open(videoFile);
            }


        }
        if (!vc.isOpened()) {
            Log.v("VideoCapture", "failed");
        } else {
            Log.v("VideoCapture", "opened");
        }
    }

}