package uk.ac.soton.comp1206.utility;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

/**
 * This class handles the background music and sound effects of the game.
 */
public class Multimedia {

    private static final Logger logger = LogManager.getLogger(Multimedia.class);
    private static BooleanProperty audioProperty = new SimpleBooleanProperty(false);
    private static MediaPlayer playSound;
    private static MediaPlayer playMusic;


    /**
     * Play sound effects
     * @param file sound file to play
     */

    public static void playSound(String file) {
        if(audioProperty().get()){
        String toPlaySound = Multimedia.class.getResource("/sounds/" + file).toExternalForm();
        logger.info("Playing audio: " + toPlaySound);
        //Initialize the Media and MediaPlayer and play it
        try {
            Media sound = new Media(toPlaySound);
            playSound = new MediaPlayer(sound);
            playSound.play();
        } catch (Exception e) {
            audioProperty().setValue(false);
            e.printStackTrace();
            logger.error("Audio file error");
        }
        }else {

        }
    }

    /**
     * Play background music
     * @param file music file to play
     */
    public static void playMusic(String file) {
        if(audioProperty.get()) {
            //If music is already playing, stop playing it
            if (playMusic != null && playMusic.getStatus() == MediaPlayer.Status.PLAYING) {
                playMusic.stop();
            }
            //Initialize the Media and MediaPlayer and play it on loop
            String toPlayMusic = Multimedia.class.getResource("/music/" + file).toExternalForm();
            logger.info("Playing audio: " + toPlayMusic);
            try {
                Media music = new Media(toPlayMusic);
                playMusic = new MediaPlayer(music);
                playMusic.play();
                playMusic.setCycleCount(MediaPlayer.INDEFINITE);
            } catch (Exception e) {
                audioProperty().setValue(false);
                e.printStackTrace();
                logger.error("Audio file error");
            }
        }else{
            audioProperty().setValue(false);
            if (playMusic != null && playMusic.getStatus() == MediaPlayer.Status.PLAYING) {
                playMusic.stop();
            }
        }

    }

    /**
     * Setter method for audioProperty
     * @param set state
     */
    public void setAudioProperty(boolean set){
        audioProperty.set(set);
    }

    /**
     * Getter method for audioProperty
     * @return state
     */
    public boolean getAudioProperty(){
        return audioProperty.get();
    }

    /**
     * Expose audioProperty
     * @return audioProperty
     */
    public static BooleanProperty audioProperty(){
        return audioProperty;
    }

}