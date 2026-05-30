package com.example.wangatc;

public class DepartingPlane extends Plane {
    private Waypoint destination;
    private String color;

    private double minScale = 0.7;
    private double scaleFactor = 1;

    public DepartingPlane(int color) {
        super("ground", 0, 0);

        // create sprite
        String[] possibleColors = {"blue", "green", "orange", "pink", "red", "yellow"};
        this.color = possibleColors[color];
        this.setSprite(Util.getDepartingPlaneSprite(color));
    }

    public double getMinScale() {
        return minScale;
    }
}
