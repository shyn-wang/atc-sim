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

    private Circle runwayStartPoint;

    public Runway(double startX, double startY, double endX, double endY) {
        this.startX = startX;
        this.startY = startY;

        this.endX = endX;
        this.endY = endY;

        runwayStartPoint = new Circle(startX, startY, 5);
        runwayStartPoint.setFill(Color.rgb(255, 214, 243));
        runwayStartPoint.setVisible(false); // initially hidden
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

    // Calculates the angle the runway is pointing
    public double getHeading() {
        double[] p1 = {startX, startY};
        double[] p2 = {endX, endY};
        return Util.getHeadingTo(p1, p2);
    }

    // Returns X coordinate 150 pixels behind the runway start
    public double getFafX() {
        double hdgRad = Math.toRadians(getHeading());
        return startX - (Math.cos(hdgRad) * 70.0);
    }

    // Returns Y coordinate 150 pixels behind the runway start
    public double getFafY() {
        double hdgRad = Math.toRadians(getHeading());
        return startY - (Math.sin(hdgRad) * 70.0);
    }
}