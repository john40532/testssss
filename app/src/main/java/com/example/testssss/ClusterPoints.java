package com.example.testssss;

import org.opencv.core.Point;

public class ClusterPoints {
    boolean visited = false;
    boolean isNoise = false;
    Point point;
    int cluster = 0;

    public ClusterPoints(double x, double y) {
        this.point = new Point(x, y);
    }

    public double getDistance(Point point) {
        return Math.sqrt((this.point.x-point.x)*(this.point.x-point.x)+(this.point.y-point.y)*(this.point.y-point.y));
    }
}