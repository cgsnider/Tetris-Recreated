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
import java.util.Arrays;
import org.ejml.simple.SimpleMatrix;
import java.util.Collections;
import java.lang.Runnable;

public class TetrisBoard extends ImageView {



    public static final int HORIZONTAL_SPACES = 10;
    public static final int VERTICAL_SPACES = 20;
    public static final Color BACKGROUND_COLOR = Color.DARK_GRAY;
    public static final int DISPLAY_SCALE = 25;
    public static final Color OUTLINE_COLOR = Color.BLACK;

    private BufferedImage display;
    private Marker[][] board;
    private List<KeyCode> controls;
    private Graphics2D painter;
    private List<Color> colorBank;
    private List<Color> usedColors;
    private GamePlay game;

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

        this.initPieceColors();

        this.game = new GamePlay();

    }
/*
    protected void  paint (Color pieceColor) {
        int displayX;
        int displayY;
        for (int x = 0; x < this.board.length; x++) {
            for (int y = 0; y < this.board[x].length; y++) {
                if (this.board[x][y] == Marker.DYNAMIC || this.board[x][y] == Marker.SHADOW) {
                    displayX = x * DISPLAY_SCALE;
                    displayY = y * DISPLAY_SCALE;
                    switch (this.board[x][y]) {
                    case DYNAMIC:
                        this.painter.setColor(pieceColor);
                        break;
                    case SHADOW:
                        this.painter.setColor(BACKGROUND_COLOR);
                        this.painter.drawRect(displayX, displayY,
                        DISPLAY_SCALE, DISPLAY_SCALE);
                        this.board[x][y] = Marker.EMPTY;
                        break;
                    } //switch
                    this.painter.fillRect(displayX, displayY,
                    DISPLAY_SCALE, DISPLAY_SCALE);
                    if (this.board[x][y] == Marker.DYNAMIC) {
                        this.painter.setColor(OUTLINE_COLOR);
                        this.painter.drawRect(displayX, displayY,
                        DISPLAY_SCALE, DISPLAY_SCALE);
                    } //if
                } //if
            } //for
        } //for
        this.setImage(SwingFXUtils.toFXImage(display, null));
    } //paint()
*/
    private void markAndPaint
    (int[][] squares, Marker marker, Color fillColor, Color borderColor) {
        int x, y, displayX, displayY;
        for (int row = 0; row < squares.length; row++) {

            x = squares[row][0];
            y = squares[row][1];
            if (y >= 0) {
                this.board[x][y] = marker;

                displayX = x * DISPLAY_SCALE;
                displayY = y * DISPLAY_SCALE;
                this.painter.setColor(fillColor);
                this.painter.fillRect(displayX, displayY, DISPLAY_SCALE, DISPLAY_SCALE);
                this.painter.setColor(borderColor);
                this.painter.drawRect(displayX, displayY, DISPLAY_SCALE, DISPLAY_SCALE);
                this.setImage(SwingFXUtils.toFXImage(display, null));
            }
        }
    }

    private void mark (int[][] squares, Marker marker) {
        int x, y;
        for (int row = 0; row < squares.length; row++) {
            x = squares[row][0];
            y = squares[row][1];
            if (x < HORIZONTAL_SPACES && x >= 0 && y < VERTICAL_SPACES && y >= 0) {
                this.board[x][y] = marker;
            } //if
        } //for
    } //mark


    private void initPieceColors () {
        this.colorBank = new LinkedList<Color>(Arrays.asList(new Color[] {
            Color.BLUE, Color.CYAN, Color.GREEN,
            Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.WHITE
        }));
        this.usedColors = new LinkedList<Color>();
    }

    public static int[][] copy2D (int[][] arr) {
        int[][] clone = new int[arr.length][0];
        for (int row = 0; row < arr.length; row++) {
            clone[row] = Arrays.copyOf(arr[row], arr[row].length);
        }
        return clone;
    }

    private void print2D (int[][] arr) {
        for (int i = 0; i < arr.length; i++) {
            System.out.println(Arrays.toString(arr[i]));
        }
    }

    public enum Marker {
        STATIC, DYNAMIC, EMPTY, SHADOW
    };

    public enum PieceType {
        OPIECE (new int[][] {{0,0}, {0,1}, {1,0}, {1,1}}, 0),
        SPIECE (new int[][] {{1,0}, {1,1}, {0,1}, {2,0}}, 1),
        ZPIECE (new int[][] {{1,0}, {0,0}, {1,1}, {2,1}}, 1),
        TPIECE (new int[][] {{1,1}, {0,1}, {1,0}, {2,1}}, 1),
        JPIECE (new int[][] {{1,1}, {0,0}, {0,1}, {2,1}}, 1),
        LPIECE (new int[][] {{1,1}, {0,1}, {2,0}, {2,1}}, 1),
        IPIECE (new int[][] {{2,0}, {1,0}, {0,0}, {3,0}}, 2);

        public static final List<PieceType> PIECES =
            Collections.unmodifiableList(Arrays.asList(values()));

        private int[][] spaces;
        private int pivotNum;

        private PieceType(int[][] spaces, int pivotNum) {
            this.spaces = spaces;
            this.pivotNum = pivotNum;
        }

        public int[][] getSpaces() {
            return copy2D(this.spaces);
        }

        public int getPivotNum() {
            return this.pivotNum;
        }

        public static PieceType randomPieceType() {
            return PIECES.get((int)(Math.random() * PIECES.size()));
        }

    };

    class GamePlay {

        /**
         * TICK is the fundamental unit of game time.
         * 1 TICK = 50 milliseconds = .05 seconds
         * All other times in this class are measured in TICKS
         */
        public static final int TICK = 50;
        private long clock;
        private Object sync;

        public static final int FALL_SPEED = 10;
        public static final int GRAIVITY = 1;

        private Piece piece;
        private boolean gameOn;
        private boolean drop;
        private boolean pause;

        public GamePlay () {
            this.piece = this.selectPiece();
            this.gameOn = true;
            this.drop = false;
            this.pause = false;
            this.sync = new Object();

            this.runDaemon(this::clockLoop);
            this.runDaemon(this::controlLoop);
            this.runDaemon(this::gameplayLoop);

        }

        private void clockLoop () {
            this.clock = 0;
            while (gameOn) {
                this.sleep(TICK);
                ++clock;
                this.notifyAll(this.sync);
                //System.out.println(clock);
                //printBoard();
                //print2D(this.piece.getSpaces());
            }
        }



        private void printBoard() {
            for (int y = 0; y < board[0].length; y++) {
                for (int x = 0; x < board.length; x++) {
                    String square = "0";
                    switch (board[x][y]) {
                    case EMPTY:
                        square = "E";
                        break;
                    case SHADOW:
                        square = "Sh";
                        break;
                    case DYNAMIC:
                        square = "D";
                        break;
                    case STATIC:
                        square = "S";
                    }
                    System.out.print(square + " ");
                }
                System.out.println();
            }
        }

        private void runDaemon(Runnable run)  {
            Thread thread = new Thread(run);
            thread.setDaemon(false);
            thread.start();
        }

        private void controlLoop () {
            this.wait(this.sync);
            while (gameOn) {
                this.wait(this.sync);
                this.processControls();
                //paint(this.piece.getColor());
            }
        }

        public void wait (Object sync) {
            try {
                synchronized(this.sync) {
                    this.sync.wait();
                }
            } catch (InterruptedException ie) {
                System.err.println(ie);
                System.exit(3);
            }
        }

        private void notifyAll (Object sync) {
            synchronized(this.sync) {
                this.sync.notifyAll();
            }
        }

        private void sleep (int duration) {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException ie) {
                System.err.println(ie);
                System.exit(3);
            }
        }

        private void gameplayLoop () {
            boolean isFalling;
            this.wait(this.sync);
            while (gameOn) {
                isFalling = true;
                while (isFalling) {
                    for (int i = 0; !this.drop && i < FALL_SPEED; i++) {
                        this.wait(this.sync);
                    } //for
                    this.drop = false;
                    isFalling = (this.piece.move(0, 1)) ? true : false;
                    if (isFalling) {
                        //paint(this.piece.getColor());
                    } else {
                        mark(this.piece.getSpaces(), Marker.STATIC);
                        this.piece = this.selectPiece();
                    }
                } //while

            } //while
        }

        private Piece selectPiece() {
            return new Piece(PieceType.randomPieceType());
        }

        private void processControls () {

            while (!controls.isEmpty()) {
                System.out.println("Controls: " + controls);
                if (controls.get(0) == KeyCode.P) {
                    this.pause = (this.pause) ? false : true;
                    controls.remove(0);
                } else if (!this.pause) {
                    switch (controls.remove(0)) {
                    case A:
                        this.piece.move(-1, 0);
                        break;
                    case S:
                        this.drop = true;
                        break;
                    case D:
                        this.piece.move(1, 0);
                        break;
                    case E:
                        this.piece.rotate(1);
                        break;
                    case Q:
                        this.piece.rotate(-1);
                        break;
                    } //switch

                } //else if
            } //while
        } //processControls

    } //GamePlay

    class Piece {

        private int[][] spaces;
        private Color color;
        private PieceType type;
        private List<Integer> pivot;

        public Piece (PieceType type) {
            this.spaces = type.getSpaces();
            this.color = this.selectColor();
            this.pivot = new LinkedList<Integer>();
            for (int i = 0; i < type.getPivotNum(); i++) {
                this.pivot.add(i);
            }
            System.out.println(type);
            print2D(this.spaces);
        }

        public int[][] getSpaces () {
            return this.spaces;
        }

        public Color getColor () {
            return this.color;
        }

        public boolean rotate(int rotations) {
            System.out.println("Checkpoint 1");
            int[][] oldSpaces = this.copy2D(this.spaces);
            this.rotatePiece(rotations);
            if (this.isValidSpaces()) {
                System.out.println("Checkpoint 2, Valid");
                markAndPaint(oldSpaces, Marker.EMPTY, BACKGROUND_COLOR, BACKGROUND_COLOR);
                markAndPaint(this.spaces, Marker.DYNAMIC, this.color, OUTLINE_COLOR);
                return true;
            } else {
                System.out.println("Checkpoint 2: Invalid");
                this.spaces = oldSpaces;
                return false;
            }
        }

        public boolean move (int x, int y) {
            int[][] oldSpaces = this.copy2D(this.spaces);
            this.movePiece(x, y);
            if (this.isValidSpaces()) {
                markAndPaint(oldSpaces, Marker.EMPTY, BACKGROUND_COLOR, BACKGROUND_COLOR);
                markAndPaint(this.spaces, Marker.DYNAMIC, this.color, OUTLINE_COLOR);
                //System.out.println("Mark and Move");
                return true;
            } else {
                this.spaces = oldSpaces;
                return false;
            }
        }

        private int[][] copy2D (int[][] arr) {
            int[][] clone = new int[arr.length][0];
            for (int row = 0; row < arr.length; row++) {
                clone[row] = Arrays.copyOf(arr[row], arr[row].length);
            }
            return clone;
        }

        public boolean isValidSpaces() {
            for (int row = 0; row < this.spaces.length; row++) {
                if (this.spaces[row][1] >= VERTICAL_SPACES
                || this.spaces[row][0] < 0 || this.spaces[row][0] >= HORIZONTAL_SPACES
                || (this.spaces[row][1] >= 0
                && board[this.spaces[row][0]][this.spaces[row][1]] == Marker.STATIC)) {
                    return false;
                }
            }
            return true;
        }

        private boolean rotatePiece(int rotations) {
            print2D(this.spaces);
            if (this.pivot.size() == 0) {
                return true;
            }

            double[] center = new double[] {this.spaces[this.pivot.get(0)][0],
                                      this.spaces[this.pivot.get(0)][1]};
            this.pivot.add(this.pivot.remove(0));

            double radians = (rotations * Math.PI / 2);
            System.out.println("Radians: " + radians);
            SimpleMatrix rotation = new SimpleMatrix(new double[][] {
                {Math.cos(radians), Math.sin(radians)},
                {-Math.sin(radians), Math.cos(radians)}});

            double[][] temp = new double[this.spaces.length][this.spaces[0].length];
            for (int row = 0; row < temp.length; row++) {
                for (int col = 0; col < temp[row].length; col++) {
                    temp[row][col] = this.spaces[row][col] - center[col];
                }
            }


            SimpleMatrix spacesMatrix = new SimpleMatrix(temp);

            System.out.println();
            System.out.println(rotation.toString() + "\n " + spacesMatrix.toString());
            System.out.println();

            spacesMatrix = spacesMatrix.mult(rotation);

            for (int row = 0; row < this.spaces.length; row++) {
                for (int col = 0; col < this.spaces[row].length; col++) {
                    this.spaces[row][col] = (int) Math.round(spacesMatrix.get(row, col)
                    + center[col]);
                }
            }
            print2D(this.spaces);

            return true;

        }

        private boolean movePiece (int x, int y) {
            for (int row = 0; row < this.spaces.length; row++) {
                for (int col = 0; col < this.spaces[row].length; col++) {
                    switch (col) {
                    case 0:
                        this.spaces[row][col] += x;
                        break;
                    case 1:
                        this.spaces[row][col] += y;
                        break;
                    } //switch
                } //for
            } //for
            return true;
        }

        private Color selectColor () {
            if (colorBank.isEmpty()) {
                colorBank = usedColors;
                usedColors = new LinkedList<Color>();
            }
            Color select = colorBank.remove((int)(Math.random() * colorBank.size()));
            usedColors.add(select);
            return select;
        }

    }


}
