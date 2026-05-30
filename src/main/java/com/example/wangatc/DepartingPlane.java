package com.example.wangatc;

public class DepartingPlane extends Plane {
    private Waypoint destination;
    private String color;

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
}
