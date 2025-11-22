import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main server class that manages ServerSocket and client connections.
 * Runs on a background thread (not JavaFX thread).
 */
public class PokerServer {
    private ServerSocket serverSocket;
    private boolean running;
    private int port;
    private List<ClientHandler> clients;
    private AtomicInteger clientIdCounter;
    private StatusController statusController;
    private Thread serverThread;
    
    public PokerServer(int port, StatusController statusController) {
        this.port = port;
        this.statusController = statusController;
        this.clients = new ArrayList<>();
        this.clientIdCounter = new AtomicInteger(1);
        this.running = false;
    }
    
    /**
     * Start the server on a background thread.
     */
    public void start() {
        if (running) {
            log("Server is already running");
            return;
        }
        
        running = true;
        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                log("Server started on port " + port);
                
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        
                        if (clients.size() >= 8) {
                            log("Maximum clients (8) reached. Rejecting connection.");
                            clientSocket.close();
                            continue;
                        }
                        
                        int clientId = clientIdCounter.getAndIncrement();
                        ClientHandler handler = new ClientHandler(clientSocket, clientId, this);
                        clients.add(handler);
                        Thread clientThread = new Thread(handler);
                        clientThread.start();
                        
                        log("Client " + clientId + " connected. Total clients: " + clients.size());
                    } catch (IOException e) {
                        if (running) {
                            log("Error accepting client: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                log("Error starting server: " + e.getMessage());
            } finally {
                log("Server stopped");
            }
        });
        
        serverThread.start();
    }
    
    /**
     * Stop the server gracefully.
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        running = false;
        log("Stopping server...");
        
        // Close all client connections
        synchronized (clients) {
            for (ClientHandler client : new ArrayList<>(clients)) {
                client.disconnect();
            }
            clients.clear();
        }
        
        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                log("Error closing server socket: " + e.getMessage());
            }
        }
        
        // Wait for server thread to finish
        if (serverThread != null) {
            try {
                serverThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Remove a client from the active clients list.
     */
    public void removeClient(ClientHandler client) {
        synchronized (clients) {
            clients.remove(client);
            log("Client " + client.getClientId() + " disconnected. Total clients: " + clients.size());
        }
    }
    
    /**
     * Log a message to the status controller (thread-safe).
     */
    public void log(String message) {
        if (statusController != null) {
            javafx.application.Platform.runLater(() -> {
                statusController.log(message);
            });
        }
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public int getClientCount() {
        synchronized (clients) {
            return clients.size();
        }
    }
}

