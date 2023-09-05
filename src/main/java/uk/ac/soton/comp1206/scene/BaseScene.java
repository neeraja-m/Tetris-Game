package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * A Base Scene used in the game. Handles common functionality between all scenes.
 */
public abstract class BaseScene {

    protected final GameWindow gameWindow;

    protected GamePane root;
    protected Scene scene;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     * @param gameWindow the game window
     */
    public BaseScene(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    /**
     * Initialise this scene. Called after creation
     */
    public abstract void initialise();

    /**
     * Build the layout of the scene
     */
    public abstract void build();

    /**
     * Create a new JavaFX scene using the root contained within this scene
     * @return JavaFX scene
     */
    public Scene setScene() {
        var previous = gameWindow.getScene();
        Scene scene = new Scene(root, previous.getWidth(), previous.getHeight(), Color.BLACK);
        scene.getStylesheets().add(getClass().getResource("/style/game.css").toExternalForm());
        this.scene = scene;
        return scene;
    }

    /**
     * Get the JavaFX scene contained inside
     * @return JavaFX scene
     */
    public Scene getScene() {
        return this.scene;
    }

    /**
     * Handle escape key being pressed to navigate back to menu
     * @param event the key pressed
     */
    protected void handleEscape(KeyEvent event) {
        if(event.getCode() != KeyCode.ESCAPE) return;
        gameWindow.startMenu();
        this.cleanUp();

    }

    protected void handleSound(String file){
        if (!Multimedia.audioProperty().get()){
        Multimedia.playMusic(null);}
        else{
            Multimedia.playMusic(file);
        }
    }

    /**
     * Used to fade the titles of scenes
     * @param toFade the text title to fade
     * @return
     */
    protected Transition fader(Text toFade){
        FadeTransition fadeTitle = new FadeTransition(new Duration(500),toFade);
        fadeTitle.setFromValue(0);
        fadeTitle.setToValue(1);
        fadeTitle.play();
        return fadeTitle;
    }

    protected void cleanUp(){

    }




}
