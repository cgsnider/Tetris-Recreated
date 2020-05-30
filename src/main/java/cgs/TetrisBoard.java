
package cgs;

import java.awt.Robot;
import javafx.scene.image.ImageView;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * The componet that contains the falling shapes.
 */
public class TetrisBoard extends ImageView {

    public static final int HORIZONTAL_SPACES = 10;
    public static final int VERTICLE_SPACES = 20;
    public static final Color BACKGROUND_COLOR = Color.DARK_GRAY;
    public static final int DISPLAY_SCALE = 25;
    public static final int[] FALLING_VECTOR = new int[] {0,1};
    public static final int DELAY = 500;
    public static final Color OUTLINE_COLOR = Color.BLACK;

    public enum GameType {
        RANDOM;
    }
    private enum Marker {
        DYNAMIC, STATIC, EMPTY, SHADOW;
    }

    private TetrisPiece.Pieces[] pieceBank = new TetrisPiece.Pieces[] {
        TetrisPiece.Pieces.OPIECE, TetrisPiece.Pieces.SPIECE, TetrisPiece.Pieces.ZPIECE,
        TetrisPiece.Pieces.TPIECE, TetrisPiece.Pieces.JPIECE, TetrisPiece.Pieces.LPIECE,
        TetrisPiece.Pieces.IPIECE
    };


    private Marker[][] grid;
    private BufferedImage board;
    private TetrisPiece piece;

    public TetrisBoard(GameType type) {
        this.grid = new Marker[HORIZONTAL_SPACES][VERTICLE_SPACES];
        this.board =  new BufferedImage(
            HORIZONTAL_SPACES * DISPLAY_SCALE, VERTICLE_SPACES * DISPLAY_SCALE,
            BufferedImage.TYPE_BYTE_INDEXED);
        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                this.board.setRGB(x,y,BACKGROUND_COLOR.getRGB());
                if (x < HORIZONTAL_SPACES && y < VERTICLE_SPACES) {
                    this.grid[x][y] = Marker.EMPTY;
                }
            }
        }
        this.setImage(SwingFXUtils.toFXImage(board, null));

/*        System.out.println(this.isValidUpdate(this.piece.getSpaces()));
        this.updateGrid(this.piece.getSpaces(), false);
        this.updateBoard();
        for (int i = 0; i < this.grid[0].length; i++) {
            for (int j = 0; j < this.grid.length; j++)
                if (this.grid[j][i] == Marker.EMPTY) {
                    System.out.printf("(%d, %d,E)", j , i);
                } else if (this.grid[j][i] == Marker.DYNAMIC) {
                    System.out.printf("(%d, %d, D)", j, i);
                }
        }
        System.out.println();
*/
        Thread gamePlay = new Thread ( () -> {
            boolean gameOver = false;
            while (!gameOver) {
                this.piece = this.selectPiece(type);
                if (this.isValidUpdate(this.piece.getSpaces())) {
                    this.updateGrid(this.piece.getSpaces(),false);
                    this.updateBoard();
                    boolean isFalling = true;
                    while (isFalling) {

                        try {
                            Thread.sleep(DELAY);
                        } catch(InterruptedException ie) {
                            System.err.println(ie);
                            System.exit(3);
                        }
                        this.piece.movePiece(FALLING_VECTOR);
                        if (this.isValidUpdate(this.piece.getSpaces())) {
                            this.updateGrid(this.piece.getSpaces(), false);
                        } else {
                            this.updateGrid(null, true);
                            isFalling = false;
                        }
                        this.updateBoard();
                        System.out.println("isFalling: " + isFalling);
                    }
                } else {
                    gameOver = true;
                }
            }
        });

        gamePlay.setDaemon(true);
        gamePlay.start();

    }

    private boolean isValidUpdate(int[][] pieceCoor) {
        System.out.printf("Rows: %d\tCols: %d\n", pieceCoor.length, pieceCoor[0].length);
        for (int row = 0; row < pieceCoor.length; row++) {
            System.out.printf("(%d, %d)\n", pieceCoor[row][0], pieceCoor[row][1]);
            System.out.println(pieceCoor[row][1] >= VERTICLE_SPACES);
            if (pieceCoor[row][1] >= VERTICLE_SPACES
                || grid[pieceCoor[row][0]][pieceCoor[row][1]] == Marker.STATIC) {
                return false;
            }
        }
        return true;
    }

    private void updateBoard() {
        for (int x = 0; x < this.grid.length; x++) {
            for (int y = 0; y < this.grid[x].length; y++) {
                if (this.grid[x][y] == Marker.SHADOW) {
                    paint(x, y, BACKGROUND_COLOR);
                    this.grid[x][y] = Marker.EMPTY;
                } else if (this.grid[x][y] == Marker.DYNAMIC) {
                    paint(x, y, this.piece.getColor());
                }
            }
        }
        this.setImage(SwingFXUtils.toFXImage(board, null));
    }


    private void paint(int xGridCoor, int yGridCoor, Color color) {
        int xBoardCoor = xGridCoor * DISPLAY_SCALE;
        int yBoardCoor = yGridCoor * DISPLAY_SCALE;
        for (int x = xBoardCoor; x < xBoardCoor + DISPLAY_SCALE; x++) {
            for (int y = yBoardCoor; y < yBoardCoor + DISPLAY_SCALE; y++) {
                this.board.setRGB(x,y,color.getRGB());
            }
        }
    }

    //pieceCoor is an optinal variable, needed if onFloor is false;
    //currently inefficient. Add a better way to remove previous piece;
    private void updateGrid(int[][] pieceCoor, boolean onFloor) {
        for (int x = 0; x < this.grid.length; x++) {
            for (int y = 0; y < this.grid[x].length; y++) {
                if (grid[x][y] == Marker.DYNAMIC) {
                    if (onFloor) {
                        grid[x][y] = Marker.STATIC;
                    } else {
                        grid[x][y] = Marker.SHADOW;
                    }
                }
            }
        }
        if (!onFloor) {
            for (int row = 0; row < pieceCoor.length; row++) {
                grid[pieceCoor[row][0]][pieceCoor[row][1]] = Marker.DYNAMIC;
            }
        }
    }

    private TetrisPiece selectPiece (GameType type) {
        TetrisPiece.Pieces selection = null;
        switch (type) {
        case RANDOM:
            selection = pieceBank[(int)(Math.random() * pieceBank.length)];
        }

        try {
            return new TetrisPiece(selection);
        } catch (NullPointerException e) {
            System.err.println("Invalid game type");
            System.exit(2);
            return null;
        }
    }

    public static int getWidth() {
        return HORIZONTAL_SPACES;
    }

}
