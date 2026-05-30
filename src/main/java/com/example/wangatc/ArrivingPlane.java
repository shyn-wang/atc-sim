package com.example.wangatc;

public class ArrivingPlane extends Plane {
    private Runway targetRunway;

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

    public void setTargetRunway(Runway runway) {
        this.targetRunway = runway;
    }

    public Runway getTargetRunway() {
        return this.targetRunway;
    }

    public void approachAndLand() {
        // phase 1 -> intercept runway centerline by tracing a tractrix curve
        if (this.getState().equals("targeting runway")) {
            double sx = targetRunway.getStartX();
            double sy = targetRunway.getStartY();

            double hdgRad = Math.toRadians(targetRunway.getHeading());
            double backwardX = -Math.cos(hdgRad);
            double backwardY = -Math.sin(hdgRad);

            // dot product to find how far back the plane is along the extended centerline
            double distanceAlongCenterline = ((this.getX() - sx) * backwardX) + ((this.getY() - sy) * backwardY);

            // if the plane has crossed the 70 px mark, transition to final approach
            if (distanceAlongCenterline <= 70.0) {
                this.setState("final approach");

            } else {
                // plane is further out -> continue standard tractrix curve & follow "carrot"
                double carrotDistance = distanceAlongCenterline - 90.0; // carrot is constantly positioned 90px down the centerline from the plane's current position
                double carrotX = sx + (carrotDistance * backwardX);
                double carrotY = sy + (carrotDistance * backwardY);

                this.setTargetHeading(Util.getHeadingTo(new double[] {this.getX(), this.getY()}, new double[] {carrotX, carrotY})); // continuously target carrot
            }
        }
        // phase 2 -> final approach
        else if (this.getState().equals("final approach")) {
            double sx = targetRunway.getStartX();
            double sy = targetRunway.getStartY();

            if (this.getScaleFactor() > this.getMinScale()) {
                this.getSprite().setScaleX(this.getScaleFactor());
                this.getSprite().setScaleY(this.getScaleFactor());

                this.setScaleFactor(this.getScaleFactor() - 0.005);
            }

            this.setTargetHeading(Util.getHeadingTo(new double[] {this.getX(), this.getY()}, new double[] {sx, sy})); // target runway start

            // touchdown check
            if (Math.hypot(this.getX() - sx, this.getY() - sy) < 1.0) { // distance to runway start < 1 px
                this.setState("landing");

                this.setX(sx);
                this.setY(sy);
                this.setSpeed(0.2); // apply brakes
            }
        }
        // phase 3 -> landing roll
        else if (this.getState().equals("landing")) {
            this.setCurrentHeading(targetRunway.getHeading());
            this.setTargetHeading(targetRunway.getHeading());

            if (Math.hypot(this.getX() - targetRunway.getEndX(), this.getY() - targetRunway.getEndY()) < 30.0) { // distance to end
                this.setState("landed");
            }
        }
    }

    @Override
    public void move() {
        if (!this.getState().equals("airborne")) {
            this.approachAndLand();
        }

        super.move();
    }
}
