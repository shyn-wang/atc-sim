/*
description: game class
@author: david wang
@date: jun. 5. 26
@version: 1.0
*/

package com.example.wangatc;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.ArrayList;

public class Game {
    private Pane gameScreen;

    private ArrayList<Plane> allActivePlanes;
    private ArrayList<ArrivingPlane> arrivingPlanes;
    private ArrayList<DepartingPlane> departingPlanes;

    private ArrayList<DepartingPlane> takeoffQueue; // includes all planes that must be placed on a runway
    private int maxTakeoffQueueSize;
    private ArrayList<DepartingPlane> takeoffQueueBacklog; // max size -> 2

    private IntegerProperty score; // use javafx property wrapper to manage score
    private Label scoreLabel;

    private Plane selectedPlane = null; // tracks mouse selected plane
    private Line headingIndicator;

    private Runway runway01;
    private Runway runway02;
    private boolean runwayOccupied;

    private HBox takeoffQueueHotbar;

    private ArrayList<Waypoint> waypoints;

    private String reasonForLoss;

    private int framesSinceLastSpawn = 0;
    private int currentSpawnInterval = 800;
    private int minSpawnInterval = 120; // max difficulty -> 1 plane every 2 seconds
    private int difficultyScaling = 3;


    private Runnable onGameOverCallback;


    /*
    description:
    pre-condition:
    post-condition:
    */
    public Game(Pane gameScreen, Runnable onGameOverCallback) {
        this.gameScreen = gameScreen;
        this.onGameOverCallback = onGameOverCallback;

        this.allActivePlanes = new ArrayList<>();
        this.arrivingPlanes = new ArrayList<>();
        this.departingPlanes = new ArrayList<>();

        this.takeoffQueue = new ArrayList<>();
        this.maxTakeoffQueueSize = 3;
        this.takeoffQueueBacklog = new ArrayList<>();

        this.waypoints = new ArrayList<>();

        // configure score label
        this.score = new SimpleIntegerProperty(0);
        this.scoreLabel = new Label();
        scoreLabel.textProperty().bind(score.asString("score: %d")); // set up automatic updating -> bind score variable// to label

        scoreLabel.setLayoutX(Util.screenWidth - 150);
        scoreLabel.setLayoutY(30);

        scoreLabel.getStyleClass().add("scoreLabel");
        this.gameScreen.getChildren().add(scoreLabel); // add label to scene

        // create two runways starting on opposing sides of physical runway
        this.runway01 = new Runway(917, 603, 986, 491); // bottom start
        this.runway02 = new Runway(986, 491, 917, 603); // top start

        // render runway start points
        this.gameScreen.getChildren().add(runway01.getRunwayStartPoint());
        this.gameScreen.getChildren().add(runway02.getRunwayStartPoint());

        this.runwayOccupied = false;

        // initialize heading indicator line
        this.headingIndicator = Util.initializeHeadingIndicator();
        this.gameScreen.getChildren().add(headingIndicator); // add to scene

        // initialize hotbar
        this.takeoffQueueHotbar = Util.initializeHotbar();
        this.gameScreen.getChildren().add(takeoffQueueHotbar);
        Util.updateTakeoffHotbarUI(takeoffQueueHotbar, maxTakeoffQueueSize, takeoffQueue, takeoffQueueBacklog); // render empty slots

        // initialize global mouse listeners
        setupGlobalMouseHandlers();
    }

    // getters & setters
    public boolean isRunwayOccupied() {
        return runwayOccupied;
    }

    public int getScore() {
        return score.get();
    }

    public String getReasonForLoss() {
        return reasonForLoss;
    }

    /*
    description:
    pre-condition:
    post-condition:
    */
    private void setupGlobalMouseHandlers() {
        // global action listener for dragging mouse across screen
        this.gameScreen.setOnMouseDragged(e -> {
            if (selectedPlane != null) {
                if (!selectedPlane.getState().equals("dragging from takeoff queue")) { // not in takeoff queue -> dragging indicates navigation
                    // display heading indicator once mouse is dragged & draw endpoint at mouse cursor
                    headingIndicator.setVisible(true);
                    headingIndicator.setStroke(Color.WHITE); // reset color
                    headingIndicator.setEndX(e.getX());
                    headingIndicator.setEndY(e.getY());

                    selectedPlane.setState("airborne"); // reset to initial state
                }

                if (selectedPlane instanceof DepartingPlane) {
                    if (selectedPlane.getState().equals("dragging from takeoff queue")) {
                        selectedPlane.setX(e.getX());
                        selectedPlane.setY(e.getY());
                        selectedPlane.getSprite().setTranslateX(e.getX());
                        selectedPlane.getSprite().setTranslateY(e.getY());

                    } else if (selectedPlane.getState().equals("airborne")) { // check if mouse is dragged to corresponding waypoint
                        double snapRadius = 40.0;
                        Waypoint destination = ((DepartingPlane) selectedPlane).getDestination();

                        if (Math.hypot(e.getX() - destination.getX(), e.getY() - destination.getY()) < snapRadius) { // snap mouse to waypoint when dragged close enough
                            headingIndicator.setEndX(destination.getX());
                            headingIndicator.setEndY(destination.getY());

                            // check if approach is possible from current position
                            if (destination.isReachable(selectedPlane)) {
                                headingIndicator.setStroke(Color.LIGHTGREEN);
                                selectedPlane.setState("locked on waypoint"); // queue navigation to waypoint

                            } else {
                                headingIndicator.setStroke(Color.RED);
                            }
                        }
                    }

                } else if (selectedPlane instanceof ArrivingPlane) { // check if mouse is dragged to close to a runway -> attempt landing
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

        // global action listener for releasing mouse
        this.gameScreen.setOnMouseReleased(e -> {
            if (selectedPlane != null && !selectedPlane.getState().equals("dragging from takeoff queue")) { // planes in takeoff queue have a separate mouse release listener
                if (selectedPlane instanceof ArrivingPlane) { // hide runway indicators if releasing an arriving plane
                    runway01.setRunwayStartPointVisible(false);
                    runway02.setRunwayStartPointVisible(false);

                    if (((ArrivingPlane) selectedPlane).getTargetRunway() != null) { // valid approach has been queued -> begin approach
                        selectedPlane.setState("targeting runway");
                        selectedPlane.setTurnRate(0.23); // increase turn rate for approach and landing
                    }

                } else if (selectedPlane instanceof DepartingPlane) {
                    if (selectedPlane.getState().equals("locked on waypoint")) {
                        selectedPlane.setState("targeting waypoint"); // begin autotracking to waypoint
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

    /*
    description:
    pre-condition:
    post-condition:
    */
    public void createNewArrivingPlane() {
        ArrivingPlane plane = new ArrivingPlane();

        this.allActivePlanes.add(plane);
        this.arrivingPlanes.add(plane);

        // create separate mouse pressed action listener for each plane -> no need to loop through all planes
        plane.getSprite().setOnMousePressed(e -> {
            if (!plane.getState().equals("final approach") && !plane.getState().equals("landing")) { // planes on final approach or landing cannot be interacted with
                selectedPlane = plane;
                runway01.setRunwayStartPointVisible(true); // display available runways for landing whenever an arriving plane is selected
                runway02.setRunwayStartPointVisible(true);
            }

            e.consume(); // delete click event -> layers below plane unaffected
        });

        this.gameScreen.getChildren().add(plane.getSprite()); // add sprite to scene
    }

    /*
    description:
    pre-condition:
    post-condition:
    */
    public void createNewDepartingPlane() {
        // create plane
        int whichColor = (int) (Math.random() * (6)); // random 0-5

        DepartingPlane plane = new DepartingPlane(whichColor);

        if (this.takeoffQueue.size() < this.maxTakeoffQueueSize) {
            this.takeoffQueue.add(plane);
        } else {
            this.takeoffQueueBacklog.add(plane);
        }

        Util.updateTakeoffHotbarUI(takeoffQueueHotbar, maxTakeoffQueueSize, takeoffQueue, takeoffQueueBacklog);

        // create corresponding waypoint
        Waypoint waypoint = new Waypoint(whichColor, waypoints);

        plane.setDestination(waypoint);
        this.gameScreen.getChildren().add(waypoint.getSprite()); // add to scene


        // mouse listener for clicking departing planes
        plane.getSprite().setOnMousePressed(e -> {
            // in takeoff queue -> pick up from hotbar
            if (takeoffQueue.contains(plane)) {
                selectedPlane = plane;
                selectedPlane.setState("dragging from takeoff queue");

                // visually remove plane from hotbar slot and add to the main game screen
                if (plane.getSprite().getParent() instanceof StackPane) {
                    ((StackPane) plane.getSprite().getParent()).getChildren().remove(plane.getSprite());
                }

                if (!gameScreen.getChildren().contains(plane.getSprite())) {
                    gameScreen.getChildren().add(plane.getSprite()); // add plane to game scene
                }

                plane.getSprite().setLayoutX(0); // reset positions relative to new parent (gamescreen instead of hotbar) -> keeps plane aligned with cursor
                plane.getSprite().setLayoutY(0);

                // pre-position at cursor -> no visual jump before dragging
                javafx.geometry.Point2D localCoords = gameScreen.sceneToLocal(e.getSceneX(), e.getSceneY()); // convert mouse position on screen to position in scene -> required since mouse listener is applied to plane sprite instead of screen
                plane.setX(localCoords.getX());
                plane.setY(localCoords.getY());
                plane.getSprite().setTranslateX(localCoords.getX());
                plane.getSprite().setTranslateY(localCoords.getY());

                // show runway targets
                runway01.setRunwayStartPointVisible(true);
                runway02.setRunwayStartPointVisible(true);

            } else if (!plane.getState().equals("taking off") && !plane.getState().equals("climb") && !plane.getState().equals("ground")) { // enable user guided navigation
                selectedPlane = plane;
            }

            e.consume();
        });

        // mouse released listener
        plane.getSprite().setOnMouseReleased(e -> {
            if (selectedPlane == plane && plane.getState().equals("dragging from takeoff queue")) { // only runs when dropping planes previously in hotbar
                runway01.setRunwayStartPointVisible(false);
                runway02.setRunwayStartPointVisible(false);

                double snapRadius = 30.0;
                Runway target = null;

                // check distance to runway 01 and 02
                if (Math.hypot(plane.getX() - runway01.getStartX(), plane.getY() - runway01.getStartY()) < snapRadius) {
                    target = runway01;

                } else if (Math.hypot(plane.getX() - runway02.getStartX(), plane.getY() - runway02.getStartY()) < snapRadius) {
                    target = runway02;
                }

                // if dropped on a valid runway and the runway isn't currently occupied
                if (target != null && !isRunwayOccupied()) {
                    plane.setTakeoffRunway(target);

                    // snap to runway
                    plane.setX(target.getStartX());
                    plane.setY(target.getStartY());
                    plane.setCurrentHeading(target.getHeading());
                    plane.setTargetHeading(target.getHeading());

                    plane.getSprite().setScaleX(plane.getMinScale()); // min scale on ground
                    plane.getSprite().setScaleY(plane.getMinScale());

                    plane.setState("taking off"); // trigger take off logic

                    // add to active game loop
                    allActivePlanes.add(plane);
                    departingPlanes.add(plane);

                    // update takeoff queue
                    takeoffQueue.remove(plane);

                    if (!takeoffQueueBacklog.isEmpty()) {
                        DepartingPlane promotedPlane = takeoffQueueBacklog.remove(0);
                        promotedPlane.resetBacklogTimer(); // hide & stop timer for planes out of the backlog

                        takeoffQueue.add(promotedPlane);
                    }

                } else {
                    // not dropped on runway -> return to hotbar
                    plane.setState("ground");
                }

                selectedPlane = null; // deselect plane
                Util.updateTakeoffHotbarUI(takeoffQueueHotbar, maxTakeoffQueueSize, takeoffQueue, takeoffQueueBacklog); // refresh hotbar

                e.consume();
            }
        });
    }

    /*
    description:
    pre-condition:
    post-condition:
    */
    public void spawnNewAircraft() {
        framesSinceLastSpawn++;

        // When enough frames have passed, spawn a new plane
        if (framesSinceLastSpawn >= currentSpawnInterval) {
            framesSinceLastSpawn = 0; // Reset timer

            int arrivingOrDeparting = (int) (Math.random() * (2 - 1 + 1)) + 1; // random 1-2 -> 50% chance for departing or arriving plane

            if (score.intValue() < 25) {
                if (arrivingPlanes.size() - (takeoffQueue.size() + departingPlanes.size()) >= 2) {
                    arrivingOrDeparting = 2; // try spawning departing plane

                } else if ((takeoffQueue.size() + departingPlanes.size()) - arrivingPlanes.size() >= 2) {
                    arrivingOrDeparting = 1; // try spawning arriving plane
                }

            } else { // >= 25
                if (arrivingPlanes.size() - (takeoffQueue.size() + departingPlanes.size()) >= 3) {
                    arrivingOrDeparting = 2; // spawn departing plane

                } else if ((takeoffQueue.size() + departingPlanes.size()) - arrivingPlanes.size() >= 3) {
                    arrivingOrDeparting = 1; // spawn arriving plane
                }
            }

            if (arrivingOrDeparting == 1) {
                createNewArrivingPlane();
            } else {
                if (takeoffQueueBacklog.size() == 2) { // do not create new departing planes if backlog is full (2 planes)
                    return;
                } else {
                    createNewDepartingPlane();
                }
            }

            // increase difficulty
            if (currentSpawnInterval > minSpawnInterval) {
                currentSpawnInterval -= difficultyScaling;
            }
        }
    }

    /*
    description:
    pre-condition:
    post-condition:
    */
    public boolean checkGameOver() {
        double boundary = 30.0;
        double baseRadius = 20.0;

        for (int i = 0; i < allActivePlanes.size(); i++) {
            Plane p1 = allActivePlanes.get(i);

            // 1. check for out of bounds aircraft
            double radians = Math.toRadians(p1.getCurrentHeading());  // determine direction the plane is currently flying
            double vx = Math.cos(radians);
            double vy = Math.sin(radians);

            // plane is lost if it is beyond a boundary AND its velocity is carrying it further away (just spawned arriving planes will not trigger game over)
            boolean lostLeft   = (p1.getX() < -boundary) && (vx < 0);
            boolean lostRight  = (p1.getX() > Util.screenWidth + boundary) && (vx > 0);
            boolean lostTop    = (p1.getY() < -boundary) && (vy < 0);
            boolean lostBottom = (p1.getY() > Util.screenHeight + boundary) && (vy > 0);

            if (lostLeft || lostRight || lostTop || lostBottom) {
                this.reasonForLoss = "a plane flew out of the airspace!";
                return true;
            }

            // 2. check for collisions between aircraft
            for (int j = i + 1; j < allActivePlanes.size(); j++) {
                Plane p2 = allActivePlanes.get(j);

                double p1Scale = p1.getSprite().getScaleX();
                double p2Scale = p2.getSprite().getScaleX();

                // altitude separation check (> 0.15 scale difference -> aircraft fly over each other)
                if (Math.abs(p1Scale - p2Scale) > 0.15) {
                    continue;
                }

                // implement dynamic hitbox size based on scale
                double dynamicThreshold = (baseRadius * p1Scale) + (baseRadius * p2Scale);
                double distance = Math.hypot(p1.getX() - p2.getX(), p1.getY() - p2.getY());

                if (distance < dynamicThreshold) { // collision
                    boolean p1OnRunway = p1.getState().equals("landing") || p1.getState().equals("taking off");
                    boolean p2OnRunway = p2.getState().equals("landing") || p2.getState().equals("taking off");

                    if (p1OnRunway || p2OnRunway) {
                        this.reasonForLoss = "runway incursion!";
                    } else {
                        this.reasonForLoss = "mid-air collision!";
                    }
                    return true;
                }
            }

        }
        return false;
    }

    /*
    description:
    pre-condition:
    post-condition:
    */
    public void manageAllPlanes() {
        spawnNewAircraft();


        ArrayList<Plane> planesToRemove = new ArrayList<>();

        if (selectedPlane != null) { // continuously update starting point of heading indicator to position of plane if selected
            headingIndicator.setStartX(selectedPlane.getX());
            headingIndicator.setStartY(selectedPlane.getY());
        }

        runwayOccupied = false; // assume runway is unoccupied at start of each frame

        // manage plane movement
        for (Plane plane : allActivePlanes) {
            plane.move();

            if (plane.getState().equals("landing") || plane.getState().equals("taking off") || plane.getState().equals("climb")) {
                runwayOccupied = true;
            }

            if (plane instanceof ArrivingPlane) {
                if (plane.getState().equals("landed")) {
                    planesToRemove.add(plane);
                }

            } else if (plane instanceof DepartingPlane) {
                if (plane.getState().equals("reached waypoint")) {
                    planesToRemove.add(plane);
                }
            }
        }

        if (checkGameOver()) {
            onGameOverCallback.run();
            return;
        }

        // manage timer visuals & game over via backlog overflow
        for (DepartingPlane backlogPlane : takeoffQueueBacklog) {
            boolean timeUp = backlogPlane.updateBacklogTimer(); // only update timer for planes in backlog queue

            if (timeUp) {
                this.reasonForLoss = "departing plane backlog overflowed!";

                // trigger callback to main.java to show the game over screen
                onGameOverCallback.run();

                return; // break out immediately to halt game logic
            }
        }

        // remove any planes that have reached their objective
        for (Plane plane : planesToRemove) {
            allActivePlanes.remove(plane);

            if (plane instanceof ArrivingPlane) { // plane has landed
                arrivingPlanes.remove(plane);
                score.set(score.get() + 1);

            } else if (plane instanceof DepartingPlane) { // plane has reached waypoint
                departingPlanes.remove(plane);

                waypoints.remove(((DepartingPlane) plane).getDestination());
                gameScreen.getChildren().remove(((DepartingPlane) plane).getDestination().getSprite());

                score.set(score.get() + 1);
            }

            gameScreen.getChildren().remove(plane.getSprite());
        }
    }
}
