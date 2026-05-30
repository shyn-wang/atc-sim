package com.example.wangatc;

public class DepartingPlane extends Plane {
    private Waypoint destination;
    private String color;

    private Runway takeoffRunway;

    public DepartingPlane(int color) {
        super("ground", 0, 0);

        // create sprite
        String[] possibleColors = {"blue", "green", "orange", "pink", "red", "yellow"};
        this.color = possibleColors[color];
        this.setSprite(Util.getDepartingPlaneSprite(color));
    }

    public void setDestination(Waypoint destination) {
        this.destination = destination;
    }

    public void setTakeoffRunway(Runway takeoffRunway) {
        this.takeoffRunway = takeoffRunway;
    }

    public void takeOff() {
        if (this.getState().equals("taking off")) {
            if (Math.hypot(this.getX() - takeoffRunway.getEndX(), this.getY() - takeoffRunway.getEndY()) < 50.0) { // distance to end of runway
                this.setState("climb");
            }

        } else if (this.getState().equals("climb")) { // increase sprite size
            if (this.getMinScale() < this.getScaleFactor()) {
                this.getSprite().setScaleX(this.getMinScale());
                this.getSprite().setScaleY(this.getMinScale());

                this.setMinScale(this.getMinScale() + 0.0025);

            } else {
                this.setState("airborne"); // takeoff complete
            }
        }
    }

    @Override
    public void move() {
        if (this.getState().equals("taking off") || this.getState().equals("climb")) {
            this.takeOff();
        }

        super.move();
    }
}
