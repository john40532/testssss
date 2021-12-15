package com.example.testssss;

import org.opencv.core.Point;

import java.util.ArrayList;

public class ArrowPoint {

    int ID;
    ArrayList<Point> measuredPoints = new ArrayList<>();
    Point avg_arrow_position = new Point(0,0);
    private int update_counter = 0;
    private boolean locked = false;
    private int len = 25;
    private int region = 0;
    private int value = 0;
    boolean playSound = false;
    int ap_lifespan = 30;

    public ArrowPoint() {}

    public ArrowPoint(int ID, ArrayList<ClusterPoints> clusterPoints) {
        this.ID = ID;
        for (ClusterPoints p : clusterPoints) {
            this.measuredPoints.add(p.point);
        }
    }

    public ArrowPoint(Point point, int ID) {
        this.avg_arrow_position = point;
        this.ID = ID;
    }

    public int getRegion() {
        if (locked)     return this.region;
        else            return 0;
    }

    public int getValue() {
        if (locked)     return this.value;
        else            return 0;
    }

    public void addPoint(Point point) {
        measuredPoints.add(point);
    }

    public void setRegion(int region) {
        this.region = region;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void calcAvg() {
        double x=0, y=0;
        for (Point p : measuredPoints) {
            x += p.x;
            y += p.y;
        }
        avg_arrow_position = new Point(x/measuredPoints.size(), y/ measuredPoints.size());
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLock() {
        locked = true;
    }
}
