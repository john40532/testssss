package com.example.testssss;

import org.opencv.core.Core;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Jason on 2016/4/17.
 */
public class DBScan {


    private double radius;
    private int minPts;
    public int cluster_counts = 0;

    public DBScan(double radius, int minPts) {
        this.radius = radius;
        this.minPts = minPts;
    }

    public void process(ArrayList<ClusterPoints> points, ArrayList<ArrowPoint> arrows) {
        int cluster = 0;
        for (ClusterPoints element : points) {
            //choose an unvisited point
            checkInExistArrows:
            if (!element.classified) {
                //check if arrow is in existed arrows
                for (ArrowPoint ap : arrows) {
                    double distance = getDistance(element.point, ap.avg_arrow_position);
                    if (distance <= 10*radius) {        //extend larger area for points into the cluster
                        ap.addPoint(element.point);
                        element.classified = true;
                        break checkInExistArrows;
                    }
                }

                //check if the point has over 25 unclassified neighbors
                ArrayList<ClusterPoints> adjacentPoints = getAdjacentPoints(element, points);
                if (adjacentPoints != null && adjacentPoints.size() < minPts) {
                    element.life_span--;
                } else {
                    ArrowPoint ap = new ArrowPoint();
                    for (int i = 0; i < adjacentPoints.size(); i++) {
                        ClusterPoints adjacentPoint = adjacentPoints.get(i);
                        //only check unclassified point, cause only unclassified have the chance to add new adjacent points
                        if (!adjacentPoint.classified) {
                            adjacentPoint.classified = true;
                            ap.addPoint(adjacentPoint.point);
                            ArrayList<ClusterPoints> adjacentAdjacentPoints = getAdjacentPoints(adjacentPoint, points);
                            //add point which adjacent points not less than minPts noised
                            if (adjacentAdjacentPoints != null && adjacentAdjacentPoints.size() >= minPts) {
                                adjacentPoints.addAll(adjacentAdjacentPoints);
                            }
                        }
                    }
                    ap.calcAvg();
                    arrows.add(ap);
                }
            }
        }

        Iterator<ClusterPoints> iterPoints = points.iterator();
        while (iterPoints.hasNext()) {
            ClusterPoints cp = iterPoints.next();
            if (cp.classified || cp.life_span<0)  iterPoints.remove();
        }
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