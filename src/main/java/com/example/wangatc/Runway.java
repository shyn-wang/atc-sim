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
}
