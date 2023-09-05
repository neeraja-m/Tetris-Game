package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * Class responsible for displaying the instrucitons of the game
 */
public class InstructionsScene extends BaseScene{
    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

    public InstructionsScene(GameWindow gameWindow){
        super(gameWindow);
        logger.info("Creating Instruction Scene");

    }

    @Override
    public void initialise() {
        //Handle escape key being pressed
        scene.setOnKeyPressed(this::handleEscape);
    }

    /**
     * Build the Instruction scene
     */

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var insPane = new StackPane();
        insPane.setMaxWidth(gameWindow.getWidth());
        insPane.setMaxHeight(gameWindow.getHeight());
        insPane.getStyleClass().add("menu-background");
        root.getChildren().add(insPane);

        var mainInsPane= new BorderPane();
        insPane.getChildren().add(mainInsPane);

        //Add image containing instructions
        var image= new ImageView(new Image(this.getClass().getResource("/images/Instructions.png").toExternalForm()));
        image.setX(120);
        image.setY(40);
        image.setFitWidth(gameWindow.getWidth()*3.2/5);
        image.setFitHeight(gameWindow.getHeight()*3.2/5);
        image.setPreserveRatio(true);

        //Add instruction scene titles and their styles
        var title = new Text("Instructions");
        title.getStyleClass().add("title");
        var title2 = new Text("Game Pieces");
        title2.getStyleClass().add("title");
        var textfield = new Text("TetrECS is a fast paced game where the aim is to clear as many lines as possible. The more lines " +
                "you clear, the faster your score increases!");
        textfield.getStyleClass().add("instructions");

        var vboxcenter = new VBox();
        vboxcenter.setAlignment(Pos.CENTER);
        vboxcenter.setSpacing(3);
        title.setX(120);
        title.setY(0);

        //Add 3 HBoxs
        var hbox1 = new HBox();
        hbox1.setAlignment(Pos.TOP_CENTER);
        hbox1.setSpacing(10);
        var hbox2 = new HBox();
        hbox2.setSpacing(10);
        hbox2.setAlignment(Pos.TOP_CENTER);
        var hbox3 = new HBox();
        hbox3.setSpacing(10);
        hbox3.setAlignment(Pos.TOP_CENTER);

        vboxcenter.getChildren().addAll(title,textfield,image,title2,hbox1,hbox2,hbox3);
        mainInsPane.setTop(vboxcenter);

        //Dynamically create 5 gamepieces per HBox to display all 15 pieces
        PieceBoard pb;
        for(int i=0; i<5; i++){
            pb = new PieceBoard(new Grid(3,3),gameWindow.getWidth()/13,gameWindow.getWidth()/13);
            pb.displayNextPiece(GamePiece.createPiece(i));

            hbox1.getChildren().add(pb);

        }
        for(int i=5; i<10; i++){
            pb = new PieceBoard(new Grid(3,3),gameWindow.getWidth()/13,gameWindow.getWidth()/13);
            pb.displayNextPiece(GamePiece.createPiece(i));

            hbox2.getChildren().add(pb);

        }
        for(int i=10; i<15; i++){
            pb = new PieceBoard(new Grid(3,3),gameWindow.getWidth()/13,gameWindow.getWidth()/13);
            pb.displayNextPiece(GamePiece.createPiece(i));

            hbox3.getChildren().add(pb);

        }

        //Add animation to title
        fader(title);
    }
}
