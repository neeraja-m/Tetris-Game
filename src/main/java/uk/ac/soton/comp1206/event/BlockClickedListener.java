package uk.ac.soton.comp1206.event;

import javafx.scene.input.MouseEvent;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;

/**
 * The Block Clicked listener is used to handle the event when a block in a GameBoard is clicked. It passes the
 * GameBlock that was clicked in the message
 */
public interface BlockClickedListener {

    /**
     * Handle a block clicked event
     * @param block the block that was clicked
     * @param event if it was right click or left click
     * @param board board that was clicked on
     */
    void blockClicked(MouseEvent event, GameBlock block, GameBoard board);

}
