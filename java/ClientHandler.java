import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Handles communication and game logic for a single client.
 * Each client runs on its own thread with its own deck and game state.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private int clientId;
    private PokerServer server;
    private Deck deck;
    private int playerWinnings;
    private int currentHandId;
    private boolean connected;
    
    public ClientHandler(Socket socket, int clientId, PokerServer server) {
        this.socket = socket;
        this.clientId = clientId;
        this.server = server;
        this.deck = new Deck();
        this.playerWinnings = 0;
        this.currentHandId = 0;
        this.connected = true;
    }
    
    @Override
    public void run() {
        try {
            // Create streams (output first to avoid blocking)
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            
            // Main game loop
            while (connected && !socket.isClosed()) {
                PokerInfo info = (PokerInfo) input.readObject();
                
                if (info == null) {
                    break;
                }
                
                switch (info.getMessageType()) {
                    case INITIAL_BET:
                        handleInitialBet(info);
                        break;
                    case PLAYER_ACTION:
                        handlePlayerAction(info);
                        break;
                    case PLAY_AGAIN:
                        handlePlayAgain(info);
                        break;
                    case DISCONNECT:
                        disconnect();
                        break;
                    default:
                        server.log("Client " + clientId + ": Unknown message type");
                }
            }
        } catch (IOException e) {
            if (connected) {
                server.log("Client " + clientId + " connection error: " + e.getMessage());
            }
        } catch (ClassNotFoundException e) {
            server.log("Client " + clientId + ": Invalid object received");
        } finally {
            disconnect();
        }
    }
    
    /**
     * Handle initial bet from client.
     */
    private void handleInitialBet(PokerInfo info) throws IOException {
        currentHandId++;
        int anteBet = info.getAnteBet();
        int pairPlusBet = info.getPairPlusBet();
        
        // Validate bets
        if (anteBet < 5 || anteBet > 25) {
            server.log("Client " + clientId + ": Invalid ante bet: " + anteBet);
            return;
        }
        if (pairPlusBet < 0 || (pairPlusBet > 0 && (pairPlusBet < 5 || pairPlusBet > 25))) {
            server.log("Client " + clientId + ": Invalid pair plus bet: " + pairPlusBet);
            return;
        }
        
        server.log("Client " + clientId + " Hand #" + currentHandId + ": Ante=" + anteBet + ", PairPlus=" + pairPlusBet);
        
        // Reset and shuffle deck for new hand
        deck.reset();
        
        // Deal cards: 3 to player, 3 to dealer
        ArrayList<Card> playerCards = deck.dealCards(3);
        ArrayList<Card> dealerCards = deck.dealCards(3);
        
        // Send cards back to client (dealer cards hidden)
        PokerInfo response = new PokerInfo();
        response.setMessageType(PokerInfo.MessageType.CARDS_DEALT);
        response.setPlayerCards(playerCards);
        response.setDealerCards(dealerCards);
        response.setDealerCardsHidden(true);
        response.setAnteBet(anteBet);
        response.setPairPlusBet(pairPlusBet);
        response.setClientId(clientId);
        
        output.writeObject(response);
        output.flush();
    }
    
    /**
     * Handle player action (PLAY or FOLD).
     */
    private void handlePlayerAction(PokerInfo info) throws IOException {
        PokerInfo.PlayerAction action = info.getPlayerAction();
        
        if (action == null) {
            server.log("Client " + clientId + ": No player action specified");
            return;
        }
        
        int anteBet = info.getAnteBet();
        int pairPlusBet = info.getPairPlusBet();
        ArrayList<Card> playerCards = info.getPlayerCards();
        ArrayList<Card> dealerCards = info.getDealerCards();
        
        PokerInfo result = new PokerInfo();
        result.setMessageType(PokerInfo.MessageType.GAME_RESULT);
        result.setClientId(clientId);
        result.setPlayerCards(playerCards);
        result.setDealerCards(dealerCards);
        result.setDealerCardsHidden(false); // Reveal dealer cards
        
        int deltaWinnings = 0;
        
        if (action == PokerInfo.PlayerAction.FOLD) {
            // Player folds: loses Ante and Pair Plus
            deltaWinnings = -anteBet - pairPlusBet;
            result.setStatusMessage("Player folded. Lost Ante and Pair Plus.");
            server.log("Client " + clientId + " Hand #" + currentHandId + ": FOLDED - Lost " + 
                      Math.abs(deltaWinnings));
        } else {
            // Player plays: must send Play bet equal to Ante
            int playBet = info.getPlayBet();
            if (playBet != anteBet) {
                server.log("Client " + clientId + ": Play bet (" + playBet + ") must equal Ante (" + anteBet + ")");
                return;
            }
            
            server.log("Client " + clientId + " Hand #" + currentHandId + ": PLAY with Play bet=" + playBet);
            
            // Evaluate hands
            int playerRank = ThreeCardLogic.evalHand(playerCards);
            int dealerRank = ThreeCardLogic.evalHand(dealerCards);
            boolean dealerQualified = ThreeCardLogic.dealerQualifies(dealerCards);
            
            result.setHandRankPlayer(playerRank);
            result.setHandRankDealer(dealerRank);
            result.setDealerQualified(dealerQualified);
            
            // Calculate ante/play result
            int antePlayResult = 0;
            if (!dealerQualified) {
                // Dealer not qualified: Play bet returned, Ante pushes
                antePlayResult = playBet; // Get back play bet
                result.setStatusMessage("Dealer not qualified. Play bet returned. Ante pushes.");
                server.log("Client " + clientId + " Hand #" + currentHandId + ": Dealer not qualified");
            } else {
                // Compare hands
                int comparison = ThreeCardLogic.compareHands(dealerCards, playerCards);
                if (comparison < 0) {
                    // Dealer wins
                    antePlayResult = -anteBet - playBet;
                    result.setStatusMessage("Dealer wins. Lost Ante and Play.");
                    server.log("Client " + clientId + " Hand #" + currentHandId + ": DEALER WINS - Lost " + 
                              Math.abs(antePlayResult));
                } else if (comparison > 0) {
                    // Player wins: 1:1 on both Ante and Play
                    // "1:1" means double each wager: get back bet + equal profit
                    // Total return = 2*anteBet + 2*playBet, but player already bet anteBet+playBet
                    // So net profit = (2*anteBet + 2*playBet) - (anteBet + playBet) = anteBet + playBet
                    antePlayResult = anteBet + playBet; // Net profit (total return - bets)
                    result.setStatusMessage("Player wins! Paid 1:1 on Ante and Play.");
                    server.log("Client " + clientId + " Hand #" + currentHandId + ": PLAYER WINS - Won " + 
                              antePlayResult + " (total return: " + (2*anteBet + 2*playBet) + ")");
                } else {
                    // Tie: push
                    antePlayResult = 0;
                    result.setStatusMessage("Tie. Ante and Play push.");
                    server.log("Client " + clientId + " Hand #" + currentHandId + ": TIE");
                }
            }
            
            result.setAntePlayPayout(antePlayResult);
            deltaWinnings += antePlayResult;
            
            // Calculate Pair Plus result
            // evalPPWinnings returns total payout (bet * multiplier), so net = payout - bet
            int pairPlusPayout = ThreeCardLogic.evalPPWinnings(playerCards, pairPlusBet);
            result.setPairPlusPayout(pairPlusPayout);
            
            if (pairPlusBet > 0) {
                if (pairPlusPayout > 0) {
                    // Net winnings = payout - original bet
                    int pairPlusNet = pairPlusPayout - pairPlusBet;
                    deltaWinnings += pairPlusNet;
                    server.log("Client " + clientId + " Hand #" + currentHandId + ": Pair Plus won " + 
                              pairPlusNet + " (payout: " + pairPlusPayout + ")");
                } else {
                    // Lost Pair Plus bet
                    deltaWinnings -= pairPlusBet;
                    server.log("Client " + clientId + " Hand #" + currentHandId + ": Pair Plus lost " + pairPlusBet);
                }
            }
        }
        
        // Update total winnings
        playerWinnings += deltaWinnings;
        result.setDeltaWinningsThisHand(deltaWinnings);
        result.setTotalWinnings(playerWinnings);
        
        server.log("Client " + clientId + " Hand #" + currentHandId + ": Delta=" + deltaWinnings + 
                  ", Total=" + playerWinnings);
        
        // Send result to client
        output.writeObject(result);
        output.flush();
    }
    
    /**
     * Handle play again request.
     */
    private void handlePlayAgain(PokerInfo info) throws IOException {
        // Client wants to play another hand - just acknowledge
        // The next INITIAL_BET will start a new hand
        server.log("Client " + clientId + " ready for another hand");
    }
    
    /**
     * Disconnect this client.
     */
    public void disconnect() {
        if (!connected) {
            return;
        }
        
        connected = false;
        
        try {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignore errors during cleanup
        }
        
        server.removeClient(this);
    }
    
    public int getClientId() {
        return clientId;
    }
}

