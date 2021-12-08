package com.example.testssss;

import org.opencv.core.Point;

import java.util.ArrayList;

public class ArrowPoint {

    int ID;
    Point arrow_position;
    Point avg_arrow_position = new Point(0,0);
    private int update_counter = 0;
    private boolean locked = false;
    private int len = 25;
    private int region = 0;
    private int value = 0;

    public ArrowPoint(Point point, int ID) {
        this.arrow_position = point;
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

    public void setRegion(int region) {
        this.region = region;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLock() {
        locked = true;
    }
}
