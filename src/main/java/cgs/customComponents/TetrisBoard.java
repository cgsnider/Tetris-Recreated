package cgs.customComponents;

import javafx.scene.image.ImageView;
import java.awt.image.BufferedImage;
import java.util.List;
import javafx.scene.input.KeyCode;
import java.util.LinkedList;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javafx.embed.swing.SwingFXUtils;
import java.awt.Color;

public class TetrisBoard extends ImageView {

    public static final int HORIZONTAL_SPACES = 10;
    public static final int VERTICAL_SPACES = 20;
    public static final Color BACKGROUND_COLOR = Color.DARK_GRAY;
    public static final int DISPLAY_SCALE = 25;
    public static final int[] FALLING_VECTOR = new int[] {0,1};
    public static final int DELAY = 500;
    public static final Color OUTLINE_COLOR = Color.BLACK;

    private BufferedImage display;
    private Marker[][] board;
    private List<KeyCode> controls;
    private Graphics2D painter;

    public TetrisBoard (List<KeyCode> controls) {
        this.controls = controls;

        this.board = new Marker[HORIZONTAL_SPACES][VERTICAL_SPACES];
        for (int x = 0; x < this.board.length; x++) {
            for (int y = 0; y < this.board[x].length; y++) {
                this.board[x][y] = Marker.EMPTY;
            }
        }

        this.display = new BufferedImage(
            HORIZONTAL_SPACES * DISPLAY_SCALE, VERTICAL_SPACES * DISPLAY_SCALE,
            BufferedImage.TYPE_BYTE_INDEXED);
        this.painter = this.display.createGraphics();
        this.painter.setColor(BACKGROUND_COLOR);
        this.painter.fillRect(0, 0, this.display.getWidth(), this.display.getHeight());

        this.setImage(SwingFXUtils.toFXImage(display, null));

    }

    public enum Marker {
        STATIC, DYNAMIC, EMPTY, SHADOW
    };

    public enum PieceType {
        OPIECE (new int[][] {{0,0},{0,1},{1,0},{1,0}}),
        SPIECE (new int[][] {{0,1},{1,1},{1,0},{2,0}}),
        ZPIECE (new int[][] {{0,0},{1,0},{1,1},{2,1}}),
        TPIECE (new int[][] {{0,1},{1,0},{1,1},{2,1}}),
        JPIECE (new int[][] {{0,0},{0,1},{1,1},{2,1}}),
        LPIECE (new int[][] {{0,1},{1,1},{2,0},{2,1}}),
        IPIECE (new int[][] {{0,0},{1,0},{2,0},{3,0}});

        private int[][] initSpaces;

        private PieceType(int[][] initSpaces) {
            this.initSpaces = initSpaces;
        }

        public int[][] initSpaces() {
            return this.initSpaces();
        }

    };

    private class GamePlay {

    }

    private class Piece {

    }


}
