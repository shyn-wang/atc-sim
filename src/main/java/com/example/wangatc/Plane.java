package com.example.wangatc;

import javafx.scene.Node;

public class Plane {
    private double x;
    private double y;

    private String state; // "airborne," "targetingRunway," "landing," "landed"

    private double currentHeading;
    private double targetHeading;
    private double speed;

    private Node sprite;
    private Runway assignedRunway; // Tracks which runway the plane is landing on

    private double turnRate = 0.2;

    public Plane(String state, double x, double y) {
        this.state = state;
        this.x = x;
        this.y = y;

        speed = 0.35;
        this.currentHeading = 0.0;
        this.targetHeading = 0.0;
    }

    // getters & setters
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getCurrentHeading() { return currentHeading; }
    public void setCurrentHeading(double currentHeading) { this.currentHeading = currentHeading; }
    public double getTargetHeading() { return targetHeading; }
    public void setTargetHeading(double targetHeading) { this.targetHeading = targetHeading; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Node getSprite() { return sprite; }
    public void setSprite(Node sprite) { this.sprite = sprite; }

    public Runway getAssignedRunway() { return assignedRunway; }
    public void setAssignedRunway(Runway assignedRunway) { this.assignedRunway = assignedRunway; }

    public void setTurnRate(double turnRate) {
        this.turnRate = turnRate;
    }

    // logic methods
    public void move() {
        // --- PHASE 3: INTERCEPT CURVE ---
        if (state.equals("targetingRunway") && assignedRunway != null) {
            double sx = assignedRunway.getStartX();
            double sy = assignedRunway.getStartY();

            double hdgRad = Math.toRadians(assignedRunway.getHeading());
            double backwardX = -Math.cos(hdgRad);
            double backwardY = -Math.sin(hdgRad);

            // Dot Product to find how far back the plane is along the extended centerline
            double distanceAlongCenterline = ((this.x - sx) * backwardX) + ((this.y - sy) * backwardY);

            // If the plane has crossed the 150px mark, transition to Final Approach!
            if (distanceAlongCenterline <= 70.0) {
                this.state = "finalApproach";

                // Snap perfectly to the Final Approach Fix to kill micro-pixel deviations
//                this.x = assignedRunway.getFafX();
//                this.y = assignedRunway.getFafY();

//                // Lock heading
//                this.currentHeading = assignedRunway.getHeading();
//                this.targetHeading = assignedRunway.getHeading();
            } else {
                // Plane is further out: Continue standard Tractrix curve
                double carrotDistance = distanceAlongCenterline - 90.0;
                double carrotX = sx + (carrotDistance * backwardX);
                double carrotY = sy + (carrotDistance * backwardY);
                this.targetHeading = Util.getHeadingTo(new double[]{this.x, this.y}, new double[]{carrotX, carrotY});
            }
        }
        // --- PHASE 3.5: FINAL APPROACH (STRAIGHT AND LEVEL) ---
        else if (state.equals("finalApproach") && assignedRunway != null) {
            double sx = assignedRunway.getStartX();
            double sy = assignedRunway.getStartY();

            this.targetHeading = Util.getHeadingTo(new double[]{this.x, this.y}, new double[]{sx, sy});

            // Touchdown check
            if (Math.hypot(this.x - sx, this.y - sy) < 1.0) {
                this.state = "landing";
                this.x = sx;
                this.y = sy;
                this.speed = 0.2; // Hit the brakes
            }
        }
        // --- PHASE 4: LANDING ROLL ---
        else if (state.equals("landing") && assignedRunway != null) {
            this.setCurrentHeading(assignedRunway.getHeading());
            this.setTargetHeading(assignedRunway.getHeading());

            if (Math.hypot(this.x - assignedRunway.getEndX(), this.y - assignedRunway.getEndY()) < 5.0) {
                this.state = "landed";
            }
        }

        // Turning logic
        if (this.currentHeading != this.targetHeading) {
            double diff = this.targetHeading - this.currentHeading;

            while (diff < -180) diff += 360;
            while (diff > 180) diff -= 360;

            if (Math.abs(diff) <= turnRate) {
                this.currentHeading = this.targetHeading;
            } else {
                if (diff > 0) this.currentHeading += turnRate;
                else this.currentHeading -= turnRate;
            }
            this.currentHeading = (this.currentHeading + 360) % 360;
        }

        // Move plane physically
        double radians = Math.toRadians(this.currentHeading);
        this.x += this.speed * Math.cos(radians);
        this.y += this.speed * Math.sin(radians);

        // Update sprite on screen
        this.sprite.setTranslateX(this.x);
        this.sprite.setTranslateY(this.y);
        this.sprite.setRotate(this.currentHeading + 90);
    }
}