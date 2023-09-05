package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.ScoresList;
import uk.ac.soton.comp1206.utility.Multimedia;

import java.util.Collections;
import java.util.Comparator;

/**
 * Class used to find and display current scores
 */
public class ScoresScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(ScoresScene.class);
    //Holds list of current local scores being observed
    private ObservableList<Pair<String, Integer>> scoreArrayList = FXCollections.observableArrayList();
    //Holds list of current remote scores being observed
    private ObservableList<Pair<String, Integer>> remotescoreArrayList = FXCollections.observableArrayList();

    //Holds list of current local scores
    private ListProperty<Pair<String, Integer>> localScores = new SimpleListProperty<>(scoreArrayList);
    //Holds list of current remote scores
    private ListProperty<Pair<String, Integer>> remoteScores = new SimpleListProperty<>(remotescoreArrayList);

    CommunicationsListener communicationsListener;
    private Communicator communicator;


    private String username;
    private TextField enterName;
    private StackPane pane;
    private Integer userScore;
    private ScoresList localscoreslist;
    private ScoresList remotescoreslist;
    private boolean beatenLocal=false;
    private boolean beatenRemote=false;

    public ScoresScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Scores Scene");
    }

    /**
     * Initialise the ScoreScene
     */

    @Override
    public void initialise() {
        logger.info("Initialising scores scene");

        //Handle escape key being pressed
        scene.setOnKeyPressed(this::handleEscape);

    }

    /**
     * Build the ScoreScene
     */

    @Override
    public void build() {
        logger.info("Building scores scene");
        communicator = gameWindow.getCommunicator();

        //Play the end scores music
        Multimedia.playMusic("end.wav");

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        //If the game state passed is not null, continue
        if (gameWindow.getGameState() != null) {
            logger.info("Final score is " + gameWindow.getGameState().scoreProperty());
            username = null;

            //Set the user score to the passed game's score
            userScore = gameWindow.getGameState().getScoreProperty();

            //Initialise score lists
            localscoreslist = new ScoresList();
            remotescoreslist = new ScoresList();

            //Bind the scorelist components to corresponding lists
            localscoreslist.scoreProperty().bind(localScores);
            remotescoreslist.remotescoreProperty().bind(remoteScores);

            //Load the online scores and local scores
            loadOnlineScores();
            loadLocalScores();
            try {
                //Wait 1 second to load scores and then build scene
                Thread.sleep(1000);

                //Build the scores scene display
                Platform.runLater(() -> {

                    var scoresPane = new StackPane();
                    scoresPane.setMaxWidth(gameWindow.getWidth());
                    scoresPane.setMaxHeight(gameWindow.getHeight());
                    scoresPane.getStyleClass().add("scores-background");
                    root.getChildren().add(scoresPane);

                    var mainScoresPane = new BorderPane();
                    scoresPane.getChildren().add(mainScoresPane);
                    mainScoresPane.setPadding(new Insets(20, 20, 20, 20));

                    var vbox = new VBox();
                    vbox.setAlignment(Pos.TOP_CENTER);
                    vbox.setSpacing(20);
                    mainScoresPane.setCenter(vbox);
                    var vboxleft = new VBox();
                    vboxleft.setAlignment(Pos.CENTER_LEFT);
                    vboxleft.setSpacing(20);
                    mainScoresPane.setLeft(vboxleft);
                    var vboxright = new VBox();
                    vboxright.setAlignment(Pos.CENTER_RIGHT);
                    vboxright.setSpacing(20);
                    mainScoresPane.setRight(vboxright);
                    var hbox = new HBox();
                    hbox.setAlignment(Pos.TOP_CENTER);
                    mainScoresPane.setBottom(hbox);

                    //Add relevant labels and scorelists to scene
                    var myScore = new Text("Your score:");
                    myScore.getStyleClass().add("yourscore-label");

                    var score = new Text(userScore.toString());
                    score.getStyleClass().add("myuserScore");

                    vbox.getChildren().addAll(myScore, score);
                    vboxleft.getChildren().add(localscoreslist);
                    vboxright.getChildren().add(remotescoreslist);

                    //Add play again label
                    var playAgain = new Text("Play Again");
                    playAgain.getStyleClass().add("newgamebutton");
                    playAgain.setOnMouseClicked(event -> restart());
                    hbox.getChildren().add(playAgain);

                });
            }catch (Exception e){
                logger.info(e);
            }
        }
    }

    /**
     * Restart the game
     */
    protected void restart(){
        logger.info("Restarting game");
        gameWindow.startChallenge(gameWindow.getMode());
    }

    /**
     * Send a high score to communicator
     * @param name the players name
     * @param score the players score
     */
    protected void writeOnlineScore(String name, Integer score){
        logger.info("Sending score: {}", score);
        communicator.send("HISCORE " + name + ":" + score+"\n");
    }

    /**
     * Add the scores loaded in ScoresList to the local scores list which will trigger ScoresLists lists
     */
    protected void loadLocalScores(){
        logger.info("loading local scores");

        try {
            for (Pair<String, Integer> item : localscoreslist.loadScores()) {
                String toAddname = item.getKey();
                Integer toAddscore = item.getValue();

                addtoLocal(toAddname, toAddscore);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Request communicator for high scores
     */
    protected void loadOnlineScores(){
        logger.info("loading remote scores");
        remoteScores.clear();

        communicator.send("HISCORES");

        communicator.addListener((communication) -> {
            Platform.runLater(() -> this.receiveCommunication(communication));
        });

    }

    /**
     * Add score to remotescoreslist
     * @param username name to be added
     * @param score score to be added
     */
    protected void addtoRemote(String username, Integer score){
        remoteScores.add(new Pair<>(username, score));

        //Sort scores in descending order after every addition
        Collections.sort(remoteScores,new sortScores());

        logger.info("Added {}:{} to remote scores", username, score);
    }

    /**
     * Add score to localscoreslist
     * @param username name to be added
     * @param score score to be added
     */
    protected void addtoLocal(String username, Integer score) {
        localScores.add(new Pair<>(username, score));

        //Sort scores in descending order after every addition
        Collections.sort(localScores,new sortScores());

        //Write score to text file
        localscoreslist.writeScores(username,score);
        logger.info("Added {}:{} to local scores", username, score);

    }

    /**
     * Used to get username of user if high score has been beaten
     * @return
     */
    protected String getUsername(){
        logger.info("getting username");

        //Reset name
        if(enterName!=null && !enterName.getText().isBlank()){
        enterName.clear();}

        pane = new StackPane();
        pane.setMaxWidth(gameWindow.getWidth());
        pane.setMaxHeight(gameWindow.getHeight());

        scene = new Scene(pane,pane.getWidth(),pane.getWidth());
        pane.getStyleClass().add("scores-background");
        root.getChildren().add(pane);

        //Add titles and labels
        var title = new Text("High score beaten!");
        title.getStyleClass().add("highscore-title");

        var line = new Text("Please enter your name below");
        line.getStyleClass().add("challenge-labels");
        pane.setAlignment(Pos.TOP_CENTER);

        var button = new Button("Enter");
        line.getStyleClass().add("enterUsername");

        var vbox = new VBox();
        pane.getChildren().add(vbox);
        vbox.setSpacing(30);
        vbox.setAlignment(Pos.CENTER);

        enterName = new TextField();
        enterName.setMaxWidth(gameWindow.getWidth()/3);
        enterName.requestFocus();

        //Handle when name entered
        vbox.getChildren().addAll(title,line,enterName,button);
        enterName.setOnKeyPressed(keyEvent -> updatesScores(keyEvent,null));
        button.setOnAction(event -> updatesScores(null,event));

        return username;
    }

    /**
     * Check if local or remote scores have been beaten
     * @param userScore score to be checked
     */

    private void ifBeaten(Integer userScore){
        //Reset values
        beatenLocal=false;
        beatenRemote=false;
        //If remote scores are empty, load again
        if(remoteScores.isEmpty()){
            logger.info("online scores are empty");
            loadOnlineScores();
        }else{
            //Compare user's score with all current remote scores and update if beaten
            for (Pair<String, Integer> toCheck : remoteScores) {
                if (userScore >= toCheck.getValue()) {
                    beatenRemote = true;
                    break;
                }
            }
        }
        //If local scores are empty, high score has been beaten
        if(localScores.isEmpty()){
            beatenLocal=true;
        }else {
            //Compare user's score with all current local scores and update if beaten
            for(Pair<String,Integer> toCheck : localScores){
                if(userScore>=toCheck.getValue()){
                    beatenLocal=true;
                    break;
                }
            }
        }
        //If either local or remote scores have been beaten, get the players username and reveal scores after
        if(beatenLocal || beatenRemote){
            Platform.runLater(()->{
                getUsername();
            });
        }else{
            localscoreslist.reveal();
            remotescoreslist.reveal();}
        logger.info("local score has been beaten: {}, remote score: {}", beatenLocal, beatenRemote );
    }

    /**
     * Add score to respective score list
     * @param event key pressed
     * @param action mouse clicked
     */
    private void updatesScores(KeyEvent event, ActionEvent action){
        //If enter key pressed or button pressed continue
        if ((event == null && action != null) ||(event.getCode() == KeyCode.ENTER)){
            //Set username to text entered
            username=enterName.getText();
            logger.info("username set to "+ username);
            //If local score was beaten, add score to local score list
            if(beatenLocal){
                addtoLocal(username,userScore);
            }
            //If remote score was beaten, add to score to remote score list
            if(beatenRemote){
                addtoRemote(username,userScore);
                writeOnlineScore(username,userScore);
            }

            //Remove getUsername() pane
            root.getChildren().remove(pane);

            localscoreslist.reveal();
            remotescoreslist.reveal();

        }

    }

    /**
     * Class to sort score lists according to scores in descending order
     */
    public class sortScores implements Comparator<Pair<String,Integer>> {

        @Override
        public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {

            return o2.getValue()-o1.getValue();
        }
    }

    /**
     * Handle Communicators responses
     * @param communication the responses received
     */

    public void receiveCommunication(String communication) {
        logger.info("received communication");

        //If response contains list of high scores continue
        if (communication.startsWith("HISCORES")) {
            logger.info("received highscores");

            //Split communication with whitespaces
            var fullScore = communication.split("[\\s]");

            //Add each name and score to remote score list
            for(int i=1; i<fullScore.length; i++) {
                var scorePart = fullScore[i].split(":");

                String name = scorePart[0];
                Integer score = Integer.parseInt(scorePart[1]);

                addtoRemote(name,score);
            }
            logger.info("remote scores size: " + remoteScores.size());
        }
        //Check if scores have been beaten
        ifBeaten(userScore);

    }


}

