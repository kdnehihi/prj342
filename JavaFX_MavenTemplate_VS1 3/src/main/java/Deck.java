import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents a standard 52-card deck. Each client gets its own deck instance.
 */
public class Deck {
    private ArrayList<Card> cards;
    
    public Deck() {
        cards = new ArrayList<>();
        initializeDeck();
    }
    
    /**
     * Initialize the deck with all 52 cards.
     */
    private void initializeDeck() {
        cards.clear();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }
    
    /**
     * Shuffle the deck. Called before each hand.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }
    
    /**
     * Deal a card from the top of the deck.
     * @return the top card
     */
    public Card dealCard() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Deck is empty");
        }
        return cards.remove(0);
    }
    
    /**
     * Deal multiple cards.
     * @param count number of cards to deal
     * @return ArrayList of dealt cards
     */
    public ArrayList<Card> dealCards(int count) {
        ArrayList<Card> dealt = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            dealt.add(dealCard());
        }
        return dealt;
    }
    
    /**
     * Reset and shuffle the deck for a new hand.
     */
    public void reset() {
        initializeDeck();
        shuffle();
    }
    
    public int size() {
        return cards.size();
    }
}

