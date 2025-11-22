import java.util.ArrayList;
import java.util.Comparator;

/**
 * Static utility class for evaluating and comparing 3-card poker hands.
 * Hand rankings (high to low): Straight Flush, Three of a Kind, Straight, Flush, Pair, High Card.
 */
public class ThreeCardLogic {
    
    // Hand rank constants (higher is better)
    public static final int HIGH_CARD = 0;
    public static final int PAIR = 1;
    public static final int FLUSH = 2;
    public static final int STRAIGHT = 3;
    public static final int THREE_OF_A_KIND = 4;
    public static final int STRAIGHT_FLUSH = 5;
    
    /**
     * Evaluate a 3-card hand and return its rank value.
     * @param hand ArrayList of exactly 3 cards
     * @return rank value (HIGH_CARD=0, PAIR=1, FLUSH=2, STRAIGHT=3, THREE_OF_A_KIND=4, STRAIGHT_FLUSH=5)
     */
    public static int evalHand(ArrayList<Card> hand) {
        if (hand == null || hand.size() != 3) {
            throw new IllegalArgumentException("Hand must contain exactly 3 cards");
        }
        
        // Sort cards by rank value for easier evaluation
        ArrayList<Card> sorted = new ArrayList<>(hand);
        sorted.sort(Comparator.comparingInt(c -> c.getRank().getValue()));
        
        boolean isFlush = isFlush(sorted);
        boolean isStraight = isStraight(sorted);
        boolean isThreeOfAKind = isThreeOfAKind(sorted);
        
        // Straight Flush
        if (isFlush && isStraight) {
            return STRAIGHT_FLUSH;
        }
        
        // Three of a Kind
        if (isThreeOfAKind) {
            return THREE_OF_A_KIND;
        }
        
        // Straight
        if (isStraight) {
            return STRAIGHT;
        }
        
        // Flush
        if (isFlush) {
            return FLUSH;
        }
        
        // Pair
        if (isPair(sorted)) {
            return PAIR;
        }
        
        // High Card
        return HIGH_CARD;
    }
    
    /**
     * Check if hand is a flush (all same suit).
     */
    private static boolean isFlush(ArrayList<Card> hand) {
        Suit firstSuit = hand.get(0).getSuit();
        return hand.get(1).getSuit() == firstSuit && hand.get(2).getSuit() == firstSuit;
    }
    
    /**
     * Check if hand is a straight (consecutive ranks).
     * Ace can be low (A-2-3) or high (Q-K-A).
     */
    private static boolean isStraight(ArrayList<Card> hand) {
        int r1 = hand.get(0).getRank().getValue();
        int r2 = hand.get(1).getRank().getValue();
        int r3 = hand.get(2).getRank().getValue();
        
        // Regular straight
        if (r2 == r1 + 1 && r3 == r2 + 1) {
            return true;
        }
        
        // Ace-low straight (A-2-3) - after sorting: [1, 2, 3]
        if (r1 == 1 && r2 == 2 && r3 == 3) {
            return true;
        }
        
        // Ace-high straight (Q-K-A) - after sorting: [1, 12, 13] where 1 is Ace
        if (r1 == 1 && r2 == 12 && r3 == 13) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if hand is three of a kind.
     */
    private static boolean isThreeOfAKind(ArrayList<Card> hand) {
        Rank r1 = hand.get(0).getRank();
        Rank r2 = hand.get(1).getRank();
        Rank r3 = hand.get(2).getRank();
        return r1 == r2 && r2 == r3;
    }
    
    /**
     * Check if hand contains a pair.
     */
    private static boolean isPair(ArrayList<Card> hand) {
        Rank r1 = hand.get(0).getRank();
        Rank r2 = hand.get(1).getRank();
        Rank r3 = hand.get(2).getRank();
        return (r1 == r2) || (r2 == r3) || (r1 == r3);
    }
    
    /**
     * Compare two hands. Returns -1 if dealer wins, 1 if player wins, 0 if tie.
     * @param dealer dealer's hand
     * @param player player's hand
     * @return -1 (dealer wins), 0 (tie), 1 (player wins)
     */
    public static int compareHands(ArrayList<Card> dealer, ArrayList<Card> player) {
        int dealerRank = evalHand(dealer);
        int playerRank = evalHand(player);
        
        // Compare by rank first
        if (dealerRank > playerRank) {
            return -1; // Dealer wins
        } else if (playerRank > dealerRank) {
            return 1; // Player wins
        }
        
        // Same rank, need to compare by high cards
        return compareSameRankHands(dealer, player, dealerRank);
    }
    
    /**
     * Compare hands of the same rank by high cards.
     */
    private static int compareSameRankHands(ArrayList<Card> dealer, ArrayList<Card> player, int rank) {
        ArrayList<Card> sortedDealer = new ArrayList<>(dealer);
        ArrayList<Card> sortedPlayer = new ArrayList<>(player);
        sortedDealer.sort(Comparator.comparingInt(c -> c.getRank().getValue()));
        sortedPlayer.sort(Comparator.comparingInt(c -> c.getRank().getValue()));
        
        if (rank == THREE_OF_A_KIND || rank == PAIR) {
            // For three of a kind or pair, compare the matching cards first
            int dealerPair = getPairValue(sortedDealer);
            int playerPair = getPairValue(sortedPlayer);
            if (dealerPair != playerPair) {
                return Integer.compare(playerPair, dealerPair); // Higher pair wins
            }
        }
        
        // Compare high cards (reverse order for high to low)
        for (int i = 2; i >= 0; i--) {
            int dealerVal = sortedDealer.get(i).getRank().getValue();
            int playerVal = sortedPlayer.get(i).getRank().getValue();
            // Treat Ace as high (14) for comparison
            if (dealerVal == 1) dealerVal = 14;
            if (playerVal == 1) playerVal = 14;
            
            if (playerVal > dealerVal) {
                return 1; // Player wins
            } else if (dealerVal > playerVal) {
                return -1; // Dealer wins
            }
        }
        
        return 0; // Tie
    }
    
    /**
     * Get the rank value of the pair in a hand (or three of a kind).
     */
    private static int getPairValue(ArrayList<Card> hand) {
        int r1 = hand.get(0).getRank().getValue();
        int r2 = hand.get(1).getRank().getValue();
        int r3 = hand.get(2).getRank().getValue();
        
        if (r1 == r2 || r1 == r3) return r1;
        if (r2 == r3) return r2;
        return 0;
    }
    
    /**
     * Check if dealer qualifies (Queen high or better).
     * @param dealer dealer's hand
     * @return true if dealer qualifies
     */
    public static boolean dealerQualifies(ArrayList<Card> dealer) {
        int rank = evalHand(dealer);
        
        // Any hand better than high card qualifies
        if (rank > HIGH_CARD) {
            return true;
        }
        
        // For high card, check if highest card is Queen or better
        ArrayList<Card> sorted = new ArrayList<>(dealer);
        sorted.sort(Comparator.comparingInt(c -> c.getRank().getValue()));
        
        // Get highest card (treat Ace as high)
        int highest = sorted.get(2).getRank().getValue();
        if (highest == 1) highest = 14; // Ace
        
        return highest >= 12; // Queen = 12
    }
    
    /**
     * Evaluate Pair Plus winnings for a hand.
     * @param hand player's hand
     * @param bet the Pair Plus bet amount
     * @return winnings (0 if no qualifying hand, otherwise bet * multiplier)
     */
    public static int evalPPWinnings(ArrayList<Card> hand, int bet) {
        int rank = evalHand(hand);
        
        // Must have at least a pair of 2s or better
        if (rank == HIGH_CARD) {
            return 0; // Lose Pair Plus
        }
        
        // Check if it's at least a pair of 2s
        if (rank == PAIR) {
            ArrayList<Card> sorted = new ArrayList<>(hand);
            sorted.sort(Comparator.comparingInt(c -> c.getRank().getValue()));
            int pairValue = getPairValue(sorted);
            if (pairValue < 2) {
                return 0; // Pair of Aces doesn't count (shouldn't happen in 3-card, but safety check)
            }
        }
        
        // Calculate payout based on hand rank
        // "X:1" means profit of X per unit bet, so total return = bet * (X + 1)
        switch (rank) {
            case STRAIGHT_FLUSH:
                return bet * 41; // 40:1 (bet + 40*bet profit)
            case THREE_OF_A_KIND:
                return bet * 31; // 30:1 (bet + 30*bet profit)
            case STRAIGHT:
                return bet * 7; // 6:1 (bet + 6*bet profit)
            case FLUSH:
                return bet * 4; // 3:1 (bet + 3*bet profit)
            case PAIR:
                return bet * 2; // 1:1 (bet + 1*bet profit)
            default:
                return 0;
        }
    }
}

