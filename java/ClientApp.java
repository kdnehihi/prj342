import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * JavaFX Client Application for 3 Card Poker.
 * Provides a GUI with welcome screen, game screen, and result screen.
 */
public class ClientApp extends Application {
    private Stage primaryStage;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private int totalWinnings = 0;
    private boolean connected = false;
    
    // Scenes
    private Scene welcomeScene;
    private Scene gameScene;
    private Scene resultScene;
    
    // Welcome screen components
    private TextField welcomeHostField;
    private TextField welcomePortField;
    private Button startGameButton;
    
    // Game screen components
    private TextField anteField;
    private TextField pairPlusField;
    private Button dealButton;
    private VBox bettingSection; // Reference to betting section for hiding
    private HBox playerCardsBox;
    private HBox dealerCardsBox;
    private Button playButton;
    private Button foldButton;
    private Label gameStatusLabel;
    private Label gameWinningsLabel;
    
    // Result screen components
    private HBox resultPlayerCardsBox;
    private HBox resultDealerCardsBox;
    private Label resultTitleLabel;
    private Label resultMessageLabel;
    private Label resultDetailsLabel;
    private Label resultWinningsLabel;
    private Button playAgainButton;
    private Button backToMenuButton;
    
    private PokerInfo currentCardsInfo;
    
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("3 Card Poker");
        
        createWelcomeScreen();
        createGameScreen();
        createResultScreen();
        
        primaryStage.setScene(welcomeScene);
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    private void createWelcomeScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2c3e50;");
        
        VBox centerBox = new VBox(40);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(60));
        
        // Title
        Label titleLabel = new Label("3 CARD POKER");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 64));
        titleLabel.setTextFill(Color.GOLD);
        titleLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 15, 0, 0, 0);");
        
        // Subtitle
        Label subtitleLabel = new Label("Welcome to the Ultimate Poker Experience");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 22));
        subtitleLabel.setTextFill(Color.WHITE);
        subtitleLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 5, 0, 0, 0);");
        
        // Connection box - styled more elegantly
        VBox connectionBox = new VBox(20);
        connectionBox.setAlignment(Pos.CENTER);
        connectionBox.setPadding(new Insets(30, 40, 30, 40));
        connectionBox.setStyle("-fx-background-color: rgba(0,0,0,0.75); -fx-background-radius: 20;");
        
        Label connectionTitle = new Label("Enter Server Details");
        connectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        connectionTitle.setTextFill(Color.GOLD);
        
        // Host field with icon-like styling
        HBox hostBox = new HBox(10);
        hostBox.setAlignment(Pos.CENTER);
        Label hostIcon = new Label("🌐");
        hostIcon.setFont(Font.font(20));
        welcomeHostField = new TextField("localhost");
        welcomeHostField.setPrefWidth(250);
        welcomeHostField.setPrefHeight(35);
        welcomeHostField.setPromptText("Enter server address");
        welcomeHostField.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-padding: 8 12; " +
            "-fx-background-color: rgba(255,255,255,0.9); " +
            "-fx-background-radius: 8;"
        );
        hostBox.getChildren().addAll(hostIcon, welcomeHostField);
        
        // Port field with icon-like styling
        HBox portBox = new HBox(10);
        portBox.setAlignment(Pos.CENTER);
        Label portIcon = new Label("🔌");
        portIcon.setFont(Font.font(20));
        welcomePortField = new TextField("5555");
        welcomePortField.setPrefWidth(250);
        welcomePortField.setPrefHeight(35);
        welcomePortField.setPromptText("Enter port number");
        welcomePortField.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-padding: 8 12; " +
            "-fx-background-color: rgba(255,255,255,0.9); " +
            "-fx-background-radius: 8;"
        );
        portBox.getChildren().addAll(portIcon, welcomePortField);
        
        // Start game button - more prominent
        startGameButton = new Button("▶ START GAME");
        startGameButton.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        startGameButton.setPrefWidth(300);
        startGameButton.setPrefHeight(60);
        startGameButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #45a049); " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 8, 0, 0, 3); " +
            "-fx-cursor: hand;"
        );
        startGameButton.setOnMouseEntered(e -> startGameButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #45a049, #3d8b40); " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 4); " +
            "-fx-cursor: hand;"
        ));
        startGameButton.setOnMouseExited(e -> startGameButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #45a049); " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 8, 0, 0, 3); " +
            "-fx-cursor: hand;"
        ));
        startGameButton.setOnAction(e -> connectAndStartGame());
        
        connectionBox.getChildren().addAll(connectionTitle, hostBox, portBox, startGameButton);
        
        centerBox.getChildren().addAll(titleLabel, subtitleLabel, connectionBox);
        root.setCenter(centerBox);
        
        welcomeScene = new Scene(root, 1000, 700);
    }
    
    private void createGameScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");
        
        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(20));
        mainBox.setAlignment(Pos.CENTER);
        
        // Top status bar
        HBox statusBar = new HBox(20);
        statusBar.setAlignment(Pos.CENTER);
        statusBar.setPadding(new Insets(10));
        statusBar.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-background-radius: 10;");
        
        gameStatusLabel = new Label("Connected to Server");
        gameStatusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gameStatusLabel.setTextFill(Color.WHITE);
        
        gameWinningsLabel = new Label("Total Winnings: 0");
        gameWinningsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gameWinningsLabel.setTextFill(Color.GOLD);
        
        Button disconnectBtn = new Button("Disconnect");
        disconnectBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5;");
        disconnectBtn.setOnAction(e -> disconnectAndReturnToWelcome());
        
        statusBar.getChildren().addAll(gameStatusLabel, new Region(), gameWinningsLabel, disconnectBtn);
        HBox.setHgrow(statusBar.getChildren().get(1), Priority.ALWAYS);
        
        // Betting section
        bettingSection = new VBox(15);
        bettingSection.setAlignment(Pos.CENTER);
        bettingSection.setPadding(new Insets(20));
        bettingSection.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-background-radius: 15;");
        
        Label bettingTitle = new Label("Place Your Bets");
        bettingTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        bettingTitle.setTextFill(Color.WHITE);
        
        HBox anteBox = new HBox(10);
        anteBox.setAlignment(Pos.CENTER);
        Label anteLabel = new Label("Ante (5-25):");
        anteLabel.setFont(Font.font("Arial", 14));
        anteLabel.setTextFill(Color.WHITE);
        anteField = new TextField("10");
        anteField.setPrefWidth(100);
        anteField.setStyle("-fx-font-size: 14px; -fx-padding: 8;");
        anteBox.getChildren().addAll(anteLabel, anteField);
        
        HBox pairPlusBox = new HBox(10);
        pairPlusBox.setAlignment(Pos.CENTER);
        Label pairPlusLabel = new Label("Pair Plus (0 or 5-25):");
        pairPlusLabel.setFont(Font.font("Arial", 14));
        pairPlusLabel.setTextFill(Color.WHITE);
        pairPlusField = new TextField("0");
        pairPlusField.setPrefWidth(100);
        pairPlusField.setStyle("-fx-font-size: 14px; -fx-padding: 8;");
        pairPlusBox.getChildren().addAll(pairPlusLabel, pairPlusField);
        
        dealButton = new Button("DEAL CARDS");
        dealButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        dealButton.setPrefWidth(200);
        dealButton.setPrefHeight(45);
        dealButton.setStyle(
            "-fx-background-color: #2196F3; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 2);"
        );
        dealButton.setOnMouseEntered(e -> dealButton.setStyle(
            "-fx-background-color: #1976D2; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 2);"
        ));
        dealButton.setOnMouseExited(e -> dealButton.setStyle(
            "-fx-background-color: #2196F3; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 2);"
        ));
        dealButton.setOnAction(e -> dealCards());
        dealButton.setDisable(true);
        
        anteField.textProperty().addListener((obs, oldVal, newVal) -> validateAndEnableDealButton());
        pairPlusField.textProperty().addListener((obs, oldVal, newVal) -> validateAndEnableDealButton());
        
        bettingSection.getChildren().addAll(bettingTitle, anteBox, pairPlusBox, dealButton);
        
        // Dealer section
        VBox dealerSection = new VBox(10);
        dealerSection.setAlignment(Pos.CENTER);
        Label dealerLabel = new Label("DEALER");
        dealerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        dealerLabel.setTextFill(Color.WHITE);
        dealerCardsBox = new HBox(15);
        dealerCardsBox.setAlignment(Pos.CENTER);
        dealerCardsBox.setPadding(new Insets(10));
        dealerCardsBox.setMinHeight(180);
        dealerSection.getChildren().addAll(dealerLabel, dealerCardsBox);
        
        // Player section
        VBox playerSection = new VBox(15);
        playerSection.setAlignment(Pos.CENTER);
        playerSection.setPadding(new Insets(10));
        playerSection.setSpacing(15);
        playerSection.setVisible(true);
        playerSection.setManaged(true);
        
        Label playerLabel = new Label("YOU");
        playerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        playerLabel.setTextFill(Color.WHITE);
        playerLabel.setVisible(true);
        
        playerCardsBox = new HBox(15);
        playerCardsBox.setAlignment(Pos.CENTER);
        playerCardsBox.setPadding(new Insets(10));
        playerCardsBox.setMinHeight(180); // Ensure it takes up space even when empty
        playerCardsBox.setMinWidth(400); // Ensure minimum width
        playerCardsBox.setPrefWidth(400);
        playerCardsBox.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 2; -fx-border-radius: 10;");
        playerCardsBox.setVisible(true);
        playerCardsBox.setManaged(true);
        
        // Action buttons - Make them more prominent and always visible
        playButton = new Button("✓ PLAY");
        playButton.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        playButton.setPrefWidth(180);
        playButton.setPrefHeight(60);
        playButton.setVisible(true);
        playButton.setManaged(true);
        playButton.setDisable(true);
        // Disabled style - visible but grayed out
        playButton.setStyle(
            "-fx-background-color: rgba(76, 175, 80, 0.6); " +
            "-fx-text-fill: rgba(255,255,255,0.7); " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2); " +
            "-fx-cursor: default;"
        );
        playButton.setOnMouseEntered(e -> {
            if (!playButton.isDisable()) {
                playButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #45a049, #3d8b40); " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 4); " +
                    "-fx-cursor: hand;"
                );
            }
        });
        playButton.setOnMouseExited(e -> {
            if (!playButton.isDisable()) {
                playButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #4CAF50, #45a049); " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 8, 0, 0, 3); " +
                    "-fx-cursor: hand;"
                );
            }
        });
        playButton.setOnAction(e -> playHand());
        
        foldButton = new Button("✗ FOLD");
        foldButton.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        foldButton.setPrefWidth(180);
        foldButton.setPrefHeight(60);
        foldButton.setStyle(
            "-fx-background-color: rgba(244, 67, 54, 0.6); " +
            "-fx-text-fill: rgba(255,255,255,0.7); " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2); " +
            "-fx-cursor: default;"
        );
        foldButton.setOnMouseEntered(e -> {
            if (!foldButton.isDisable()) {
                foldButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #da190b, #c62828); " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 4); " +
                    "-fx-cursor: hand; " +
                    "-fx-opacity: 1.0;"
                );
            }
        });
        foldButton.setOnMouseExited(e -> {
            if (!foldButton.isDisable()) {
                foldButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #f44336, #da190b); " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 8, 0, 0, 3); " +
                    "-fx-cursor: hand; " +
                    "-fx-opacity: 1.0;"
                );
            }
        });
        foldButton.setOnAction(e -> foldHand());
        foldButton.setDisable(true);
        foldButton.setVisible(true);
        foldButton.setManaged(true);
        
        HBox actionBox = new HBox(20, playButton, foldButton);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setPadding(new Insets(10));
        actionBox.setMinHeight(70); // Ensure buttons area takes up space
        actionBox.setAlignment(Pos.CENTER);
        
        playerSection.getChildren().addAll(playerLabel, playerCardsBox, actionBox);
        
        mainBox.getChildren().addAll(statusBar, bettingSection, dealerSection, playerSection);
        root.setCenter(mainBox);
        
        gameScene = new Scene(root, 1000, 700);
    }
    
    private void createResultScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");
        
        VBox mainBox = new VBox(25);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setPadding(new Insets(30));
        
        // Result title
        resultTitleLabel = new Label("GAME RESULT");
        resultTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        resultTitleLabel.setTextFill(Color.GOLD);
        resultTitleLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
        
        // Result message
        resultMessageLabel = new Label("");
        resultMessageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        resultMessageLabel.setTextFill(Color.WHITE);
        resultMessageLabel.setWrapText(true);
        resultMessageLabel.setTextAlignment(TextAlignment.CENTER);
        resultMessageLabel.setPrefWidth(600);
        
        // Cards display
        VBox cardsBox = new VBox(20);
        cardsBox.setAlignment(Pos.CENTER);
        cardsBox.setPadding(new Insets(20));
        cardsBox.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-background-radius: 15;");
        
        Label dealerResultLabel = new Label("Dealer Cards");
        dealerResultLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        dealerResultLabel.setTextFill(Color.WHITE);
        resultDealerCardsBox = new HBox(15);
        resultDealerCardsBox.setAlignment(Pos.CENTER);
        
        Label playerResultLabel = new Label("Your Cards");
        playerResultLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        playerResultLabel.setTextFill(Color.WHITE);
        resultPlayerCardsBox = new HBox(15);
        resultPlayerCardsBox.setAlignment(Pos.CENTER);
        
        cardsBox.getChildren().addAll(dealerResultLabel, resultDealerCardsBox, playerResultLabel, resultPlayerCardsBox);
        
        // Result details
        resultDetailsLabel = new Label("");
        resultDetailsLabel.setFont(Font.font("Arial", 16));
        resultDetailsLabel.setTextFill(Color.WHITE);
        resultDetailsLabel.setWrapText(true);
        resultDetailsLabel.setTextAlignment(TextAlignment.CENTER);
        resultDetailsLabel.setPrefWidth(600);
        
        // Winnings
        resultWinningsLabel = new Label("Total Winnings: 0");
        resultWinningsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        resultWinningsLabel.setTextFill(Color.GOLD);
        resultWinningsLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 0);");
        
        // Buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        
        playAgainButton = new Button("PLAY AGAIN");
        playAgainButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        playAgainButton.setPrefWidth(200);
        playAgainButton.setPrefHeight(50);
        playAgainButton.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 2);"
        );
        playAgainButton.setOnMouseEntered(e -> playAgainButton.setStyle(
            "-fx-background-color: #45a049; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 2);"
        ));
        playAgainButton.setOnMouseExited(e -> playAgainButton.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 2);"
        ));
        playAgainButton.setOnAction(e -> returnToGame());
        playAgainButton.setDisable(true);
        
        backToMenuButton = new Button("BACK TO MENU");
        backToMenuButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        backToMenuButton.setPrefWidth(200);
        backToMenuButton.setPrefHeight(50);
        backToMenuButton.setStyle(
            "-fx-background-color: #2196F3; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 2);"
        );
        backToMenuButton.setOnMouseEntered(e -> backToMenuButton.setStyle(
            "-fx-background-color: #1976D2; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 2);"
        ));
        backToMenuButton.setOnMouseExited(e -> backToMenuButton.setStyle(
            "-fx-background-color: #2196F3; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 2);"
        ));
        backToMenuButton.setOnAction(e -> disconnectAndReturnToWelcome());
        
        buttonBox.getChildren().addAll(playAgainButton, backToMenuButton);
        
        mainBox.getChildren().addAll(resultTitleLabel, resultMessageLabel, cardsBox, resultDetailsLabel, resultWinningsLabel, buttonBox);
        root.setCenter(mainBox);
        
        resultScene = new Scene(root, 1000, 700);
    }
    
    private void connectAndStartGame() {
        try {
            String host = welcomeHostField.getText().trim();
            int port = Integer.parseInt(welcomePortField.getText().trim());
            
            socket = new Socket(host, port);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            connected = true;
            
            gameStatusLabel.setText("Connected to " + host + ":" + port);
            gameWinningsLabel.setText("Total Winnings: 0");
            totalWinnings = 0;
            
            primaryStage.setScene(gameScene);
        } catch (Exception e) {
            showAlert("Connection Error", "Failed to connect to server:\n" + e.getMessage());
        }
    }
    
    private void disconnectAndReturnToWelcome() {
        try {
            if (connected && output != null) {
                PokerInfo disconnect = new PokerInfo();
                disconnect.setMessageType(PokerInfo.MessageType.DISCONNECT);
                output.writeObject(disconnect);
            }
        } catch (Exception e) {
            // Ignore
        }
        
        closeConnection();
        primaryStage.setScene(welcomeScene);
    }
    
    private void dealCards() {
        try {
            int anteBet = Integer.parseInt(anteField.getText());
            int pairPlusBet = Integer.parseInt(pairPlusField.getText());
            
            if (anteBet < 5 || anteBet > 25) {
                showAlert("Invalid Bet", "Ante must be between 5 and 25");
                return;
            }
            if (pairPlusBet != 0 && (pairPlusBet < 5 || pairPlusBet > 25)) {
                showAlert("Invalid Bet", "Pair Plus must be 0 or between 5 and 25");
                return;
            }
            
            dealButton.setDisable(true);
            gameStatusLabel.setText("Dealing cards...");
            
            new Thread(() -> {
                try {
                    PokerInfo betInfo = new PokerInfo();
                    betInfo.setMessageType(PokerInfo.MessageType.INITIAL_BET);
                    betInfo.setAnteBet(anteBet);
                    betInfo.setPairPlusBet(pairPlusBet);
                    output.writeObject(betInfo);
                    output.flush();
                    
                    PokerInfo cardsInfo = (PokerInfo) input.readObject();
                    
                    if (cardsInfo == null || cardsInfo.getPlayerCards() == null || cardsInfo.getPlayerCards().isEmpty()) {
                        Platform.runLater(() -> {
                            showAlert("Error", "No cards received from server");
                            dealButton.setDisable(false);
                            gameStatusLabel.setText("Error receiving cards");
                        });
                        return;
                    }
                    
                    Platform.runLater(() -> {
                        System.out.println("DEBUG: Received cards from server");
                        System.out.println("DEBUG: Player cards count: " + (cardsInfo.getPlayerCards() != null ? cardsInfo.getPlayerCards().size() : 0));
                        System.out.println("DEBUG: Dealer cards count: " + (cardsInfo.getDealerCards() != null ? cardsInfo.getDealerCards().size() : 0));
                        
                        // Display player cards (face up) - MUST show actual cards
                        if (cardsInfo.getPlayerCards() != null && !cardsInfo.getPlayerCards().isEmpty()) {
                            System.out.println("DEBUG: Displaying player cards...");
                            displayCards(playerCardsBox, cardsInfo.getPlayerCards(), false);
                            System.out.println("DEBUG: Player cards box children count: " + playerCardsBox.getChildren().size());
                        } else {
                            System.err.println("ERROR: Player cards are null or empty!");
                        }
                        
                        // Display dealer cards (face down) - MUST show back_card.jpeg
                        if (cardsInfo.getDealerCards() != null && !cardsInfo.getDealerCards().isEmpty()) {
                            System.out.println("DEBUG: Displaying dealer cards (face down)...");
                            displayCards(dealerCardsBox, cardsInfo.getDealerCards(), true);
                        }
                        
                        currentCardsInfo = cardsInfo;
                        
                        // Hide betting section completely
                        bettingSection.setVisible(false);
                        bettingSection.setManaged(false);
                        
                        // Enable PLAY and FOLD buttons - make them visible and prominent
                        System.out.println("DEBUG: Enabling PLAY and FOLD buttons");
                        playButton.setDisable(false);
                        foldButton.setDisable(false);
                        playButton.setOpacity(1.0);
                        foldButton.setOpacity(1.0);
                        playButton.setVisible(true);
                        foldButton.setVisible(true);
                        playButton.setManaged(true);
                        foldButton.setManaged(true);
                        
                        // Update button styles to enabled state
                        playButton.setStyle(
                            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #45a049); " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 12; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 8, 0, 0, 3); " +
                            "-fx-cursor: hand;"
                        );
                        foldButton.setStyle(
                            "-fx-background-color: linear-gradient(to bottom, #f44336, #da190b); " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 12; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 8, 0, 0, 3); " +
                            "-fx-cursor: hand;"
                        );
                        
                        // Disable betting fields
                        anteField.setDisable(true);
                        pairPlusField.setDisable(true);
                        dealButton.setDisable(true);
                        
                        // Update status
                        gameStatusLabel.setText("🎴 Cards Dealt! Choose PLAY or FOLD");
                        gameStatusLabel.setTextFill(Color.YELLOW);
                        
                        // Force layout update
                        playerCardsBox.requestLayout();
                        dealerCardsBox.requestLayout();
                        playButton.getParent().requestLayout();
                        foldButton.getParent().requestLayout();
                        
                        System.out.println("DEBUG: UI update complete");
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert("Error", "Failed to deal cards: " + e.getMessage());
                        dealButton.setDisable(false);
                        gameStatusLabel.setText("Error: " + e.getMessage());
                    });
                    e.printStackTrace();
                }
            }).start();
            
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for bets");
            dealButton.setDisable(false);
        }
    }
    
    private void playHand() {
        playButton.setDisable(true);
        foldButton.setDisable(true);
        gameStatusLabel.setText("Processing...");
        
        new Thread(() -> {
            try {
                int anteBet = Integer.parseInt(anteField.getText());
                int pairPlusBet = Integer.parseInt(pairPlusField.getText());
                
                PokerInfo actionInfo = new PokerInfo();
                actionInfo.setMessageType(PokerInfo.MessageType.PLAYER_ACTION);
                actionInfo.setPlayerAction(PokerInfo.PlayerAction.PLAY);
                actionInfo.setAnteBet(anteBet);
                actionInfo.setPairPlusBet(pairPlusBet);
                actionInfo.setPlayBet(anteBet);
                actionInfo.setPlayerCards(currentCardsInfo.getPlayerCards());
                actionInfo.setDealerCards(currentCardsInfo.getDealerCards());
                
                output.writeObject(actionInfo);
                output.flush();
                
                processResult();
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to play: " + e.getMessage());
                    playButton.setDisable(false);
                    foldButton.setDisable(false);
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    private void foldHand() {
        playButton.setDisable(true);
        foldButton.setDisable(true);
        gameStatusLabel.setText("Processing...");
        
        new Thread(() -> {
            try {
                int anteBet = Integer.parseInt(anteField.getText());
                int pairPlusBet = Integer.parseInt(pairPlusField.getText());
                
                PokerInfo actionInfo = new PokerInfo();
                actionInfo.setMessageType(PokerInfo.MessageType.PLAYER_ACTION);
                actionInfo.setPlayerAction(PokerInfo.PlayerAction.FOLD);
                actionInfo.setAnteBet(anteBet);
                actionInfo.setPairPlusBet(pairPlusBet);
                actionInfo.setPlayBet(0);
                actionInfo.setPlayerCards(currentCardsInfo.getPlayerCards());
                actionInfo.setDealerCards(currentCardsInfo.getDealerCards());
                
                output.writeObject(actionInfo);
                output.flush();
                
                processResult();
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to fold: " + e.getMessage());
                    playButton.setDisable(false);
                    foldButton.setDisable(false);
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    private void processResult() {
        try {
            PokerInfo result = (PokerInfo) input.readObject();
            
            if (result == null) {
                Platform.runLater(() -> showAlert("Error", "Received null result from server"));
                return;
            }
            
            Platform.runLater(() -> {
                // Display cards - dealer cards revealed
                displayCards(resultPlayerCardsBox, result.getPlayerCards(), false);
                displayCards(resultDealerCardsBox, result.getDealerCards(), false);
                
                // Determine win/loss
                int delta = result.getDeltaWinningsThisHand();
                String winLossMessage = "";
                Color messageColor = Color.WHITE;
                
                if (delta > 0) {
                    winLossMessage = "🎉 YOU WIN! 🎉";
                    messageColor = Color.LIMEGREEN;
                } else if (delta < 0) {
                    winLossMessage = "😔 YOU LOSE 😔";
                    messageColor = Color.INDIANRED;
                } else {
                    winLossMessage = "🤝 PUSH (Tie) 🤝";
                    messageColor = Color.LIGHTGRAY;
                }
                
                resultMessageLabel.setText(winLossMessage);
                resultMessageLabel.setTextFill(messageColor);
                
                // Build details
                StringBuilder details = new StringBuilder();
                details.append("═══════════════════════════════\n");
                
                if (result.getStatusMessage() != null) {
                    details.append(result.getStatusMessage()).append("\n\n");
                }
                
                if (result.getPlayerAction() == PokerInfo.PlayerAction.PLAY) {
                    if (result.isDealerQualified()) {
                        details.append("✓ Dealer qualified\n");
                    } else {
                        details.append("✗ Dealer did not qualify\n");
                        details.append("   → Play bet returned, Ante pushes\n");
                    }
                } else {
                    details.append("You folded - Lost Ante and Pair Plus\n");
                }
                
                details.append("\n");
                
                if (result.getPairPlusPayout() > 0) {
                    details.append("💰 Pair Plus: Won ").append(result.getPairPlusPayout()).append("\n");
                } else if (Integer.parseInt(pairPlusField.getText()) > 0) {
                    details.append("❌ Pair Plus: Lost ").append(Integer.parseInt(pairPlusField.getText())).append("\n");
                }
                
                details.append("\n");
                details.append("This Hand: ").append(delta >= 0 ? "+" : "").append(delta).append("\n");
                details.append("═══════════════════════════════");
                
                resultDetailsLabel.setText(details.toString());
                
                totalWinnings = result.getTotalWinnings();
                resultWinningsLabel.setText("Total Winnings: " + (totalWinnings >= 0 ? "+" : "") + totalWinnings);
                gameWinningsLabel.setText("Total Winnings: " + (totalWinnings >= 0 ? "+" : "") + totalWinnings);
                
                // Change winnings color based on positive/negative
                if (totalWinnings > 0) {
                    resultWinningsLabel.setTextFill(Color.LIMEGREEN);
                    gameWinningsLabel.setTextFill(Color.LIMEGREEN);
                } else if (totalWinnings < 0) {
                    resultWinningsLabel.setTextFill(Color.INDIANRED);
                    gameWinningsLabel.setTextFill(Color.INDIANRED);
                } else {
                    resultWinningsLabel.setTextFill(Color.GOLD);
                    gameWinningsLabel.setTextFill(Color.GOLD);
                }
                
                playAgainButton.setDisable(false);
                primaryStage.setScene(resultScene);
            });
            
            PokerInfo playAgain = new PokerInfo();
            playAgain.setMessageType(PokerInfo.MessageType.PLAY_AGAIN);
            output.writeObject(playAgain);
            output.flush();
            
        } catch (Exception e) {
            Platform.runLater(() -> {
                showAlert("Error", "Failed to get result: " + e.getMessage());
            });
            e.printStackTrace();
        }
    }
    
    private void returnToGame() {
        // Clear all cards
        playerCardsBox.getChildren().clear();
        dealerCardsBox.getChildren().clear();
        resultPlayerCardsBox.getChildren().clear();
        resultDealerCardsBox.getChildren().clear();
        
        // Show betting section again
        bettingSection.setVisible(true);
        bettingSection.setManaged(true);
        
        // Reset betting fields
        anteField.setDisable(false);
        pairPlusField.setDisable(false);
        anteField.clear();
        pairPlusField.clear();
        anteField.setText("10");
        pairPlusField.setText("0");
        
        // Reset buttons
        dealButton.setDisable(true); // Will be enabled when valid bets entered
        playButton.setDisable(true);
        foldButton.setDisable(true);
        playButton.setOpacity(0.5);
        foldButton.setOpacity(0.5);
        playButton.setVisible(true); // Keep visible but disabled
        foldButton.setVisible(true); // Keep visible but disabled
        playAgainButton.setDisable(true);
        
        // Reset status
        gameStatusLabel.setText("Place your bets");
        gameStatusLabel.setTextFill(Color.WHITE);
        
        // Reset winnings display color
        gameWinningsLabel.setTextFill(Color.GOLD);
        
        primaryStage.setScene(gameScene);
    }
    
    private String cardToString(Card card) {
        String rank = card.getRank().toString();
        String suit = card.getSuit().toString();
        return rank.substring(0, 1) + rank.substring(1).toLowerCase() + " of " + suit.substring(0, 1) + suit.substring(1).toLowerCase();
    }
    
    private void displayCards(HBox container, ArrayList<Card> cards, boolean showBack) {
        container.getChildren().clear();
        
        if (cards == null || cards.isEmpty()) {
            System.err.println("Warning: displayCards called with null or empty cards");
            return;
        }
        
        for (Card card : cards) {
            Label cardLabel;
            
            if (showBack) {
                // Dealer cards: show as "???"
                cardLabel = new Label("???");
                cardLabel.setStyle(
                    "-fx-background-color: #8B0000; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 16px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 10 20; " +
                    "-fx-background-radius: 5; " +
                    "-fx-min-width: 80; " +
                    "-fx-alignment: center;"
                );
            } else {
                // Player cards: show card text
                String cardText = cardToString(card);
                cardLabel = new Label(cardText);
                cardLabel.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-text-fill: black; " +
                    "-fx-font-size: 14px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 10 15; " +
                    "-fx-background-radius: 5; " +
                    "-fx-border-color: #333; " +
                    "-fx-border-width: 1; " +
                    "-fx-min-width: 120; " +
                    "-fx-alignment: center;"
                );
            }
            
            container.getChildren().add(cardLabel);
        }
    }
    
    private void validateAndEnableDealButton() {
        if (!connected || !playButton.isDisable()) {
            return; // Don't validate if not connected or if cards are already dealt
        }
        
        try {
            String anteText = anteField.getText().trim();
            String pairPlusText = pairPlusField.getText().trim();
            
            if (anteText.isEmpty() || pairPlusText.isEmpty()) {
                dealButton.setDisable(true);
                return;
            }
            
            int anteBet = Integer.parseInt(anteText);
            int pairPlusBet = Integer.parseInt(pairPlusText);
            
            boolean validAnte = anteBet >= 5 && anteBet <= 25;
            boolean validPairPlus = pairPlusBet == 0 || (pairPlusBet >= 5 && pairPlusBet <= 25);
            
            dealButton.setDisable(!(validAnte && validPairPlus));
        } catch (NumberFormatException e) {
            dealButton.setDisable(true);
        }
    }
    
    private void closeConnection() {
        connected = false;
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void stop() {
        closeConnection();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
