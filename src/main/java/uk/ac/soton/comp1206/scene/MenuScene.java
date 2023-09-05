package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

import java.util.Timer;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    private App app = new App();

    //tetrECS logo animation
    private ParallelTransition parallelTransition;


    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);
        mainPane.setPadding(new Insets(20,20,20,20));

        var vbox = new VBox();
        vbox.setAlignment(Pos.BOTTOM_CENTER);
        vbox.setSpacing(20);

        //add the tetrECS title logo
        var image= new ImageView(new Image(this.getClass().getResource("/images/TetrECS.png").toExternalForm()));
        image.setFitWidth(gameWindow.getWidth());
        image.setFitHeight(gameWindow.getHeight());
        image.setPreserveRatio(true);
        image.setX(20);
        image.setY(85);

        mainPane.getChildren().add(image);
        mainPane.setCenter(vbox);

        //Play the rotation and scaling animations at the same time
        parallelTransition = new ParallelTransition(rotate(image),diminish(image));
        parallelTransition.play();

        //create buttons
        var singlePlayerButton = new Button("Singleplayer");
        var multiPlayerButton = new Button("Multiplayer");
        var insButton = new Button("Instructions");
        var exitButton = new Button("Exit");
        var soundButton = new CheckBox("Sound");

        //Bind sound checkbox to game audio
        soundButton.selectedProperty().bindBidirectional(Multimedia.audioProperty());
        soundButton.setOnMouseClicked(event -> handleSound("menu.mp3"));
        soundButton.setSelected(true);

        Multimedia.playMusic("menu.mp3");


        //Add buttons to menu
        vbox.getChildren().add(singlePlayerButton);
        vbox.getChildren().add(multiPlayerButton);
        vbox.getChildren().add(insButton);
        vbox.getChildren().add(exitButton);
        mainPane.setBottom(soundButton);

        //Add style to buttons
        singlePlayerButton.getStyleClass().add("menuItem");
        multiPlayerButton.getStyleClass().add("menuItem");
        insButton.getStyleClass().add("menuItem");
        exitButton.getStyleClass().add("menuItem");
        soundButton.getStyleClass().add("soundbutton");

        //Listen to the button action to the startGame method in the menu
        singlePlayerButton.setOnAction(event -> chooseMode());
        //Listen to the button action to open instructions scene
        insButton.setOnAction(this::insPage);
        //Listen the button action to open multiplayer lobby
        multiPlayerButton.setOnAction(this::multiplayerPage);
        //Listen the button action to close the game
        exitButton.setOnAction(event -> app.shutdown());

    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        logger.info("Initialising menu");

    }

    /**
     * Select game mode
     */
    private void chooseMode(){
        Multimedia.playSound("pling.wav");
        var pane = new StackPane();
        pane.setMaxWidth(2*gameWindow.getWidth()/3);
        pane.setMaxHeight(gameWindow.getHeight()/2);

        scene = new Scene(pane,pane.getWidth(),pane.getWidth());
        pane.getStyleClass().add("mode-background");
        root.getChildren().add(pane);
        root.setAlignment(Pos.CENTER);

        //Add titles and labels
        var title = new Text("Pick your difficulty");
        title.getStyleClass().add("menu-item");


        var normal = new Button("Challenge Mode");
        normal.getStyleClass().add("menuItem");
        pane.setAlignment(Pos.TOP_CENTER);
        var specialMode = new Button("Special Mode");
        specialMode.getStyleClass().add("menuItem");
        pane.setAlignment(Pos.TOP_CENTER);

        var vbox = new VBox();
        vbox.getChildren().addAll(normal,specialMode);
        vbox.setSpacing(10);
        pane.getChildren().add(vbox);
        vbox.setAlignment(Pos.CENTER);

        //Add listener to choose the game mode
        normal.setOnAction(event -> startGame(event,normal.getText()));
        specialMode.setOnAction(event -> startGame(event,specialMode.getText()));

    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event,String mode) {
        //Stops the animation and starts the challenge scene
        parallelTransition.stop();
        gameWindow.startChallenge(mode);
    }

    /**
     * Handle when the Instruction Game button is pressed
     * @param event event
     */
    private void insPage(ActionEvent event){
        //Stops the animation and starts the instructions scene
        parallelTransition.stop();
        gameWindow.startInstructionScene();
    }

    /**
     * Handle when the Multiplayer button is pressed
     * @param event event
     */
    private void multiplayerPage(ActionEvent event){
        //Stop the animation and starts the multiplayer scene
        parallelTransition.stop();
        gameWindow.startMultiplayerScene();}

    /**
     * Animate rotating of logo
     * @param logo tetrECS logo
     * @return transition
     */
    private Transition rotate(ImageView logo){
        //Animation to rotate the logo
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(7000),logo);
        logo.setScaleX(0.8);
        logo.setScaleY(0.8);

        rotateTransition.setByAngle(360);

        //Loop the animation until stopped
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setAutoReverse(true);
        rotateTransition.play();
        
        return rotateTransition;

    }

    /**
     * Animate diminishing of logo
     * @param logo tetrECS logo
     * @return transition
     */

    private Transition diminish(ImageView logo){
        //Animation to diminish the logo
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(3500),logo);
        logo.setScaleX(0.8);
        logo.setScaleY(0.8);

        scaleTransition.setByX(-0.5);
        scaleTransition.setByY(-0.5);

        //Loop the animation until stopped
        scaleTransition.setCycleCount(Animation.INDEFINITE);
        scaleTransition.setAutoReverse(true);
        scaleTransition.play();

        return scaleTransition;


    }

}
