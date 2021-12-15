package com.example.testssss;

import android.util.Log;

import org.opencv.core.Point;

public class Target {
    private Point center = new Point(0,0);
    private double radius = 100;
    private int rings;

    public Target(Point center, double radius, int rings) {
        this.center = center;
        this.radius = radius;
        this.rings = rings;
    }

    public boolean inTarget(ArrowPoint arrow) {
        Point p = cartesian2polar(arrow.avg_arrow_position);
        double distance = p.x;
        if (distance<radius)    return true;
        else                    return false;
    }

    public int getArrowScore(ArrowPoint arrow) {
        if (!arrow.isLocked()) {
            Point p = cartesian2polar(arrow.avg_arrow_position);
            double distance = p.x;
            int value = 10 - (int)Math.floor(distance*(rings)/this.radius);
            arrow.setValue(value);
            return value;
        }
        else {
            return arrow.getValue();
        }
    }

    public int getArrowRegion(ArrowPoint arrow) {
        if (!arrow.isLocked()) {
            Point p = cartesian2polar(arrow.avg_arrow_position);
            int phase = (int)(p.y+180);
            int region;
            if (phase>=15 && phase<45)          region =  10;
            else if (phase>=45 && phase<75)     region =  11;
            else if (phase>=75 && phase<105)    region =  12;
            else if (phase>=105 && phase<135)   region =  1;
            else if (phase>=135 && phase<165)   region =  2;
            else if (phase>=165 && phase<195)   region =  3;
            else if (phase>=195 && phase<225)   region =  4;
            else if (phase>=225 && phase<255)   region =  5;
            else if (phase>=255 && phase<285)   region =  6;
            else if (phase>=285 && phase<315)   region =  7;
            else if (phase>=315 && phase<345)   region =  8;
            else                                region =  9;
            arrow.setRegion(region);
            return region;
        }
        else {
            return arrow.getRegion();
        }
    }

    private Point cartesian2polar(Point arrow) {
        Point vector = new Point(arrow.x-this.center.x, arrow.y-this.center.y);
        double r = Math.sqrt(Math.pow(vector.x, 2)+Math.pow(vector.y, 2));
        double theta = Math.toDegrees(Math.atan2(vector.y, vector.x));

        return new Point(r, theta);
    }

    public Point getTargetCenter() {
        return this.center;
    }

    public void setTargetCenter(Point point) {
        this.center = point.clone();
    }

    public double getTargetRadius() {
        return this.radius;
    }
}
