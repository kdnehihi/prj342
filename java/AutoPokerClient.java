import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Automated test client that plays a few hands automatically.
 * Useful for testing the server without manual input.
 */
public class AutoPokerClient {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private int totalWinnings;
    private int handNumber;
    
    public AutoPokerClient(String host, int port) {
        totalWinnings = 0;
        handNumber = 0;
        
        try {
            System.out.println("=== 3 Card Poker Client ===");
            System.out.println("Connecting to server at " + host + ":" + port + "...");
            socket = new Socket(host, port);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            System.out.println("✓ Connected to server!");
            System.out.println();
        } catch (IOException e) {
            System.err.println("✗ Error connecting to server: " + e.getMessage());
            System.exit(1);
        }
    }
    
    public void playHand(int anteBet, int pairPlusBet, boolean shouldPlay) {
        handNumber++;
        try {
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("HAND #" + handNumber);
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("Current total winnings: " + totalWinnings);
            System.out.println("Bets: Ante=" + anteBet + ", Pair Plus=" + pairPlusBet);
            System.out.println();
            
            // Send initial bet
            PokerInfo betInfo = new PokerInfo();
            betInfo.setMessageType(PokerInfo.MessageType.INITIAL_BET);
            betInfo.setAnteBet(anteBet);
            betInfo.setPairPlusBet(pairPlusBet);
            output.writeObject(betInfo);
            output.flush();
            
            // Receive dealt cards
            PokerInfo cardsInfo = (PokerInfo) input.readObject();
            System.out.println("Your cards:");
            printCards(cardsInfo.getPlayerCards());
            System.out.println();
            
            // Decide action
            String action = shouldPlay ? "PLAY" : "FOLD";
            System.out.println("Action: " + action);
            
            PokerInfo actionInfo = new PokerInfo();
            actionInfo.setMessageType(PokerInfo.MessageType.PLAYER_ACTION);
            actionInfo.setAnteBet(anteBet);
            actionInfo.setPairPlusBet(pairPlusBet);
            actionInfo.setPlayerCards(cardsInfo.getPlayerCards());
            actionInfo.setDealerCards(cardsInfo.getDealerCards());
            
            if (shouldPlay) {
                actionInfo.setPlayerAction(PokerInfo.PlayerAction.PLAY);
                actionInfo.setPlayBet(anteBet);
            } else {
                actionInfo.setPlayerAction(PokerInfo.PlayerAction.FOLD);
                actionInfo.setPlayBet(0);
            }
            
            output.writeObject(actionInfo);
            output.flush();
            
            // Receive game result
            PokerInfo result = (PokerInfo) input.readObject();
            System.out.println();
            System.out.println("=== RESULT ===");
            System.out.println("Dealer cards:");
            printCards(result.getDealerCards());
            System.out.println();
            
            if (result.getStatusMessage() != null) {
                System.out.println("➤ " + result.getStatusMessage());
            }
            
            if (result.isDealerQualified()) {
                System.out.println("✓ Dealer qualified");
            } else if (shouldPlay) {
                System.out.println("✗ Dealer did not qualify");
            }
            
            if (result.getPairPlusPayout() > 0) {
                System.out.println("💰 Pair Plus payout: " + result.getPairPlusPayout());
            } else if (pairPlusBet > 0 && result.getPairPlusPayout() == 0) {
                System.out.println("❌ Pair Plus: Lost " + pairPlusBet);
            }
            
            System.out.println("📊 Delta this hand: " + result.getDeltaWinningsThisHand());
            totalWinnings = result.getTotalWinnings();
            System.out.println("💵 Total winnings: " + totalWinnings);
            System.out.println();
            
            // Send play again
            PokerInfo playAgainInfo = new PokerInfo();
            playAgainInfo.setMessageType(PokerInfo.MessageType.PLAY_AGAIN);
            output.writeObject(playAgainInfo);
            output.flush();
            
            Thread.sleep(1000); // Brief pause between hands
            
        } catch (Exception e) {
            System.err.println("Error during gameplay: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void playDemo() {
        try {
            // Hand 1: Play with Pair Plus
            playHand(10, 5, true);
            
            // Hand 2: Fold
            playHand(10, 0, false);
            
            // Hand 3: Play without Pair Plus
            playHand(15, 0, true);
            
            // Hand 4: Play with Pair Plus
            playHand(10, 10, true);
            
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("Demo complete!");
            System.out.println("Final total winnings: " + totalWinnings);
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            // Disconnect
            PokerInfo disconnectInfo = new PokerInfo();
            disconnectInfo.setMessageType(PokerInfo.MessageType.DISCONNECT);
            output.writeObject(disconnectInfo);
            output.flush();
            
            System.out.println("Disconnected from server.");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            close();
        }
    }
    
    private void printCards(ArrayList<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            System.out.println("  " + (i + 1) + ". " + card);
        }
    }
    
    private void close() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }
    
    public static void main(String[] args) {
        String host = "localhost";
        int port = 5555;
        
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }
        
        AutoPokerClient client = new AutoPokerClient(host, port);
        client.playDemo();
    }
}

