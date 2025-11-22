import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

/**
 * Controller for the status scene that displays server logs and controls.
 */
public class StatusController {
    @FXML
    private ListView<String> logListView;
    
    @FXML
    private Button stopButton;
    
    private PokerServer server;
    
    @FXML
    private void initialize() {
        log("Server status view initialized. Waiting for server start...");
    }
    
    /**
     * Set the server reference (called from IntroController).
     */
    public void setServer(PokerServer server) {
        this.server = server;
    }
    
    /**
     * Log a message to the ListView (thread-safe).
     * This method can be called from any thread.
     */
    public void log(String message) {
        Platform.runLater(() -> {
            String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            logListView.getItems().add("[" + timestamp + "] " + message);
            // Auto-scroll to bottom
            logListView.scrollTo(logListView.getItems().size() - 1);
        });
    }
    
    @FXML
    private void handleStopServer() {
        if (server != null && server.isRunning()) {
            server.stop();
            log("Server stopped by user");
            stopButton.setDisable(true);
        }
    }
}

