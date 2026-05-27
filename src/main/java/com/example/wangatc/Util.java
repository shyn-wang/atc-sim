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

    public static Node getArrivingPlaneSprite() {
        ImageView imageNode = new ImageView(arrivingPlaneImage); // wrap in ImageView -> enables javafx to render

        imageNode.setFitWidth(64);
        imageNode.setFitHeight(64);
        imageNode.setPreserveRatio(true);

        // set origin of sprite to center -> enables rotation around central point
        imageNode.setX(-32);
        imageNode.setY(-32);

        // create larger invisible circle around image -> larger click hitbox
        Circle clickTarget = new Circle(0, 0, 20);
        clickTarget.setFill(Color.color(0, 0, 0, 0.01)); // use 0.01 opacity -> completely transparent cannot register clicks

        // combine into group container
        Group sprite = new Group(imageNode, clickTarget);

        return sprite;
    }
}
