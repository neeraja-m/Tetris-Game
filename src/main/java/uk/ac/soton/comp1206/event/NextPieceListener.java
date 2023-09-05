package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

public interface NextPieceListener {
     /**
      * Handle when the game pieces have been updated
      * @param upComing current upcoming game piece
      * @param followingPiece next upcoming game piece
      */
     void nextPiece(GamePiece upComing, GamePiece followingPiece);

}
