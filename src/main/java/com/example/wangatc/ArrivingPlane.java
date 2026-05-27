package com.example.wangatc;

public class ArrivingPlane extends Plane {

    public ArrivingPlane() {
        super("airborne", 0, 0);

        int[] spawnPosition = Util.generateRandomSpawnPoint();
        this.setX(spawnPosition[0]);
        this.setY(spawnPosition[1]);

        // point plane towards center of screen
        double[] p1 = {this.getX(), this.getY()};
        double[] p2 = {(double) Util.screenWidth / 2, (double) Util.screenHeight / 2};

        double initialHeading = Util.getHeadingTo(p1, p2);

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
