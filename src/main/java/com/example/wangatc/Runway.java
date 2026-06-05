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

    public Runway(double startX, double startY, double endX, double endY) {
        this.startX = startX;
        this.startY = startY;

        this.endX = endX;
        this.endY = endY;

        runwayStartPoint = new Circle(startX, startY, 5);
        runwayStartPoint.setFill(Color.rgb(255, 214, 243));
        runwayStartPoint.setVisible(false); // initially hidden

        // calculate runway heading
        double[] p1 = {startX, startY};
        double[] p2 = {endX, endY};
        this.heading = Util.getHeadingTo(p1, p2);

        // calculate localizer position (70px behind runway start) -> point at which planes should be flying on runway heading
        double radians = Math.toRadians(heading);
        this.localizerInterceptX = startX - (70 * Math.cos(radians)); // x component
        this.localizerInterceptY = startY - (70 * Math.sin(radians)); // y component
    }

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


    public boolean isApproachAngleValid(double[] planePos) { // check if plane is able to intercept the localizer on runway heading
        double angleToLocalizer = Util.getHeadingTo(planePos, new double[] {this.localizerInterceptX, this.localizerInterceptY});
        double runwayHeading = this.getHeading();

        // compare angle between plane & localizer to runway heading
        double diff = Math.abs(angleToLocalizer - runwayHeading);
        while (diff > 180) {
            diff -= 360; // normalize to 0-180
        }
        diff = Math.abs(diff);

        double distance = Math.hypot(planePos[0] - this.localizerInterceptX, planePos[1] - this.localizerInterceptY); // get distance to localizer intercept

        // determine if an approach is valid based on angle difference & distance -> does the plane have sufficient distance to make up the angle difference?
        if (distance < 150) {
            return diff < 8; // when the plane is within 150px of the localizer intercept, approaches will only be permitted when the angle difference is less than 8 deg

        } else if (distance < 300) {
            return diff < 20;

        } else if (distance < 500) {
            return diff < 40;

        } else if (distance < 600) {
            return diff < 55;

        } else {
            return diff < 65;

        }
    }
}
