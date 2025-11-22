import java.io.Serializable;
import java.util.ArrayList;

/**
 * Serializable class used for all communication between client and server.
 * Contains all necessary fields for the complete game flow.
 */
public class PokerInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Message types/phases
    public enum MessageType {
        INITIAL_BET,        // Client sends initial bets
        CARDS_DEALT,        // Server sends dealt cards
        PLAYER_ACTION,      // Client sends PLAY or FOLD
        GAME_RESULT,        // Server sends final result
        PLAY_AGAIN,         // Client asks to play again
        DISCONNECT          // Client disconnects
    }
    
    public enum PlayerAction {
        PLAY, FOLD
    }
    
    private MessageType messageType;
    private PlayerAction playerAction;
    
    // Betting fields
    private int anteBet;
    private int pairPlusBet;
    private int playBet;
    
    // Card fields
    private ArrayList<Card> playerCards;
    private ArrayList<Card> dealerCards;
    private boolean dealerCardsHidden; // For initial deal
    
    // Game result fields
    private boolean dealerQualified;
    private int handRankPlayer;
    private int handRankDealer;
    private int pairPlusPayout;
    private int antePlayPayout;
    private int deltaWinningsThisHand;
    private int totalWinnings;
    
    // Status message for logging
    private String statusMessage;
    
    // Client identification
    private int clientId;
    
    public PokerInfo() {
        playerCards = new ArrayList<>();
        dealerCards = new ArrayList<>();
        dealerCardsHidden = true;
    }
    
    // Getters and setters
    public MessageType getMessageType() {
        return messageType;
    }
    
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
    
    public PlayerAction getPlayerAction() {
        return playerAction;
    }
    
    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }
    
    public int getAnteBet() {
        return anteBet;
    }
    
    public void setAnteBet(int anteBet) {
        this.anteBet = anteBet;
    }
    
    public int getPairPlusBet() {
        return pairPlusBet;
    }
    
    public void setPairPlusBet(int pairPlusBet) {
        this.pairPlusBet = pairPlusBet;
    }
    
    public int getPlayBet() {
        return playBet;
    }
    
    public void setPlayBet(int playBet) {
        this.playBet = playBet;
    }
    
    public ArrayList<Card> getPlayerCards() {
        return playerCards;
    }
    
    public void setPlayerCards(ArrayList<Card> playerCards) {
        this.playerCards = playerCards;
    }
    
    public ArrayList<Card> getDealerCards() {
        return dealerCards;
    }
    
    public void setDealerCards(ArrayList<Card> dealerCards) {
        this.dealerCards = dealerCards;
    }
    
    public boolean isDealerCardsHidden() {
        return dealerCardsHidden;
    }
    
    public void setDealerCardsHidden(boolean dealerCardsHidden) {
        this.dealerCardsHidden = dealerCardsHidden;
    }
    
    public boolean isDealerQualified() {
        return dealerQualified;
    }
    
    public void setDealerQualified(boolean dealerQualified) {
        this.dealerQualified = dealerQualified;
    }
    
    public int getHandRankPlayer() {
        return handRankPlayer;
    }
    
    public void setHandRankPlayer(int handRankPlayer) {
        this.handRankPlayer = handRankPlayer;
    }
    
    public int getHandRankDealer() {
        return handRankDealer;
    }
    
    public void setHandRankDealer(int handRankDealer) {
        this.handRankDealer = handRankDealer;
    }
    
    public int getPairPlusPayout() {
        return pairPlusPayout;
    }
    
    public void setPairPlusPayout(int pairPlusPayout) {
        this.pairPlusPayout = pairPlusPayout;
    }
    
    public int getAntePlayPayout() {
        return antePlayPayout;
    }
    
    public void setAntePlayPayout(int antePlayPayout) {
        this.antePlayPayout = antePlayPayout;
    }
    
    public int getDeltaWinningsThisHand() {
        return deltaWinningsThisHand;
    }
    
    public void setDeltaWinningsThisHand(int deltaWinningsThisHand) {
        this.deltaWinningsThisHand = deltaWinningsThisHand;
    }
    
    public int getTotalWinnings() {
        return totalWinnings;
    }
    
    public void setTotalWinnings(int totalWinnings) {
        this.totalWinnings = totalWinnings;
    }
    
    public String getStatusMessage() {
        return statusMessage;
    }
    
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    public int getClientId() {
        return clientId;
    }
    
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }
}

