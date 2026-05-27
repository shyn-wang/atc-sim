package com.example.wangatc;

import javafx.scene.layout.Pane;

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
    }

    public void createNewArrivingPlane() {
        ArrivingPlane newPlane = new ArrivingPlane();

        this.allActivePlanes.add(newPlane);
        this.allAirbornePlanes.add(newPlane);
        this.arrivingPlanes.add(newPlane);

        this.gameScreen.getChildren().add(newPlane.getSprite()); // add sprite to scene
    }

    public void moveAllAirbornePlanes() {
        for (Plane plane : allAirbornePlanes) {
            plane.move();
        }
    }


}
