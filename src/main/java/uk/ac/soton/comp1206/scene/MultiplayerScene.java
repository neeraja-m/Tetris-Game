package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Multiplayer Scene holding UI for multiplayer lobby and game
 */
public class MultiplayerScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);
    Timer timer;
    TimerTask timertask;
    Communicator communicator = gameWindow.getCommunicator();
    CommunicationsListener communicationsListener;

    //Observed list containing channels
    private ObservableList<String> channelArrayList = FXCollections.observableArrayList();
    //List holding existing channels
    ListProperty<String> channelList = new SimpleListProperty<>(channelArrayList);
    private VBox vboxleft;
    private VBox vboxright;
    private TextField textfield;
    //Observed list containing players in a channel
    private ObservableList<String> playersArrayList = FXCollections.observableArrayList();
    //List holding players in a channel
    private ListProperty<String> playersList = new SimpleListProperty<>(playersArrayList);
    //Property to hold new channel name
    private StringProperty channelname = new SimpleStringProperty("Create a new game");
    private VBox playersBox;
    private boolean joined = false;
    private Text createNewChannel;
    private Text leave;
    private boolean nameTaken;



    public MultiplayerScene(GameWindow gameWindow){
        super(gameWindow);
        logger.info("Creating Multiplayer Scene");

    }

    /**
     * Initialize Multiplayer scene
     */

    @Override
    public void initialise() {

        //Handle escape key being pressed
        scene.setOnKeyPressed(this::handleEscape);


    }

    /**
     * Build Multiplayer lobby
     */
    @Override
    public void build() {

        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        //Start requesting communicator for channels
        handleTimer();

        //Add listeners to lists to trigger their respective methods
        channelList.addListener((ListChangeListener<? super String>) (e)-> updateChannels());
        playersList.addListener((ListChangeListener<? super String>) (e)-> updatePlayers());


        var multiPane = new StackPane();
        multiPane.setMaxWidth(gameWindow.getWidth());
        multiPane.setMaxHeight(gameWindow.getHeight());
        multiPane.getStyleClass().add("multiplayer-background");
        root.getChildren().add(multiPane);
        logger.info("added root child");

        var mainmultiPane= new BorderPane();
        multiPane.getChildren().add(mainmultiPane);

        var title = new Text("Lobby");
        title.getStyleClass().add("title");

        var vbox = new VBox();
        mainmultiPane.setCenter(vbox);
        vbox.getChildren().add(title);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setPadding(new Insets(0,0,0,30));

        //Holds players in the selected channel
        playersBox = new VBox();
        playersBox.setSpacing(10);
        playersBox.setAlignment(Pos.CENTER);

        vboxleft = new VBox();
        mainmultiPane.setLeft(vboxleft);
        vboxleft.setAlignment(Pos.TOP_CENTER);
        vboxleft.setSpacing(20);
        vboxleft.setPadding(new Insets(60,20,20,40));

        vboxright = new VBox();
        mainmultiPane.setRight(vboxright);
        vboxright.setPadding(new Insets(30,40,60,20));
        vboxright.setSpacing(10);
        vboxright.setAlignment(Pos.TOP_CENTER);

        //Create section for the current channels

        var currentChannels = new Text("Current channels");
        currentChannels.getStyleClass().add("mp-labels");
        currentChannels.setX(20);
        currentChannels.setY(50);
        mainmultiPane.getChildren().add(currentChannels);

        //Create section to start a new channel
        createNewChannel = new Text("Create a new game");
        createNewChannel.textProperty().bind(channelname);
        createNewChannel.setX(50);
        createNewChannel.setY(50);
        createNewChannel.getStyleClass().add("newgamebutton");
        vboxright.getChildren().add(createNewChannel);
        vboxright.getChildren().add(playersBox);

        //Used to leave a channel
        leave = new Text("Leave");
        leave.getStyleClass().add("newgamebutton");
        vboxright.getChildren().add(leave);
        leave.setVisible(false);
        leave.setOnMouseClicked(event -> leaveChannel());


        //Create a textfield to get the channel name
        //Set this to invisible until needed
        textfield = new TextField();
        vboxright.getChildren().add(textfield);
        textfield.setMaxWidth(gameWindow.getWidth()/4);
        textfield.setVisible(false);

        //Add listener to when user wants to create a new channel
        createNewChannel.setOnMouseClicked(event -> getchannelName());

        //Add animation text components
        fader(title);
        fader(currentChannels);

    }

    /**
     * Expose list property
     * @return the list of channels
     */
    public ListProperty<String> channelProperty() {
        return channelList;
    }

    /**
     * Schedule requests to communicator
     */
    protected void handleTimer(){
        //Initialize the timer and schedule the task of requesting for channels every 3 seconds
        timer = new Timer();
        timertask = new TimerTask() {
            @Override
            public void run() {
                requestChannels();
            }
        };
        timer.scheduleAtFixedRate(timertask,0,3000);
    }

    /**
     * Request communicator for existing channels
     */
    protected void requestChannels(){
        logger.info("requesting channels");
        communicator.send("LIST");

        communicator.addListener((communication) -> {
            Platform.runLater(() -> this.receiveCommunication(communication));
        });
    }

    /**
     * Updates the players in the selected channel
     */
    protected void updatePlayers(){
        playersBox.getChildren().clear();
        for(int i=0; i<playersList.size();i++){
            Text player = new Text();
            player.textProperty().bind(playersList.valueAt(i));
            player.getStyleClass().add("channelItem");
            playersBox.getChildren().add(player);
        }

    }

    /**
     * Handles responses from the communicator
     * @param communication response from the communicator
     */
    protected void receiveCommunication(String communication){
        //If response contains existing channels, continue
        if(communication.startsWith("CHANNELS")){
            logger.info("received channels");

            //Clear current list of channels
            channelList.clear();

            //Split communication using whitespaces
            var channels= communication.split("[\\s]");

            //Add the channel names to the channel list
            for(int i=1; i<channels.length; i++){
                channelList.add(channels[i]);
                logger.info("Added channel {}",channels[i]);
            }
        }

        //If response contains existing players in the specified channel, continue
        if(communication.startsWith("USERS")){
            logger.info("received users");

            //Clear the current players
            playersList.clear();

            //Split communication using whitespaces
            var players= communication.split("[\\s]");

            //Add player names to the player list
            for(int i=1; i<players.length; i++){
                playersList.add(players[i]);
                logger.info("Added players {}",players[i]);
            }

        }
        //If response is an error message, continue
        if(communication.startsWith("ERROR")){
            logger.error(communication);
        }
    }

    /**
     * Used to leave a currently joined channel
     */
    protected void leaveChannel(){
        if(joined){
            logger.info("Leaving channel");
            communicator.send("PART");
            channelname.setValue("Create a new game");
            playersList.clear();
            leave.setVisible(false);
            joined=false;}
        else{
            Multimedia.playSound("fail.wav");
        }
    }

    /**
     * Request communicator to join a channel
     * @param event click on channel
     * @param channel the channel to join
     */
    protected void joinChannel(MouseEvent event,String channel){
        if(!joined) {
            logger.info("joining channel: " + channel);
            joined = true;
            Multimedia.playSound("pling.wav");
            communicator.send("JOIN " + channel);
            channelname.setValue(channel);
            leave.setVisible(true);
        }else{
            Multimedia.playSound("fail.wav");
        }
    }

    /**
     * Update the displayed list of current channels
     */
    protected void updateChannels(){
        //Clear the current text components
        vboxleft.getChildren().clear();
        for(int i=0; i<channelList.size();i++){
            //Create a new text component for each channel
            Text channel = new Text();
            //Bind the text property to the channel names
            channel.textProperty().bind(channelList.valueAt(i));
            channel.getStyleClass().add("channelItem");
            vboxleft.getChildren().add(channel);
            //When a channel name is clicked, trigger the method to join the clicked channel
            channel.setOnMouseClicked(MouseEvent-> joinChannel(MouseEvent,channel.getText()) );
        }

    }

    /**
     * Exit the server
     */
    protected void quit(){
        logger.info("Exiting server");
        communicator.send("QUIT");
        cleanUp();
    }

    /**
     * Create a new channel
     * @param channelName the name of the channel
     * @param keyEvent key pressed
     */
    protected void startChannel(String channelName, KeyEvent keyEvent){
        //When enter key is pressed, create a channel with the given channel name and send to server
        if(!joined) {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                //Ensure the channel name does not already exist
                for (String name : channelList) {
                    if (textfield.getText().equals(name)) {
                        Multimedia.playSound("fail.wav");
                        textfield.clear();
                        nameTaken=true;
                        break;
                    }else{nameTaken=false;}
                }if(!nameTaken) {
                    //Send created channel to the server
                    channelname.setValue(textfield.getText());
                    textfield.clear();
                    textfield.setVisible(false);
                    createNewChannel.getStyleClass().add("mp-labels");
                    logger.info("starting channel");
                    leave.setVisible(true);
                    joined = true;
                    Multimedia.playSound("pling.wav");
                    communicator.send("CREATE " + channelName);
                }else{
                    getchannelName();
                }
            }
        }
    }


    /**
     * Get the channel name from player
     *
     */
    protected void getchannelName(){
        if(!joined){
        //Clear the text field and pass typed channel name to method to send the channel name to server
        logger.info("getting channel name");
        if(textfield!=null && !textfield.getText().isBlank()){
            textfield.clear();}
        textfield.setVisible(true);
        textfield.requestFocus();
        textfield.setOnKeyPressed(keyEvent -> startChannel(textfield.getText(),keyEvent));}
    }

    /**
     * Clean up scene
     */
    protected void cleanUp(){
        if(joined){
        leaveChannel();}
        timer.cancel();
        timertask.cancel();

    }


}
