package cgs;

import javafx.scene.image.ImageView;
import java.awt.image.BufferedImage;
import java.awt.Color;

/**
 * The componet that contains the falling shapes.
 */
public class TetrisBoard extends ImageView {

    public static final int HORIZONTAL_SPACES = 10;
    public static final int VERTICLE_SPACES = 20;
    public static final Color BACKGROUND_COLOR = Color.DARK_GRAY;

    private enum Marker {
        DYNAMIC, STATIC, EMPTY;
    }

    private Marker[][] grid = new Marker[HORIZONTAL_SPACES][VERTICLE_SPACES];
    private BufferedImage board = new BufferedImage(
        HORIZONTAL_SPACES * 10, VERTICLE_SPACES * 10, BufferedImage.TYPE_BYTE_INDEXED);

    //Front End


    //BackEnd



}
