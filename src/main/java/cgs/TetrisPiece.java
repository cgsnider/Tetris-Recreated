package cgs;

import java.awt.Color;

public class TetrisPiece {

    public enum Pieces {
        OPiece, SPiece, ZPiece, TPiece, JPiece, LPiece, IPiece;
    }

    public static final Color[] COLOR_BANK = new Color[] {
        Color.BLUE, Color.CYAN, Color.GREEN,
        Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.WHITE
    };

    private Pieces piece;
    private Color color;


    public TetrisPiece(Pieces piece) {
        this.piece = piece;
        this.color = COLOR_BANK[(int)(Math.random() * COLOR_BANK.length)];
    }


}
