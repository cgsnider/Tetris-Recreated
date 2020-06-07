
package cgs;

import java.awt.Color;
import java.awt.Point;
import org.ejml.simple.SimpleMatrix;
import java.util.ArrayList;
import java.util.Arrays;

public class TetrisPiece {

    public enum Pieces {
        OPIECE, SPIECE, ZPIECE, TPIECE, JPIECE, LPIECE, IPIECE;
    }

    public ArrayList<Color> colorBank = new ArrayList<> (Arrays.asList(
        new Color[] { Color.BLUE, Color.CYAN, Color.GREEN,
        Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.WHITE
        }
        ));

    public ArrayList<Color> usedColors = new ArrayList<>();

    public static double CLOCKWISE = Math.PI * 0.5;
    public static double COUNTERCLOCKWISE = Math.PI * -0.5;

    private Color color;
    private SimpleMatrix spaces;
    private int[] loc;

    public TetrisPiece(Pieces piece) {
        //this.color = COLOR_BANK.get(int)(Math.random() * COLOR_BANK.length));
        this.color = this.selectColor();
        this.spaces = new SimpleMatrix(findSpaces(piece));
        this.loc = new int[] {TetrisBoard.getWidth() / 2, 0};
        //System.out.println(spaces);
        //System.out.printf("X-coordinate: %d Y-coordinate: %d\n",loc[0], loc[1]);
        //System.out.println(piece);
    }

    public TetrisPiece(Pieces piece, int[] loc) {
        this.color = this.selectColor();
        this.spaces = new SimpleMatrix(findSpaces(piece));
        this.loc = loc;
    }

    private Color selectColor() {
        if (this.colorBank.isEmpty()) {
            this.colorBank = this.usedColors;
            this.usedColors = new ArrayList<>();
            this.usedColors.add(this.colorBank.remove(this.colorBank.size() - 1));
        }
        Color select =  colorBank.remove((int)(Math.random() * colorBank.size()));
        usedColors.add(select);
        return select;
    }


    public void rotatePiece(int rotations) {
        double radians = rotations * Math.PI / 2;
        SimpleMatrix rotation = new SimpleMatrix(new double[][] {
            {Math.cos(radians), Math.sin(radians)}, {-1 * Math.sin(radians), Math.cos(radians)}});
        System.out.println(rotation + " " + this.spaces);
        this.spaces = this.spaces.mult(rotation);
    }

    public Color getColor() {
        return this.color;
    }

    public boolean movePiece(int[] vector) {
        if (vector.length != 2) {
            return false;
        }
        this.loc[0] += vector[0];
        this.loc[1] += vector[1];
        return true;
    }

    public int[][] getSpaces() {
        int[][] mappedSpaces = new int[this.spaces.numRows()][this.spaces.numCols()];
            for (int row = 0; row < mappedSpaces.length; row++) {
                for (int col = 0; col < mappedSpaces[row].length; col++) {
                    mappedSpaces[row][col] = (int)Math.round(this.spaces.get(row, col));
                    switch (col) {
                    case 0: mappedSpaces[row][col] += this.loc[0];
                        break;
                    case 1: mappedSpaces[row][col] += this.loc[1];
                        break;
                    }
                    //System.out.print(mappedSpaces[row][col] + ", ");
                }
                //System.out.println();
            }
            return mappedSpaces;
    }

    private static double[][] findSpaces(Pieces piece) {
        switch (piece) {
        case OPIECE: return new double[][] {
                {0,0},{0,1},{1,0},{1,0}
            };
        case SPIECE: return new double[][] {
                {0,1},{1,1},{1,0},{2,0}
            };
        case ZPIECE: return new double[][] {
                {0,0},{1,0},{1,1},{2,1}
            };
        case TPIECE: return new double[][] {
                {0,1},{1,0},{1,1},{2,1}
            };
        case JPIECE: return new double[][] {
                {0,0},{0,1},{1,1},{2,1}
            };
        case LPIECE: return new double[][] {
                {0,1},{1,1},{2,0},{2,1}
            };
        case IPIECE: return new double[][] {
                {0,0},{1,0},{2,0},{3,0}
            };
        }
        return null;
    }

}
