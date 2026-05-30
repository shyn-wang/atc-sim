package com.example.wangatc;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

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


    // LOGIC METHODS

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


    // CREATE PLANE SPRITE METHODS

    public static Node getArrivingPlaneSprite() {
        ImageView imageNode = new ImageView(arrivingPlaneImage); // wrap in ImageView -> enables javafx to render

        imageNode.setFitWidth(64);
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

        return sprite;
    }

    public static Node getDepartingPlaneSprite(int color) {
        Image[] departingPlaneOptions = {
                departingPlaneImageBlue,
                departingPlaneImageGreen,
                departingPlaneImageOrange,
                departingPlaneImagePink,
                departingPlaneImageRed,
                departingPlaneImageYellow
        };

        ImageView imageNode = new ImageView(departingPlaneOptions[color]);

        imageNode.setFitWidth(50);
        imageNode.setFitHeight(50);
        imageNode.setPreserveRatio(true);

        // set origin of sprite to center -> enables rotation around central point
        imageNode.setX(-25);
        imageNode.setY(-25);

        // create larger invisible circle around image -> larger click hitbox
        Circle clickTarget = new Circle(0, 0, 30);
        clickTarget.setFill(Color.color(0, 0, 0, 0.01)); // use 0.01 opacity -> completely transparent cannot register clicks

        // combine into group container
        Group sprite = new Group(imageNode, clickTarget);

        return sprite;
    }


    // UI METHODS


}
