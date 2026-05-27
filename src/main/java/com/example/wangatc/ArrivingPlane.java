package com.example.wangatc;

public class ArrivingPlane extends Plane {

    public ArrivingPlane() {
        super("airborne", 0, 0);

        int[] spawnPosition = Util.generateRandomSpawnPoint();
        this.setX(spawnPosition[0]);
        this.setY(spawnPosition[1]);

        // point plane towards center of screen

        // calculate x and y components of direct line to center
        double deltaX = ((double) Util.screenWidth / 2) - this.getX();
        double deltaY = ((double) Util.screenHeight / 2) - this.getY();

        // calculate angle of flight path to center (direction plane must travel in relative to map)
        double initialHeading = Math.toDegrees(Math.atan2(deltaY, deltaX));

        this.setCurrentHeading(initialHeading);
        this.setTargetHeading(initialHeading);

        // assign image & hitbox
        this.setSprite(Util.getArrivingPlaneSprite());

        // set sprite properties
        this.getSprite().setTranslateX(this.getX());
        this.getSprite().setTranslateY(this.getY());
        this.getSprite().setRotate(initialHeading + 90); // add 90 -> sprite drawn facing upwards
    }



}
