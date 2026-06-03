package com.example.wangatc;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.paint.Color;

public class DepartingPlane extends Plane {
    private Waypoint destination;
    private String color;

    private Runway takeoffRunway;

    private int backlogTimer = 0;
    private int maxBacklogTime = 1800; // ~30 seconds at 60 FPS
    private Arc timerVisual;

    public DepartingPlane(int color) {
        super("ground", 0, 0);

        this.setSpeed(0.25);

        // create sprite
        String[] possibleColors = {"blue", "green", "orange", "pink", "red", "yellow"};
        this.color = possibleColors[color];
        this.setSprite(Util.getDepartingPlaneSprite(color));

        // initialize visual timer ring for backlogged planes
        this.timerVisual = new Arc(0, 0, 32, 32, 90, 360);

        this.timerVisual.setType(ArcType.OPEN);
        this.timerVisual.setStroke(Color.RED);
        this.timerVisual.setStrokeWidth(4);
        this.timerVisual.setFill(Color.TRANSPARENT);

        this.timerVisual.setVisible(false);

        ((Pane) this.getSprite()).getChildren().add(this.timerVisual); // add timer visual to sprite group
    }

    public Waypoint getDestination() {
        return destination;
    }

    public void setDestination(Waypoint destination) {
        this.destination = destination;
    }

    public void setTakeoffRunway(Runway takeoffRunway) {
        this.takeoffRunway = takeoffRunway;
    }


    public boolean updateBacklogTimer() {
        this.backlogTimer++;
        this.timerVisual.setVisible(true);

        // calculate remaining ratio (starts at 1.0, drops to 0.0)
        double remainingRatio = 1.0 - ((double) backlogTimer / maxBacklogTime);

        // update visual length of the arc (360 deg * ratio)
        this.timerVisual.setLength(360 * remainingRatio);

        // flash dark red when remaining time < 25%
        if (remainingRatio < 0.25 && backlogTimer % 30 < 15) {
            this.timerVisual.setStroke(Color.DARKRED);
        } else {
            this.timerVisual.setStroke(Color.RED);
        }

        // return true if the timer has run out
        return this.backlogTimer >= maxBacklogTime;
    }

    public void resetBacklogTimer() {
        this.backlogTimer = 0;
        this.timerVisual.setVisible(false);
    }

    public void takeOff() {
        if (this.getState().equals("taking off")) {
            if (Math.hypot(this.getX() - takeoffRunway.getEndX(), this.getY() - takeoffRunway.getEndY()) < 50.0) { // distance to end of runway
                this.setState("climb");
            }

        } else if (this.getState().equals("climb")) {
            if (this.getSpeed() < 0.35) {
                this.setSpeed(this.getSpeed() + 0.001); // increase speed from ground speed to flight speed
            }

            if (this.getMinScale() < this.getScaleFactor()) { // increase sprite size
                this.getSprite().setScaleX(this.getMinScale());
                this.getSprite().setScaleY(this.getMinScale());

                this.setMinScale(this.getMinScale() + 0.0025);

            } else {
                this.setState("airborne"); // takeoff complete
            }
        }
    }

    public void navigateToWaypoint() {
        double targetX = this.destination.getX();
        double targetY = this.destination.getY();

        // continuously update target heading to point towards waypoint
        this.setTargetHeading(Util.getHeadingTo(new double[] {this.getX(), this.getY()}, new double[] {targetX, targetY}));

        // check if waypoint is reached
        if (Math.hypot(this.getX() - targetX, this.getY() - targetY) < 5.0) {
            this.setState("reached waypoint");
        }
    }

    @Override
    public void move() {
        if (this.getState().equals("taking off") || this.getState().equals("climb")) {
            this.takeOff();

        } else if (this.getState().equals("targeting waypoint")) {
            this.navigateToWaypoint();
        }

        super.move();
    }
}
