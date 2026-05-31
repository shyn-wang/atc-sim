package com.example.wangatc;

public class DepartingPlane extends Plane {
    private Waypoint destination;
    private String color;

    private Runway takeoffRunway;

    public DepartingPlane(int color) {
        super("ground", 0, 0);

        this.setSpeed(0.25);

        // create sprite
        String[] possibleColors = {"blue", "green", "orange", "pink", "red", "yellow"};
        this.color = possibleColors[color];
        this.setSprite(Util.getDepartingPlaneSprite(color));
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

    @Override
    public void move() {
        if (this.getState().equals("taking off") || this.getState().equals("climb")) {
            this.takeOff();
        }

        super.move();
    }
}
