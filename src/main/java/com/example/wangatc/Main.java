/*
description: main class
@author: david wang
@date: jun. 5. 26
@version: 1.0
*/

package com.example.wangatc;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;

public class Main extends Application {
    private Game game;

    private StackPane scaledContainer;
    private AnimationTimer gameLoop;

    private ArrayList<String[]> highScores;


    /*
    description: entry point of javafx gui
    pre-condition: none
    post-condition: configures stage, creates ui components
    */
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
        stage.setFullScreen(true);
        stage.setFullScreenExitHint(""); // disable popup message for entering full screen
        stage.show();

        showMainMenu(); // initially render main menu
    }

    /*
    description: renders main menu gui
    pre-condition: none
    post-condition: displays main menu
    */
    private void showMainMenu() {
        VBox mainMenu = new VBox(500); // 500px spacing between elements/groups in mainmenu vbox
        mainMenu.setAlignment(Pos.CENTER);
        mainMenu.getStyleClass().add("background");

        Label title = new Label("mini airways");
        title.setStyle("-fx-font-size: 72px; -fx-text-fill: white; -fx-font-weight: bold;");

        Label description = new Label("java remake");
        description.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");

        // group text fields together with 5px spacing
        VBox textGroup = new VBox(5);
        textGroup.setAlignment(Pos.CENTER);
        textGroup.getChildren().addAll(title, description);

        Button startBtn = new Button("play");
        startBtn.getStyleClass().add("button");
        startBtn.setOnAction(e -> startGame()); // start game on press

        Button scoreListBtn = new Button("scores");
        scoreListBtn.getStyleClass().add("button");
        scoreListBtn.setOnAction(e -> showScoreScreen());

        Button quitButton = new Button("quit");
        quitButton.getStyleClass().add("button");
        quitButton.setOnAction(e -> {System.exit(0);});

        VBox buttonGroup = new VBox(25);
        buttonGroup.setAlignment(Pos.CENTER);
        buttonGroup.getChildren().addAll(startBtn, scoreListBtn, quitButton);

        mainMenu.getChildren().addAll(textGroup, buttonGroup);

        // setAll -> show only main menu
        scaledContainer.getChildren().setAll(mainMenu);
    }

    /*
    description: renders high scores screen
    pre-condition: none
    post-condition: displays top 10 scores saved in scores.txt
    */
    private void showScoreScreen() {
        VBox scoreList = new VBox(100);
        scoreList.setAlignment(Pos.CENTER);
        scoreList.getStyleClass().add("background");

        Label title = new Label("top scores");
        title.setStyle("-fx-font-size: 60px; -fx-text-fill: white; -fx-font-weight: bold;");

        // use grid layout for tabular display
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(60); // horizontal spacing between columns
        grid.setVgap(15); // vertical spacing between rows

        // setup headers
        String headerStyle = "-fx-font-size: 28px; -fx-text-fill: #ffcc00; -fx-font-weight: bold;";
        Label rankHeader = new Label("rank");
        Label dateHeader = new Label("date");
        Label scoreHeader = new Label("score");

        rankHeader.setStyle(headerStyle);
        scoreHeader.setStyle(headerStyle);
        dateHeader.setStyle(headerStyle);

        grid.addRow(0, rankHeader, dateHeader, scoreHeader); // create row for headers

        // fetch scores from file
        highScores = new ArrayList<>();
        Util.reloadData(highScores);

        if (highScores.isEmpty()) {
            Label noScores = new Label("no scores recorded yet!");
            noScores.setStyle("-fx-font-size: 24px; -fx-text-fill: #aaaaaa;");

            grid.add(noScores, 0, 1, 3, 1);
            GridPane.setHalignment(noScores, HPos.CENTER);

        } else {
            String rowStyle = "-fx-font-size: 24px; -fx-text-fill: white;";
            for (int i = 0; i < highScores.size(); i++) {
                String[] score = highScores.get(i);

                Label rankLbl = new Label("#" + (i + 1));
                Label dateLbl = new Label(score[0]);
                Label scoreLbl = new Label(score[1]);

                rankLbl.setStyle(rowStyle);
                scoreLbl.setStyle(rowStyle);
                dateLbl.setStyle(rowStyle);

                // add to row (i + 1 -> row 0 is headers)
                grid.addRow(i + 1, rankLbl, dateLbl, scoreLbl);
            }
        }

        Button backBtn = new Button("back");
        backBtn.getStyleClass().add("button");
        backBtn.setOnAction(e -> showMainMenu());

        scoreList.getChildren().addAll(title, grid, backBtn);
        scaledContainer.getChildren().setAll(scoreList);
    }

    /*
    description: renders game screen & runs game loop
    pre-condition: none
    post-condition: creates new game object & game loop
    */
    private void startGame() {
        Pane gameScreen = new Pane(); // create pane for game contents
        gameScreen.setPrefSize(Util.screenWidth, Util.screenHeight);
        gameScreen.getStyleClass().add("background");

        // swap out menu for the game screen
        scaledContainer.getChildren().setAll(gameScreen);

        // create new game object every time a new game is started
        game = new Game(gameScreen, () -> showGameOver(game.getScore(), game.getReasonForLoss())); // give game object callback to game over method in main -> runs when game is over

        gameLoop = new AnimationTimer() { // run game
            @Override
            public void handle(long currentNanoTime) {
                game.manageAllPlanes();
            }
        };

        gameLoop.start(); // initiate animation loop
        game.createNewDepartingPlane(); // start with one departing aircraft
    }

    /*
    description: renders game over screen
    pre-condition: finalScore is a valid int, reasonForLoss is a valid String
    post-condition: freezes game loop, sort & save high scores to scores.txt
    */
    private void showGameOver(int finalScore, String reasonForLoss) {
        gameLoop.stop(); // freeze game screen as is in background

        // check for high score & save scores
        highScores = new ArrayList<>(); // fetch existing scores from file
        Util.reloadData(highScores);
        Util.saveData(highScores, finalScore);

        // UI
        VBox gameOverMenu = new VBox(150);
        gameOverMenu.setAlignment(Pos.CENTER);
        gameOverMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.65);"); // semi-transparent dark overlay

        Label title = new Label("game over!");
        title.setStyle("-fx-font-size: 80px; -fx-text-fill: #ff4c4c; -fx-font-weight: bold;");

        Label scoreLabel = new Label("score: " + finalScore);
        scoreLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: white;");

        Label description = new Label(reasonForLoss);
        description.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        VBox textGroup = new VBox(5);
        textGroup.setAlignment(Pos.CENTER);
        textGroup.getChildren().addAll(title, scoreLabel, description);

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

        // add the game over screen ON TOP of the frozen game state
        scaledContainer.getChildren().add(gameOverMenu);
    }

    /*
    description: starting point of program
    pre-condition: none
    post-condition: none
    */
    public static void main(String[] args) {
        launch(args); // start javafx runtime
    }
}