package com.example.wangatc;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {
    private Game game;

    private StackPane scaledContainer;
    private AnimationTimer gameLoop;

    @Override
    public void start(Stage stage) {
        // create central container to store different screens
        scaledContainer = new StackPane();
        scaledContainer.setPrefSize(Util.screenWidth, Util.screenHeight);
        scaledContainer.setMinSize(Util.screenWidth, Util.screenHeight);
        scaledContainer.setMaxSize(Util.screenWidth, Util.screenHeight);

        // lock dimensions to 1920 x 1080
        double actualScreenWidth = Screen.getPrimary().getBounds().getWidth(); // scale window size to screen size
        double actualScreenHeight = Screen.getPrimary().getBounds().getHeight();

        double scaleX = actualScreenWidth / Util.screenWidth;
        double scaleY = actualScreenHeight / Util.screenHeight;
        double scaleFactor = Math.min(scaleX, scaleY);

        Scale scale = new Scale(scaleFactor, scaleFactor); // scale by smaller factor -> ensure window fits on screen
        scale.setPivotX(Util.screenWidth / 2.0); // use center as pivot point -> screen scales uniformly
        scale.setPivotY(Util.screenHeight / 2.0);
        scaledContainer.getTransforms().add(scale);

        Rectangle clipBox = new Rectangle(Util.screenWidth, Util.screenHeight); // hide all off-screen pixels (in black bars)
        scaledContainer.setClip(clipBox);

        // place canvas in separate root container
        javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane(scaledContainer);
        root.setStyle("-fx-background-color: black;"); // add letterboxing bars

        Scene scene = new Scene(root, Util.screenWidth, Util.screenHeight);

        String css = this.getClass().getResource("/style.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setScene(scene);
        // stage.setFullScreen(true);
        stage.show();

        showMainMenu(); // initially render main menu
    }

    private void showMainMenu() {
        VBox mainMenu = new VBox(500); // 500px spacing between elements/groups in mainmenu vbox
        mainMenu.setAlignment(Pos.CENTER);
        mainMenu.getStyleClass().add("background");

        Label title = new Label("mini airways");
        title.setStyle("-fx-font-size: 72px; -fx-text-fill: white; -fx-font-weight: bold;");

        Label description = new Label("(java remake)");
        description.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");

        // group text fields together with 5px spacing
        VBox textGroup = new VBox(5);
        textGroup.setAlignment(Pos.CENTER);
        textGroup.getChildren().addAll(title, description);

        Button startBtn = new Button("play");
        startBtn.getStyleClass().add("button");
        startBtn.setOnAction(e -> startGame()); // start game on press

        mainMenu.getChildren().addAll(textGroup, startBtn);

        // setAll -> show only main menu
        scaledContainer.getChildren().setAll(mainMenu);
    }

    private void startGame() {
        Pane gameScreen = new Pane(); // create pane for game contents
        gameScreen.setPrefSize(Util.screenWidth, Util.screenHeight);
        gameScreen.getStyleClass().add("background");

        // swap out menu for the game screen
        scaledContainer.getChildren().setAll(gameScreen);

        // create new game object every time a new game is started
        game = new Game(gameScreen, () -> showGameOver(game.getScore())); // give game object callback to game over method in main -> runs when game is over

        gameLoop = new AnimationTimer() { // run game
            @Override
            public void handle(long currentNanoTime) {
                game.manageAllPlanes();
            }
        };

        gameLoop.start(); // initiate animation loop
        game.createNewDepartingPlane(); // start with one departing aircraft
    }

    private void showGameOver(int finalScore) {
        if (gameLoop != null) {
            gameLoop.stop(); // freeze game screen as is in background
        }

        VBox gameOverMenu = new VBox(150);
        gameOverMenu.setAlignment(Pos.CENTER);
        gameOverMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.65);"); // semi-transparent dark overlay

        Label title = new Label("game over!");
        title.setStyle("-fx-font-size: 80px; -fx-text-fill: #ff4c4c; -fx-font-weight: bold;");

        Label scoreLabel = new Label("score: " + finalScore);
        scoreLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: white;");

        VBox textGroup = new VBox(5);
        textGroup.setAlignment(Pos.CENTER);
        textGroup.getChildren().addAll(title, scoreLabel);

        Button restartBtn = new Button("new game");
        restartBtn.getStyleClass().add("button");
        restartBtn.setOnAction(e -> startGame());

        Button menuBtn = new Button("main menu");
        menuBtn.getStyleClass().add("button");
        menuBtn.setOnAction(e -> showMainMenu());

        VBox buttonGroup = new VBox(25);
        buttonGroup.setAlignment(Pos.CENTER);
        buttonGroup.getChildren().addAll(restartBtn, menuBtn);

        gameOverMenu.getChildren().addAll(textGroup, buttonGroup);

        // add the game over screen on top of the frozen game state
        scaledContainer.getChildren().add(gameOverMenu);
    }

    public static void main(String[] args) {
        launch(args);
    }
}