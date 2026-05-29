package com.example.wangatc;

import javafx.scene.Node;

public class Plane {
    private double x;
    private double y;

    private String state; // "airborne," "targetingMouse," "targetingWaypoint," "targetingRunway," "landing," "takingOff," "inTakeoffQueue"

    private double currentHeading;
    private double targetHeading;

    private double speed;
    double turnRate = 0.2; // turn rate per frame

    private Node sprite;

    public Plane(String state, double x, double y) {
        this.state = state;
        this.x = x;
        this.y = y;

        speed = 0.35;

        // initialize headings to 0
        this.currentHeading = 0.0;
        this.targetHeading = 0.0;
    }

    // getters & setters

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


    public Node getSprite() {
        return sprite;
    }

    public void setSprite(Node sprite) {
        this.sprite = sprite;
    }

    public double getTurnRate() {
        return turnRate;
    }

    public void setTurnRate(double turnRate) {
        this.turnRate = turnRate;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    // logic methods

    public void move() {
        if (this.currentHeading != this.targetHeading) { // gradually turn plane towards target direction
            // find smallest angle between current heading & target heading (which direction to turn in) -> normalization
            double diff = this.targetHeading - this.currentHeading;

            while (diff < -180) {
                diff += 360;
            }
            while (diff > 180) {
                diff -= 360;
            }

            // check if difference is small enough to snap to target heading
            if (Math.abs(diff) <= turnRate) {
                this.currentHeading = this.targetHeading;

            } else {
                // turn in direction of the shortest path based on diff
                if (diff > 0) {
                    this.currentHeading += turnRate; // positive -> clockwise
                } else {
                    this.currentHeading -= turnRate; // negative -> counter-clockwise
                }
            }

            // lock heading between 0-359
            this.currentHeading = (this.currentHeading + 360) % 360;
        }


        // move plane
        double radians = Math.toRadians(this.currentHeading);

        // move based on resultant velocity components -> always fly in direction of currentHeading
        this.x += this.speed * Math.cos(radians);
        this.y += this.speed * Math.sin(radians);

        // update sprite on screen
        this.sprite.setTranslateX(this.x);
        this.sprite.setTranslateY(this.y);
        this.sprite.setRotate(this.currentHeading + 90);
    }
}
