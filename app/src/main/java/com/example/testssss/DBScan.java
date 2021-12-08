package com.example.testssss;

import org.opencv.core.Core;
import org.opencv.core.Point;

import java.util.ArrayList;

/**
 * Created by Jason on 2016/4/17.
 */
public class DBScan {


    private double radius;
    private int minPts;
    private int time_span;
    public int cluster_counts = 0;

    public DBScan(double radius, int minPts, int time_span) {
        this.radius = radius;
        this.minPts = minPts;
        this.time_span = time_span;
    }

    public int process(ArrayList<ClusterPoints> points) {
        int cluster = 0;
        for(ClusterPoints element : points) {
            //choose an unvisited point
            if (!element.visited) {
                element.visited = true;//set visited
                ArrayList<ClusterPoints> adjacentPoints = getAdjacentPoints(element, points);
                //set the point which adjacent points less than minPts noised
                if (adjacentPoints != null && adjacentPoints.size() < minPts) {
                    element.isNoise = true;
                } else {
                    cluster++;
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
                }
            }
        }
        this.cluster_counts = cluster;
        return this.cluster_counts;
    }


    private ArrayList<ClusterPoints> getAdjacentPoints(ClusterPoints centerPoint,ArrayList<ClusterPoints> points) {
        ArrayList<ClusterPoints> adjacentPoints = new ArrayList<ClusterPoints>();
        for (ClusterPoints p:points) {
            //include centerPoint itself
            double distance = getDistance(centerPoint.point, p.point);
            if (distance<=radius) {
                adjacentPoints.add(p);
            }
        }
        return adjacentPoints;
    }

    public double getDistance(Point pt1, Point pt2) {
        return Math.sqrt(Math.pow((pt1.x-pt2.x), 2) + Math.pow((pt1.y-pt2.y),2));
    }

}