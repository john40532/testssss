package com.example.testssss;

import org.opencv.core.Point;

import java.util.ArrayList;

/**
 * Created by Jason on 2016/4/17.
 */
public class DBScan {
    class ClusterPoints {
        boolean visited = false;
        boolean isNoise = false;
        Point point;
        int cluster = 0;

        public double getDistance(Point point) {
            return Math.sqrt((this.point.x-point.x)*(this.point.x-point.x)+(this.point.y-point.y)*(this.point.y-point.y));
        }

    }

    private double radius;
    private int minPts;

    public DBScan(double radius,int minPts) {
        this.radius = radius;
        this.minPts = minPts;
    }

    public void process(ArrayList<ClusterPoints> points) {
        int cluster = 1;
        for(ClusterPoints element : points) {
            //choose an unvisited point
            if (!element.visited) {
                element.visited = true;//set visited
                ArrayList<ClusterPoints> adjacentPoints = getAdjacentPoints(element, points);
                //set the point which adjacent points less than minPts noised
                if (adjacentPoints != null && adjacentPoints.size() < minPts) {
                    element.isNoise = true;
                } else {
                    element.cluster = cluster;
                    for (int i = 0; i < adjacentPoints.size(); i++) {
                        ClusterPoints adjacentPoint = adjacentPoints.get(i);
                        //only check unvisited point, cause only unvisited have the chance to add new adjacent points
                        if (!adjacentPoint.visited) {
                            adjacentPoint.visited = true;
                            ArrayList<ClusterPoints> adjacentAdjacentPoints = getAdjacentPoints(adjacentPoint, points);
                            //add point which adjacent points not less than minPts noised
                            if (adjacentAdjacentPoints != null && adjacentAdjacentPoints.size() >= minPts) {
                                adjacentPoints.addAll(adjacentAdjacentPoints);
                            }
                        }
                        //add point which doest not belong to any cluster
                        if (adjacentPoint.cluster == 0) {
                            adjacentPoint.cluster = cluster;
                            //set point which marked noised before non-noised
                            if (adjacentPoint.isNoise) {
                                adjacentPoint.isNoise = false;
                            }
                        }
                    }
                    cluster++;
                }
            }
        }
    }

    private ArrayList<ClusterPoints> getAdjacentPoints(ClusterPoints centerPoint,ArrayList<ClusterPoints> points) {
        ArrayList<ClusterPoints> adjacentPoints = new ArrayList<ClusterPoints>();
        for (ClusterPoints p:points) {
            //include centerPoint itself
            double distance = centerPoint.getDistance(p.point);
            if (distance<=radius) {
                adjacentPoints.add(p);
            }
        }
        return adjacentPoints;
    }


}