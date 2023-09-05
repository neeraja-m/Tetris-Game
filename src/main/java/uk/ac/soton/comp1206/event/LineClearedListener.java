package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.Set;

public interface LineClearedListener {

    /**
     * Handle animation when line has been cleared
     * @param blockstoClear the set of blocks cleared to animate
     */
    void lineCleared(Set<GameBlockCoordinate> blockstoClear);

}
