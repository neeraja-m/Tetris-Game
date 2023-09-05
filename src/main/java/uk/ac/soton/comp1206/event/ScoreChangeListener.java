package uk.ac.soton.comp1206.event;

public interface ScoreChangeListener {
    /**
     * Handle when the game score has changed
     * @param score current score of the user
     */
    void scoreChanged(int score);
}
