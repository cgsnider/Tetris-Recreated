
package cgs;

import java.awt.Robot;
import javafx.scene.image.ImageView;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import javafx.scene.input.KeyCode;

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
    private GameType type;
    private boolean gameOver = false;
    private List<KeyCode> controls;

    public TetrisBoard(GameType type, List<KeyCode> controls) {
        this.grid = new Marker[HORIZONTAL_SPACES][VERTICLE_SPACES];
        this.type = type;
        this.controls = controls;
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
        Thread gamePlay = new Thread(this::gameplayLoop);

        gamePlay.setDaemon(true);
        gamePlay.start();

    }

    private void gameplayLoop() {
        int[] movement = new int[] {0, 0, 0};
        while (!gameOver) {
            this.piece = this.selectPiece(this.type);
            if (this.isValidUpdate(this.piece.getSpaces())) {
                this.updateGrid(this.piece.getSpaces(),false);
                this.updateBoard();
                boolean isFalling = true;
                while (isFalling) {
                    /*vector = controller.getMoveVector();
                    rotation = controller.getRotation();
                    System.out.println(Arrays.toString(vector));
                    System.out.println(rotation);
                    */
                    try {
                        Thread.sleep(DELAY);
                    } catch(InterruptedException ie) {
                        System.err.println(ie);
                        System.exit(3);
                    }
                    movement = this.processInput();
                    if (movement[0] != 0 || movement[1] != 0) {
                        this.piece.movePiece(new int[] {movement[0], movement[1]});
                    }
                    if (movement[2] != 0) {
                        this.piece.rotatePiece(movement[2]);
                    }
                    this.piece.movePiece(FALLING_VECTOR);
                    if (this.isValidUpdate(this.piece.getSpaces())) {
                        this.updateGrid(this.piece.getSpaces(), false);
                    } else {

                        this.updateGrid(null, true);
                        isFalling = false;
                    }
                    this.updateBoard();
                    //System.out.println("isFalling: " + isFalling);
                }
            } else {
                this.gameOver = true;
            }
        }
    }

    private int[] processInput() {
        int[] vector = new int[] {0,0,0};
        KeyCode[] options = new KeyCode[] {KeyCode.A, KeyCode.S, KeyCode.D, KeyCode.Q, KeyCode.E};
        for (KeyCode opt : options) {
            int index = this.controls.indexOf(opt);
            while (index != -1) {
                switch (opt) {
                case A: vector[0]--;
                    break;
                case S: vector[1]++;
                    break;
                case D: vector[0]++;
                    break;
                case Q: vector[2]--;
                    break;
                case E: vector[2]++;
                    break;
                } //switch
                this.controls.remove(index);
                index = this.controls.indexOf(opt);
                System.out.println(this.controls);
            } //while
        } //for-each
        return vector;
    }



    private boolean isValidUpdate(int[][] pieceCoor) {
        //System.out.printf("Rows: %d\tCols: %d\n", pieceCoor.length, pieceCoor[0].length);
        for (int row = 0; row < pieceCoor.length; row++) {
            //System.out.printf("(%d, %d)\n", pieceCoor[row][0], pieceCoor[row][1]);
            //System.out.println(pieceCoor[row][1] >= VERTICLE_SPACES);
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
/**
class Controls extends KeyAdapter {

    private int[] moveVector;
    private double rotation;

    public Controls() {
        this.moveVector = new int[] {0, 0};
    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println(e.getKeyCode());
        switch (e.getKeyCode()) {
        case KeyEvent.VK_A: --this.moveVector[0];
            System.out.println("A Detected");
            break;
        case KeyEvent.VK_S: ++this.moveVector[1];
            break;
        case KeyEvent.VK_D: ++this.moveVector[0];
            break;
        case KeyEvent.VK_Q: rotation += Math.PI / 2;
            break;
        case KeyEvent.VK_E: rotation -= Math.PI / 2;
            break;
        }
    }

    public int[] getMoveVector() {
        return this.moveVector;
    }

    public double getRotation() {
        return this.rotation;
    }

}
*/
