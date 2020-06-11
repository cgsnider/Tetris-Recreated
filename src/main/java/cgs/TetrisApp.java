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
import java.util.ArrayList;
import java.util.List;

/**
 * Creates an instance of the game tetris.
 */
public class TetrisApp extends Application implements EventHandler<KeyEvent> {

    private List<KeyCode> input;

    private VBox spine;
    private TetrisBoard gameBoard;


    /**
     * Starts the javafx Tetris application
     */
    @Override
    public void start(Stage stage) {
        this.input = new ArrayList<>();

        this.spine = new VBox();
        this.gameBoard = new TetrisBoard(this.input);

        this.spine.getChildren().addAll(gameBoard);

        Scene scene = new Scene(this.spine);
        scene.setOnKeyPressed(this::handle);


        stage.setTitle("Tetris");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();


    } //start

    @Override
    public void handle(KeyEvent e) {
        //System.out.println(e.getCode());
        this.input.add(e.getCode());
    }

} //TetrisApp
