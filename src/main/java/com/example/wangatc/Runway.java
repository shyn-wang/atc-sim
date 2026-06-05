/*
description: runway class
@author: david wang
@date: jun. 5. 26
@version: 1.0
*/

package com.example.wangatc;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Runway {
    // touchdown point
    private double startX;
    private double startY;

    // vacate point (plane despawns)
    private double endX;
    private double endY;

    private double heading; // required landing angle

    private Circle runwayStartPoint;

    private double localizerInterceptX;
    private double localizerInterceptY;

    /*
    description: constructor for Runway  objects
    pre-condition: valid arguments
    post-condition: initializes instance variables
    */
    public Runway(double startX, double startY, double endX, double endY) {
        this.startX = startX;
        this.startY = startY;

        this.endX = endX;
        this.endY = endY;

        runwayStartPoint = new Circle(startX, startY, 5); // create visual indicator for start point
        runwayStartPoint.setFill(Color.rgb(255, 214, 243));
        runwayStartPoint.setVisible(false); // initially hidden

        // calculate runway heading based on start/end points
        double[] p1 = {startX, startY};
        double[] p2 = {endX, endY};
        this.heading = Util.getHeadingTo(p1, p2);

        // calculate localizer position (70px behind runway start) -> point at which arriving planes should be flying on runway heading
        double radians = Math.toRadians(heading);
        this.localizerInterceptX = startX - (70 * Math.cos(radians)); // x component
        this.localizerInterceptY = startY - (70 * Math.sin(radians)); // y component
    }

    // getters & setters

    public Circle getRunwayStartPoint() {
        return this.runwayStartPoint;
    }

    public void setRunwayStartPointVisible(boolean visible) {
        this.runwayStartPoint.setVisible(visible);
    }


    public double getStartX() {
        return this.startX;
    }

    public double getStartY() {
        return this.startY;
    }

    public double getEndX() {
        return this.endX;
    }

    public double getEndY() {
        return this.endY;
    }


    public double getHeading() {
        return heading;
    }

    /*
    description: determines if an arriving aircraft is able to initiate automated approach & landing based on current position/heading & required heading relative to runway
    pre-condition: plane is a valid plane
    post-condition: returns true or false
    */
    public boolean isApproachAngleValid(Plane plane) { // check if plane is able to intercept the localizer on runway heading
        double[] planePos = {plane.getX(), plane.getY()};
        double angleToLocalizer = Util.getHeadingTo(planePos, new double[]{this.localizerInterceptX, this.localizerInterceptY});
        double runwayHeading = this.getHeading();

        // 1. calculate position difference (via angle) between plane & runway -> is the plane physically in the approach cone?
        double positionDiff = angleToLocalizer - runwayHeading; // angle plane makes with localizer from current position - runway angle

        while (positionDiff < -180) { // normalize to between -180 to 180
            positionDiff += 360;
        }
        while (positionDiff > 180) {
            positionDiff -= 360;
        }

        positionDiff = Math.abs(positionDiff);

        // 2. calculate heading difference between plane & runway -> is the plane facing the general direction of the runway?
        double headingDiff = plane.getCurrentHeading() - runwayHeading;

        while (headingDiff < -180) {
            headingDiff += 360;
        }
        while (headingDiff > 180) {
            headingDiff -= 360;
        }

        headingDiff = Math.abs(headingDiff);

        // calculate distance to runway
        double distance = Math.hypot(planePos[0] - this.localizerInterceptX, planePos[1] - this.localizerInterceptY); // get distance to localizer intercept

        // determine if an approach is valid based on position difference & heading difference relative to distance -> does the plane have sufficient distance to make up the position & heading difference?
        if (distance < 150) { // requirements become more forgiving as distance increases
            return positionDiff < 8 && headingDiff < 15; // very close -> must be practically on the centerline and facing the runway

        } else if (distance < 250) {
            return positionDiff < 14 && headingDiff < 30;

        } else if (distance < 350) {
            return positionDiff < 20 && headingDiff < 45;

        } else if (distance < 500) {
            return positionDiff < 40 && headingDiff < 70;

        } else if (distance < 600) {
            return positionDiff < 55 && headingDiff < 85;

        } else {
            return positionDiff < 65 && headingDiff < 100;
        }
    }
}
