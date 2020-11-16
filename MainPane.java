import javafx.scene.layout.Pane;
import javafx.scene.layout.BorderPane;
import javafx.scene.Scene;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import java.util.Date;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.animation.RotateTransition;
import javafx.scene.input.KeyCode;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import java.text.DecimalFormat;

public class MainPane extends Pane {
    private Manager manager;
    private Timeline mainTimeline;
    
    private Terrain terrain;
    private Tank playerTank;
    private Tank opponentTank;

    private Circle playerCircle, opponentCircle;
    private Line opponentLine, playerLine;
    private GraphicsContext g;
    private Scene scene;
    private Circle projectileCircle;
    private EventHandler<KeyEvent> keyHandler;
    private Canvas canvas;

    private Label playerTurns;
    private ProgressBar playerHealthProgress, opponentHealthProgress;

    private GridPane winPane;
    private Label winnerText;
    private Button winButton;

    private double previousXCoord;
    private int movementDirection;
    private double cannonPreviousAngle;
    private int cannonDirection;
    private double projectileTime;

    private static int maxUpgrades = 10;

    public MainPane(Manager manager) {
        this.manager = manager;
        terrain = manager.getTerrain();

        canvas = new Canvas(terrain.getXMax(), terrain.getYMax());
        g = canvas.getGraphicsContext2D();
        setupTerrainGraphics(terrain);
    }

    public void start() {
        playerTank = manager.getPlayer();
        opponentTank = manager.getOpponent();
    }

    public void runRound(Scene scene, Stage stage, HBox upgradePane, Scene upgradeScene) {
        this.scene = scene;

        mainTimeline = new Timeline();
		mainTimeline.setCycleCount(Timeline.INDEFINITE);
		KeyFrame runRound = new KeyFrame(Duration.millis(500), action -> {
            if (!manager.isGameOver()) {
                runTurn();
                updateStatsMenu();
            } else {
                mainTimeline.stop();

                winnerText.setText("You Lose!");

                double moneyEarned = 0;
                
                //determine winner prize
                if (playerTank.getHealth() > 0) {
                    moneyEarned = manager.calculateMoney(playerTank.getHealth());

                    playerTank.setMoney(playerTank.getMoney() + moneyEarned);
                    playerTank.editFile(1, moneyEarned);

                    winnerText.setText("You Win!");
                }

                if (manager.getTurnsRemaining() == 0) {
                    moneyEarned = 0;

                    winnerText.setText("It's a Tie!");
                }

                //have to copy to a final variable in order to use in the lamba expression 
                final double moneyEarned2 = moneyEarned;

                winButton.setOnAction(e -> {
                    setupUpgradePane(stage, upgradePane, upgradeScene, moneyEarned2);
                });

                winPane.setVisible(true);
            }
		});

		mainTimeline.getKeyFrames().add(runRound);

		mainTimeline.play();
    }

    public void setupUpgradePane(Stage stage, HBox upgradePane, Scene upgradeScene, double moneyEarned) {
        GridPane upgradePaneInner = new GridPane();

        //remove all previous elements and start fresh
        upgradePaneInner.getChildren().clear();
        upgradePane.getChildren().clear();

        VBox titlePane = new VBox(10);
        VBox healthMultiplierPane = new VBox(10);
        VBox projectileVelocityMultiplierPane = new VBox(10);
        VBox projectileDamageMultiplierPane = new VBox(10);
        VBox distanceMultiplierPane = new VBox(10);
        VBox difficultyPane = new VBox(10);
        VBox optionsPane = new VBox(10);
        titlePane.setAlignment(Pos.CENTER);
        optionsPane.setAlignment(Pos.CENTER);

        //error label
        Label upgradeError = new Label();
        upgradeError.getStyleClass().add("error");
        upgradeError.setVisible(false);

        //titlePane
        Text upgradeTitleText = new Text("Tank Upgrades");
        TextFlow upgradeTitle = new TextFlow(upgradeTitleText);
        upgradeTitleText.getStyleClass().add("page_title");
        upgradeTitle.getStyleClass().add("page_title_wrapper");
        DecimalFormat format = new DecimalFormat("##.00");
        Label balance = new Label("Your Balance: $" + format.format(playerTank.getMoney()) + " (+$" + format.format(moneyEarned) + " this round)");
        balance.getStyleClass().add("text");
        titlePane.getChildren().addAll(upgradeTitle, balance);

        //healthMultiplierPane
        Label healthLabel = new Label("Health");
        healthLabel.getStyleClass().add("title");
        Label healthCurrent = new Label("Current Upgrade: " + Double.toString(playerTank.getHealthMultiplier()));
        healthCurrent.getStyleClass().add("text");
        Button healthButton = new Button("Upgrade Health ($" + manager.getUpgradeCost(playerTank.getHealthMultiplier()) + ")");
        healthButton.getStyleClass().add("text");
        healthMultiplierPane.getChildren().addAll(healthLabel, healthCurrent, healthButton);

        healthButton.setOnAction(e -> {
            upgradeError.setVisible(false);

            if (playerTank.getHealthMultiplier() < maxUpgrades) {
                if (playerTank.getMoney() >= manager.getUpgradeCost(playerTank.getHealthMultiplier())) {
                    manager.upgradeMultiplier(2, playerTank);

                    //edit text and labels
                    healthButton.setText("Upgrade Health ($" + manager.getUpgradeCost(playerTank.getHealthMultiplier()) + ")");
                    healthCurrent.setText("Current Upgrade: " + Double.toString(playerTank.getHealthMultiplier()));
                    balance.setText("Your Balance: $" + playerTank.getMoney());
                } else {
                    upgradeError.setText("Insufficient Funds");
                    upgradeError.setVisible(true);
                }
            } else {
                upgradeError.setText("Maximum upgrades reached for this item");
                upgradeError.setVisible(true);
            }
        });

        //projectileVelocityMultiplierPane
        Label projectileVelocityLabel = new Label("Projectile Velocity");
        projectileVelocityLabel.getStyleClass().add("title");
        Label projectileVelocityCurrent = new Label("Current Upgrade: " + Double.toString(playerTank.getProjectileVelocityMultiplier()));
        projectileVelocityCurrent.getStyleClass().add("text");
        Button projectileVelocityButton = new Button("Upgrade Projectile Velocity ($" + 100 * (playerTank.getProjectileVelocityMultiplier() + 1) + ")");
        projectileVelocityButton.getStyleClass().add("text");
        projectileVelocityMultiplierPane.getChildren().addAll(projectileVelocityLabel, projectileVelocityCurrent, projectileVelocityButton);

        projectileVelocityButton.setOnAction(e -> {
            upgradeError.setVisible(false);

            if (playerTank.getProjectileVelocityMultiplier() < maxUpgrades) {
                if (playerTank.getMoney() >= manager.getUpgradeCost(playerTank.getProjectileVelocityMultiplier())) {
                    manager.upgradeMultiplier(3, playerTank);

                    //edit text and labels
                    projectileVelocityButton.setText("Upgrade Projectile Velocity ($" + manager.getUpgradeCost(playerTank.getProjectileVelocityMultiplier()) + ")");
                    projectileVelocityCurrent.setText("Current Upgrade: " + Double.toString(playerTank.getProjectileVelocityMultiplier()));
                    balance.setText("Your Balance: $" + playerTank.getMoney());
                } else {
                    upgradeError.setText("Insufficient Funds");
                    upgradeError.setVisible(true);
                }
            } else {
                upgradeError.setText("Maximum upgrades reached for this item");
                upgradeError.setVisible(true);
            }
        });

        //projectileDamageMultiplierPane
        Label projectileDamageLabel = new Label("Projectile Damage");
        projectileDamageLabel.getStyleClass().add("title");
        Label projectileDamageCurrent = new Label("Current Upgrade: " + Double.toString(playerTank.getProjectileDamageMultiplier()));
        projectileDamageCurrent.getStyleClass().add("text");
        Button projectileDamageButton = new Button("Upgrade Projectile Damage ($" + 100 * (playerTank.getProjectileDamageMultiplier() + 1) + ")");
        projectileDamageButton.getStyleClass().add("text");
        projectileDamageMultiplierPane.getChildren().addAll(projectileDamageLabel, projectileDamageCurrent, projectileDamageButton);

        projectileDamageButton.setOnAction(e -> {
            upgradeError.setVisible(false);

            if (playerTank.getProjectileDamageMultiplier() < maxUpgrades) {
                if (playerTank.getMoney() >= manager.getUpgradeCost(playerTank.getProjectileDamageMultiplier())) {
                    manager.upgradeMultiplier(4, playerTank);

                    //edit text and labels
                    projectileDamageButton.setText("Upgrade Projectile Damage ($" + manager.getUpgradeCost(playerTank.getProjectileDamageMultiplier()) + ")");
                    projectileDamageCurrent.setText("Current Upgrade: " + Double.toString(playerTank.getProjectileDamageMultiplier()));
                    balance.setText("Your Balance: $" + playerTank.getMoney());
                } else {
                    upgradeError.setText("Insufficient Funds");
                    upgradeError.setVisible(true);
                }
            } else {
                upgradeError.setText("Maximum upgrades reached for this item");
                upgradeError.setVisible(true);
            }
        });

        //distanceMultiplierPane
        Label distanceLabel = new Label("Distance");
        distanceLabel.getStyleClass().add("title");
        Label distanceCurrent = new Label("Current Upgrade: " + Double.toString(playerTank.getDistanceMultiplier()));
        distanceCurrent.getStyleClass().add("text");
        Button distanceButton = new Button("Upgrade Distance ($" + 100 * (playerTank.getDistanceMultiplier() + 1) + ")");
        distanceButton.getStyleClass().add("text");
        distanceMultiplierPane.getChildren().addAll(distanceLabel, distanceCurrent, distanceButton);

        distanceButton.setOnAction(e -> {
            upgradeError.setVisible(false);

            if (playerTank.getDistanceMultiplier() < maxUpgrades) {
                if (playerTank.getMoney() >= manager.getUpgradeCost(playerTank.getDistanceMultiplier())) {
                    manager.upgradeMultiplier(5, playerTank);

                    //edit text and labels
                    distanceButton.setText("Upgrade Distance ($" + manager.getUpgradeCost(playerTank.getDistanceMultiplier()) + ")");
                    distanceCurrent.setText("Current Upgrade: " + Double.toString(playerTank.getDistanceMultiplier()));
                    balance.setText("Your Balance: $" + playerTank.getMoney());
                } else {
                    upgradeError.setText("Insufficient Funds");
                    upgradeError.setVisible(true);
                }
            } else {
                upgradeError.setText("Maximum upgrades reached for this item");
                upgradeError.setVisible(true);
            }
        });

        //difficulty pane
        Label difficultyLabel = new Label("Select Opponent Difficulty");
        difficultyLabel.getStyleClass().add("text");
        ComboBox<String> difficulty = new ComboBox<String>();
        difficulty.getItems().addAll("Easy", "Hard");
        difficultyPane.getChildren().addAll(difficultyLabel, difficulty);

        //options pane
        Button playAgainButton = new Button("Play Again");
        playAgainButton.getStyleClass().add("text");
        Button quitButton = new Button("Quit");
        quitButton.getStyleClass().add("text");
        optionsPane.getChildren().addAll(playAgainButton, quitButton);

        playAgainButton.setOnAction(e -> {
            Manager manager = new Manager();
            MainPane gamePane = new MainPane(manager);

            if (difficulty.getSelectionModel().getSelectedItem() != null) {
                String difficultyChoice = difficulty.getSelectionModel().getSelectedItem().toString();

                int newDifficulty = 0;
                if (difficultyChoice == "Hard") {
                    newDifficulty = 1;
                }

                manager.setDifficulty(newDifficulty);

                String[] userInformation = new String[]{playerTank.getName(), Double.toString(playerTank.getMoney()), Double.toString(playerTank.getHealthMultiplier()), Double.toString(playerTank.getProjectileVelocityMultiplier()), Double.toString(playerTank.getProjectileDamageMultiplier()), Double.toString(playerTank.getDistanceMultiplier())};

                manager.generateTank(userInformation);
                manager.generateOpponent();

                gamePane.getStyleClass().add("game");

                gamePane.start();

                gamePane.setupTankGraphics(gamePane);
                gamePane.setupStatsGraphics(gamePane);

                int maxWidth = gamePane.getTerrain().getXMax();
                int maxHeight = gamePane.getTerrain().getYMax();

                Scene gameScene = new Scene(gamePane, maxWidth, maxHeight);

                gameScene.getStylesheets().add("styles.css");

                stage.setScene(gameScene);

                gamePane.runRound(gameScene, stage, upgradePane, upgradeScene);
            } else {
                upgradeError.setText("Please choose a difficulty.");
                upgradeError.setVisible(true);
            }
        });

        quitButton.setOnAction(e -> {
            stage.close();
        });

        upgradePaneInner.add(titlePane, 0, 0);
        upgradePaneInner.setColumnSpan(titlePane, 2);
        upgradePaneInner.add(healthMultiplierPane, 0, 1);
        upgradePaneInner.add(projectileVelocityMultiplierPane, 1, 1);
        upgradePaneInner.add(projectileDamageMultiplierPane, 0, 2);
        upgradePaneInner.add(distanceMultiplierPane, 1, 2);
        upgradePaneInner.add(difficultyPane, 0, 3);
        upgradePaneInner.add(optionsPane, 1, 3);
        upgradePaneInner.add(upgradeError, 0, 4);
        upgradePaneInner.setColumnSpan(upgradeError, 2);
        
        upgradePaneInner.setHgap(50);
        upgradePaneInner.setVgap(50);
        upgradePaneInner.setPadding(new Insets(50,50,50,50));

        upgradePaneInner.setHalignment(upgradeError, HPos.CENTER);
        upgradePaneInner.setValignment(upgradeError, VPos.CENTER);

        upgradePane.getChildren().add(upgradePaneInner);
        upgradePane.setAlignment(Pos.CENTER);

        stage.setScene(upgradeScene);
    }    

    public void runTurn() {
        if (manager.getTurnNumber() % 2 == 0) {
            //player's turn
            playerTank.newTurn();

            awaitKeyboardInput(playerTank, playerCircle, playerLine);
        } else {
            //opponent's turn
            opponentTank.newTurn();

            //randomly move a number of spaces
            opponentTank.randomMove(terrain, playerTank.getXCoord());

            //start the animation chain
            animateMovement(opponentTank, opponentCircle, opponentLine, playerTank.getXCoord());
        }

        //increment turn number
        manager.nextTurn();
    }

	public void setupTankGraphics(Pane gamePane) {
        Pane pane = new Pane();

        double opponentYCoord = terrain.getYMax() - opponentTank.getYCoord();
        double playerYCoord = terrain.getYMax() - playerTank.getYCoord();

		opponentCircle = new Circle(opponentTank.getXCoord(), terrain.getYMax() - opponentTank.getYCoord(), 10, Color.RED);
		playerCircle = new Circle(playerTank.getXCoord(), terrain.getYMax() - playerTank.getYCoord(), 10, Color.BLUE);

        opponentLine = new Line(opponentTank.getXCoord(), opponentYCoord, opponentTank.getXCoord() + 20, opponentYCoord);
        playerLine = new Line(playerTank.getXCoord(), playerYCoord, playerTank.getXCoord() + 20, playerYCoord);

        opponentLine.setStroke(Color.RED);
        playerLine.setStroke(Color.BLUE);

        opponentLine.setStrokeWidth(5);
        playerLine.setStrokeWidth(5);

        opponentLine.getTransforms().add(new Rotate(-Math.toDegrees(opponentTank.getAngle()), opponentCircle.getCenterX(), opponentCircle.getCenterY()));
        playerLine.getTransforms().add(new Rotate(-Math.toDegrees(playerTank.getAngle()),playerCircle.getCenterX(), playerCircle.getCenterY()));

        projectileCircle = new Circle(0, 0, 5, Color.BLACK);
        projectileCircle.setVisible(false);

        pane.getChildren().add(getCanvas());

        pane.getChildren().add(opponentCircle);
        pane.getChildren().add(playerCircle);

        pane.getChildren().add(opponentLine);
        pane.getChildren().add(playerLine);

        pane.getChildren().add(projectileCircle);

        winnerText = new Label();
        winnerText.getStyleClass().add("title_dark");
        winButton = new Button("Continue");
        winButton.getStyleClass().add("title");
        winPane = new GridPane();
        winPane.add(winnerText, 0, 0);
        winPane.add(winButton, 0, 1);
        pane.getChildren().add(winPane);
        winPane.setLayoutX(manager.getTerrain().getXMax() / 2);
        winPane.setLayoutY(manager.getTerrain().getYMax() / 2);
        winPane.setVisible(false);

        gamePane.getChildren().add(pane);
	}

    public void setupStatsGraphics(Pane gamePane) {
        BorderPane statsPane = new BorderPane();

        GridPane playerPane = new GridPane();
        Label playerName = new Label(playerTank.getName());
        playerName.getStyleClass().add("title_dark");
        playerTurns = new Label("Turns: " + manager.getTurnsRemaining());
        playerTurns.getStyleClass().add("text_dark");
        playerHealthProgress = new ProgressBar();
        playerHealthProgress.setProgress(1);
        playerPane.add(playerName, 0, 0);
        playerPane.add(playerHealthProgress, 0, 1);
        playerPane.add(playerTurns, 0, 2);

        GridPane opponentPane = new GridPane();
        Label opponentName = new Label("Opponent");
        opponentName.getStyleClass().add("title_dark");
        opponentHealthProgress = new ProgressBar();
        opponentHealthProgress.setProgress(1);
        opponentPane.add(opponentName, 0, 0);
        opponentPane.add(opponentHealthProgress, 0, 1);
        
        statsPane.setLeft(playerPane);
        statsPane.setRight(opponentPane);

        gamePane.getChildren().add(statsPane);

        statsPane.setLayoutX(0);
        statsPane.setLayoutY(0);
        statsPane.setMinWidth(terrain.getXMax());
	}

    private void setupTerrainGraphics(Terrain terrain) {
        for (int i = 0; i < terrain.getXMax(); i++) {
            g.setStroke(Color.FORESTGREEN);
            g.strokeRect(i, terrain.getYMax() - terrain.getTerrainYCoordinate(i), 1, terrain.getYMax());
        }
    }

    public void awaitKeyboardInput(Tank tank, Circle circle, Line line) {
        mainTimeline.stop();

        keyHandler = new EventHandler<KeyEvent>() {
            @Override public void handle(KeyEvent e) {
                double previousXCoord = tank.getXCoord();

                if (e.getCode() == KeyCode.UP) {
                    //cannon movement counterclockwise
                    double newAngle = Math.toDegrees(tank.getAngle()) + 1;

                    if (tank.isLegalAngle(newAngle)) {
                        tank.setAngle(Math.toRadians(newAngle));
                        line.getTransforms().add(new Rotate(-1, line.getStartX(), line.getStartY()));
                    }
                } else if (e.getCode() == KeyCode.DOWN) {
                    //cannon movement clockwise
                    double newAngle = Math.toDegrees(tank.getAngle()) - 1;

                    if (tank.isLegalAngle(newAngle)) {
                        tank.setAngle(Math.toRadians(newAngle));
                        line.getTransforms().add(new Rotate(1, line.getStartX(), line.getStartY()));
                    }
                } else if (e.getCode() == KeyCode.RIGHT && tank.canMove(1, 1, terrain.getXMax())) {
                    //tank movement right
                    tank.move(1, terrain, 1);
                    moveCircle(line, circle, tank, tank.getXCoord(), previousXCoord);
                } else if (e.getCode() == KeyCode.LEFT && tank.canMove(0, 1, terrain.getXMax())) {
                    //tank movement left
                    tank.move(0, terrain, 1);
                    moveCircle(line, circle, tank, tank.getXCoord(), previousXCoord);
                } else if (e.getCode() == KeyCode.SPACE) {
                    //stop listening for keyboard input for now
                    scene.removeEventHandler(KeyEvent.KEY_PRESSED, keyHandler);

                    Projectile projectile = new Projectile(tank.getXCoord(), tank.getYCoord() + 5, tank.getAngle(), tank.getProjectileVelocityMultiplier());

                    animateProjectile(tank, projectile);
                }
            }
        };

        scene.addEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
    }

    public void animateMovement(Tank tank, Circle circle, Line line, double otherTankXCoord) {
        mainTimeline.stop();

        if (tank.getPreviousXCoord() > tank.getXCoord()) {
            movementDirection = 0;
        } else {
            movementDirection = 1;
        }

        previousXCoord = tank.getPreviousXCoord();

        Timeline animateMovementTimeline = new Timeline();
        animateMovementTimeline.setCycleCount(Timeline.INDEFINITE);
    
		KeyFrame animateMovement = new KeyFrame(Duration.millis(25), action -> {
            //animate opponent movement
            if (movementDirection == 0 && previousXCoord > tank.getXCoord()) {
                double i = previousXCoord--;

                moveCircle(line, circle, tank, i, ++i);
            } else if (movementDirection == 1 && previousXCoord < tank.getXCoord()) {
                double i = previousXCoord++;
                
                moveCircle(line, circle, tank, i, --i);
            } else {
                animateMovementTimeline.stop();
                

                animateCannonMovement(line, tank, circle, otherTankXCoord);
            }
        });

        animateMovementTimeline.getKeyFrames().add(animateMovement);

		animateMovementTimeline.play();
	}

    public void moveCircle(Line line, Circle circle, Tank tank, double newXCoord, double previousXCoord) {
        //set rotate to 0 deg
        line.getTransforms().add(new Rotate(Math.toDegrees(tank.getAngle())));

        double yCoord = terrain.getYMax() - terrain.getTerrainYCoordinate(newXCoord);
        
        circle.setCenterX(newXCoord);
        circle.setCenterY(yCoord);

        int translation;
        
        //currently broke sometimes
        line.getTransforms().add(new Translate(newXCoord - previousXCoord,  yCoord - (terrain.getYMax() - terrain.getTerrainYCoordinate(previousXCoord))));
        
        //reset rotate back to what it was before 
        line.getTransforms().add(new Rotate(-Math.toDegrees(tank.getAngle())));
    }

    public void animateCannonMovement(Line line, Tank tank, Circle circle, double otherTankXCoord) {
        Timeline animateCannonMovementTimeline = new Timeline();
        animateCannonMovementTimeline.setCycleCount(Timeline.INDEFINITE);

        cannonPreviousAngle = tank.getAngle();
        
        Projectile projectile;
        projectile = new Projectile(tank.getXCoord(), tank.getYCoord() + 5, tank.getAngle(), tank.getProjectileVelocityMultiplier());

        tank.cannonMove(terrain, projectile, otherTankXCoord);
        projectile.setAngle(tank.getAngle());

        // 0 = clockwise, 1 = counterclockwise
        if (tank.getPreviousAngle() > tank.getAngle()) {
            cannonDirection = 0;
        } else {
            cannonDirection = 1;
        }

		KeyFrame animateCannonMovement = new KeyFrame(Duration.millis(25), action -> {
            if (cannonDirection == 0 && cannonPreviousAngle > tank.getAngle()) {
                cannonPreviousAngle = cannonPreviousAngle - Math.toRadians(1);
                           
                line.getTransforms().add(new Rotate(1, line.getStartX(), line.getStartY()));
            } else if (cannonDirection == 1 && cannonPreviousAngle < tank.getAngle()) {
                cannonPreviousAngle = cannonPreviousAngle + Math.toRadians(1);
                                
                line.getTransforms().add(new Rotate(-1, line.getStartX(), line.getStartY()));
            } else {
                animateCannonMovementTimeline.stop();

                animateProjectile(tank, projectile);
            }
        });

        animateCannonMovementTimeline.getKeyFrames().add(animateCannonMovement);

        animateCannonMovementTimeline.play();
    }

    public void animateProjectile(Tank tank, Projectile projectile) {
        Timeline animateProjectileTimeline = new Timeline();
        animateProjectileTimeline.setCycleCount(Timeline.INDEFINITE);

        projectileCircle.setCenterX(tank.getXCoord());
        projectileCircle.setCenterY(terrain.getYMax() - tank.getYCoord());

        projectile.fire();

        projectileTime = new Date().getTime();
        
		KeyFrame animateProjectile = new KeyFrame(Duration.millis(5), action -> {
            
            if (!projectile.isLanded(terrain)) {
                projectile.moveX(projectileTime);
                projectile.moveY(projectileTime);

                projectileTime += 20;

                projectileCircle.setCenterX(projectile.getX());
                projectileCircle.setCenterY(terrain.getYMax() - projectile.getY());
            } else {
                projectileCircle.setVisible(false);

                animateProjectileTimeline.stop();

                //figure out if the projectile hit player
                manager.calculateProjectileDamage(projectile);

                mainTimeline.play();
            }
        });

        animateProjectileTimeline.getKeyFrames().add(animateProjectile);

        projectileCircle.setVisible(true);
        animateProjectileTimeline.play();
    }

    public void updateStatsMenu() {
        playerHealthProgress.setProgress(playerTank.getHealth() / playerTank.getStartingHealth());
        opponentHealthProgress.setProgress(opponentTank.getHealth() / opponentTank.getStartingHealth());
        playerTurns.setText("Turns: " + manager.getTurnsRemaining());
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public Terrain getTerrain() {
        return terrain;
    }
}