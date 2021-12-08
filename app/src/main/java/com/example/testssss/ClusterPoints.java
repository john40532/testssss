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

}