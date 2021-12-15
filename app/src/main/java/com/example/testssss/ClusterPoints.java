package com.example.testssss;

import org.opencv.core.Point;

public class ClusterPoints {
    boolean classified = false;
    boolean isNoise = false;
    int life_span = 30;
    Point point;
    int cluster = 0;

    public ClusterPoints(double x, double y) {
        this.point = new Point(x, y);
    }

    public ClusterPoints(Point point) {
        this.point = point.clone();
    }

}