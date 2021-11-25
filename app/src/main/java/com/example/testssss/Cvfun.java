package com.example.testssss;


import static org.opencv.core.Core.kmeans;

import android.util.Log;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;

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
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class Cvfun {

    private Random rng = new Random(12345);
    private BackgroundSubtractorMOG2 mog = Video.createBackgroundSubtractorMOG2();
    private Mat foreground = new Mat();
    public Point arrow_pos=new Point();
    private List<Point> arrow_candidate = new ArrayList<>();
    int i = 10;


    public Mat img_proc(Mat mat) {
        Mat target_frame = new Mat();
        mat.copyTo(target_frame);
        Mat fgmask = new Mat();
        Mat masked_frame = new Mat();
        Mat blurred_frame = new Mat();
        Mat thresh1 = new Mat();

        find_target(target_frame);

        Core.bitwise_and(mat, mat, masked_frame, target_frame);

        mog.apply(masked_frame, fgmask);

        Imgproc.GaussianBlur(fgmask, blurred_frame, new Size(13, 13), 0);
        Imgproc.threshold(blurred_frame, thresh1, 250, 255, Imgproc.THRESH_BINARY);

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


        return masked_frame;
    }

    private void find_target(Mat mat) {

        Imgproc.GaussianBlur(mat, mat, new Size(21, 21), 0);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2HSV);
        Scalar lower_white = new Scalar(0, 0, 168);
        Scalar upper_white = new Scalar(172, 111, 255);
        Core.inRange(mat, lower_white, upper_white, mat);

        Imgproc.erode(mat, mat, new Mat(), new Point(-1, -1), 5);
        Imgproc.dilate(mat, mat, new Mat(), new Point(-1, -1), 5);

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
        Imgproc.rectangle(mat, rect, new Scalar(255,1,1), 2);

    }

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
            Imgproc.rectangle(mat, rect, new Scalar(255,255,0), 2);
            this.arrow_pos = new Point(rect.x + rect.width/2, rect.y + rect.height/2);
        }
    }

    private void find_target_center(Mat mat) {
        Mat hsv_mat = new Mat();
        Imgproc.cvtColor(mat, hsv_mat, Imgproc.COLOR_RGB2HSV);

        Scalar lower_red_0 = new Scalar(0, 70, 0);
        Scalar upper_red_0 = new Scalar(5, 255, 255);
        Scalar lower_red_1 = new Scalar(175, 70, 0);
        Scalar upper_red_1 = new Scalar(180, 255, 255);
        Mat red_mask0 = new Mat(mat.size(), CvType.CV_8UC3);
        Mat red_mask1 = new Mat(mat.size(), CvType.CV_8UC3);
        Mat red_mask = new Mat(mat.size(), CvType.CV_8UC3);
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

            if (radius[0] > 10) {
                Moments moments = Imgproc.moments(contour);
                Point center = new Point((int) (moments.get_m10() / moments.get_m00()), (int) (moments.get_m01() / moments.get_m00()));
                Imgproc.circle(mat, centers, (int) radius[0], new Scalar(0, 255, 255), 2);
                Imgproc.circle(mat, center, 5, new Scalar(0, 0, 255), 2);
            }
        }

    }
}
