package cgs;

import cgs.customComponents.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import java.util.LinkedList;
import java.util.List;
import javafx.scene.layout.Priority;

/**
 * Creates an instance of the game tetris.
 */
public class TetrisApp extends Application implements EventHandler<KeyEvent> {

    private List<KeyCode> input;

    private VBox spine;
    private TetrisBoard gameBoard;


    /**
     * Starts the javafx Tetris application.
     * @param stage the stage of the application.
     */
    @Override
    public void start(Stage stage) {
        this.input = new LinkedList<>();

        this.spine = new VBox();

        this.gameBoard = new TetrisBoard(this.input, stage);
        this.gameBoard.fitWidthProperty().bind(this.spine.widthProperty());
        this.gameBoard.fitHeightProperty().bind(this.spine.heightProperty());
        this.gameBoard.setPreserveRatio(true);

        this.spine.getChildren().addAll(gameBoard);



        Scene scene = new Scene(this.spine);
        scene.setOnKeyPressed(this::handle);


        stage.setTitle("Tetris");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();


    } //start

    /**
     * Handles KeyEvents by logging them in a list.
     */
    @Override
    public void handle(KeyEvent e) {
        //System.out.println(e.getCode());
        this.input.add(e.getCode());
    }

} //TetrisApp
