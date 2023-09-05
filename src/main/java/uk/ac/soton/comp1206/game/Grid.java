package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {
    private static final Logger logger = LogManager.getLogger(Grid.class);
    private int offsetX=0;
    private int offsetY=0;



    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Check if a piece can be played
     * @param gamePiece piece to be placed
     * @param gridX x coordinate of position of block
     * @param gridY y coordinate of position of block
     * @return if piece can be played
     */
    public boolean canPlayPiece(GamePiece gamePiece,int gridX,int gridY)  {
        //Use center of a GamePiece in a 3x3 grid (1,1) to offset the block clicked on the grid
        offsetX=gridX-1;
        offsetY=gridY-1;
        logger.info("offset: ("+offsetX+", " + offsetY +")");

        int playableBlock=0;
        int totalBlocks=0;
        try {
            for (int x = 0; x < gamePiece.getBlocks().length; x++) {
                for (int y = 0; y < gamePiece.getBlocks()[x].length; y++) {
                    if (gamePiece.getBlocks()[x][y] != 0) {
                        totalBlocks++;

                        //If space on board is empty, then that block is playable
                        if (grid[x + offsetX][y + offsetY].get() == 0) {
                            playableBlock++;
                        } else {
                            Multimedia.playSound("fail.wav");
                            return false;
                            //throw new Exception("Piece cannot be played");
                            //Return false as soon as a GamePiece does not have a corresponding empty block on the grid

                        }
                    }
                }
            }
        }catch (ArrayIndexOutOfBoundsException e){
            logger.info("Cannot place block");
            Multimedia.playSound("fail.wav");
        }
        //Return true if the all blocks in the gamepiece are playable
        return totalBlocks == playableBlock;

    }

    /**
     * Place the game piece onto the board
     * @param gamePiece piece to be places
     * @param gridX x coordinate position of block
     * @param gridY y coordinate postion of block
     */
    public void playPiece(GamePiece gamePiece,int gridX,int gridY) {
        Multimedia.playSound("place.wav");

        //If game piece can be played, set the corresponding coordinates to the value of the game piece
        if (canPlayPiece(gamePiece,gridX,gridY)) {
            for (int x = 0; x < gamePiece.getBlocks().length; x++) {
                for (int y = 0; y < gamePiece.getBlocks()[x].length; y++) {
                    if (gamePiece.getBlocks()[x][y] != 0) {
                        grid[x+offsetX][y+offsetY].set(gamePiece.getValue());

                    }else {}
                }
            }
        }
    }


    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

}
