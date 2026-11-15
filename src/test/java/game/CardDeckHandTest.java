package game;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class CardDeckHandTest {
    private Card card(Card.Rank rank) {
        return new Card(rank, Card.Suit.HEARTS);
    }

    @Test
    void cardsHaveBlackjackValues() {
        assertEquals(1, card(Card.Rank.ACE).getValue());
        assertEquals(2, card(Card.Rank.TWO).getValue());
        assertEquals(9, card(Card.Rank.NINE).getValue());
        assertEquals(10, card(Card.Rank.TEN).getValue());
        assertEquals(10, card(Card.Rank.JACK).getValue());
        assertEquals(10, card(Card.Rank.QUEEN).getValue());
        assertEquals(10, card(Card.Rank.KING).getValue());
    }

    @Test
    void deckContains52UniqueCardsAndRejectsDealingWhenEmpty() {
        Deck deck = new Deck();
        assertEquals(52, deck.remainingCards());
        deck.shuffle();
        Set<String> cards = new HashSet<>();
        while (deck.remainingCards() > 0) {
            Card card = deck.deal();
            assertTrue(cards.add(card.getRank() + ":" + card.getSuit()));
        }
        assertEquals(52, cards.size());
        assertThrows(IllegalStateException.class, deck::deal);
    }

    @Test
    void acesUseTheBestScoreWithoutGoingOver21() {
        Hand hand = new Hand();
        hand.addCard(card(Card.Rank.ACE));
        hand.addCard(card(Card.Rank.ACE));
        assertEquals(12, hand.getScore());
        hand.addCard(card(Card.Rank.NINE));
        assertEquals(21, hand.getScore());
        assertFalse(hand.isBlackjack());
        hand.addCard(card(Card.Rank.KING));
        assertEquals(21, hand.getScore());
        hand.addCard(card(Card.Rank.TWO));
        assertTrue(hand.isBust());
    }

    @Test
    void onlyAnInitialTwoCard21IsBlackjack() {
        Hand hand = new Hand();
        hand.addCard(card(Card.Rank.ACE));
        hand.addCard(card(Card.Rank.KING));
        assertTrue(hand.isBlackjack());
        assertFalse(hand.isBust());
        assertThrows(UnsupportedOperationException.class, () -> hand.getCards().clear());
        assertEquals(2, hand.getCards().size());
    }
}
