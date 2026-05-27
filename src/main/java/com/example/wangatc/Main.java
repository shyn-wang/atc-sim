package com.example.wangatc;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {
    private Pane gameScreen;

    private Game game;

    @Override
    public void start(Stage stage) {
        gameScreen = new Pane();
        gameScreen.setPrefSize(1920, 1080);

        game = new Game(gameScreen); // create game object

        game.createNewArrivingPlane();

        // assign css tag
        gameScreen.getStyleClass().add("background");

        Scene scene = new Scene(gameScreen);

        String css = this.getClass().getResource("/style.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("atc-sim");
        stage.setScene(scene);
        stage.show();

        startGameLoop();
    }

    private void startGameLoop() {
        // 60 fps
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                runGame();
            }
        };

        gameLoop.start(); // initiate animation loop
    }






    private void runGame() {
        game.moveAllAirbornePlanes();
    }








    public static void main(String[] args) {
        launch(args);
    }
}