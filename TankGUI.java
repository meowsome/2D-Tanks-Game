import javafx.application.Application;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.ComboBox;
import javafx.geometry.Pos;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.geometry.Insets;

public class TankGUI extends Application {
    private HBox menuPane;
    private MainPane gamePane;
    private HBox upgradePane;
    private Scene gameScene;
    private Scene menuScene;
    private Scene upgradeScene;
    private Stage stage;
    private Manager manager;

	@Override
	public void start(Stage stage) {
        this.stage = stage;
        
        manager = new Manager();

        menuPane = new HBox(10);
        menuPane.getStyleClass().add("menu");
        gamePane = new MainPane(manager);
        gamePane.getStyleClass().add("game");
        upgradePane = new HBox(10);
        upgradePane.getStyleClass().add("menu");

        int maxWidth = gamePane.getTerrain().getXMax();
        int maxHeight = gamePane.getTerrain().getYMax();

        menuScene = new Scene(menuPane, maxWidth, maxHeight);
        gameScene = new Scene(gamePane, maxWidth, maxHeight);
        upgradeScene = new Scene(upgradePane, maxWidth, maxHeight);

        menuScene.getStylesheets().add("styles.css");
        gameScene.getStylesheets().add("styles.css");
        upgradeScene.getStylesheets().add("styles.css");

        setupMenuPane();
        
        stage.setScene(menuScene);
		stage.setTitle("2D Tanks Game");
		stage.show();
	}

    public void setupMenuPane() {
        GridPane menuPaneInner = new GridPane();

        VBox authPane = new VBox(10);
        VBox difficultyPane = new VBox(10);

        //welcome
        Text titleText = new Text("2D Tanks");
        TextFlow title = new TextFlow(titleText);
        title.getStyleClass().add("page_title_wrapper");
        titleText.getStyleClass().add("page_title");

        //auth pane
        Label playedBeforeLabel = new Label("Have you played before?");
        playedBeforeLabel.getStyleClass().add("text");
        ComboBox<String> playedBefore = new ComboBox<String>();
        playedBefore.getItems().addAll("Yes", "No");
        Label usernameLabel = new Label("Username:");
        usernameLabel.getStyleClass().add("text");
        TextField usernameInput = new TextField();
        usernameLabel.setPrefWidth(250);
        usernameLabel.setMaxWidth(250);
        authPane.getChildren().addAll(playedBeforeLabel, playedBefore, usernameLabel, usernameInput);

        //difficulty pane
        Label difficultyLabel = new Label("Select Opponent Difficulty");
        difficultyLabel.getStyleClass().add("text");
        ComboBox<String> difficulty = new ComboBox<String>();
        difficulty.getItems().addAll("Easy", "Hard");
        difficultyPane.getChildren().addAll(difficultyLabel, difficulty);

        //error
        Label error = new Label();
        error.getStyleClass().add("error");
        error.setVisible(false);

        Button startButton = new Button("Start Game");
        startButton.getStyleClass().add("text");
        startButton.setOnAction(e -> {
            //reset error message
            error.setVisible(false);

            if (difficulty.getSelectionModel().getSelectedItem() != null) {
                String difficultyChoice = difficulty.getSelectionModel().getSelectedItem().toString();
                
                if (usernameInput.getText() != null && !usernameInput.getText().isEmpty()) {
                    String[] userInformation = manager.findUser(usernameInput.getText());

                    if (playedBefore.getValue() == "Yes") {
                        if (userInformation.length > 0) {
                            setupGamePane(difficultyChoice, userInformation);
                        } else {
                            error.setText("Username does not exist.");
                            error.setVisible(true);
                        }
                    } else {
                        //create new account
                        if (userInformation.length == 0) {
                            manager.createUser(usernameInput.getText());
                            userInformation = new String[] {usernameInput.getText(), "0", "0", "0", "0", "0"};
                            setupGamePane(difficultyChoice, userInformation);
                        } else {
                            error.setText("That username already exists.");
                            error.setVisible(true);
                        }
                    }
                } else {
                    error.setText("Please enter a username.");
                    error.setVisible(true);
                }
            } else {
                error.setText("Please choose a difficulty.");
                error.setVisible(true);
            }
        });

        menuPaneInner.add(title, 0, 0);
        menuPaneInner.add(authPane, 0, 1);
        menuPaneInner.add(difficultyPane, 1, 1);
        menuPaneInner.add(startButton, 0, 2);
        menuPaneInner.add(error, 0, 3);
        menuPaneInner.setColumnSpan(title, 2);
        menuPaneInner.setColumnSpan(startButton, 2);
        menuPaneInner.setColumnSpan(error, 2);

        menuPaneInner.setHgap(50);
        menuPaneInner.setVgap(50);
        menuPaneInner.setPadding(new Insets(50,50,50,50));

        GridPane.setHalignment(title, HPos.RIGHT);
        GridPane.setValignment(title, VPos.CENTER);
        GridPane.setHalignment(startButton, HPos.CENTER);
        GridPane.setValignment(startButton, VPos.CENTER);
        GridPane.setHalignment(error, HPos.CENTER);
        GridPane.setValignment(error, VPos.CENTER);

        menuPane.getChildren().add(menuPaneInner);
        menuPane.setAlignment(Pos.CENTER);
    }

    public void setupGamePane(String difficulty, String[] userInformation) {
        int newDifficulty = 0;
        if (difficulty.equals("Hard")) {
            newDifficulty = 1;
        }

        manager.setDifficulty(newDifficulty);

        manager.generateTank(userInformation);
        manager.generateOpponent();

        gamePane.start();

        gamePane.setupTankGraphics(gamePane);
        gamePane.setupStatsGraphics(gamePane);

        stage.setScene(gameScene);

        gamePane.runRound(gameScene, stage, upgradePane, upgradeScene);
    }    

	public static void main(String[] args) {
		launch(args);
	}
}