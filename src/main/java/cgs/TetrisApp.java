package cgs;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Creates an instance of the game tetris.
 */
public class TetrisApp extends Application {

    private VBox spine;

    /**
     * Starts the javafx Tetris application
     */
    @Override
    public void start(Stage stage) {

        this.spine = new VBox();

        Scene scene = new Scene(this.spine);

        stage.setTitle("Tetris");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();


    } //start

} //TetrisApp
