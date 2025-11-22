import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX Application for the 3 Card Poker Server.
 */
public class ServerApp extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/intro.fxml"));
        Scene scene = new Scene(loader.load(), 400, 200);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        primaryStage.setTitle("3 Card Poker Server");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    @Override
    public void stop() {
        // Cleanup when application closes
        System.exit(0);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

