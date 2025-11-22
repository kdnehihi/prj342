import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Simple command-line client for testing the 3 Card Poker server.
 * Connects to the server and allows playing the game via console input.
 */
public class PokerClient {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Scanner scanner;
    private int totalWinnings;
    
    public PokerClient(String host, int port) {
        scanner = new Scanner(System.in);
        totalWinnings = 0;
        
        try {
            System.out.println("Connecting to server at " + host + ":" + port + "...");
            socket = new Socket(host, port);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            System.out.println("Connected to server!");
            System.out.println();
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            System.exit(1);
        }
    }
    
    public void play() {
        try {
            while (true) {
                System.out.println("=== New Hand ===");
                System.out.println("Current total winnings: " + totalWinnings);
                System.out.println();
                
                // Get initial bets
                System.out.print("Enter Ante bet (5-25): ");
                int anteBet = scanner.nextInt();
                scanner.nextLine(); // consume newline
                
                System.out.print("Enter Pair Plus bet (0 or 5-25, 0 to skip): ");
                int pairPlusBet = scanner.nextInt();
                scanner.nextLine(); // consume newline
                
                // Send initial bet
                PokerInfo betInfo = new PokerInfo();
                betInfo.setMessageType(PokerInfo.MessageType.INITIAL_BET);
                betInfo.setAnteBet(anteBet);
                betInfo.setPairPlusBet(pairPlusBet);
                output.writeObject(betInfo);
                output.flush();
                
                // Receive dealt cards
                PokerInfo cardsInfo = (PokerInfo) input.readObject();
                System.out.println();
                System.out.println("=== Your Cards ===");
                printCards(cardsInfo.getPlayerCards());
                System.out.println();
                System.out.println("Dealer has 3 cards (hidden)");
                System.out.println();
                
                // Get player action
                System.out.print("Do you want to PLAY or FOLD? (P/F): ");
                String action = scanner.nextLine().trim().toUpperCase();
                
                PokerInfo actionInfo = new PokerInfo();
                actionInfo.setMessageType(PokerInfo.MessageType.PLAYER_ACTION);
                actionInfo.setAnteBet(anteBet);
                actionInfo.setPairPlusBet(pairPlusBet);
                actionInfo.setPlayerCards(cardsInfo.getPlayerCards());
                actionInfo.setDealerCards(cardsInfo.getDealerCards());
                
                if (action.equals("P") || action.equals("PLAY")) {
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
                System.out.println("=== Game Result ===");
                System.out.println("Your cards:");
                printCards(result.getPlayerCards());
                System.out.println();
                System.out.println("Dealer cards:");
                printCards(result.getDealerCards());
                System.out.println();
                
                if (result.getStatusMessage() != null) {
                    System.out.println(result.getStatusMessage());
                }
                
                if (!result.isDealerQualified()) {
                    System.out.println("Dealer did not qualify (not Queen high or better)");
                }
                
                if (result.getPairPlusPayout() > 0) {
                    System.out.println("Pair Plus payout: " + result.getPairPlusPayout());
                } else if (pairPlusBet > 0 && result.getPairPlusPayout() == 0) {
                    System.out.println("Pair Plus: Lost " + pairPlusBet);
                }
                
                System.out.println("Delta this hand: " + result.getDeltaWinningsThisHand());
                totalWinnings = result.getTotalWinnings();
                System.out.println("Total winnings: " + totalWinnings);
                System.out.println();
                
                // Ask to play again
                System.out.print("Play again? (Y/N): ");
                String playAgain = scanner.nextLine().trim().toUpperCase();
                
                PokerInfo playAgainInfo = new PokerInfo();
                playAgainInfo.setMessageType(PokerInfo.MessageType.PLAY_AGAIN);
                output.writeObject(playAgainInfo);
                output.flush();
                
                if (!playAgain.equals("Y") && !playAgain.equals("YES")) {
                    break;
                }
                System.out.println();
            }
            
            // Disconnect
            PokerInfo disconnectInfo = new PokerInfo();
            disconnectInfo.setMessageType(PokerInfo.MessageType.DISCONNECT);
            output.writeObject(disconnectInfo);
            output.flush();
            
            System.out.println("Disconnected from server. Final winnings: " + totalWinnings);
            
        } catch (Exception e) {
            System.err.println("Error during gameplay: " + e.getMessage());
            e.printStackTrace();
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
            if (scanner != null) scanner.close();
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
        
        PokerClient client = new PokerClient(host, port);
        client.play();
    }
}

