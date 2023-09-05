package uk.ac.soton.comp1206.event;

import java.util.Timer;

public interface GameLoopListener {
    /**
     * Handle when a new game loop was starting
     * @param time duration of the next game loop
     * @param gameState if game has ended or not
     */
    void setTimer(Long time, boolean gameState);
}
