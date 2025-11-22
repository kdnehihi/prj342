import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Controller for the intro scene where user enters port and starts server.
 */
public class IntroController {
    @FXML
    private TextField portField;
    
    @FXML
    private Button startButton;
    
    private PokerServer server;
    private StatusController statusController;
    
    @FXML
    private void initialize() {
        // Set default port
        portField.setText("5555");
    }
    
    @FXML
    private void handleStartServer() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            
            if (port < 1024 || port > 65535) {
                showAlert("Invalid Port", "Port must be between 1024 and 65535");
                return;
            }
            
            // Load status scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/status.fxml"));
            Scene statusScene = new Scene(loader.load(), 800, 600);
            statusScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            
            statusController = loader.getController();
            
            // Create and start server
            server = new PokerServer(port, statusController);
            server.start();
            
            // Switch to status scene
            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.setScene(statusScene);
            stage.setTitle("3 Card Poker Server - Running");
            stage.setResizable(true);
            
            // Store server reference in status controller
            statusController.setServer(server);
            
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid port number");
        } catch (Exception e) {
            showAlert("Error", "Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

