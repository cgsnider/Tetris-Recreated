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
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.application.Platform;
import javafx.scene.control.Button;

/**
 * A custom component built from an ImageView that runs the game Tetris.
 * Contains all the necessary code to run the game Tetris enabling easy plug in play capabilities
 * in a javaFX application.
 * The {@code TetrisBoard} outer class contains all the methods for managing the displayed image
 * and the grid that represents the game internally.
 *
 * Methods for managing gameplay are contained within the inner class {@code GamePlay} which
 * and launches the actual gameplay threads by calling the {@code GamePlay} constructor.
 *
 * The methods managing the falling pieces are contained in the inner class {@code Piece}
 * which handles the operations such as moving/rotating the piece.
 *
 * Creating an instance of {@code TetrisBoard} automatically begins the game.
 */
public class TetrisBoard extends ImageView {

    /* The size of the internal game grid (board) */
    public static final int HORIZONTAL_SPACES = 10;
    public static final int VERTICAL_SPACES = 20;

    /* The scale that used to convert the grid (board) into an image */
    public static final int DISPLAY_SCALE = 25;

    /* The default colors */
    public static final Color BACKGROUND_COLOR = Color.DARK_GRAY;
    public static final Color OUTLINE_COLOR = Color.BLACK;
    public static final Color PILE_COLOR = Color.YELLOW; //After a row is cleared

    /* The image stored in memory that can be edited*/
    private BufferedImage display;

    /* Object that can paint onto the display image*/
    private Graphics2D painter;

    /* The grid that stores the internal state of the game */
    private Marker[][] board;

    /* The list of all the KeyCodes entered by the player */
    private List<KeyCode> controls;

    /* The manages possible colors for the pieces */
    private List<Color> colorBank;
    private List<Color> usedColors;

    /* The gameplay and the threads that control the game */
    private GamePlay game;

    /* The stage that contains this TetrisBoard, allows the creation of new windows */

    private Stage stage;

    /**
     * Creates a new TetrisBoard componet instance with a grey rectangle
     * representing an empty board.
     * @param controls the list of player input.
     */
    public TetrisBoard (List<KeyCode> controls, Stage stage) {
        this.controls = controls;
        this.stage = stage;

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

    /* Constructor Helper Methods*/

    /**
     * Helper Method for constuctor.
     * Initalizes colorBank and usedColor lists to keep track of which colors are
     * aready present on the board.
     */
    private void initPieceColors () {
        this.colorBank = new LinkedList<Color>(Arrays.asList(new Color[] {
            Color.BLUE, Color.CYAN, Color.GREEN,
            Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.WHITE
        }));
        this.usedColors = new LinkedList<Color>();
    }

    /* Board and Display Mutators */

    /**
     * Clears any row that is full and converts and filled in squares above it into a piece
     * to enable the squares above the row to fall.
     * @return the custom piece that is only suppose to fall (no rotate or to move left or right)
     */
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

    /**
     * Marks the board with a specified marker and fills in the corresponging pixels on the
     * image display to the specified colors then updates the image in the being shown.
     * @param squares the coordinates that will be marked in the {@code board}
     * @param marker the {@code Marker} that will be places at the coordinates on {@code board}
     * @param fillColor the color of the square on {@code display}
     * @param borderColor the color of the outline of the square on {@code display}
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

    /**
     * Marks the {@code board} with the designted {@code Marker}.
     * @param squares the array of coordinates for the squares
     * @param marker the {@code Marker} that will be added at the location
     */
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

    /* ManageBoard() helper methods */

    /**
     * Helper method for ManageBoard()
     * Clears the {@code board} and {@code display} at a specified row.
     * Marks {@code board} with {@code Marker.EMPTY} on this row.
     * Paints {@code display} with squares the color of the Background.
     * @param row the row that is to be cleared.
     */
    private void clearRow (int row){
        int[][] coordinates = new int[HORIZONTAL_SPACES][2];
        for (int i = 0; i < HORIZONTAL_SPACES; i++) {
            coordinates[i][0] = i;
            coordinates[i][1] = row;
        }
        this.markAndPaint(coordinates, Marker.EMPTY, BACKGROUND_COLOR, BACKGROUND_COLOR);
    }

    /**
     * Helper method for ManageBoard()
     * Checks if a row is full.
     * Specifically, checks if a row stored in {@code board} contains only {@code Marker.STATIC}.
     * @param row the row that is to be checked.
     * @return the status of the row (FULL, EMPTY, PARTIAL).
     */
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

    /**
     * Helper method for ManageBoard()
     * Creates the custom piece object that represents all of
     * the spaces marked {@code Marker.STATIC} above the specified row.
     * Changes the {@code Marker.STATIC} to {@code Marker.EMPTY}.
     * Sets color of the custom piece as the {@code PILE_COLOR}.
     * @param row the row that marks the bottom of the custom piece.
     * @return a custom piece that takes up all of the spaces of fallen pieces
     * above the specified row.
     */
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
        return new Piece(spaces, PILE_COLOR);
    }

    /* Utility methods */

    /**
     * Copies a 2D int array
     * @param arr the 2D int array to be copied
     * @return the copied 2D int array.
     */
    public static int[][] copy2D (int[][] arr) {
        int[][] clone = new int[arr.length][0];
        for (int row = 0; row < arr.length; row++) {
            clone[row] = Arrays.copyOf(arr[row], arr[row].length);
        }
        return clone;
    }

    /**
     * Prints a 2D array for testing/developing purposes.
     * @param arr the array to be printed.
     */
    private void print2D (int[][] arr) {
        for (int i = 0; i < arr.length; i++) {
            System.out.println(Arrays.toString(arr[i]));
        }
    }

    /**
     * Prints the {@code board} to console for testing/development purposes.
     */
    private void printBoard() {
        for (int y = 0; y < board[0].length; y++) {
            for (int x = 0; x < board.length; x++) {
                String square = "0";
                switch (board[x][y]) {
                case EMPTY:
                    square = "E";
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

    /* Enum Types */

    /**
     * Used to represent the status of the row and describe if the row is ready to be cleared.
     */
    public enum RowStatus {
        FULL, EMPTY, PARTIAL;
    }

    /**
     * Used to mark the {@code board}.
     * STATIC dennotes a piece at that space that has fallen and no longer moves.
     * DYNAMIC dennotes a piece at that space that can move and is falling.
     * EMPTY dennotes that there is no piece at that space.
     */
    public enum Marker {
        STATIC, DYNAMIC, EMPTY;
    };

    /**
     * The options predescribed pieces and contains their inital spaces
     * and the number of pivots (for rotation).
     */
    public enum PieceType {
        OPIECE (new int[][] {{0,0}, {0,1}, {1,0}, {1,1}}, 0),
        SPIECE (new int[][] {{1,0}, {1,1}, {0,1}, {2,0}}, 1),
        ZPIECE (new int[][] {{1,0}, {0,0}, {1,1}, {2,1}}, 1),
        TPIECE (new int[][] {{1,1}, {0,1}, {1,0}, {2,1}}, 1),
        JPIECE (new int[][] {{1,1}, {0,0}, {0,1}, {2,1}}, 1),
        LPIECE (new int[][] {{1,1}, {0,1}, {2,0}, {2,1}}, 1),
        IPIECE (new int[][] {{2,0}, {1,0}, {0,0}, {3,0}}, 2);

        /* A list of all the options in order */
        public static final List<PieceType> PIECES =
            Collections.unmodifiableList(Arrays.asList(values()));

        /* The starting spaces of the falling piece */
        private int[][] spaces;

        /* The number of pivots */
        private int pivotNum;

        /**
         * Creates a new instance of enum type with spaces and number of pivots.
         * @param spaces the inital spaces of the piece
         * @param pivotNum the number of pivots
         */
        private PieceType(int[][] spaces, int pivotNum) {
            this.spaces = spaces;
            this.pivotNum = pivotNum;
        }

        /**
         * Gets a copy of the array containing the spaces.
         * @return a copy of the array containg the spaces
         */
        public int[][] getSpaces() {
            return copy2D(this.spaces);
        }

        /**
         * Gets the number of pivots
         * @return the number of pivots
         */
        public int getPivotNum() {
            return this.pivotNum;
        }

        /**
         * selects a random {@code PieceType}
         * @return a random {@code PieceType}.
         */
        public static PieceType randomPieceType() {
            return PIECES.get((int)(Math.random() * PIECES.size() - 1));
        }

    };

    /* Innner Classes */

    /**
     * Handles the execution of the game.
     * Executes and contains the threads this {@code TetrisBoard} object needs to play the its game
     * properly. Contains only methods that manage threads or methods that contains the actions of
     * the threads. All of the actual action taken uses the outer {@code TetrisBoard} class.
     */
    class GamePlay {

        /*
         * Timing:
         * TICK is the fundamental unit of game time.
         * 1 TICK = 50 milliseconds = .05 seconds
         * All other times in this class are measured in TICKS
         * {@code sync} object is used to synchronize all threads with game clock.
         */
        public static final int TICK = 50;
        private Object sync;

        /* Controls the the number of ticks required for the piece to fall. */
        public static final int FALL_SPEED = 10;

        /* If true then bypasses FALL_SPEED and moves piece down immediatly. */
        private boolean drop;

        /* lets a thread know to pause. */
        private boolean pause;

        /* Waits until the pause ends. */
        private Object pauseWaiter;

        /* The piece that is currently in play */
        private Piece piece;

        /* The state of the game */
        private boolean gameOn;

        /* Enable or disbale player controls */
        private boolean enableControls;

        /**
         * Sets up and begins the game.
         * Starts threads for the game clock, player controls, and gameplay.
         */
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

        /* Thread methods */

        /**
         * To be run in its own thread.
         * Controls the clock enabling all the threads to follow one game beat.
         */
        private void clockLoop () {
            while (gameOn) {
                this.sleep(TICK);
                this.notifyAll(this.sync);
            }
        }

        /**
         * To be run in its own thread.
         * Takes player intput and moves the piece appropriately.
         */
        private void controlLoop () {
            this.wait(this.sync);
            this.enableControls = true;
            while (gameOn) {
                this.wait(this.sync);
                this.processControls();
            }
        }

        /**
         * To be run in its own thread.
         * The main gameplay thread that handles the core elements of the game.
         * Moves the piece downwards, clears full rows, manages whether the game is in play.
         */
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
            Platform.runLater(this::gameOver);
        }

        /* Thread method helpers */

        /**
         * Helper method to {@code controlLoop}
         * Handles the player's input by checking a list of all keypresses,
         * removing the last unprocessed keypress, and performing the appriopriate action.
         * A - moves the piece left
         * S - moves the piece down (bypasses the {@code FALL_SPEED} once)
         * D - moves the piece right
         * Q - rotates the piece Counter-Clockwise
         * E - rotates the piece Clockwise
         */
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

        /**
         * Creates a new Piece object with a random pieceType.
         */
        private Piece selectPiece() {
            return new Piece(PieceType.randomPieceType());
        }

        /* Thread utility methods */

        /**
         * Starts a Demon thread. Designed to be able to use method references.
         * @param run the method that will be run in a new thread
         */
        private void runDaemon(Runnable run)  {
            Thread thread = new Thread(run);
            thread.setDaemon(true);
            thread.start();
        }

        /**
         * Makes the current thread wait via the wait method desceribed in the {@code Object} class.
         * Handles synchronization
         * and handles InterruptedException by exiting the program with exit code 3.
         * @param sync the object being used to synchronize this thread.
         */
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

        /**
         * Notifies all threads waiting on the {@code sync} object to resume.
         * @param the object being used to synchronize the current thread.
         */
        private void notifyAll (Object sync) {
            synchronized(sync) {
                sync.notifyAll();
            }
        }

        /**
         * Causes the current thread to sleep for {@code duration} time.
         * Catches possible InterruptedException by exiting with exit code 3.
         * @param duration the time in milliseconds the the current thread will sleep.
         */
        private void sleep (int duration) {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException ie) {
                System.err.println(ie);
                System.exit(3);
            }
        }

        private void gameOver () {
            Stage popup = new Stage();
            popup.initOwner(stage);
            popup.initModality(Modality.WINDOW_MODAL);
            popup.setAlwaysOnTop(true);
            popup.setMinWidth(TetrisBoard.this.getFitWidth() / 5);
            popup.setMinHeight(popup.getMinWidth() / 1.5);
            popup.setTitle("Game Over!");

            ImageView imv = new ImageView("file:resources/GameOver.png");
            imv.setPreserveRatio(true);

            Button exit = new Button("Exit Game");
            exit.setOnAction(ae -> System.exit(0));
            Button closeWindow = new Button("Close GameOver");
            closeWindow.setOnAction(ae -> popup.close());
            HBox buttonBox = new HBox(exit, closeWindow);
            buttonBox.setAlignment(javafx.geometry.Pos.CENTER);

            VBox vbox = new VBox();
            vbox.getChildren().addAll(imv, buttonBox);
            vbox.setAlignment(javafx.geometry.Pos.CENTER);

            Scene scene = new Scene(vbox);
            popup.setScene(scene);
            popup.showAndWait();
        }



    } //GamePlay

    /**
     * An object that represents the chunck of squares
     * falling on the {@code TetrisBoard}. Stores the spaces and color and enables the
     * perform the actual operations that enable the chucnk of squares to rotate and move.
     */
    class Piece {

        /* An array of the coordinates of the squares in the piece */
        private int[][] spaces;

        /* The color of the piece */
        private Color color;

        /* A list of the index (in spaces) which can be used as center of rotation */
        private List<Integer> pivot;

        /**
         * Creates a new Piece of a certain type.
         * @param type the type of piece that is created.
         */
        public Piece (PieceType type) {
            this.spaces = type.getSpaces();
            this.color = this.selectColor();
            this.pivot = new LinkedList<Integer>();
            for (int i = 0; i < type.getPivotNum(); i++) {
                this.pivot.add(i);
            }
        }

        /**
         * Creates a custom piece that cannot be rotated.
         * @param spaces an array of the coordinates of the squares in the piece
         * @param color the color of the Piece.
         */
        public Piece (int[][] spaces, Color color) {
            this.spaces = spaces;
            this.color = color;
            this.pivot = null;
        }

        /* Helper for constructors */

        /**
         * Selects a random color from the colorBank without repeating
         * until all colors have been used.
         */
        private Color selectColor () {
            if (colorBank.isEmpty()) {
                colorBank = usedColors;
                usedColors = new LinkedList<Color>();
            }
            Color select = colorBank.remove((int)(Math.random() * colorBank.size()));
            usedColors.add(select);
            return select;
        }

        /* Controling the piece */

        /**
         * Rotates the piece 90 degrees clockwise.
         * @param the number of rotations. Negative values rotate counter-clockwise.
         * @return true if the rotation is valid (successful); false otherwise.
         */
        public boolean rotate(int rotations) {
            int[][] oldSpaces = copy2D(this.spaces);
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
        /**
         * Moves the piece linearly (i.e. left, right, down, up, diagonaly)
         * @param x the amount the piece should move in the x direction.
         * @param y the amount the piece should move in the y direction.
         * @return true if the move is valid (successful); false otherwise.
         */
        public boolean move (int x, int y) {
            int[][] oldSpaces = copy2D(this.spaces);
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

        /* Helper methods for controling the pieces */

        /**
         * Checks if the spaces of the current piece is a valid location.
         * If the piece is out of bounds, on a taken space then the location is invalid.
         * @return true if location is valid; false otherwise.
         */
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

        /**
         * Helper method for rotate().
         * Performs actual rotation, but does not check if the spaces are valid.
         * @param the number of rotations. Negative values rotate counter-clockwise.
         * @return true if the rotation is successful; false if piece has no pivot.
         */
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

        /**
         * Helper method for move().
         * Performs actual move, but does not check if the move is valid.
         * @param x the amount the piece should move in the x direction.
         * @param y the amount the piece should move in the y direction.
         * @return true if the move is successful; false if no move occurs.
         */
        private boolean movePiece (int x, int y) {
            if (x == 0 && y == 0) {
                return false;
            }
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

        /* Getter methods */

        /**
         * Gets a copy of the array of spaces for this piece.
         * @return a copy of the array of spaces for this piece.
         */
        public int[][] getSpaces () {
            return copy2D(this.spaces);
        }

        /**
         * Gets the color of the piece.
         * @return the color of the piece
         */
        public Color getColor () {
            return this.color;
        }

    }


}
