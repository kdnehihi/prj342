import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Full coverage JUnit tests for ThreeCardLogic class.
 * Covers hand evaluation, comparison, tie-breakers, dealer qualification,
 * and Pair Plus payouts (3-Card Poker rules).
 */
class ThreeCardLogicTest {

    private ArrayList<Card> hand;

    @BeforeEach
    void setUp() {
        hand = new ArrayList<>();
    }

    // =========================================================
    // =============== evalHand Tests ==========================
    // =========================================================

    @Test
    @DisplayName("evalHand — Straight Flush (Ace low)")
    void testEvalHand_StraightFlush_AceLow() {
        hand.add(new Card(Suit.HEARTS, Rank.ACE));
        hand.add(new Card(Suit.HEARTS, Rank.TWO));
        hand.add(new Card(Suit.HEARTS, Rank.THREE));
        assertEquals(ThreeCardLogic.STRAIGHT_FLUSH, ThreeCardLogic.evalHand(hand));
    }

    @Test
    @DisplayName("evalHand — Straight Flush (Ace high)")
    void testEvalHand_StraightFlush_AceHigh() {
        hand.add(new Card(Suit.HEARTS, Rank.QUEEN));
        hand.add(new Card(Suit.HEARTS, Rank.KING));
        hand.add(new Card(Suit.HEARTS, Rank.ACE));
        assertEquals(ThreeCardLogic.STRAIGHT_FLUSH, ThreeCardLogic.evalHand(hand));
    }

    @Test
    @DisplayName("evalHand — Three of a Kind")
    void testEvalHand_ThreeOfAKind() {
        hand.add(new Card(Suit.HEARTS, Rank.KING));
        hand.add(new Card(Suit.DIAMONDS, Rank.KING));
        hand.add(new Card(Suit.CLUBS, Rank.KING));
        assertEquals(ThreeCardLogic.THREE_OF_A_KIND, ThreeCardLogic.evalHand(hand));
    }

    @Test
    @DisplayName("evalHand — Straight (4-5-6)")
    void testEvalHand_Straight() {
        hand.add(new Card(Suit.HEARTS, Rank.FOUR));
        hand.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        hand.add(new Card(Suit.CLUBS, Rank.SIX));
        assertEquals(ThreeCardLogic.STRAIGHT, ThreeCardLogic.evalHand(hand));
    }

    @Test
    @DisplayName("evalHand — Straight (Ace low A-2-3)")
    void testEvalHand_Straight_AceLow() {
        hand.add(new Card(Suit.HEARTS, Rank.ACE));
        hand.add(new Card(Suit.DIAMONDS, Rank.TWO));
        hand.add(new Card(Suit.CLUBS, Rank.THREE));
        assertEquals(ThreeCardLogic.STRAIGHT, ThreeCardLogic.evalHand(hand));
    }

    @Test
    @DisplayName("evalHand — Straight (Ace high Q-K-A)")
    void testEvalHand_Straight_AceHigh() {
        hand.add(new Card(Suit.HEARTS, Rank.QUEEN));
        hand.add(new Card(Suit.DIAMONDS, Rank.KING));
        hand.add(new Card(Suit.CLUBS, Rank.ACE));
        assertEquals(ThreeCardLogic.STRAIGHT, ThreeCardLogic.evalHand(hand));
    }

    @Test
    @DisplayName("evalHand — Flush")
    void testEvalHand_Flush() {
        hand.add(new Card(Suit.SPADES, Rank.TWO));
        hand.add(new Card(Suit.SPADES, Rank.FIVE));
        hand.add(new Card(Suit.SPADES, Rank.NINE));
        assertEquals(ThreeCardLogic.FLUSH, ThreeCardLogic.evalHand(hand));
    }

    @Test
    @DisplayName("evalHand — Pair")
    void testEvalHand_Pair() {
        hand.add(new Card(Suit.HEARTS, Rank.JACK));
        hand.add(new Card(Suit.DIAMONDS, Rank.JACK));
        hand.add(new Card(Suit.CLUBS, Rank.SEVEN));
        assertEquals(ThreeCardLogic.PAIR, ThreeCardLogic.evalHand(hand));
    }

    @Test
    @DisplayName("evalHand — High Card")
    void testEvalHand_HighCard() {
        hand.add(new Card(Suit.HEARTS, Rank.TWO));
        hand.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        hand.add(new Card(Suit.CLUBS, Rank.NINE));
        assertEquals(ThreeCardLogic.HIGH_CARD, ThreeCardLogic.evalHand(hand));
    }

    @Test
    @DisplayName("evalHand — Invalid size throws exception")
    void testEvalHand_InvalidSize() {
        hand.add(new Card(Suit.HEARTS, Rank.ACE));
        hand.add(new Card(Suit.HEARTS, Rank.TWO));
        assertThrows(IllegalArgumentException.class, () -> ThreeCardLogic.evalHand(hand));
    }


    // =========================================================
    // =============== compareHands Tests ======================
    // =========================================================

    @Test
    @DisplayName("compareHands — Player wins with higher rank")
    void testCompareHands_PlayerWins_HigherRank() {
        ArrayList<Card> player = new ArrayList<>();
        player.add(new Card(Suit.HEARTS, Rank.KING));
        player.add(new Card(Suit.DIAMONDS, Rank.KING));
        player.add(new Card(Suit.CLUBS, Rank.KING));

        ArrayList<Card> dealer = new ArrayList<>();
        dealer.add(new Card(Suit.HEARTS, Rank.QUEEN));
        dealer.add(new Card(Suit.DIAMONDS, Rank.QUEEN));
        dealer.add(new Card(Suit.CLUBS, Rank.QUEEN));

        assertTrue(ThreeCardLogic.compareHands(dealer, player) > 0);
    }

    @Test
    @DisplayName("compareHands — Dealer wins with higher rank")
    void testCompareHands_DealerWins_HigherRank() {
        ArrayList<Card> player = new ArrayList<>();
        player.add(new Card(Suit.HEARTS, Rank.TWO));
        player.add(new Card(Suit.DIAMONDS, Rank.THREE));
        player.add(new Card(Suit.CLUBS, Rank.FOUR));

        ArrayList<Card> dealer = new ArrayList<>();
        dealer.add(new Card(Suit.HEARTS, Rank.FIVE));
        dealer.add(new Card(Suit.DIAMONDS, Rank.SIX));
        dealer.add(new Card(Suit.CLUBS, Rank.SEVEN));

        assertTrue(ThreeCardLogic.compareHands(dealer, player) < 0);
    }

    @Test
    @DisplayName("compareHands — Tie (same rank and cards)")
    void testCompareHands_Tie() {
        ArrayList<Card> player = new ArrayList<>();
        player.add(new Card(Suit.HEARTS, Rank.TEN));
        player.add(new Card(Suit.DIAMONDS, Rank.JACK));
        player.add(new Card(Suit.CLUBS, Rank.QUEEN));

        ArrayList<Card> dealer = new ArrayList<>();
        dealer.add(new Card(Suit.SPADES, Rank.TEN));
        dealer.add(new Card(Suit.CLUBS, Rank.JACK));
        dealer.add(new Card(Suit.DIAMONDS, Rank.QUEEN));

        assertEquals(0, ThreeCardLogic.compareHands(dealer, player));
    }

    @Test
    @DisplayName("compareHands — Player wins with higher Pair")
    void testCompareHands_PlayerWins_HigherPair() {
        ArrayList<Card> player = new ArrayList<>();
        player.add(new Card(Suit.HEARTS, Rank.KING));
        player.add(new Card(Suit.DIAMONDS, Rank.KING));
        player.add(new Card(Suit.CLUBS, Rank.ACE));

        ArrayList<Card> dealer = new ArrayList<>();
        dealer.add(new Card(Suit.HEARTS, Rank.QUEEN));
        dealer.add(new Card(Suit.DIAMONDS, Rank.QUEEN));
        dealer.add(new Card(Suit.CLUBS, Rank.ACE));

        assertTrue(ThreeCardLogic.compareHands(dealer, player) > 0);
    }

    @Test
    @DisplayName("compareHands — Player wins with higher High Card")
    void testCompareHands_PlayerWins_HigherHighCard() {
        ArrayList<Card> player = new ArrayList<>();
        player.add(new Card(Suit.HEARTS, Rank.TWO));
        player.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        player.add(new Card(Suit.CLUBS, Rank.KING));

        ArrayList<Card> dealer = new ArrayList<>();
        dealer.add(new Card(Suit.HEARTS, Rank.TWO));
        dealer.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        dealer.add(new Card(Suit.CLUBS, Rank.QUEEN));

        assertTrue(ThreeCardLogic.compareHands(dealer, player) > 0);
    }

    // ---- New tie-breaker tests for 100% coverage ---- //

    @Test
    @DisplayName("compareHands — Straight vs Straight, higher straight wins")
    void testCompareHands_StraightVsStraight_Higher() {
        ArrayList<Card> player = new ArrayList<>();
        player.add(new Card(Suit.HEARTS, Rank.FIVE));
        player.add(new Card(Suit.DIAMONDS, Rank.SIX));
        player.add(new Card(Suit.CLUBS, Rank.SEVEN));

        ArrayList<Card> dealer = new ArrayList<>();
        dealer.add(new Card(Suit.HEARTS, Rank.FOUR));
        dealer.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        dealer.add(new Card(Suit.CLUBS, Rank.SIX));

        assertTrue(ThreeCardLogic.compareHands(dealer, player) > 0);
    }

    @Test
    @DisplayName("compareHands — Flush vs Flush, kicker decides")
    void testCompareHands_FlushVsFlush_HigherKicker() {
        ArrayList<Card> player = new ArrayList<>();
        player.add(new Card(Suit.HEARTS, Rank.TWO));
        player.add(new Card(Suit.HEARTS, Rank.SEVEN));
        player.add(new Card(Suit.HEARTS, Rank.KING));

        ArrayList<Card> dealer = new ArrayList<>();
        dealer.add(new Card(Suit.CLUBS, Rank.TWO));
        dealer.add(new Card(Suit.CLUBS, Rank.SEVEN));
        dealer.add(new Card(Suit.CLUBS, Rank.QUEEN));

        assertTrue(ThreeCardLogic.compareHands(dealer, player) > 0);
    }

    @Test
    @DisplayName("compareHands — Pair tie with same kicker")
    void testCompareHands_PairPerfectTie() {
        ArrayList<Card> player = new ArrayList<>();
        player.add(new Card(Suit.HEARTS, Rank.KING));
        player.add(new Card(Suit.CLUBS, Rank.KING));
        player.add(new Card(Suit.DIAMONDS, Rank.JACK));

        ArrayList<Card> dealer = new ArrayList<>();
        dealer.add(new Card(Suit.SPADES, Rank.KING));
        dealer.add(new Card(Suit.DIAMONDS, Rank.KING));
        dealer.add(new Card(Suit.CLUBS, Rank.JACK));

        assertEquals(0, ThreeCardLogic.compareHands(dealer, player));
    }


    // =========================================================
    // =============== evalPPWinnings Tests ====================
    // =========================================================

    @Test
    @DisplayName("evalPPWinnings — Straight Flush 40:1")
    void testEvalPPWinnings_StraightFlush() {
        hand.add(new Card(Suit.HEARTS, Rank.ACE));
        hand.add(new Card(Suit.HEARTS, Rank.TWO));
        hand.add(new Card(Suit.HEARTS, Rank.THREE));
        assertEquals(410, ThreeCardLogic.evalPPWinnings(hand, 10));
    }

    @Test
    @DisplayName("evalPPWinnings — Three of a Kind 30:1")
    void testEvalPPWinnings_ThreeOfAKind() {
        hand.add(new Card(Suit.HEARTS, Rank.SEVEN));
        hand.add(new Card(Suit.DIAMONDS, Rank.SEVEN));
        hand.add(new Card(Suit.CLUBS, Rank.SEVEN));
        assertEquals(155, ThreeCardLogic.evalPPWinnings(hand, 5));
    }

    @Test
    @DisplayName("evalPPWinnings — Straight 6:1")
    void testEvalPPWinnings_Straight() {
        hand.add(new Card(Suit.HEARTS, Rank.FOUR));
        hand.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        hand.add(new Card(Suit.CLUBS, Rank.SIX));
        assertEquals(70, ThreeCardLogic.evalPPWinnings(hand, 10));
    }

    @Test
    @DisplayName("evalPPWinnings — Flush 3:1")
    void testEvalPPWinnings_Flush() {
        hand.add(new Card(Suit.SPADES, Rank.TWO));
        hand.add(new Card(Suit.SPADES, Rank.FIVE));
        hand.add(new Card(Suit.SPADES, Rank.NINE));
        assertEquals(40, ThreeCardLogic.evalPPWinnings(hand, 10));
    }

    @Test
    @DisplayName("evalPPWinnings — Pair 1:1")
    void testEvalPPWinnings_Pair() {
        hand.add(new Card(Suit.HEARTS, Rank.JACK));
        hand.add(new Card(Suit.DIAMONDS, Rank.JACK));
        hand.add(new Card(Suit.CLUBS, Rank.SEVEN));
        assertEquals(20, ThreeCardLogic.evalPPWinnings(hand, 10));
    }

    @Test
    @DisplayName("evalPPWinnings — High Card loses")
    void testEvalPPWinnings_HighCard() {
        hand.add(new Card(Suit.HEARTS, Rank.TWO));
        hand.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        hand.add(new Card(Suit.CLUBS, Rank.NINE));
        assertEquals(0, ThreeCardLogic.evalPPWinnings(hand, 10));
    }

    @Test
    @DisplayName("evalPPWinnings — Pair of 2s still wins (1:1)")
    void testEvalPPWinnings_PairOfTwos() {
        hand.add(new Card(Suit.HEARTS, Rank.TWO));
        hand.add(new Card(Suit.DIAMONDS, Rank.TWO));
        hand.add(new Card(Suit.CLUBS, Rank.NINE));
        assertEquals(20, ThreeCardLogic.evalPPWinnings(hand, 10));
    }


    // =========================================================
    // =============== dealerQualifies Tests ===================
    // =========================================================

    @Test
    @DisplayName("dealerQualifies — Queen high qualifies")
    void testDealerQualifies_QueenHigh() {
        hand.add(new Card(Suit.HEARTS, Rank.TWO));
        hand.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        hand.add(new Card(Suit.CLUBS, Rank.QUEEN));
        assertTrue(ThreeCardLogic.dealerQualifies(hand));
    }

    @Test
    @DisplayName("dealerQualifies — King high qualifies")
    void testDealerQualifies_KingHigh() {
        hand.add(new Card(Suit.HEARTS, Rank.TWO));
        hand.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        hand.add(new Card(Suit.CLUBS, Rank.KING));
        assertTrue(ThreeCardLogic.dealerQualifies(hand));
    }

    @Test
    @DisplayName("dealerQualifies — Jack high does NOT qualify")
    void testDealerQualifies_JackHigh() {
        hand.add(new Card(Suit.HEARTS, Rank.TWO));
        hand.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        hand.add(new Card(Suit.CLUBS, Rank.JACK));
        assertFalse(ThreeCardLogic.dealerQualifies(hand));
    }

    @Test
    @DisplayName("dealerQualifies — Pair qualifies")
    void testDealerQualifies_Pair() {
        hand.add(new Card(Suit.HEARTS, Rank.TWO));
        hand.add(new Card(Suit.DIAMONDS, Rank.TWO));
        hand.add(new Card(Suit.CLUBS, Rank.FIVE));
        assertTrue(ThreeCardLogic.dealerQualifies(hand));
    }

    @Test
    @DisplayName("dealerQualifies — Straight qualifies")
    void testDealerQualifies_Straight() {
        hand.add(new Card(Suit.HEARTS, Rank.FOUR));
        hand.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        hand.add(new Card(Suit.CLUBS, Rank.SIX));
        assertTrue(ThreeCardLogic.dealerQualifies(hand));
    }
}
