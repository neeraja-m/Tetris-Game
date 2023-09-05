package uk.ac.soton.comp1206.ui;

import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.scene.ScoresScene;

import java.io.*;
import java.util.*;

/**
 * Class represents a score list. Used to display remote and local scores list.
 */
public class ScoresList extends VBox {

    private static final Logger logger = LogManager.getLogger(ScoresList.class);
    //Holds local scores
    private ListProperty<Pair<String,Integer>> scoresProperty = new SimpleListProperty<>();
    //Holds remote scores
    private ListProperty<Pair<String,Integer>> remotescoresProperty = new SimpleListProperty<>();
    //Reads from scores text file
    private BufferedReader bufferedReader;
    //Writes to scores text file
    private BufferedWriter bufferedWriter;
    //Scores read from text file
    private List<Pair<String,Integer>> defaultScores = new ArrayList<>();
    //Score to write to text file
    private List<Pair<String,Integer>> toAddScores = new ArrayList<>();




    public ScoresList(){
        setSpacing(4);
        setAlignment(Pos.CENTER);
        getStyleClass().add("scorelist");

        //Trigger update method when list properties change
        scoresProperty.addListener((ListChangeListener<? super Pair<String, Integer>>) (e)-> updatelocalScores());
        remotescoresProperty.addListener((ListChangeListener<? super Pair<String, Integer>>) (e)->updateRemoteScores());

    }

    /**
     * Expose scoresproperty
     * @return local scores list
     */
    public ListProperty<Pair<String,Integer>> scoreProperty() {
        return scoresProperty;
    }

    /**
     *Expose remotescoresProperty
     * @return remote scores list
     */
    public ListProperty<Pair<String,Integer>> remotescoreProperty() {
        return remotescoresProperty;
    }

    /**
     * Update the UI list of local scores
     */
    private void updatelocalScores(){

        logger.info("Updating scores");
        getChildren().clear();

        var title = new Text("Local scores");
        title.getStyleClass().add("score-labels");
        title.setTextAlignment(TextAlignment.CENTER);
        getChildren().add(title);


        int scoresAdded = 0;

        for (Pair<String, Integer> toAdd : scoresProperty) {

            if (scoresAdded < 10) {

                HBox userScores = new HBox();
                userScores.setSpacing(20);
                userScores.setAlignment(Pos.CENTER);

                var userName = new Text(toAdd.getKey());
                userName.getStyleClass().add("username");
                userName.setTextAlignment(TextAlignment.CENTER);
                HBox.setHgrow(userName, Priority.ALWAYS);

                //Add points
                var score = new Text(toAdd.getValue().toString());
                score.getStyleClass().add("userScore");
                score.setTextAlignment(TextAlignment.CENTER);
                HBox.setHgrow(score, Priority.ALWAYS);

                //Add names and scores
                userScores.getChildren().addAll(userName, score);

                //Add scores list
                getChildren().add(userScores);

                scoresAdded++;

            }
        }
    }

    /**
     *Update the UI list of remote scores
     */
    protected void updateRemoteScores(){
        Platform.runLater(()-> {
            logger.info("Updating scores");
            getChildren().clear();

            var title = new Text("Online scores");
            title.getStyleClass().add("score-labels");
            title.setTextAlignment(TextAlignment.CENTER);
            getChildren().add(title);

            int scoresAdded = 0;

            for (Pair<String, Integer> toAdd : remotescoresProperty) {

                if (scoresAdded < 10) {

                    HBox userScores = new HBox();
                    userScores.setSpacing(20);
                    userScores.setAlignment(Pos.CENTER);


                    var userName = new Text(toAdd.getKey());
                    userName.getStyleClass().add("username");
                    userName.setTextAlignment(TextAlignment.CENTER);
                    HBox.setHgrow(userName, Priority.ALWAYS);

                    //Add points
                    var score = new Text(toAdd.getValue().toString());
                    score.getStyleClass().add("userScore");
                    score.setTextAlignment(TextAlignment.CENTER);
                    HBox.setHgrow(score, Priority.ALWAYS);

                    //Add names and scores
                    userScores.getChildren().addAll(userName, score);

                    //Add scores list
                    getChildren().add(userScores);

                    scoresAdded++;

                }
            }
        });
    }

    /**
     * Animation of scorelists
     * @return transition
     */
    public Transition reveal(){

        FadeTransition fade = new FadeTransition(new Duration(500),this);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
        return fade;

    }

    /**
     * Load scores in scores text file
     * @return list containing scores and names
     */
    public List<Pair<String,Integer>> loadScores(){
        logger.info("Loading default scores");
        try {
            bufferedReader = new BufferedReader(new FileReader("scores.txt"));
            String line= bufferedReader.readLine();
            while(line!=null){
                try {
                    String info[] = line.split(":");
                    String name = info[0];
                    Integer score = Integer.parseInt(info[1]);
                    defaultScores.add(new Pair<>(name, score));

                } catch (Exception e) {
                    System.out.println(e);

                }
                line= bufferedReader.readLine();

            }
            bufferedReader.close();

        } catch (Exception e) {
            System.out.println(e);
            writeScores(null,null);
            loadScores();
        }

        return defaultScores;

    }

    /**
     * Write scores into scores text file
     * @param name name of player
     * @param score score of player
     */
    public void writeScores(String name, Integer score) {
        logger.info("Writing scores");

        if (name != null && score != null) {
            //Add to list of all players scores as back up
            toAddScores.add(new Pair<>(name, score));

                try {
                    bufferedWriter = new BufferedWriter(new FileWriter("scores.txt", false));
                    int added = 0;
                    for (Pair<String, Integer> toAdd : scoresProperty) {
                        //Write the top 10 scores into the scores.txt file
                        if (added < 10) {
                            bufferedWriter.write(toAdd.getKey() + ":" + toAdd.getValue().toString() + "\n");
                            added++;
                        }
                    }bufferedWriter.close();
                }catch (Exception e) {
                    System.out.println(e);
                }
            } else{
            //If file doesnt exist, create the file
            try {
                FileWriter scores = new FileWriter("scores.txt");

            }catch (Exception exception){
                System.out.println(exception);
            }
        }

    }


}
