package com.example.wangatc;

public class Plane {
    private double x;
    private double y;

    private String state; // "airborne," "targetingMouse," "targetingWaypoint," "targetingRunway," "landing," "takingOff," "inTakeoffQueue"

    private double currentHeading;
    private double targetHeading;

    private int speed;

    private boolean mouseSelected;

    public Plane(String state, double x, double y) {
        this.state = state;
        this.x = x;
        this.y = y;

        speed = 3;
        mouseSelected = false;

        // initialize headings to 0
        this.currentHeading = 0.0;
        this.targetHeading = 0.0;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }


    public double getCurrentHeading() {
        return currentHeading;
    }

    public void setCurrentHeading(double currentHeading) {
        this.currentHeading = currentHeading;
    }


    public double getTargetHeading() {
        return targetHeading;
    }

    public void setTargetHeading(double targetHeading) {
        this.targetHeading = targetHeading;
    }


    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
