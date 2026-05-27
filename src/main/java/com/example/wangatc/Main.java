package com.example.wangatc;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {
    private Pane gameScreen;

    private Game game;

    @Override
    public void start(Stage stage) {
        // create game canvas
        gameScreen = new Pane();

        // lock dimensions to 1920 x 1080
        gameScreen.setPrefSize(Util.screenWidth, Util.screenHeight);
        gameScreen.setMinSize(Util.screenWidth, Util.screenHeight);
        gameScreen.setMaxSize(Util.screenWidth, Util.screenHeight);

        Rectangle clipBox = new Rectangle(Util.screenWidth, Util.screenHeight); // hide all off-screen pixels (in black bars)
        gameScreen.setClip(clipBox);

        game = new Game(gameScreen); // create game object
        game.createNewArrivingPlane();

        // assign css tag
        gameScreen.getStyleClass().add("background");

        // place canvas in separate root container
        javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane(gameScreen);
        root.setStyle("-fx-background-color: black;"); // add letterboxing bars

        Scene scene = new Scene(root, Util.screenWidth, Util.screenHeight);

        // scale window size to screen size
        double actualScreenWidth = Screen.getPrimary().getBounds().getWidth();
        double actualScreenHeight = Screen.getPrimary().getBounds().getHeight();

        double scaleX = actualScreenWidth / Util.screenWidth;
        double scaleY = actualScreenHeight / Util.screenHeight;
        double scaleFactor = Math.min(scaleX, scaleY);

        Scale scale = new Scale(scaleFactor, scaleFactor); // scale by smaller factor -> ensure window fits on screen
        scale.setPivotX(Util.screenWidth / 2.0); // use center as pivot point -> screen scales uniformly
        scale.setPivotY(Util.screenHeight / 2.0);
        gameScreen.getTransforms().add(scale);


        String css = this.getClass().getResource("/style.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("atc-sim");
        stage.setScene(scene);
        // stage.setFullScreen(true);
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