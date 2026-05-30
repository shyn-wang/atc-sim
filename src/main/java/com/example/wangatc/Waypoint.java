package com.example.wangatc;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;

public class Waypoint {
    private double x;
    private double y;
    private ArrayList<Waypoint> existingWaypoints;

    private int color;

    private Circle sprite;

    public Waypoint(int color, ArrayList<Waypoint> existingWaypoints) {
        this.color = color;
        this.existingWaypoints = existingWaypoints;

        double[] position = Util.generateRandomWaypointLocation(existingWaypoints, color); // set position
        this.setX(position[0]);
        this.setY(position[1]);

        existingWaypoints.add(this); // add waypoint to master waypoint list

        this.sprite = new Circle(0, 0, 10); // create sprite
        setColor(this.color);

        this.sprite.setTranslateX(this.x); // place sprite in position
        this.sprite.setTranslateY(this.y);
    }

    public void setColor(int color) {
        String[] colors = {
                "#30ffff", // blue
                "#9ED860", // green
                "#E1742B", // orange
                "#EAA6DA", // pink
                "#DC2C2C", // red
                "#F6DE64" // yellow
        };

        this.sprite.setFill(Color.web(colors[color]));
    }

    public int getColor() {
        return color;
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

    public Circle getSprite() {
        return sprite;
    }
}
