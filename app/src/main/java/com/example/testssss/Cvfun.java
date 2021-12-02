package com.example.testssss;


import static org.opencv.calib3d.Calib3d.estimateAffinePartial2D;
import static org.opencv.core.Core.kmeans;
import static org.opencv.imgproc.Imgproc.HoughLinesP;
import static org.opencv.imgproc.Imgproc.INTER_NEAREST;
import static org.opencv.imgproc.Imgproc.MORPH_OPEN;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.WARP_INVERSE_MAP;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.morphologyEx;
import static org.opencv.imgproc.Imgproc.resize;
import static org.opencv.imgproc.Imgproc.warpAffine;

import android.util.Log;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.utils.Converters;
import org.opencv.video.BackgroundSubtractorKNN;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class Cvfun {

    private Random rng = new Random(12345);
    private BackgroundSubtractorMOG2 mog = Video.createBackgroundSubtractorMOG2(500, 15, false);
    private BackgroundSubtractorKNN mogKNN = Video.createBackgroundSubtractorKNN();
    //    private Mat foreground = new Mat();
    public Point arrow_pos = new Point();
    private List<Point> arrow_candidate = new ArrayList<>();
    int i = 10;
    private Mat prev_frame = new Mat();
    private boolean frame_initilized = false;

    public void reset(){
        frame_initilized = false;
    }
    public Mat img_proc(Mat mat) {

        Mat target_frame = new Mat();
        mat.copyTo(target_frame);
        Mat fgmask = new Mat();
        Mat masked_frame = new Mat();
        Mat blurred_frame = new Mat();
        Mat thresh1 = new Mat();

        target_frame = find_target(target_frame);

        Core.bitwise_and(mat, mat, masked_frame, target_frame);

        if (!frame_initilized) {
            masked_frame.copyTo(prev_frame);
            frame_initilized = true;
        }
        else {
            Mat temp = masked_frame.clone();
            Core.subtract(prev_frame, masked_frame, masked_frame);
            temp.copyTo(prev_frame);
        }

        Imgproc.threshold(masked_frame, fgmask, 100, 255, THRESH_BINARY);

        mogKNN.setHistory(100);
        mogKNN.setNSamples(10);
        mogKNN.setkNNSamples(5);
        mogKNN.setDist2Threshold(50);
        mogKNN.setDetectShadows(false);
        mogKNN.apply(fgmask, fgmask);
        Imgproc.GaussianBlur(fgmask, blurred_frame, new Size(3, 3), 0);
        Imgproc.threshold(blurred_frame, thresh1, 175, 255, Imgproc.THRESH_BINARY);
        Imgproc.erode(thresh1, thresh1, new Mat(), new Point(-1, -1), 1);
        Imgproc.dilate(thresh1, thresh1, new Mat(), new Point(-1, -1), 10);

        find_arrow(thresh1);
        find_target_center(masked_frame);
        if(this.arrow_pos != null) {

            this.arrow_candidate.add(this.arrow_pos);
            if(this.arrow_candidate.size() > 2){
                Mat mp = Converters.vector_Point_to_Mat(this.arrow_candidate);
                mp.convertTo(mp, CvType.CV_32F);

                Mat labels = new Mat();
                TermCriteria criteria = new TermCriteria(TermCriteria.COUNT, 100, 1);
                Mat centers = new Mat();
                kmeans(mp, 2, labels, criteria, 1, Core.KMEANS_PP_CENTERS, centers);
                Point c;
                c = new Point(centers.get(0,0)[0], centers.get(0,1)[0]);
                Imgproc.circle(masked_frame, c, 15, new Scalar(255, 0, 255), -1);
                c = new Point(centers.get(1,0)[0], centers.get(1,1)[0]);
                Imgproc.circle(masked_frame, c, 15, new Scalar(255, 0, 255), -1);

            }
        }
        Imgproc.cvtColor(thresh1,masked_frame,Imgproc.COLOR_GRAY2BGR);

//        Imgproc.cvtColor(masked_frame, masked_frame, Imgproc.COLOR_GRAY2BGR);
//        Core.add(mat, masked_frame, masked_frame);
        return masked_frame;




//        Core.bitwise_and(mat, mat, masked_frame, target_frame);
////        Imgproc.cvtColor(masked_frame, masked_frame, Imgproc.COLOR_BGR2HSV);
//
//        mog.apply(masked_frame, fgmask);
//        Mat kernel = getStructuringElement(MORPH_RECT, new Size(5, 5));
//        morphologyEx(fgmask, fgmask, MORPH_OPEN, kernel);
//
//        Imgproc.GaussianBlur(fgmask, blurred_frame, new Size(13, 13), 0);
//        Imgproc.threshold(blurred_frame, thresh1, 175, 255, Imgproc.THRESH_BINARY);
//
//        find_arrow(thresh1);
//        find_target_center(masked_frame);
//
//        if(this.arrow_pos != null) {
//
//            this.arrow_candidate.add(this.arrow_pos);
//            if(this.arrow_candidate.size() > 2){
//                Mat mp = Converters.vector_Point_to_Mat(this.arrow_candidate);
//                mp.convertTo(mp, CvType.CV_32F);
//
//                Mat labels = new Mat();
//                TermCriteria criteria = new TermCriteria(TermCriteria.COUNT, 100, 1);
//                Mat centers = new Mat();
//                kmeans(mp, 2, labels, criteria, 1, Core.KMEANS_PP_CENTERS, centers);
//                Point c;
//                c = new Point(centers.get(0,0)[0], centers.get(0,1)[0]);
//                Imgproc.circle(masked_frame, c, 15, new Scalar(255, 0, 255), -1);
//                c = new Point(centers.get(1,0)[0], centers.get(1,1)[0]);
//                Imgproc.circle(masked_frame, c, 15, new Scalar(255, 0, 255), -1);
//
//            }
//        }
//        Imgproc.cvtColor(thresh1,masked_frame,Imgproc.COLOR_GRAY2BGR);
//        masked_frame = thresh1;
//        Imgproc.cvtColor(masked_frame, masked_frame, Imgproc.COLOR_GRAY2BGR);
//        Core.add(mat, masked_frame, masked_frame);

//        return masked_frame;

    }

    /* Output C8U1 */
    private Mat find_target(Mat mat) {

        Imgproc.GaussianBlur(mat, mat, new Size(21, 21), 0);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2HSV);
        Scalar lower_blue = new Scalar(90, 150, 100);
        Scalar upper_blue = new Scalar(130, 255, 255);
        Core.inRange(mat, lower_blue, upper_blue, mat);


        Imgproc.erode(mat, mat, new Mat(), new Point(-1, -1), 5);
        Imgproc.dilate(mat, mat, new Mat(), new Point(-1, -1), 10);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = 0;
        MatOfPoint max_contour = new MatOfPoint();
        Iterator<MatOfPoint> iterator = contours.iterator();
        while (iterator.hasNext()){
            MatOfPoint contour = iterator.next();
            double area = Imgproc.contourArea(contour);
            if(area > maxArea){
                maxArea = area;
                max_contour = contour;
            }
        }
        Rect rect = Imgproc.boundingRect(max_contour);
        Mat targetMat = Mat.zeros(mat.size(), CvType.CV_8UC1);
        Imgproc.rectangle(targetMat, rect, new Scalar(255), -1);

        return targetMat;
    }

    /* Input is C8U1 */
    private void find_arrow(Mat mat) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = 0;
        MatOfPoint max_contour = new MatOfPoint();
        Iterator<MatOfPoint> iterator = contours.iterator();
        while (iterator.hasNext()){
            MatOfPoint contour = iterator.next();
            double area = Imgproc.contourArea(contour);
            if(area > maxArea){
                maxArea = area;
                max_contour = contour;
            }
        }
        Rect rect = Imgproc.boundingRect(max_contour);
        if (rect.height>50 || rect.width>50){
            this.arrow_pos = null;
        }
        else {
            Imgproc.rectangle(mat, rect, new Scalar(255), -1);
            this.arrow_pos = new Point(rect.x + rect.width/2, rect.y + rect.height/2);
        }
    }

    private void find_target_center(Mat mat) {
        Mat hsv_mat = new Mat();
        Imgproc.cvtColor(mat, hsv_mat, Imgproc.COLOR_BGR2HSV);

        Scalar lower_red_0 = new Scalar(0, 100, 200);
        Scalar upper_red_0 = new Scalar(10, 255, 255);
        Scalar lower_red_1 = new Scalar(170, 100, 200);
        Scalar upper_red_1 = new Scalar(180, 255, 255);
        Mat red_mask0 = new Mat(mat.size(), CvType.CV_8UC1);
        Mat red_mask1 = new Mat(mat.size(), CvType.CV_8UC1);
        Mat red_mask = new Mat(mat.size(), CvType.CV_8UC1);
        Core.inRange(hsv_mat, lower_red_0, upper_red_0, red_mask0);
        Core.inRange(hsv_mat, lower_red_1, upper_red_1, red_mask1);
        Core.bitwise_or(red_mask0, red_mask1, red_mask);


        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(red_mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


        Point centers = new Point();
        float[] radius = new float[1];

        Iterator<MatOfPoint> iterator = contours.iterator();
        while (iterator.hasNext()){
            MatOfPoint contour = iterator.next();
            Imgproc.minEnclosingCircle(new MatOfPoint2f(contour.toArray()), centers, radius);

            if (radius[0] > 50) {
                Moments moments = Imgproc.moments(contour);
                Point center = new Point((int) (moments.get_m10() / moments.get_m00()), (int) (moments.get_m01() / moments.get_m00()));
                Imgproc.circle(mat, centers, (int) radius[0], new Scalar(0, 255, 255), 2);
                Imgproc.circle(mat, center, 5, new Scalar(0, 0, 255), -1);
            }
        }

    }
}
