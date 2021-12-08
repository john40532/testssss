package com.example.testssss;


import static org.opencv.calib3d.Calib3d.estimateAffinePartial2D;
import static org.opencv.core.Core.CMP_GT;
import static org.opencv.core.Core.addWeighted;
import static org.opencv.core.Core.convertScaleAbs;
import static org.opencv.core.Core.kmeans;
import static org.opencv.imgproc.Imgproc.Canny;
import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_SIMPLEX;
import static org.opencv.imgproc.Imgproc.HoughLinesP;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.ellipse;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.morphologyEx;
import static org.opencv.imgproc.Imgproc.putText;
import static org.opencv.imgproc.Imgproc.resize;
import static org.opencv.imgproc.Imgproc.warpAffine;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Cvfun {
    private enum ColorMask {GOLDEN, ORANGE, BLUE, BLACK, WHITE}
    private BackgroundSubtractorMOG2 mog = Video.createBackgroundSubtractorMOG2(500, 20, false);
    private ArrayList<ClusterPoints> arrow_candidate = new ArrayList<>();
    private ArrayList<Integer> arrow_candidate_number = new ArrayList<>();
    private ArrayList<ArrowPoint> apList = new ArrayList<>();

    int arrow_counter = 0;
    private Mat prev_frame = new Mat();
    private boolean frame_initilized = false;
    private DBScan dbscan = new DBScan(5, 25, 30);

    public void reset(){
        frame_initilized = false;
    }
    public Mat img_proc(Mat mat, PlaySound ps) {
        Mat roi_mask = Mat.zeros(mat.size(), CvType.CV_8UC1);
        Mat arrow_mask;

        ArrayList<Target> tg = get_target_region(mat);
        for (Target target: tg) {
            Imgproc.circle(mat, target.getTargetCenter(), (int)target.getTargetRadius(), new Scalar(255, 255, 0), 3);
            Imgproc.circle(roi_mask, target.getTargetCenter(), (int)target.getTargetRadius(), new Scalar(255), -1);
        }

        arrow_mask = get_arrow_mask(mat, roi_mask);

        find_arrow(arrow_mask);

        for (ArrowPoint ap:apList) {
            int region = 0;
            int value = 0;
            if (!ap.isLocked()) {
                for (Target target: tg) {
                    if (target.inTarget(ap)) {
                        region = target.getArrowRegion(ap);
                        value = target.getArrowScore(ap);
                        ap.setLock();
                        ps.play(region, value);
                    }
                }
            }
            else {
                region = ap.getRegion();
                value = ap.getValue();
            }
            String string = new String(""+region+"."+value);
            Imgproc.circle(mat, ap.arrow_position, 3, new Scalar(255, 0, 255), -1);
            putText(mat, string, ap.arrow_position, FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255));

        }
//        Imgproc.cvtColor(arrow_mask, roi_mask, Imgproc.COLOR_GRAY2BGR);

        return mat;
    }

    /* Input C8U1, Output C8U1 */
    private void find_arrow(Mat mat) {

        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        int arrows_number = 0;
        Iterator<MatOfPoint> iterator = contours.iterator();
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2BGR);
        while (iterator.hasNext()){
            MatOfPoint contour = iterator.next();
            double area = Imgproc.contourArea(contour);
            if(area < 2500 && area > 50){
                arrows_number++;
                Rect rect = Imgproc.boundingRect(contour);
                arrow_candidate.add(new ClusterPoints(rect.x + rect.width/2, rect.y + rect.height/2));
            }
        }
        arrow_candidate_number.add(arrows_number);


        int detected_clusters = dbscan.process(arrow_candidate);

        if (detected_clusters>0) {
            for (ClusterPoints p :arrow_candidate) {
                if (p.cluster > 0) {
                    arrow_counter++;
                    ArrowPoint ap = new ArrowPoint(p.point, arrow_counter);
                    apList.add(ap);
                    arrow_candidate.clear();
                    arrow_candidate_number.clear();
                    break;
                }
            }
        }

        // remove old candidates in oldest frame
        if (arrow_candidate_number.size()>30){
            int number = arrow_candidate_number.get(0);
            arrow_candidate_number.remove(0);
            for (int i = 0; i < number; i++) {
                arrow_candidate.remove(0);
            }
        }
    }


    /* Input:
        src         : C8U3
        roi_mask    : C8U1
       Output:
        mat         : C8U1
    */
    private Mat get_arrow_mask(Mat src, Mat roi_mask) {

        Mat black_mask = src.clone();
        Mat canny_mat = src.clone();
        Mat output_mat = new Mat();

        /* Black filter */
        black_mask = get_color_mask(black_mask, ColorMask.BLACK);
        Imgproc.erode(black_mask, black_mask, new Mat(), new Point(-1, -1), 1);

        mog.apply(black_mask, black_mask);
        Imgproc.GaussianBlur(black_mask, black_mask, new Size(23, 23), 0);
        Imgproc.threshold(black_mask, black_mask, 100, 255, THRESH_BINARY);

        /* Canny filter */
        Imgproc.cvtColor(canny_mat, canny_mat, Imgproc.COLOR_BGR2GRAY);
        Canny(canny_mat, canny_mat, 100, 180,3);
        Imgproc.dilate(canny_mat, canny_mat, new Mat(), new Point(-1, -1), 2);

        mog.apply(canny_mat, canny_mat);
        Imgproc.GaussianBlur(canny_mat, canny_mat, new Size(23, 23), 0);
        Imgproc.threshold(canny_mat, canny_mat, 100, 255, THRESH_BINARY);

//      May try addWeighted();
        Core.subtract(black_mask, canny_mat, output_mat);

        Core.bitwise_and(output_mat, roi_mask, output_mat);

        return output_mat;
    }


    /* Input C8U3, Output C8U1 */
    private ArrayList<Target> get_target_region(Mat src) {
        ArrayList<Target> targets = new ArrayList<>();

        Mat mat = new Mat();
        src.copyTo(mat);

        Imgproc.GaussianBlur(mat, mat, new Size(21, 21), 0);

        mat = get_color_mask(mat, ColorMask.BLUE);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Iterator<MatOfPoint> iterator = contours.iterator();
        Mat targetMat = Mat.zeros(mat.size(), CvType.CV_8UC1);
        while (iterator.hasNext()){
            Point centers = new Point();
            float[] radius = new float[1];
            MatOfPoint contour = iterator.next();

            double area = Imgproc.contourArea(contour);
            Imgproc.minEnclosingCircle(new MatOfPoint2f(contour.toArray()), centers, radius);

            if (area > 5000) {
                Moments moments = Imgproc.moments(contour);
                Point center = new Point((int) (moments.get_m10() / moments.get_m00()), (int) (moments.get_m01() / moments.get_m00()));
                Target abc = new Target(center, radius[0], 5);
                targets.add(abc);
            }
        }

        return targets;
    }

    private Mat get_color_mask(Mat mat, ColorMask color) {
        Mat circle = new Mat();
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2HSV);

        switch (color) {
            case GOLDEN:
                Scalar lower_gold = new Scalar(20, 10, 150);
                Scalar upper_gold = new Scalar(40, 255, 255);
                Core.inRange(mat, lower_gold, upper_gold, circle);
                break;
            case ORANGE:
                Scalar lower_orange_0 = new Scalar(0, 100, 200);
                Scalar upper_orange_0 = new Scalar(10, 255, 255);
                Scalar lower_orange_1 = new Scalar(170, 100, 200);
                Scalar upper_orange_1 = new Scalar(180, 255, 255);
                Mat orange_mask0 = new Mat(mat.size(), CvType.CV_8UC1);
                Mat orange_mask1 = new Mat(mat.size(), CvType.CV_8UC1);
                Mat orange_mask = new Mat(mat.size(), CvType.CV_8UC1);
                Core.inRange(mat, lower_orange_0, upper_orange_0, orange_mask0);
                Core.inRange(mat, lower_orange_1, upper_orange_1, orange_mask1);
                Core.bitwise_or(orange_mask0, orange_mask1, circle);
                break;
            case BLUE:
                Scalar lower_blue = new Scalar(90, 150, 100);
                Scalar upper_blue = new Scalar(130, 255, 255);
                Core.inRange(mat, lower_blue, upper_blue, circle);
                break;
            case BLACK:
                Scalar lower_black = new Scalar(0, 0, 0);
                Scalar upper_black = new Scalar(179, 255, 180);
                Core.inRange(mat, lower_black, upper_black, circle);
                break;
            case WHITE:
                Scalar lower_white = new Scalar(0, 0, 180);
                Scalar upper_white = new Scalar(179,100,255);
                Core.inRange(mat, lower_white, upper_white, circle);
                break;
        }

//        List<MatOfPoint> contours = new ArrayList<>();
//        Mat circle_mask = new Mat(circle.size(), CvType.CV_8UC1, new Scalar(0));
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(circle, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        double maxArea = 0;
//        int max_contour_index = 0;
//        MatOfPoint max_contour = new MatOfPoint();
//        ListIterator<MatOfPoint> iterator = contours.listIterator();
//        while (iterator.hasNext()){
//            int index = iterator.nextIndex();
//            MatOfPoint contour = iterator.next();
//            double area = Imgproc.contourArea(contour);
//            if(area > maxArea){
//                maxArea = area;
//                max_contour = contour;
//                max_contour_index = index;
//            }
//        }
//
//        Imgproc.drawContours(circle_mask, contours, max_contour_index, new Scalar(255), -1);
//
//        Mat filled_circle_mask = fill_ellipse(circle_mask);
//        Imgproc.erode(filled_circle_mask, filled_circle_mask, new Mat(), new Point(-1, -1), 1);
//        Core.subtract(filled_circle_mask, circle_mask, circle_mask);

        return circle;
    }

//    private Mat old_way(Mat mat) {
//
//
//        Mat target_frame = new Mat();
//        mat.copyTo(target_frame);
//        Mat fgmask = new Mat();
//        Mat masked_frame = new Mat();
//        Mat blurred_frame = new Mat();
//        Mat thresh1 = new Mat();
//
//        target_frame = find_target(target_frame);
//        Core.bitwise_and(mat, mat, masked_frame, target_frame);
//
//        if (!frame_initilized) {
//            masked_frame.copyTo(prev_frame);
//            frame_initilized = true;
//        }
//        else {
//            Mat temp = masked_frame.clone();
//            Core.subtract(prev_frame, masked_frame, masked_frame);
//            temp.copyTo(prev_frame);
//        }
//
//        Imgproc.threshold(masked_frame, fgmask, 100, 255, THRESH_BINARY);
//
//        mogKNN.setHistory(100);
//        mogKNN.setNSamples(10);
//        mogKNN.setkNNSamples(5);
//        mogKNN.setDist2Threshold(50);
//        mogKNN.setDetectShadows(false);
//        mogKNN.apply(fgmask, fgmask);
//        Imgproc.GaussianBlur(fgmask, blurred_frame, new Size(3, 3), 0);
//        Imgproc.threshold(blurred_frame, thresh1, 175, 255, Imgproc.THRESH_BINARY);
//        Imgproc.erode(thresh1, thresh1, new Mat(), new Point(-1, -1), 1);
//        Imgproc.dilate(thresh1, thresh1, new Mat(), new Point(-1, -1), 10);
//
//        find_arrow(thresh1);
//        find_target_center(masked_frame);
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
//
//        return masked_frame;
//    }

//    private ArrayList<Target> find_target_center(Mat mat) {
//        Mat red_mask = get_color_mask(mat, ColorMask.ORANGE);
//
//        List<MatOfPoint> contours = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(red_mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        Point centers = new Point();
//        float[] radius = new float[1];
//
//        Iterator<MatOfPoint> iterator = contours.iterator();
//        while (iterator.hasNext()){
//            MatOfPoint contour = iterator.next();
//            Imgproc.minEnclosingCircle(new MatOfPoint2f(contour.toArray()), centers, radius);
//
//            if (radius[0] > 50) {
//                Moments moments = Imgproc.moments(contour);
//                Point center = new Point((int) (moments.get_m10() / moments.get_m00()), (int) (moments.get_m01() / moments.get_m00()));
//
//            }
//        }
//        return targets;
//
//    }

    private Mat extract_arrows(Mat mat) {
//        Imgproc.GaussianBlur(mat, mat, new Size(3, 3), 0);

        /* find golden circle */
        Mat golden_mask = get_color_mask(mat, ColorMask.GOLDEN);
        Mat orange_mask = get_color_mask(mat, ColorMask.ORANGE);
        Mat blue_mask = get_color_mask(mat, ColorMask.BLUE);
        Mat sum_mask = new Mat();
        Core.bitwise_or(golden_mask, orange_mask, sum_mask);
        Core.bitwise_or(sum_mask, blue_mask, sum_mask);
        Mat output = sum_mask;
        Imgproc.cvtColor(output, output, Imgproc.COLOR_GRAY2BGR);

        return output;
    }

    private Mat fill_ellipse(Mat mat) {
        Mat magX = new Mat();
        Mat absmagX = new Mat();
        Imgproc.Sobel(mat, magX, CvType.CV_32FC1, 1, 0);
        Core.convertScaleAbs(magX, absmagX);

        Mat magY = new Mat();
        Mat absmagY = new Mat();
        Imgproc.Sobel(mat, magY, CvType.CV_32FC1, 1, 0);
        Core.convertScaleAbs(magY, absmagY);

        Mat mag = new Mat();
        Core.add(absmagX, absmagY, mag);

        Mat edgeMask = new Mat();
        Core.compare(mag, new Scalar(0), edgeMask, CMP_GT);

        List<Point> ptsEdges = new ArrayList<>();
        for (int j = 0; j < edgeMask.rows(); j++) {
            for (int i = 0; i < edgeMask.cols(); i++) {
                if(edgeMask.get(j, i)[0]>0){
                    ptsEdges.add(new Point(i,j));
                }
            }
        }
        Point pointarray[] = new Point[ptsEdges.size()];
        ptsEdges.toArray(pointarray);

        MatOfPoint2f asdf = new MatOfPoint2f(pointarray);
        RotatedRect result = Imgproc.fitEllipse(asdf);
        Mat output = new Mat(mat.size(), CvType.CV_8UC1, new Scalar(0));
        ellipse(output, result, new Scalar(255),-1);

        return output;
    }

}
