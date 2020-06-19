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
    public static final Color PILE_COLOR = Color.YELLOW;

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

    private Piece manageBoard() {

        List<Integer> fullRows = new LinkedList<>();
        RowStatus status = RowStatus.PARTIAL;
        for (int row = VERTICAL_SPACES - 1; row >= 0 && status != RowStatus.EMPTY; row--) {
            status = this.checkRow(row);
            if (status == RowStatus.FULL) {
                fullRows.add(row);
            }
        }
        if (fullRows.size() > 0) {
            int topRow = fullRows.get(fullRows.size() - 1);
            for (Integer row : fullRows) {
                this.clearRow(row);
            }
            Piece piece = this.createAvalanche(topRow);
            this.markAndPaint(piece.getSpaces(), Marker.DYNAMIC,
                BACKGROUND_COLOR, BACKGROUND_COLOR);
            return piece;
        } else {
            return null;
        }
    }

    private void clearRow (int row){
        int[][] coordinates = new int[HORIZONTAL_SPACES][2];
        for (int i = 0; i < HORIZONTAL_SPACES; i++) {
            coordinates[i][0] = i;
            coordinates[i][1] = row;
        }
        this.markAndPaint(coordinates, Marker.EMPTY, BACKGROUND_COLOR, BACKGROUND_COLOR);
    }

    private RowStatus checkRow (int row) {
        int numEmpty = 0;
        for (int x = 0; x < HORIZONTAL_SPACES; x++) {
            if (this.board[x][row] == Marker.EMPTY) {
                ++numEmpty;
            }
        }
        if (numEmpty == 0) {
            return RowStatus.FULL;
        } else if (numEmpty == HORIZONTAL_SPACES) {
            return RowStatus.EMPTY;
        } else {
            return RowStatus.PARTIAL;
        }
    }

    private Piece createAvalanche (int row) {
        List<int[]> avalancheSpaces = new LinkedList<int[]>();
        for (int x = 0; x < HORIZONTAL_SPACES; x++) {
            for (int y = 0; y < row && y < VERTICAL_SPACES; y++) {
                if (this.board[x][y] == Marker.STATIC) {
                    int[] coor = new int[] {x, y};
                    avalancheSpaces.add(coor);
                    this.board[x][y] = Marker.EMPTY;
                } //if
            } //for
        } //for
        int[][] spaces = new int[avalancheSpaces.size()][2];
        for (int i = 0; i < spaces.length; i++) {
            spaces[i] = avalancheSpaces.get(i);
        }
        return new Piece(spaces, Color.MAGENTA);
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

    public enum RowStatus {
        FULL, EMPTY, PARTIAL;
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
        IPIECE (new int[][] {{2,0}, {1,0}, {0,0}, {3,0}}, 2),

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
            return PIECES.get((int)(Math.random() * PIECES.size() - 1));
        }

    };

    /**
     * Handles the execution of the game.
     * Executes and contains the threads this {@code TetrisBoard} object needs to play the its game
     * properly. Contains only methods that manage threads or methods that contains the actions of
     * the threads. All of the actual action taken uses the outer {@code TetrisBoard} class.
     */
    class GamePlay {

        /**
         * Timing:
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
        private Object pauseWaiter;
        private boolean enableControls;

        public GamePlay () {
            this.piece = this.selectPiece();
            this.gameOn = true;
            this.drop = false;
            this.pause = false;
            this.enableControls = false;
            this.sync = new Object();
            this.pauseWaiter = new Object();

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
            thread.setDaemon(true);
            thread.start();
        }

        private void controlLoop () {
            this.wait(this.sync);
            this.enableControls = true;
            while (gameOn) {
                this.wait(this.sync);
                this.processControls();
            }
        }

        public void wait (Object sync) {
            try {
                synchronized(sync) {
                    sync.wait();
                }
            } catch (InterruptedException ie) {
                System.err.println(ie);
                System.exit(3);
            }
        }

        private void notifyAll (Object sync) {
            synchronized(sync) {
                sync.notifyAll();
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
                    if (this.pause) {
                        this.wait(this.pauseWaiter);
                    }
                    for (int i = 0; !this.drop && i < FALL_SPEED; i++) {
                        this.wait(this.sync);
                    } //for
                    this.drop = false;
                    isFalling = (this.piece.move(0, 1)) ? true : false;
                    if (!isFalling) {
                        mark(this.piece.getSpaces(), Marker.STATIC);
                        this.enableControls = false;
                        this.piece = manageBoard();
                        if (this.piece != null) {
                            while (this.piece.move(0,1)) {
                                this.wait(this.sync);
                            }
                            mark(this.piece.getSpaces(), Marker.STATIC);
                        }
                        this.piece = this.selectPiece();
                        this.enableControls = true;
                        this.gameOn =
                            (this.piece.isValidSpaces()) ? true : false;
                    }

                } //while

            } //while
        }

        private Piece selectPiece() {
            return new Piece(PieceType.randomPieceType());
        }

        private void processControls () {
            while (!controls.isEmpty()) {
                if (controls.get(0) == KeyCode.P) {
                    this.pause = (this.pause) ? false : true;
                    controls.remove(0);
                    if (this.pause == false) {
                        this.notifyAll(this.pauseWaiter);
                    }
                } else if (!this.pause && this.enableControls) {
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

                } else {
                    controls.remove(0);
                }
            } //while
        } //processControls


    } //GamePlay

    class Piece {

        private int[][] spaces;
        private Color color;
        private List<Integer> pivot;

        public Piece (PieceType type) {
            this.spaces = type.getSpaces();
            this.color = this.selectColor();
            this.pivot = new LinkedList<Integer>();
            for (int i = 0; i < type.getPivotNum(); i++) {
                this.pivot.add(i);
            }
        }

        public Piece (int[][] spaces, Color color) {
            this.spaces = spaces;
            this.color = color;
            this.pivot = null;
        }

        public int[][] getSpaces () {
            return this.spaces;
        }

        public Color getColor () {
            return this.color;
        }

        public boolean rotate(int rotations) {
            int[][] oldSpaces = this.copy2D(this.spaces);
            this.rotatePiece(rotations);
            if (this.isValidSpaces()) {
                markAndPaint(oldSpaces, Marker.EMPTY, BACKGROUND_COLOR, BACKGROUND_COLOR);
                markAndPaint(this.spaces, Marker.DYNAMIC, this.color, OUTLINE_COLOR);
                return true;
            } else {
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
            if (this.pivot.size() == 0 && this.pivot != null) {
                return false;
            }

            double[] center = new double[] {this.spaces[this.pivot.get(0)][0],
                                      this.spaces[this.pivot.get(0)][1]};
            this.pivot.add(this.pivot.remove(0));

            double radians = (rotations * Math.PI / 2);
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

            spacesMatrix = spacesMatrix.mult(rotation);

            for (int row = 0; row < this.spaces.length; row++) {
                for (int col = 0; col < this.spaces[row].length; col++) {
                    this.spaces[row][col] = (int) Math.round(spacesMatrix.get(row, col)
                    + center[col]);
                }
            }

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
