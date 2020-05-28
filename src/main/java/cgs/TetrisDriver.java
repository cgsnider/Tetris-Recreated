package cgs;

import javafx.application.Application;

/**
 * Driver for the {@code TetrisApp} class.
 */
public class TetrisDriver {

    /**
     * Main entry-point into the application
     * @param args the comand-line arguments.
     */
    public static void main(String[] args) {

        try {
            Application.launch(TetrisApp.class, args);
        } catch(UnsupportedOperationException e) {
            System.err.print(e);
            System.err.println("Rending Graphics Error");
            System.exit(1);
        } //try


    }//main

} //TetrisDriver
