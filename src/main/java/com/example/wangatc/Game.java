package com.example.wangatc;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.ArrayList;

public class Game {
    private Pane gameScreen;

    private ArrayList<Plane> allActivePlanes;
    private ArrayList<Plane> allAirbornePlanes;
    private ArrayList<ArrivingPlane> arrivingPlanes;
    private ArrayList<DepartingPlane> departingPlanes;

    private ArrayList<Plane> takeoffQueue;
    private int maxTakeoffQueueSize;
    private ArrayList<Plane> takeoffQueueBacklog;

    private int score;

    private Plane selectedPlane = null;
    private Runway hoveredRunway = null; // Tracks which runway the mouse is currently snapping to
    private Line headingIndicator;

    private Runway runway01;
    private Runway runway02;

    public Game(Pane gameScreen) {
        this.gameScreen = gameScreen;

        this.allActivePlanes = new ArrayList<>();
        this.allAirbornePlanes = new ArrayList<>();
        this.arrivingPlanes = new ArrayList<>();
        this.departingPlanes = new ArrayList<>();

        this.takeoffQueue = new ArrayList<>();
        this.maxTakeoffQueueSize = 3;
        this.takeoffQueueBacklog = new ArrayList<>();

        this.score = 0;

        this.runway01 = new Runway(917, 603, 986, 491); // bottom start
        this.runway02 = new Runway(986, 491, 917, 603); // top start

        this.gameScreen.getChildren().add(runway01.getRunwayStartPoint());
        this.gameScreen.getChildren().add(runway02.getRunwayStartPoint());

        initializeHeadingIndicator();
        setupGlobalMouseHandlers();
    }

    private void initializeHeadingIndicator() {
        headingIndicator = new Line();
        headingIndicator.setStroke(Color.WHITE);
        headingIndicator.setStrokeWidth(2.0);
        headingIndicator.getStrokeDashArray().addAll(10d, 5d);
        headingIndicator.setVisible(false);
        this.gameScreen.getChildren().add(headingIndicator);
    }

    private void setupGlobalMouseHandlers() {
        // --- PHASE 1: DRAG & VALIDATE ---
        this.gameScreen.setOnMouseDragged(e -> {
            if (selectedPlane != null) {
                headingIndicator.setVisible(true);
                headingIndicator.setEndX(e.getX());
                headingIndicator.setEndY(e.getY());
                hoveredRunway = null; // Reset every frame

                if (selectedPlane instanceof ArrivingPlane) {
                    double snapRadius = 30.0;
                    double[] planePos = {selectedPlane.getX(), selectedPlane.getY()};

                    double dist01 = Math.hypot(e.getX() - runway01.getStartX(), e.getY() - runway01.getStartY());
                    if (dist01 < snapRadius && isApproachAngleValid(planePos, runway01)) {
                        selectedPlane.setTurnRate(0.23); // increase turn rate for approach and landing

                        headingIndicator.setEndX(runway01.getStartX());
                        headingIndicator.setEndY(runway01.getStartY());
                        hoveredRunway = runway01;
                    }

                    double dist02 = Math.hypot(e.getX() - runway02.getStartX(), e.getY() - runway02.getStartY());
                    if (dist02 < snapRadius && isApproachAngleValid(planePos, runway02)) {
                        selectedPlane.setTurnRate(0.23); // increase turn rate for approach and landing

                        headingIndicator.setEndX(runway02.getStartX());
                        headingIndicator.setEndY(runway02.getStartY());
                        hoveredRunway = runway02;
                    }
                }
            }
        });

        // --- PHASE 2: LOCK APPROACH ---
        this.gameScreen.setOnMouseReleased(e -> {
            if (selectedPlane != null) {
                if (selectedPlane instanceof ArrivingPlane) {
                    runway01.setRunwayStartPointVisible(false);
                    runway02.setRunwayStartPointVisible(false);
                }

                if (hoveredRunway != null) {
                    selectedPlane.setState("targetingRunway");
                    selectedPlane.setAssignedRunway(hoveredRunway);
                } else {
                    selectedPlane.setState("airborne");
                    selectedPlane.setAssignedRunway(null);

                    double[] p1 = {selectedPlane.getX(), selectedPlane.getY()};
                    double[] p2 = {e.getX(), e.getY()};
                    selectedPlane.setTargetHeading(Util.getHeadingTo(p1, p2));
                }

                selectedPlane = null;
                hoveredRunway = null;
                headingIndicator.setVisible(false);
            }
        });
    }

    // Validates if the plane has enough room to finish its turn BEFORE the 150px Final Approach mark
    private boolean isApproachAngleValid(double[] planePos, Runway runway) {
        double fafX = runway.getFafX();
        double fafY = runway.getFafY();

        // Find angle to the FAF (not the runway threshold)
        double angleToFAF = Util.getHeadingTo(planePos, new double[]{fafX, fafY});
        double runwayHeading = runway.getHeading();

        // Calculate diff
        double diff = Math.abs(angleToFAF - runwayHeading);
        while (diff > 180) diff -= 360;
        diff = Math.abs(diff);

        // Calculate distance to the FAF
        double distanceToFAF = Math.hypot(planePos[0] - fafX, planePos[1] - fafY);

        if (distanceToFAF < 250) {
            // Very close to the FAF mark: Plane must already be perfectly aligned
            return diff < 10;
        }
        else if (distanceToFAF < 400) {
            // Moderate distance from the FAF
            return diff < 25;

        } else if (distanceToFAF < 500) {
            return diff < 35;

        } else if (distanceToFAF < 600) {
            return diff < 50;

        } else if (distanceToFAF < 700) {
            return diff < 60;

        } else {
            // Plenty of room: can approach from wide angles
            return diff < 70;
        }
    }

    public void createNewArrivingPlane() {
        ArrivingPlane plane = new ArrivingPlane();

        this.allActivePlanes.add(plane);
        this.allAirbornePlanes.add(plane);
        this.arrivingPlanes.add(plane);

        plane.getSprite().setOnMousePressed(e -> {
            selectedPlane = plane;
            runway01.setRunwayStartPointVisible(true);
            runway02.setRunwayStartPointVisible(true);
            e.consume();
        });

        this.gameScreen.getChildren().add(plane.getSprite());
    }

    // --- PHASE 6: DESPAWN & SCORE ---
    public void manageAllAirbornePlanes() {
        if (selectedPlane != null) {
            headingIndicator.setStartX(selectedPlane.getX());
            headingIndicator.setStartY(selectedPlane.getY());
        }

        ArrayList<Plane> planesToRemove = new ArrayList<>();

        for (Plane plane : allAirbornePlanes) {
            plane.move();

            if (plane.getState().equals("landed")) {
                planesToRemove.add(plane);
            }
        }

        // Safely wipe landed planes from memory and screen
        for (Plane landedPlane : planesToRemove) {
            allAirbornePlanes.remove(landedPlane);
            allActivePlanes.remove(landedPlane);
            if (landedPlane instanceof ArrivingPlane) {
                arrivingPlanes.remove(landedPlane);
            }

            gameScreen.getChildren().remove(landedPlane.getSprite());

            score++;
            System.out.println("Plane landed successfully! Score: " + score);
        }
    }
}