package com.example.wangatc;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.ArrayList;

public class Game {
    private Pane gameScreen;

    private ArrayList<Plane> allActivePlanes;
    private ArrayList<Plane> allAirbornePlanes; // does not include planes in motion on the runway
    private ArrayList<ArrivingPlane> arrivingPlanes;
    private ArrayList<DepartingPlane> departingPlanes;

    private ArrayList<Plane> takeoffQueue; // includes all planes that must be placed on a runway
    private int maxTakeoffQueueSize;
    private ArrayList<Plane> takeoffQueueBacklog;

    private int score;

    private Plane selectedPlane = null; // tracks mouse selected plane
    private Line headingIndicator;


    private Runway runway01;
    private Runway runway02;
    private boolean runwayOccupied;

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

        // create two runways starting on opposing sides of physical runway
        this.runway01 = new Runway(917, 603, 986, 491); // bottom start
        this.runway02 = new Runway(986, 491, 917, 603); // top start

        // render runway start points
        this.gameScreen.getChildren().add(runway01.getRunwayStartPoint());
        this.gameScreen.getChildren().add(runway02.getRunwayStartPoint());

        // initialize input handlers and heading indicator line
        initializeHeadingIndicator();
        setupGlobalMouseHandlers();
    }

    public boolean isRunwayOccupied() {
        return runwayOccupied;
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
                headingIndicator.setStroke(Color.WHITE); // reset color
                headingIndicator.setEndX(e.getX());
                headingIndicator.setEndY(e.getY());

                selectedPlane.setState("airborne"); // reset to initial state

                if (selectedPlane instanceof ArrivingPlane) { // check if mouse is dragged to close to a runway -> attempt landing
                    ((ArrivingPlane) selectedPlane).setTargetRunway(null); // no target runway initially

                    double snapRadius = 30.0;
                    double[] planePos = {selectedPlane.getX(), selectedPlane.getY()};

                    // check distance to runway 01
                    double dist01 = Math.hypot(e.getX() - runway01.getStartX(), e.getY() - runway01.getStartY());
                    if (dist01 < snapRadius) { // snap endpoint of line to start point of runway
                        headingIndicator.setEndX(runway01.getStartX());
                        headingIndicator.setEndY(runway01.getStartY());

                        // check if landing is possible from current plane position
                        if (runway01.isApproachAngleValid(planePos)) {
                            ((ArrivingPlane) selectedPlane).setTargetRunway(runway01); // queue potential approach
                            headingIndicator.setStroke(Color.LIGHTGREEN);

                        } else {
                            headingIndicator.setStroke(Color.RED);
                        }
                    }

                    // check distance to runway 02
                    double dist02 = Math.hypot(e.getX() - runway02.getStartX(), e.getY() - runway02.getStartY());
                    if (dist02 < snapRadius) {
                        headingIndicator.setEndX(runway02.getStartX());
                        headingIndicator.setEndY(runway02.getStartY());

                        // check if landing is possible from current plane position
                        if (runway02.isApproachAngleValid(planePos)) {
                            ((ArrivingPlane) selectedPlane).setTargetRunway(runway02);
                            headingIndicator.setStroke(Color.LIGHTGREEN);

                        } else {
                            headingIndicator.setStroke(Color.RED);
                        }
                    }
                }
            }
        });

        // action listener for releasing mouse
        this.gameScreen.setOnMouseReleased(e -> {
            if (selectedPlane != null) {
                if (selectedPlane instanceof ArrivingPlane) { // hide runway indicators if releasing an arriving plane
                    runway01.setRunwayStartPointVisible(false);
                    runway02.setRunwayStartPointVisible(false);

                    if (((ArrivingPlane) selectedPlane).getTargetRunway() != null) { // valid approach has been queued -> begin approach
                        selectedPlane.setState("targeting runway");
                        selectedPlane.setTurnRate(0.23); // increase turn rate for approach and landing
                    }
                }

                if (selectedPlane.getState().equals("airborne")) { // targeting angle given by cursor instead of runway/waypoint
                    selectedPlane.setTurnRate(0.20); // reset to default (in case approach is cancelled)

                    // update targetHeading based on mouse position at point of release

                    // calculate angle between center of plane & mouse endpoint
                    double[] p1 = {selectedPlane.getX(), selectedPlane.getY()};
                    double[] p2 = {e.getX(), e.getY()};
                    double targetAngle = Util.getHeadingTo(p1, p2);

                    // set target heading to angle
                    selectedPlane.setTargetHeading(targetAngle);
                }

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
            if (!plane.getState().equals("landing")) {
                selectedPlane = plane;
                runway01.setRunwayStartPointVisible(true); // display available runways for landing whenever an arriving plane is selected
                runway02.setRunwayStartPointVisible(true);
            }

            e.consume(); // delete click event -> layers below plane unaffected
        });

        this.gameScreen.getChildren().add(plane.getSprite()); // add sprite to scene
    }

    public void manageAllAirbornePlanes() {
        ArrayList<Plane> planesToRemove = new ArrayList<>();

        if (selectedPlane != null) { // continuously update starting point of heading indicator to position of plane if selected
            headingIndicator.setStartX(selectedPlane.getX());
            headingIndicator.setStartY(selectedPlane.getY());
        }

        for (Plane plane : allAirbornePlanes) {
            if (plane instanceof ArrivingPlane && !plane.getState().equals("airborne")) {
                ((ArrivingPlane) plane).approachAndLand();

                if (plane.getState().equals("landing")) {
                    runwayOccupied = true;

                } else if (plane.getState().equals("landed")) {
                    planesToRemove.add(plane);
                    runwayOccupied = false;
                }
            }

            plane.move();
        }

        // remove any planes that have reached their objective
        for (Plane plane : planesToRemove) {
            allAirbornePlanes.remove(plane);
            allActivePlanes.remove(plane);

            if (plane instanceof ArrivingPlane) { // plane has landed
                arrivingPlanes.remove(plane);

                score++;
                System.out.println("Plane landed successfully! Score: " + score);
            }

            gameScreen.getChildren().remove(plane.getSprite());
        }
    }
}
