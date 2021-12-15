package com.example.testssss;


import static org.opencv.calib3d.Calib3d.estimateAffinePartial2D;
import static org.opencv.core.Core.CMP_GT;
import static org.opencv.core.Core.addWeighted;
import static org.opencv.core.Core.convertScaleAbs;
import static org.opencv.core.Core.kmeans;
import static org.opencv.imgproc.Imgproc.Canny;
import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_SIMPLEX;
import static org.opencv.imgproc.Imgproc.HoughLinesP;
import static org.opencv.imgproc.Imgproc.INTER_NEAREST;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.WARP_INVERSE_MAP;
import static org.opencv.imgproc.Imgproc.ellipse;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.goodFeaturesToTrack;
import static org.opencv.imgproc.Imgproc.morphologyEx;
import static org.opencv.imgproc.Imgproc.putText;
import static org.opencv.imgproc.Imgproc.resize;
import static org.opencv.imgproc.Imgproc.warpAffine;

import android.util.Log;

import androidx.compose.ui.graphics.DegreesKt;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
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

    public enum DebugIndex {COLORMASK, ROI, ARROWMASK, MOG, CANNY, FULL}

    private DebugIndex debugOutputTemp = DebugIndex.FULL;
    private DebugIndex debugOutput = DebugIndex.FULL;
    private Mat debugMat = new Mat();
    private boolean thread_lock = false;

    private BackgroundSubtractorMOG2 mogb = Video.createBackgroundSubtractorMOG2(500, 18, false);
    private BackgroundSubtractorMOG2 mogg = Video.createBackgroundSubtractorMOG2(500, 18, false);
    private BackgroundSubtractorMOG2 mogr = Video.createBackgroundSubtractorMOG2(500, 18, false);
    private ArrayList<ClusterPoints> arrow_candidate = new ArrayList<>();
    private ArrayList<Integer> arrow_candidate_number = new ArrayList<>();
    private ArrayList<ArrowPoint> apList = new ArrayList<>();

    int arrow_counter = 0;
    private ArrayList<Target> prev_tg = new ArrayList<>();
    private boolean frame_initilized = false;
    private DBScan dbscan = new DBScan(5, 25);

    public void setDebugOutput(DebugIndex index) {
        this.debugOutputTemp = index;
    }

    public void reset() {
        frame_initilized = false;
    }

    public Mat img_proc(Mat src, PlaySound ps) {
        debugOutput = this.debugOutputTemp;
        Mat mat = new Mat();
        src.copyTo(mat);
        ArrayList<Target> tg = get_target_region(mat);

        fixTargetTranslate(mat, tg, prev_tg);
        prev_tg = tg;

        Mat roi_mask = Mat.zeros(mat.size(), CvType.CV_8UC1);
        Mat arrow_mask;

        for (Target target : tg) {
            Imgproc.circle(mat, target.getTargetCenter(), (int) target.getTargetRadius(), new Scalar(255, 255, 0), 3);
            Imgproc.circle(roi_mask, target.getTargetCenter(), (int) target.getTargetRadius(), new Scalar(255), -1);
        }
        if (debugOutput == DebugIndex.ROI) debugMat = roi_mask;

        arrow_mask = get_arrow_mask(mat, roi_mask);

        find_arrow(arrow_mask);

        for (ArrowPoint ap : apList) {
            int region = 0;
            int value = 0;
            if (!ap.isLocked()) {
                for (Target target : tg) {
                    if (target.inTarget(ap)) {
                        region = target.getArrowRegion(ap);
                        value = target.getArrowScore(ap);
                        ap.ap_lifespan--;
                    }
                }
                if (ap.ap_lifespan<0) {
                    ap.setLock();
                }
            } else {
                region = ap.getRegion();
                value = ap.getValue();
            }

            if (!ap.playSound) {
                ap.playSound = true;
                ps.play(region, value);
            }
            String string = new String("" + region + "." + value);
            Imgproc.circle(mat, ap.avg_arrow_position, 3, new Scalar(255, 0, 255), -1);
            putText(mat, string, ap.avg_arrow_position, FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255));

        }

        if (debugOutput == DebugIndex.ROI ||
                debugOutput == DebugIndex.COLORMASK ||
                debugOutput == DebugIndex.ARROWMASK ||
                debugOutput == DebugIndex.CANNY ||
                debugOutput == DebugIndex.MOG) {
            if (debugMat.type() == CvType.CV_8UC1)
                Imgproc.cvtColor(debugMat, debugMat, Imgproc.COLOR_GRAY2BGR);
            return debugMat;
        } else {
            return mat;
        }
    }

    private void fixTargetTranslate(Mat src, ArrayList<Target> curr_target, ArrayList<Target> prev_target) {
        if (curr_target.size()!=prev_target.size())
            return;
        double sum_x=0, sum_y=0;
        for (int i = 0; i < curr_target.size(); i++) {
            sum_x += curr_target.get(i).getTargetCenter().x - prev_target.get(i).getTargetCenter().x;
            sum_y += curr_target.get(i).getTargetCenter().y - prev_target.get(i).getTargetCenter().y;
        }
        double offsetX = sum_x/curr_target.size();
        double offsetY = sum_y/curr_target.size();

        for (int i = 0; i < curr_target.size(); i++) {
            Point currentTargetCenter = curr_target.get(i).getTargetCenter();
            double update_x = currentTargetCenter.x - offsetX;
            double update_y = currentTargetCenter.y - offsetY;
            curr_target.get(i).setTargetCenter(new Point(update_x, update_y));
        }
        Mat M = new Mat( 2, 3, CvType.CV_32F );
        M.put(0,0, 1,0,-offsetX, 0,1,-offsetY);
        warpAffine(src, src, M, src.size());
    }

    /* Input C8U1, Output C8U1 */
    private void find_arrow(Mat mat) {

        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        int arrows_number = 0;
        Iterator<MatOfPoint> iterator = contours.iterator();
        while (iterator.hasNext()) {
            MatOfPoint contour = iterator.next();
            double area = Imgproc.contourArea(contour);
            Log.d("degg", "area:"+area);
            if (area < 2500 && area > 50) {
                arrows_number++;
                double density = 0.01;
                for (Point p: createPolygonPoint(contour, 10)) {
                    arrow_candidate.add(new ClusterPoints(p));
                    Imgproc.circle(mat, p, 2, new Scalar(100), -1);
                }

            }
        }
        if (debugOutput == DebugIndex.ARROWMASK) mat.copyTo(debugMat);
        arrow_candidate_number.add(arrows_number);


        dbscan.process(arrow_candidate, apList);

    }

    private ArrayList<Point> createPolygonPoint(MatOfPoint contour, int samples_number) {

        ArrayList<Point> samplesList = new ArrayList<>();
        Random random = new Random();
        int counter = 0;

        while (counter < samples_number) {
            int listSize = (int) contour.size().height;
            int startIndex = random.nextInt(listSize);
            int endIndex = random.nextInt(listSize);
            Point startP = contour.toArray()[startIndex];
            Point endP = contour.toArray()[endIndex];

            double ratio = random.nextFloat();
            Point samplePoint = new Point(endP.x * ratio - (ratio - 1) * startP.x, endP.y * ratio - (ratio - 1) * startP.y);
            samplesList.add(samplePoint);
            counter++;
        }

        return samplesList;
    }


    /* Input:
        src         : C8U3
        roi_mask    : C8U1
       Output:
        mat         : C8U1
    */
    private Mat get_arrow_mask(Mat src, Mat roi_mask) {

        ArrayList<Mat> dst = new ArrayList<>(3);
        Core.split(src, dst);
        Mat bMask = dst.get(0).clone();
        Mat gMask = dst.get(1).clone();
        Mat rMask = dst.get(2).clone();
        Mat output_mat = new Mat();

        mogb.apply(bMask, bMask);
        Imgproc.GaussianBlur(bMask, bMask, new Size(31, 31), 0);
        if (debugOutput == DebugIndex.MOG) bMask.copyTo(debugMat);

        mogg.apply(gMask, gMask);
        Imgproc.GaussianBlur(gMask, gMask, new Size(31, 31), 0);
        if (debugOutput == DebugIndex.CANNY) gMask.copyTo(debugMat);

        mogr.apply(rMask, rMask);
        Imgproc.GaussianBlur(rMask, rMask, new Size(31, 31), 0);

        Core.addWeighted(bMask, 1, gMask, 1, -50, output_mat);
        Core.addWeighted(output_mat, 1, rMask, 1, -50, output_mat);
        Imgproc.threshold(output_mat, output_mat, 200, 255, THRESH_BINARY);
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
        Imgproc.dilate(mat, mat, new Mat(), new Point(-1, -1), 5);
        Imgproc.erode(mat, mat, new Mat(), new Point(-1, -1), 5);
        if (debugOutput == DebugIndex.COLORMASK) mat.copyTo(debugMat);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Iterator<MatOfPoint> iterator = contours.iterator();
        while (iterator.hasNext()) {
            Point centers = new Point();
            float[] radius = new float[1];
            MatOfPoint contour = iterator.next();

            double area = Imgproc.contourArea(contour);
            Imgproc.minEnclosingCircle(new MatOfPoint2f(contour.toArray()), centers, radius);

            if (area > 20000) {
                Moments moments = Imgproc.moments(contour);
                Point center = new Point((int) (moments.get_m10() / moments.get_m00()), (int) (moments.get_m01() / moments.get_m00()));
                Target abc = new Target(center, radius[0], 5);
                targets.add(abc);
            }
        }

        return targets;
    }

    private Mat get_color_mask(Mat src, ColorMask color) {
        Mat dst = new Mat();
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2HSV);

        switch (color) {
            case GOLDEN:
                Scalar lower_gold = new Scalar(20, 10, 150);
                Scalar upper_gold = new Scalar(40, 255, 255);
                Core.inRange(dst, lower_gold, upper_gold, dst);
                break;
            case ORANGE:
                Scalar lower_orange_0 = new Scalar(0, 100, 200);
                Scalar upper_orange_0 = new Scalar(10, 255, 255);
                Scalar lower_orange_1 = new Scalar(170, 100, 200);
                Scalar upper_orange_1 = new Scalar(180, 255, 255);
                Mat orange_mask0 = new Mat(dst.size(), CvType.CV_8UC1);
                Mat orange_mask1 = new Mat(dst.size(), CvType.CV_8UC1);
                Core.inRange(dst, lower_orange_0, upper_orange_0, orange_mask0);
                Core.inRange(dst, lower_orange_1, upper_orange_1, orange_mask1);
                Core.bitwise_or(orange_mask0, orange_mask1, dst);
                break;
            case BLUE:
                Scalar lower_blue = new Scalar(90, 190, 100);
                Scalar upper_blue = new Scalar(130, 255, 255);
                Core.inRange(dst, lower_blue, upper_blue, dst);
                break;
            case BLACK:
                Scalar lower_black = new Scalar(0, 0, 0);
                Scalar upper_black = new Scalar(179, 255, 180);
                Core.inRange(dst, lower_black, upper_black, dst);
                break;
            case WHITE:
                Scalar lower_white = new Scalar(0, 0, 180);
                Scalar upper_white = new Scalar(179, 100, 255);
                Core.inRange(dst, lower_white, upper_white, dst);
                break;
        }

        return dst;
    }


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
                if (edgeMask.get(j, i)[0] > 0) {
                    ptsEdges.add(new Point(i, j));
                }
            }
        }
        Point pointarray[] = new Point[ptsEdges.size()];
        ptsEdges.toArray(pointarray);

        MatOfPoint2f asdf = new MatOfPoint2f(pointarray);
        RotatedRect result = Imgproc.fitEllipse(asdf);
        Mat output = new Mat(mat.size(), CvType.CV_8UC1, new Scalar(0));
        ellipse(output, result, new Scalar(255), -1);

        return output;
    }

}
