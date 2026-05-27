package com.example.wangatc;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.ArrayList;

public class Game {
    private Pane gameScreen;

    private ArrayList<Plane> allActivePlanes;
    private ArrayList<Plane> allAirbornePlanes; // does not include planes in motion on the runway
    private ArrayList<Plane> arrivingPlanes;
    private ArrayList<Plane> departingPlanes;

    private ArrayList<Plane> takeoffQueue; // includes all planes that must be placed on a runway
    private int maxTakeoffQueueSize;
    private ArrayList<Plane> takeoffQueueBacklog;

    private int score;

    private Plane selectedPlane = null; // tracks mouse selected plane
    private Line headingIndicator;

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

        // initialize input handlers and heading indicator line
        initializeHeadingIndicator();
        setupGlobalMouseHandlers();
    }

    private void initializeHeadingIndicator() {
        headingIndicator = new Line();
        headingIndicator.setStroke(Color.WHITE);
        headingIndicator.setStrokeWidth(2.0);
        headingIndicator.getStrokeDashArray().addAll(10d, 5d); // dashed line
        headingIndicator.setVisible(false); // initially invisible

        this.gameScreen.getChildren().add(headingIndicator); // add to scene
    }

    private void setupGlobalMouseHandlers() {
        // action listener for dragging mouse across screen
        this.gameScreen.setOnMouseDragged(e -> {
            if (selectedPlane != null) {
                // display heading indicator once mouse is dragged & draw endpoint at mouse cursor
                headingIndicator.setVisible(true);
                headingIndicator.setEndX(e.getX());
                headingIndicator.setEndY(e.getY());

                // calculate displacement components from center of plane to mouse endpoint
                double deltaX = e.getX() - selectedPlane.getX();
                double deltaY = e.getY() - selectedPlane.getY();

                // calculate angle between plane & mouse
                double targetAngle = Math.toDegrees(Math.atan2(deltaY, deltaX));

                // normalize angle to between 0-359
                targetAngle = (targetAngle + 360) % 360;

                // set target heading to angle
                selectedPlane.setTargetHeading(targetAngle);
            }
        });

        // action listener for releasing mouse
        this.gameScreen.setOnMouseReleased(e -> {
            if (selectedPlane != null) { // reset properties -> stop tracking mouse movement
                selectedPlane = null;
                headingIndicator.setVisible(false);
            }
        });
    }


    public void createNewArrivingPlane() {
        ArrivingPlane plane = new ArrivingPlane();

        this.allActivePlanes.add(plane);
        this.allAirbornePlanes.add(plane);
        this.arrivingPlanes.add(plane);

        // create separate mouse pressed action listener for each plane -> no need to loop through all planes
        plane.getSprite().setOnMousePressed(e -> {
            selectedPlane = plane;
            e.consume(); // delete click event -> layers below plane unaffected
        });

        this.gameScreen.getChildren().add(plane.getSprite()); // add sprite to scene
    }

    public void moveAllAirbornePlanes() {
        if (selectedPlane != null) { // continuously update starting point of heading indicator to position of plane if selected
            headingIndicator.setStartX(selectedPlane.getX());
            headingIndicator.setStartY(selectedPlane.getY());
        }

        for (Plane plane : allAirbornePlanes) {
            plane.move();
        }
    }
}
