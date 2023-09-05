package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.event.ScoreChangeListener;
import uk.ac.soton.comp1206.utility.Multimedia;

import java.lang.reflect.Array;
import java.util.*;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    //Create a random number generator starting at 0
    Random ran = new Random(0);

    //Create integer properties for score,level,lives,mulitplier
    private IntegerProperty scoreProperty = new SimpleIntegerProperty(0);
    private IntegerProperty levelProperty = new SimpleIntegerProperty(0);
    private IntegerProperty livesProperty = new SimpleIntegerProperty(3);
    private IntegerProperty multiplier = new SimpleIntegerProperty(1);
    private IntegerProperty regenerated = new SimpleIntegerProperty(3);

    //Declare listeners
    LineClearedListener linesClearedlistener;
    ScoreChangeListener scoreChangeListener;
    protected GameLoopListener gameLoopListener;
    public boolean nextPieceClicked;
    NextPieceListener nextPieceListener;

    //Initialize number of lines cleared and number of blocks cleared
    int filled=0;
    int noOflines=0;

    //Arraylist of temporary blocks to clear
    ArrayList<GameBlockCoordinate> tempList = new ArrayList<>();

    //Set containing blocks to clear
    Set<GameBlockCoordinate> toClear= new HashSet<>();
    private long delayTime;

    //Property for duration of each game loop
    private DoubleProperty timerProperty = new SimpleDoubleProperty(12000);

    //Holds current gamepiece to place
    private GamePiece currentPiece;

    //Holds upcoming gamepiece to place
    private GamePiece followingPiece;

    //Keeps track of if game has ended
    private boolean state;

    //Keeps track of if a piece was placed in the previous gameloop
    private boolean ifPlaced;

    //Keeps track of if it is the first gameloop
    private boolean start = true;

    //Keeps track of if timer is running
    private boolean timerisRunning;
    private TimerTask timerTask;
    private Timer timer;

    //Which game mode it is
    private String gameMode;

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows,String mode) {
        this.cols = cols;
        this.rows = rows;
        this.gameMode = mode;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);

        //Reset special mode feature
        regeneratedProperty().set(3);

        //Set number of lives to 1 in special mode
        if(mode.equals("Special Mode")){
            livesProperty().set(1);
        }

        //Set the first two pieces
        spawnPiece();
        followingPiece = GamePiece.createPiece(1);
    }

    /**
     * Handles checking of lives and state of the game at the end of the previous loop
     */
    public void gameLoop(){
        logger.info("entered gameloop");
        logger.info("lives: {}", getLivesProperty());
        logger.info("if placed: {}",ifPlaced);

        //If no lives left and no piece was placed in the previous loop, end the game
        if(getLivesProperty()==0 &&!ifPlaced){
            logger.info("no lives left - ending game");
            //Set true that game has ended
            state=true;
            //Cancel timer and task
            timer.cancel();
            timerTask.cancel();
            //Notify ChallengeScene
            gameLoopListener.setTimer(getTimerDelay(),state);
        }
        //If there are remaining lives and no piece was placed and it is not the first loop of the game
        else if((getLivesProperty()>0) && !ifPlaced && !start) {
            logger.info("No block placed");
            Multimedia.playSound("lifelose.wav");
            //Lose a life
            setLivesProperty(getLivesProperty()-1);
            //Reset multiplier to 1
            setMultiplierProperty(1);
            //Replace the upcoming gamepieces
            nextPiece();
            //Notify listeners to display the updated pieces
            nextPieceListener.nextPiece(getCurrentPiece(),getFollowingPiece());
            //Reset the timer
            handleTimer();
            //Notify ChallengeScene that a new loop is starting
            gameLoopListener.setTimer(getTimerDelay(),state);}
        else {
            //Set after first loop of game has finished
            setIfStart(false);
            //Reset timer
            handleTimer();
            //Notify ChallengeScene that a new loop is starting
            gameLoopListener.setTimer(getTimerDelay(),state);
        }
    }

    /**
     * Regenerate the upcoming game piece in Special Mode
     */
    public void getNewPiece(){
        logger.info("Switching pieces");
        //If player has regenerations left, then regenerate
        if(regeneratedProperty().get()>0){
            nextPiece();
            nextPieceListener.nextPiece(getCurrentPiece(),getFollowingPiece());
            regenerated.set(regeneratedProperty().get()-1);
        }else{
            Multimedia.playSound("fail.wav");
            logger.info("No more switches left");
        }
    }

    /**
     * Handles cancelling and initializing Timer and TimerTask at the end of each game loop
     */
    private void handleTimer(){
        //If Timer is running then cancel it
        if (timerisRunning) {
            timer.cancel();
            timerTask.cancel();
            logger.info("timer {} cancelled",timer.toString());
            timerisRunning=false;
        }//Initialize a new Timer and TimerTask
            timer = new Timer();
            logger.info("new timer {} initialised",timer.toString());
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    gameLoop();
                }
            };
            logger.info("new timer task {} initialised",timerTask.toString());
            //Update timers state
            timerisRunning=true;
            //Schedule a new task
            timer.schedule(timerTask,getTimerDelay());
            logger.info("timer scheduled task");

    }

    /**
     * Handle keyboard support
     * @param keyEvent the key pressed
     */
    public void keyboardSupport(KeyEvent keyEvent){
        if (keyEvent.getCode() == KeyCode.Q || keyEvent.getCode()==KeyCode.OPEN_BRACKET || keyEvent.getCode() == KeyCode.Z){
            logger.info("Key pressed: {} --> Rotating current piece anticlockwise",keyEvent.getCode() );
            currentPiece.rotate(3);
        }else if(keyEvent.getCode() == KeyCode.E || keyEvent.getCode() == KeyCode.C|| keyEvent.getCode() == KeyCode.CLOSE_BRACKET){
            logger.info("Key pressed: {} --> Rotating current piece clockwise",keyEvent.getCode() );
            currentPiece.rotate(1);
        }else if(keyEvent.getCode() == KeyCode.SPACE || keyEvent.getCode() == KeyCode.R) {
            logger.info("Key pressed: {} --> Swapping current and upcoming piece",keyEvent.getCode() );
            swapCurrentPiece();
        }
    }

    /**
     * Set game loop listener
     * @param listener the listener listening to start of game loop
     */
    public void setGameLoopListener(GameLoopListener listener){
        gameLoopListener=listener;
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
        state=false;
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        gameLoop();
        gameLoopListener.setTimer(getTimerDelay(),state);

    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     * @param event if it was right click or left click
     * @param gameBoard the game board that was clicked
     */
    public void blockClicked(MouseEvent event, GameBlock gameBlock, GameBoard gameBoard) {
        //If it was a left click the main game board was clicked, continue
        if(event.getButton().equals(MouseButton.PRIMARY) &&(gameBoard.getClass().equals(GameBoard.class))) {
            logger.info("Blocked Clicked");
            //Get the position of this block
            int x = gameBlock.getX();
            int y = gameBlock.getY();

            //If the piece can be played, place the piece and set that a piece was placed
            //Get the current and next game piece
            //Call the game loop
            //Notify the nextpiecelistener with updated game pieces
            //Check if any lines have to be cleared

            try {
                if (grid.canPlayPiece(currentPiece, x, y)) {
                    grid.playPiece(currentPiece, x, y);
                    ifPlaced=true;
                    nextPiece();
                    gameLoop();
                    ifPlaced=false;
                    nextPieceListener.nextPiece(currentPiece, followingPiece);
                    afterPiece();
                }else{
                    //If a piece cannot be played, set that the piece was not placed
                    ifPlaced=false;}
            } catch (Exception e) {
                logger.info(e);
            }
        }
        //If it was a right click and main game board was clicked, rotate the current piece
        else if(event.getButton().equals(MouseButton.SECONDARY) &&(gameBoard.getClass().equals(GameBoard.class))){
            rotateCurrentPiece(gameBlock);
        }
        //If it was a left click and the incoming pieceboard was clicked, rotate the current piece
        else if(event.getButton().equals(MouseButton.PRIMARY) &&(gameBoard.getClass().equals(PieceBoard.class)&&(!nextPieceClicked))){
            rotateCurrentPiece(gameBlock);
        //If it was a left click and the next incoming pieceboard was clicked, swao the current and following piece
        }else if(event.getButton().equals(MouseButton.PRIMARY) &&(gameBoard.getClass().equals(PieceBoard.class)&&(nextPieceClicked))){
            swapCurrentPiece();
            //reset state of if next incoming pieceboard was clicked back to false
            setState(false);

        }

    }

    /**
     * Return an initial game piece
     * @return the game piece created
     */
    public GamePiece spawnPiece(){
        logger.info("Entered spawnPiece method");

        int pieceNumber = ran.nextInt(15);
        currentPiece = GamePiece.createPiece(pieceNumber);

        return currentPiece ;
    }

    /**
     * Updates the current piece to the next piece, and creates a new following piece
     * @return
     */
    public GamePiece nextPiece(){
        logger.info("Entered nextPiece method");
        int pieceNumber = ran.nextInt(15);

        logger.info("Created new piece: "+GamePiece.createPiece(pieceNumber).toString());
        currentPiece = followingPiece;
        followingPiece = GamePiece.createPiece(pieceNumber);

        return followingPiece;
    }

    /**
     * Handles checking if any lines need to be cleared
     */
    public void afterPiece(){
        //Initialize starting point coordinates
        int initialX =0;
        int initialY=0;

        //Traverse each block in a row and if their value is not 0, add to a temporary list
        //Once it reaches a block in a row that is 0, break the loop and check the next row
        for(int i=0; i<grid.getRows(); i++) {
            logger.info("Checking blocks across rows X: "+initialX +" ,Y: "+initialY);

            while (grid.get(initialX, initialY) != 0 && initialX < grid.getCols()) {
                tempList.add(new GameBlockCoordinate(initialX, initialY));
                logger.info("Added coordinate: "+ initialX +", " +initialY+" to templist");

                //Increment to move to next block in row
                initialX++;
                //Increment to keep track of how many blocks in a row had was filled
                filled++;
            }
            //Handles clearing of temporary list
            handleListClear();
            initialX=0;

            //Increment y coordinate to move to the next row
            initialY++;

        }

        //Initialize starting point coordinates
        initialX =0;
        initialY=0;

        //Traverse each block in a row and if their value is not 0, add to a temporary list
        //Once it reaches a block in a row that is 0, break the loop and check the next row
        for(int i=0; i<grid.getCols(); i++) {
            logger.info("Checking blocks down columns X: "+initialX +" ,Y: "+initialY);
            while (grid.get(initialX, initialY) != 0 && initialY < grid.getRows()) {
                tempList.add(new GameBlockCoordinate(initialX, initialY));
                logger.info("Added coordinate: "+ initialX +", " +initialY+" to templist");

                //Increment to move to next block in column
                initialY++;
                //Increment to keep track of how many blocks in a row had was filled
                filled++;
            }
            //Handles clearing of temporary list
            handleListClear();
            initialY=0;

            //Increment x coordinate to move to the next column
            initialX++;

        }
        //If the blocks to clear are not empty, clear the blocks and play the relevant sound
        if(!toClear.isEmpty()) {
            Multimedia.playSound("clear.wav");

            for (GameBlockCoordinate block : toClear) {
                grid.set(block.getX(), block.getY(), 0);
                logger.info("cleared " + block.getX() + ", "+block.getY()+ " block");
            }

            //trigger listener when lines cleared
            if(linesClearedlistener!=null){
                linesClearedlistener.lineCleared(getToClear());
            }
        }
        int noOfBlocks = toClear.size();
        logger.info("number of lines "+noOflines);

        //Pass number of blocks and lines cleared to calculate score
        score(noOflines,noOfBlocks);

        //Reset blocks and lines cleared
        toClear.clear();
        noOflines=0;
    }

    /**
     * Handles clearing and resetting of lists
     */
    public void handleListClear(){
        //If the number of blocks in a row or column filled is 5, the whole row/column has been filled
        //Add these blocks to the set containing blocks to clear

        if (filled == 5) {
            toClear.addAll(tempList);
            for(GameBlockCoordinate gb: toClear){
                logger.info("Added: "+ gb.getX()+", " +gb.getY()+ "to toClear");}

            //Increment the number of lines cleared
            noOflines++;
            tempList.clear();
        } else {
            for(GameBlockCoordinate gb: tempList){
                logger.info("Removed: "+ gb.getX()+", " +gb.getY()+ " from tempList");}

            //Reset the temporary list
            tempList.clear();
        }
        //Reset number of blocks filled
        filled=0;
    }

    /**
     * Handles updating of scores
     * @param noOflines number of lines cleared
     * @param noOfblocks number of blocks cleared
     */
    public void score(int noOflines, int noOfblocks){
        //If no lines were cleared in a game loop, reset the multiplier to 1
        if(noOflines==0){
            setMultiplierProperty(1);
        }else {
            int toAdd = noOflines * noOfblocks * 10 * getMultiplierProperty();
            int currScore = getScoreProperty();
            int newScore = toAdd + currScore;
            //Set score as the updated score
            setScoreProperty(newScore);
            //Notify listener that score has changed
            if(scoreChangeListener!=null){
            scoreChangeListener.scoreChanged(getScoreProperty());}
            //Increment multiplier
            setMultiplierProperty(getMultiplierProperty()+1);
            //In Special Mode, when multiplier reaches, 4 gain a life
            if(gameMode.equals("Special Mode")){
                if(getMultiplierProperty()==4){
                    setLivesProperty(getLivesProperty()+1);
                    Multimedia.playSound("lifegain.wav");
                }
            }
            //Check if level needs to be updated
            updateLevel(newScore);
        }
    }

    /**
     * Handles updating of level
     * @param score the current score of user
     */
    public void updateLevel(int score){
        //Initialize the new level
        int toupdatedlevel=0;
        if(score>0) {
            for (int i = 1; i <= score; i++) {
                //If score is a multiple of 1000, increment the level
                if (i % 1000 == 0) {
                    toupdatedlevel++;

                    //If the updated level is higher than current level, play the sound
                    if(getLevelProperty()<toupdatedlevel){
                    Multimedia.playSound("level.wav");}
                }
            }
            //Update level
            setLevelProperty(toupdatedlevel);

        }

    }

    /**
     * Set next piece listener
     * @param listener the component listening
     */
    public void setNextPieceListener(NextPieceListener listener){
    this.nextPieceListener = listener;
    }

    /**
     * Set lines cleared listener
     * @param listener the component listening
     */
    public void setLinesClearedListener(LineClearedListener listener){
        this.linesClearedlistener = listener;
    }

    /**
     * Rotates the current piece
     * @param block game block
     */
    public void rotateCurrentPiece(GameBlock block){
        logger.info("rotating current piece");

        currentPiece.rotate();

        Multimedia.playSound("rotate.wav");
    }

    /**
     * Swap the current and following pieces
     */
    public void swapCurrentPiece(){
        logger.info("swapping pieces");
        GamePiece tempCurrent = currentPiece;

        currentPiece= followingPiece;
        followingPiece=tempCurrent;
        Multimedia.playSound("rotate.wav");

    }

    /**
     * Return the duration of next game loop
     * @return time of game loop
     */
    public long getTimerDelay(){
        delayTime= (12000-(500*getLevelProperty()));

        if (delayTime < 2500){
            delayTime=2500;}

        return delayTime;

    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Methods to expose properties
     */
    public IntegerProperty scoreProperty(){
        return scoreProperty;
    }
    public IntegerProperty levelProperty(){
        return levelProperty;
    }
    public IntegerProperty livesProperty(){ return livesProperty; }
    public IntegerProperty multiplierProperty(){
        return multiplier;
    }
    public DoubleProperty timerProperty(){
        return timerProperty;
    }
    public IntegerProperty regeneratedProperty(){
        return regenerated;
    }

    /*
    Setter methods for properties
     */
    public void setLevelProperty(int set){
        logger.info("setting level to "+set);
        levelProperty().set(set);
    }
    public void setMultiplierProperty(int set){
        multiplierProperty().set(set);

    }
    public void setScoreProperty(int set){
        scoreProperty().set(set);
    }
    public void setLivesProperty(int set){
        livesProperty().set(set);
    }

    /*
    Getter methods for properties
     */
    public int getLevelProperty(){
        return levelProperty().get();
    }

    public int getScoreProperty() {
        return scoreProperty().get();
    }
    public int getLivesProperty() {
        return livesProperty().get();
    }
    public int getMultiplierProperty(){
        return multiplierProperty().get();
    }

    /**
     *Tracks if game has ended
     * @return state
     */
    public boolean getState(){
        return state;
    }

    /**
     * Gets current piece
     * @return current piece
     */
    public GamePiece getCurrentPiece(){
        return currentPiece;
    }

    /**
     * Gets next piece
     * @return next piece
     */
    public GamePiece getFollowingPiece(){
        return followingPiece;
    }

    /**
     * Set of all blocks to fade/clear
     * @return set of blocks
     */
    public Set<GameBlockCoordinate> getToClear(){
        return toClear;
    }

    /**
     * Used to differentiate between clicking on piece board displaying current piece or following piece
     */
    public void setState(boolean state){
        logger.info("state set to "+state);
        this.nextPieceClicked=state;
    }

    /**
     * To set when game has ended
     * @param state true if ended
     */
    public void setIfStart(boolean state){
        this.start = state;
    }

    /**
     * Set listener for changes in score
     * @param listener
     */
    public void setScoreChangeListener(ScoreChangeListener listener){
        scoreChangeListener = listener;
    }

    /**
     * Get the current running timer to cancel
     * @return running timer
     */
    public Timer getTimer(){
        return timer;
    }




}
