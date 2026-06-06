/*
description: util class
@author: david wang
@date: jun. 5. 26
@version: 1.0
*/

package com.example.wangatc;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

public class Util { // use static variables & methods -> enables access in other classes without creating a util object
    public static int screenWidth = 1920;
    public static int screenHeight = 1080;
    private static int spawnOffScreenMargin = 100;

    // load plane icons
    private static Image arrivingPlaneImage = new Image(Util.class.getResourceAsStream("/images/planeArriving.png"));

    private static Image departingPlaneImageBlue = new Image(Util.class.getResourceAsStream("/images/planeBlue.png"));
    private static Image departingPlaneImageGreen = new Image(Util.class.getResourceAsStream("/images/planeGreen.png"));
    private static Image departingPlaneImageOrange = new Image(Util.class.getResourceAsStream("/images/planeOrange.png"));
    private static Image departingPlaneImagePink = new Image(Util.class.getResourceAsStream("/images/planePink.png"));
    private static Image departingPlaneImageRed = new Image(Util.class.getResourceAsStream("/images/planeRed.png"));
    private static Image departingPlaneImageYellow = new Image(Util.class.getResourceAsStream("/images/planeYellow.png"));

    // FILE HANDLERS

    /*
    description: locally saves the 10 highest player scores to scores.txt in descending sorted order
    pre-condition: highScores is a valid ArrayList<String[]>, finalScore is a valid int
    post-condition: highScores & scores.txt are modified
    */
    public static void saveData(ArrayList<String[]> highScores, int finalScore) {
        String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE); // get date

        if (highScores.size() < 10) {
            highScores.add(new String[] {date, String.valueOf(finalScore)}); // add all new scores if list is not full (less than 10 recorded scores)

        } else { // list is full -> only add new top scores
            int minScore = Integer.parseInt(highScores.getLast()[1]); // check last score for min score (sorted)

            if (finalScore >= minScore) {
                highScores.remove(highScores.getLast());
                highScores.add(new String[] {date, String.valueOf(finalScore)}); // replace min score with new score
            }
        }

        // re-sort list
        String[][] temp = highScores.toArray(new String[0][]); // convert arraylist to array
        mergeSort(temp);

        highScores.clear(); // empty arraylist
        Collections.addAll(highScores, temp); // re-add sorted scores to arraylist


        try (BufferedWriter bw = new BufferedWriter(new FileWriter("scores.txt"))) {
            for (String[] score : highScores) { // copy all score info into scores.txt
                bw.write(score[0]); // date
                bw.write("|"); // separate by |

                bw.write(score[1]); // score
                bw.write("|");

                bw.newLine();
            }
        } catch (IOException _) {

        }
    }

    /*
    description: loads score information from scores.txt into highScores
    pre-condition: highScores is a valid ArrayList<String[]>
    post-condition: highScores is populated with saved scores
    */
    public static void reloadData(ArrayList<String[]> highScores) {
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader("scores.txt"))) { // read file contents
            while ((line = br.readLine()) != null) { // parse each line in file containing data
                String[] score = line.split("\\|"); // splits line using | as the delimiter; stores individual score info in String[] array

                if (score.length == 2) { // ignore empty lines (will produce an array of length 1 with an empty string)
                    highScores.add(score);
                }
            }

        } catch (Exception _) {

        }
    }

    /*
    description: recursively splits an array into halves until each element is its own array of length 1; calls merge to combine arrays in order
    pre-condition: inputArray is a valid String[][] array
    post-condition: inputArray is sorted in descending order
    */
    private static void mergeSort(String[][] inputArray) {
        int length = inputArray.length;

        if (length <= 1) {
            return; // array is sorted if contains only one element
        } else {
            int mid = length / 2;
            String[][] left = new String[mid][]; // create new array to store left half elements of inputArray
            String[][] right = new String[length - mid][]; // create new array to store right half elements of inputArray

            for (int i = 0; i < mid; i++) {
                left[i] = inputArray[i]; // fill left half array
            }

            for (int i = mid; i < length; i++) {
                right[i - mid] = inputArray[i]; // fill right half array
            }

            mergeSort(left); // recursively call mergeSort to continue splitting left half array in a deeper call stack layer
            mergeSort(right); // recursively call mergeSort to continue splitting right half array in a deeper call stack layer
            merge(inputArray, left, right); // merge left and right half arrays (starting from deepest layer in call stack)
        }
    }

    /*
    description: merge the elements of two arrays into one array in descending order
    pre-condition: input, left, and right are valid String[][] arrays
    post-condition: modifies contents of input to be in descending order
    */
    private static void merge(String[][] input, String[][] left, String[][] right) {
        int leftSize = left.length;
        int rightSize = right.length;

        // i represents index position in left array, j represents index position in right array, k represents index position in merged array
        int i = 0, j = 0, k = 0;

        while (i < leftSize && j < rightSize) {
            // get scores
            int leftScore = Integer.parseInt(left[i][1]);
            int rightScore = Integer.parseInt(right[j][1]);

            if (leftScore >= rightScore) { // merge greatest to least (descending)
                input[k] = left[i];
                i++;
            } else {
                input[k] = right[j];
                j++;
            }

            k++; // move to next element in merged array
        }

        while (i < leftSize) { // only runs if left array has remaining elements
            input[k] = left[i]; // adds remaining elements directly to merged array; elements are already sorted
            i++;
            k++;
        }

        while (j < rightSize) { // only runs if right array has remaining elements
            input[k] = right[j]; // adds remaining elements directly to merged array; elements are already sorted
            j++;
            k++;
        }
    }


    // LOGIC METHODS

    /*
    description: calculates the angle between two points (heading from p1 to p2)
    pre-condition: p1 & p2 are valid double[] arrays
    post-condition: returns calculated heading in degrees
    */
    public static double getHeadingTo(double[] p1, double[] p2) {
        double x1 = p1[0];
        double y1 = p1[1];

        double x2 = p2[0];
        double y2 = p2[1];

        // calculate x & y components of displacement vector
        double deltaX = x2 - x1;
        double deltaY = y2 - y1;

        // return angle (direction plane must travel in relative to map)
        double heading = Math.toDegrees(Math.atan2(deltaY, deltaX));

        // normalize angle to between 0-359 (initially between -180 & 180)
        heading = (heading + 360) % 360;

        return heading;
    }

    /*
    description: generates random spawn coordinates off-screen for newly spawned arriving planes
    pre-condition: none
    post-condition: returns int[] array containing x,y coordinates, sets plane heading to point towards center of screen
    */
    public static int[] generateRandomSpawnPoint() { // generate random spawn point for arriving aircraft beyond one of four screen edges
        int whichEdge = (int) (Math.random() * (4 - 1 + 1)) + 1; // random 1-4
        int spawnX = 0;
        int spawnY = 0;

        switch (whichEdge) {
            case 1: // top
                spawnX = (int) (Math.random() * (screenWidth + 1)); // random x along horizontal screen side
                spawnY = -spawnOffScreenMargin;
                break;
            case 2: // right
                spawnX = screenWidth + spawnOffScreenMargin;
                spawnY = (int) (Math.random() * (screenHeight + 1)); // random y along vertical screen side
                break;
            case 3: // bottom
                spawnX = (int) (Math.random() * (screenWidth + 1)); // random x along horizontal screen side
                spawnY = screenHeight + spawnOffScreenMargin;
                break;
            case 4: // left
                spawnX = -spawnOffScreenMargin;
                spawnY = (int) (Math.random() * (screenHeight + 1)); // random y along vertical screen side
                break;
        }

        return new int[] {spawnX, spawnY};
    }

    /*
    description: generates random coordinate for waypoints
    pre-condition: existingWaypoints is a valid ArrayList<Waypoint>, color is a valid int
    post-condition: returns double[] array containing x,y coordinates
    */
    public static double[] generateRandomWaypointLocation(ArrayList<Waypoint> existingWaypoints, int color) {
        // if a waypoint of a specific color already exists, spawn all waypoints of that color at the same location
        for (Waypoint waypoint : existingWaypoints) { // -> linearly search through existingWaypoints for a matching color
            if (waypoint.getColor() == color) {
                return new double[] {waypoint.getX(), waypoint.getY()};
            }
        }

        // waypoint of specific color does not already exist -> generate random spawn position
        boolean isValid;
        double x = 0;
        double y = 0;

        do {
            isValid = true;

            // generate random x,y pair
            x = 50 + Math.random() * (Util.screenWidth - 100); // 50 px margin on all sides
            y = 50 + Math.random() * (Util.screenHeight - 100);

            // block point from being within exclusion square
            if (isInsideExclusionZone(x, y)) {
                isValid = false;
            }

            // check distance to already placed waypoints
            if (isValid) {
                for (Waypoint waypoint : existingWaypoints) {
                    if (Math.hypot(x - waypoint.getX(), y - waypoint.getY()) < 150) { // spawn position cannot be within 150 px of other waypoints
                        isValid = false;
                        break;
                    }
                }
            }

        } while (!isValid);

        return new double[] {x, y};
    }

    /*
    description: checks if an x,y coordinate is within a square exclusion zone located at the center of the screen
    pre-condition: x & y are valid doubles
    post-condition: returns boolean true or false
    */
    private static boolean isInsideExclusionZone(double x, double y) {
        // find center of screen
        double centerX = Util.screenWidth / 2.0; // 960
        double centerY = Util.screenHeight / 2.0; // 540

        double squareSize = 500;
        double halfSize = squareSize / 2.0; // 250

        // define borders
        double minX = centerX - halfSize; // 710
        double maxX = centerX + halfSize; // 1210
        double minY = centerY - halfSize; // 290
        double maxY = centerY + halfSize; // 790

        // check if (x,y) is in square
        return (x >= minX && x <= maxX) && (y >= minY && y <= maxY);
    }


    // CREATE PLANE SPRITE METHODS

    /*
    description: creates the visual sprite for an arriving aircraft
    pre-condition: none
    post-condition: returns sprite as a Node
    */
    public static Node getArrivingPlaneSprite() {
        ImageView imageNode = new ImageView(arrivingPlaneImage); // wrap in ImageView -> enables javafx to render

        imageNode.setFitWidth(64); // set size
        imageNode.setFitHeight(64);
        imageNode.setPreserveRatio(true);

        // set origin of sprite to center -> enables rotation around central point
        imageNode.setX(-32);
        imageNode.setY(-32);

        // create larger invisible circle around image -> larger click hitbox
        Circle clickTarget = new Circle(0, 0, 40);
        clickTarget.setFill(Color.color(0, 0, 0, 0.01)); // use 0.01 opacity -> completely transparent cannot register clicks

        // combine into group container
        Group sprite = new Group(imageNode, clickTarget);

        return sprite; // Group is a child of Node -> can be returned as a Node
    }

    /*
    description: creates the visual sprite for a departing aircraft
    pre-condition: color is a valid int
    post-condition: returns sprite as a Node
    */
    public static Node getDepartingPlaneSprite(int color) {
        Image[] departingPlaneOptions = { // possible colors
                departingPlaneImageBlue,
                departingPlaneImageGreen,
                departingPlaneImageOrange,
                departingPlaneImagePink,
                departingPlaneImageRed,
                departingPlaneImageYellow
        };

        ImageView imageNode = new ImageView(departingPlaneOptions[color]); // map int color onto color options to determine corresponding color

        imageNode.setFitWidth(50);
        imageNode.setFitHeight(50);
        imageNode.setPreserveRatio(true);

        // set origin of sprite to center -> enables rotation around central point
        imageNode.setX(-25);
        imageNode.setY(-25);

        // create larger invisible circle around image -> larger click hitbox
        Circle clickTarget = new Circle(0, 0, 30);
        clickTarget.setFill(Color.color(0, 0, 0, 0.01)); // use 0.01 opacity -> completely transparent cannot register clicks

        // combine into Pane
        Pane sprite = new Pane(imageNode, clickTarget);

        sprite.setPrefSize(0, 0); // force 0x0 size -> center aircraft in hotbar slots
        sprite.setMinSize(0, 0);
        sprite.setMaxSize(0, 0);

        return sprite;
    }


    // UI METHODS

    /*
    description: initializes line used for indicating heading
    pre-condition: none
    post-condition: returns Line headingIndicator
    */
    public static Line initializeHeadingIndicator() {
        Line headingIndicator = new Line();
        headingIndicator.setStroke(Color.WHITE);
        headingIndicator.setStrokeWidth(2.0);
        headingIndicator.getStrokeDashArray().addAll(10d, 5d); // dashed line
        headingIndicator.setVisible(false); // initially invisible

        return headingIndicator;
    }

    /*
    description: initializes hotbar used to store queued departing aircraft
    pre-condition: none
    post-condition: returns HBox takeoffQueueHotbar
    */
    public static HBox initializeHotbar() {
        HBox takeoffQueueHotbar = new HBox(15); // 15 px horizontal spacing between slots

        takeoffQueueHotbar.setScaleX(0.7);
        takeoffQueueHotbar.setScaleY(0.7);

        // position in the bottom left corner
        takeoffQueueHotbar.setLayoutX(10);
        takeoffQueueHotbar.setLayoutY(screenHeight - 130);

        takeoffQueueHotbar.getStyleClass().add("hotbar");

        return takeoffQueueHotbar;
    }

    /*
    description: populates hotbar with queued & backlogged aircraft
    pre-condition: valid arguments
    post-condition: updates hotbar slots
    */
    public static void updateTakeoffHotbarUI(HBox takeoffQueueHotbar, int maxTakeoffQueueSize, ArrayList<DepartingPlane> takeoffQueue, ArrayList<DepartingPlane> takeoffQueueBacklog) {
        takeoffQueueHotbar.getChildren().clear(); // clear existing slots

        for (int i = 0; i < maxTakeoffQueueSize; i++) { // create # of slots based on predefined max size
            // new slot
            StackPane slot = new StackPane();
            slot.setPrefSize(80, 80);
            slot.getStyleClass().add("slot");

            // check if slot can be filled by a plane
            if (i < takeoffQueue.size()) {
                Plane queuedPlane = takeoffQueue.get(i);

                if (!queuedPlane.getState().equals("dragging from takeoff queue")) { // if called while a plane is being dragged -> will not reset plane to hotbar
                    Node planeSprite = queuedPlane.getSprite();

                    planeSprite.setTranslateX(0); // set position relative to slot
                    planeSprite.setTranslateY(0);

                    slot.getChildren().add(planeSprite); // add sprite to slot
                }
            }

            takeoffQueueHotbar.getChildren().add(slot); // add slot to hotbar
        }

        // render any aircraft in backlog
        for (Plane plane : takeoffQueueBacklog) {
            StackPane slot = new StackPane();
            slot.setPrefSize(80, 80);
            slot.getStyleClass().add("slot");

            plane.getSprite().setTranslateX(0); // set position relative to slot
            plane.getSprite().setTranslateY(0);

            slot.getChildren().add(plane.getSprite());

            takeoffQueueHotbar.getChildren().add(slot); // add slot to hotbar
        }

    }
}
