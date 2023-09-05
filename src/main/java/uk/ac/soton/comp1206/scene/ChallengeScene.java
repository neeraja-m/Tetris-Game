package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {


    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
    protected Game game;
    protected PieceBoard incomingpieceBoard;
    protected PieceBoard nextincomingpieceBoard;
    protected Rectangle rec;
    private long timerWidth=760;
    protected Timeline timeline = new Timeline();
    private Text currHighScore;
    private Integer highScore =0;
    private ArrayList<Integer> scores;
    private String mode;


    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow,String mode) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
        this.mode = mode;
        logger.info("{} mode selected", mode);
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());


        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
        timeline = new Timeline();

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);
        mainPane.setPadding(new Insets(20,20,20,20));

        //Add the gameboard with the relevant grid and dimensions
        var board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);

        //Handle block on gameboard grid being clicked
        board.setOnBlockClick((MouseEvent event, GameBlock gameBlock,GameBoard gameBoard) -> {
            blockClicked(event,gameBlock,gameBoard);

        });

        //Handle whenever the next piece has been set to display the updated piece
        game.setNextPieceListener((GamePiece upComing, GamePiece followingPiece)-> {
            nextPiece(upComing,followingPiece);
        });


        var vboxleft = new VBox();
        var vboxleft1 = new VBox();
        var vboxright = new VBox();
        var vboxright1 = new VBox();
        var vboxcenter = new VBox();
        var hbox = new HBox();
        var hbox2 = new HBox();

        //Add a rectangle to represent the timer with an initial width
        rec= new Rectangle(10,0,timerWidth,20);
        rec.setFill(Color.rgb(20,220,0,1));
        hbox.getChildren().add(rec);

        //Add an arrow showing current and upcoming piece can be swapped
        var image= new ImageView(new Image(this.getClass().getResource("/images/arrow.png").toExternalForm()));
        image.setX(300);
        image.setY(1000);
        image.setFitWidth(gameWindow.getWidth()*0.5/5);
        image.setFitHeight(gameWindow.getHeight()*0.5/5);
        image.setPreserveRatio(true);

        //Add relevant text components for score, highscore, multiplier, lives, level, sound
        var score = new Text("Score");
        var lives = new Text("Lives");
        var highscore = new Text("High Score");
        var incoming = new Text("Incoming");
        var level = new Text("Level");
        var multiplier = new Text("Multiplier");
        var modeType= new Text(mode);
        var currScore = new Text();
        var currLives = new Text();
        var currLevel = new Text();
        var currMultiplier = new Text();
        var soundButton = new CheckBox("Sound");
        var regeneratePiecesButton = new Text("Regenerate pieces");
        var currRegenerate = new Text();


        //Set high score as current high score read from text file
        currHighScore= new Text(findHighscore().toString());

        //Bind the corresponding text properties to the users score, lives, multiplier, level,sound
        currScore.textProperty().bind(game.scoreProperty().asString());
        currLives.textProperty().bind(game.livesProperty().asString());
        currLevel.textProperty().bind(game.levelProperty().asString());
        currMultiplier.textProperty().bind(game.multiplierProperty().asString());
        soundButton.selectedProperty().bindBidirectional(Multimedia.audioProperty());
        currRegenerate.textProperty().bind(game.regeneratedProperty().asString());

        mainPane.setRight(vboxright1);
        mainPane.setLeft(vboxleft);
        mainPane.setCenter(vboxcenter);
        mainPane.setBottom(hbox);
        vboxright1.getChildren().add(vboxright);

        //Add components
        vboxleft.getChildren().addAll(score,currScore,multiplier,currMultiplier,highscore,currHighScore,vboxleft1);
        vboxright.getChildren().addAll(lives,currLives,level,currLevel);
        vboxcenter.getChildren().addAll(modeType,board);
        vboxright1.getChildren().add(incoming);
        vboxleft1.getChildren().addAll(regeneratePiecesButton,currRegenerate,hbox2);
        hbox2.getChildren().add(soundButton);
        //Disable using space bar to toggle sound checkbox
        soundButton.addEventFilter(KeyEvent.KEY_PRESSED,keyEvent ->{
            if(KeyCode.SPACE == keyEvent.getCode()){
                keyEvent.consume();
                keySupport(keyEvent);
            }
        });
        regeneratePiecesButton.setVisible(false);
        currRegenerate.setVisible(false);


        //Set alignments
        vboxleft.setAlignment(Pos.TOP_CENTER);
        vboxleft1.setAlignment(Pos.BOTTOM_CENTER);
        vboxright.setAlignment(Pos.TOP_CENTER);
        vboxcenter.setAlignment(Pos. TOP_CENTER);
        vboxright1.setAlignment(Pos.TOP_CENTER);
        hbox2.setAlignment(Pos.BOTTOM_CENTER);

        //Set spacing
        vboxleft.setSpacing(10);
        vboxleft1.setPadding(new Insets(50,0,0,0));
        vboxleft1.setSpacing(20);
        vboxright.setSpacing(5);
        vboxright1.setSpacing(10);
        vboxcenter.setSpacing(70);
        hbox2.setPadding(new Insets(65,0,0,0));

        //Add styles to displayed text components
        score.getStyleClass().add("challenge-labels");
        lives.getStyleClass().add("challenge-labels");
        level.getStyleClass().add("challenge-labels");
        incoming.getStyleClass().add("challenge-labels");
        highscore.getStyleClass().add("challenge-labels");
        modeType.getStyleClass().add("challenge-labels");
        multiplier.getStyleClass().add("challenge-labels");
        currScore.getStyleClass().add("myscore");
        currLevel.getStyleClass().add("myscore");
        currLives.getStyleClass().add("myscore");
        currMultiplier.getStyleClass().add("myscore");
        currHighScore.getStyleClass().add("myscore");
        soundButton.getStyleClass().add("soundbutton");
        regeneratePiecesButton.getStyleClass().add("newgamebutton");
        currRegenerate.getStyleClass().add("challenge-labels");

        //Add listener to regenerate button if in special mode
        if(mode.equals("Special Mode")){
            regeneratePiecesButton.setOnMouseClicked(event -> game.getNewPiece());
            regeneratePiecesButton.setVisible(true);
            currRegenerate.setVisible(true);}

        //Add listener to sound button
        soundButton.setOnMouseClicked(event -> handleSound("game.wav"));


        //Play the music for the game
        Multimedia.playMusic("game.wav");

        //Create a pieceboard representing the incoming game piece and pass in the grid and dimensions
        incomingpieceBoard = new PieceBoard(new Grid(3,3),gameWindow.getWidth()/8,gameWindow.getWidth()/8);
        incomingpieceBoard.displayCurrentPiece(game.getCurrentPiece());

        vboxright1.getChildren().add(incomingpieceBoard);
        vboxright1.getChildren().add(image);

        //Create a slightly smaller pieceboard representing the next incoming game piece and pass in the grid and dimensions
        nextincomingpieceBoard = new PieceBoard(new Grid(3,3),gameWindow.getWidth()/9,gameWindow.getWidth()/9);
        nextincomingpieceBoard.displayNextPiece(game.getFollowingPiece());

        vboxright1.getChildren().add(nextincomingpieceBoard);

        //Notify when a block on incoming pieceboard has been clicked
        incomingpieceBoard.setOnBlockClick((MouseEvent event, GameBlock gameBlock,GameBoard gameBoard) -> {
            blockClicked(event, gameBlock,gameBoard);
        });

        //Notified when a block on the next incoming pieceboard has been clicked
        //Set state to true to distinguish between which pieceboard has been clicked
        nextincomingpieceBoard.setOnBlockClick((MouseEvent event, GameBlock gameBlock,GameBoard gameBoard) -> {
            game.setState(true);
            blockClicked(event, gameBlock,gameBoard);
        });

        //Notified when a line has been cleared to call the fadeOut method in GameBoard
        game.setLinesClearedListener((Set<GameBlockCoordinate> blockstoClear)-> {
            board.fadeOut(blockstoClear);

        });

        //Notified when a new gameloop is starting with the time span and whether the game has ended
        game.setGameLoopListener((Long time,boolean state)->{
                handleTime(time,state);
        });

        //Notified when the initial highscore has been reached
        game.setScoreChangeListener((int changedScore)->{
            getHighScore(changedScore);
        });

    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     * @param event to distinguish if it was a right click or left click
     * @param gameBoard the game board that was clicked
     */
    private void blockClicked(MouseEvent event, GameBlock gameBlock,GameBoard gameBoard) {
        //Handle the block being clicked
        game.blockClicked(event, gameBlock,gameBoard);
        //make sure correct gamepieces are being displayed in the pieceboards
        nextPiece(game.getCurrentPiece(),game.getFollowingPiece());
    }

    /**
     * To update the current and upcoming game pieces
     * @param upComing the current upcoming piece
     * @param followingPiece the following upcoming piece
     */
    private void nextPiece(GamePiece upComing,GamePiece followingPiece) {
        incomingpieceBoard.displayCurrentPiece(upComing);
        nextincomingpieceBoard.displayNextPiece(followingPiece);
    }

    /**
     * Handles keyboard presses
     * @param keyEvent
     */
    private void keySupport(KeyEvent keyEvent){
        //Escape key navigates to menu scene
        //Game handles other key presses
        if(keyEvent.getCode()== KeyCode.ESCAPE){
            handleEscape(keyEvent);
            game.getTimer().cancel();
        }else {
            game.keyboardSupport(keyEvent);
            nextPiece(game.getCurrentPiece(), game.getFollowingPiece());
        }
    }

    /**
     * Handle the rectangle representing the timer to change colour and its duration
     * @param timetoPlay the duration of the timer
     * @param ifEnd if the game has ended
     */
    private void handleTime (Long timetoPlay,boolean ifEnd){
        //Stop the previously playing timeline
        timeline.stop();
        //Set the rectangles width to the initial width each time the timer is reset
        rec.setWidth(timerWidth);
        logger.info("playing timeline");
        //During the passed in time to play, set the rectangle from initial width to 0
        timeline.getKeyFrames().addAll(
                new KeyFrame(new Duration(timetoPlay), new KeyValue(rec.widthProperty(), 0, Interpolator.EASE_BOTH)),
                //Start with rectangle colour as green
                new KeyFrame(Duration.millis(1), e -> rec.setFill(Color.GREEN.brighter())),
                //Change the rectangle colour to yellow after a third of the time
                new KeyFrame(Duration.millis(timetoPlay / 3), e -> rec.setFill(Color.YELLOW)),
                //Change the rectangle colour to red after two third's of the time
                new KeyFrame(Duration.millis((2 * timetoPlay / 3)), e -> rec.setFill(Color.RED)));
        timeline.play();
        //If the game ended, stop the timeline
        if (ifEnd) {
            timeline.stop();
            logger.info("stopping timeline");
            endGame();
        }

    }


    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5,mode);
    }

    /**
     * Handle when the game ended and pass this game and start the score scene
     * Clean up the game to reset relevant components
     */
    public void endGame(){
        logger.info("ending the game");
        Platform.runLater(()-> {
            gameWindow.passGameState(game);
            gameWindow.startScoreScene();

        });
        cleanUp();
    }

    /**
     * Compares current and initial high score to update the display component
     * @param userScore the users current score
     * @return the current high score
     */
    public int getHighScore(int userScore){

        if(userScore>highScore){
            currHighScore.textProperty().bind(game.scoreProperty().asString());
        }else{
            currHighScore.setText(highScore.toString());
        }
        return highScore;

    }

    /**
     * Finds the initial high score
     * @return the initial high score
     */
    protected Integer findHighscore(){
        logger.info("finding highscore");
        //Create an arraylist to hold all scores
        //ArrayList<Integer> scoresscores = new ArrayList<>();
        scores= new ArrayList<>();

        //Use a buffered reader to read from scores.txt file to find initial high score
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("scores.txt"));
            String line= bufferedReader.readLine();
            while(line!=null){
                try {
                    String info[] = line.split(":");
                    Integer score = Integer.parseInt(info[1]);
                    scores.add(score);

                } catch (Exception e) {
                    System.out.println(e);
                }
                line= bufferedReader.readLine();
            }
            bufferedReader.close();
            Collections.sort(scores,Collections.reverseOrder());
            //Set the first item after sorting in descending order as the high score
            highScore = scores.get(0);
        } catch (Exception e) {
            //If scores file does not currently exist, create a new file and set inital high score to 0
            try{
            FileWriter writtenScores = new FileWriter("scores.txt");}catch (Exception ex){
                System.out.println(ex);}
            System.out.println(e);
            highScore=0;
        }
        logger.info("Highscore set to: "+highScore);
        return highScore;
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");

        game.start();

        //Keyboard support
        scene.setOnKeyPressed(keyEvent -> keySupport(keyEvent));
    }

    /**
     * Clean up the game
     */
    public void cleanUp(){
        logger.info("End of game");
        scores.clear();
        highScore=0;
        game.setIfStart(true);
    }
}
