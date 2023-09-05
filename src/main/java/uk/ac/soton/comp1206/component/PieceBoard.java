package uk.ac.soton.comp1206.component;

import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * Class responsible for displaying upcoming and current game piece
 */
public class PieceBoard extends GameBoard {
    private static final Logger logger = LogManager.getLogger(PieceBoard.class);


    /**
     * PieceBoard contructor
     * @param grid the grid containing rows and columns
     * @param width width of board
     * @param height height of board
     */
    public PieceBoard(Grid grid, double width, double height) {
        super(grid, width, height);
    }

    /**
     * Display the current piece to be placed
     * @param gamePiece the current piece
     */
    public void displayCurrentPiece(GamePiece gamePiece) {
        for (int x = 0; x < gamePiece.getBlocks().length; x++) {
            for (int y = 0; y < gamePiece.getBlocks()[x].length; y++) {
                grid.getGridProperty(x, y).setValue(0);
            }
        }
        //For each coordinate, if value of block is not 0, set the boards value to the value of the game block
        for (int x = 0; x < gamePiece.getBlocks().length; x++) {
            for (int y = 0; y < gamePiece.getBlocks()[x].length; y++) {
                if (gamePiece.getBlocks()[x][y] != 0) {
                    grid.getGridProperty(x, y).setValue(gamePiece.getValue());
                }
                //Add a circle to the centre of the piece board
                if(x==1 && y==1 ){
                    this.getBlock(x,y).addCircle();
                }
            }

        }
    }

    /**
     * Display the next piece to be placed
     * @param gamePiece game piece to be displayed
     */
    public void displayNextPiece(GamePiece gamePiece) {
        for (int x = 0; x < gamePiece.getBlocks().length; x++) {
            for (int y = 0; y < gamePiece.getBlocks()[x].length; y++) {
                grid.getGridProperty(x, y).setValue(0);
            }
        }
        //For each coordinate, if value of block is not 0, set the boards value to the value of the game block
        for (int x = 0; x < gamePiece.getBlocks().length; x++) {
            for (int y = 0; y < gamePiece.getBlocks()[x].length; y++) {
                if (gamePiece.getBlocks()[x][y] != 0) {
                    grid.getGridProperty(x, y).setValue(gamePiece.getValue());
                }
            }

        }
    }
}
