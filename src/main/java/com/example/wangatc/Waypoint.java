/*
description: waypoint class
@author: david wang
@date: jun. 5. 26
@version: 1.0
*/

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

    /*
    description: constructor for Waypoint objects
    pre-condition: valid arguments
    post-condition: initializes instance variables
    */
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


    // getters & setters


    public void setColor(int color) {
        String[] colors = {
                "#30ffff", // blue
                "#9ED860", // green
                "#E1742B", // orange
                "#EAA6DA", // pink
                "#DC2C2C", // red
                "#F6DE64" // yellow
        };

        this.sprite.setFill(Color.web(colors[color])); // map int color onto corresponding hex code
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

    /*
    description: determines if a waypoint is reachable within one turn given an aircraft's current position & heading
    pre-condition: plane is a valid Plane
    post-condition: returns true or false
    */
    public boolean isReachable(Plane plane) {
        double wpX = this.getX();
        double wpY = this.getY();

        // calculate minimum turning radius of plane based on speed & turn rate
        double radius = (180.0 * plane.getSpeed()) / (Math.PI * plane.getTurnRate());

        // determine whether plane must turn left or right to face waypoint

        double headingToWp = Util.getHeadingTo(new double[] {plane.getX(), plane.getY()}, new double[] {wpX, wpY}); // angle between plane & waypoint
        double diff = headingToWp - plane.getCurrentHeading(); // difference in the required angle to reach the waypoint & the plane's current heading

        while (diff < -180) { // normalize difference -> -180 to 180
            diff += 360;
        }
        while (diff > 180) {
            diff -= 360;
        }

        // find center coordinates of the turning circle (to left or right of plane based on which way plane should turn)
        double radians = Math.toRadians(plane.getCurrentHeading());
        double centerX, centerY;

        if (diff >= 0) { // turn right -> clockwise
            centerX = plane.getX() - (radius * Math.sin(radians)); // find center of turning radius circle to right of plane -> always perpendicular to plane direction at a distance of 1 radius
            centerY = plane.getY() + (radius * Math.cos(radians));

        } else { // turn left -> counter-clockwise
            centerX = plane.getX() + radius * Math.sin(radians); // find center of turning radius circle to left of plane
            centerY = plane.getY() - radius * Math.cos(radians);
        }

        // check if waypoint is inside the turning circle radius -> plane is unable to reach waypoint in a single turn if true
        double distanceToCenter = Math.hypot(wpX - centerX, wpY - centerY);

        // waypoint is outside of the plane's turning radius when its distance to the center of the turning circle is greater than the radius
        return distanceToCenter >= radius;
    }
}
