package game;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class DealerTest {
    private Card card(Card.Rank rank) {
        return new Card(rank, Card.Suit.CLUBS);
    }

    private Deck deck(Card... cards) {
        return new Deck() {
            private final Queue<Card> sequence = new ArrayDeque<>(Arrays.asList(cards));

            @Override
            public Card deal() {
                return sequence.remove();
            }
        };
    }

    @Test
    void drawsUntilSeventeenOrMore() {
        Dealer dealer = new Dealer();
        dealer.getHand().addCard(card(Card.Rank.SIX));
        dealer.getHand().addCard(card(Card.Rank.FIVE));
        dealer.play(deck(card(Card.Rank.TWO), card(Card.Rank.FOUR)));
        assertEquals(17, dealer.getHand().getScore());
        assertEquals(4, dealer.getHand().getCards().size());
    }

    @Test
    void standsOnSoftSeventeen() {
        Dealer dealer = new Dealer();
        dealer.getHand().addCard(card(Card.Rank.ACE));
        dealer.getHand().addCard(card(Card.Rank.SIX));
        dealer.play(deck());
        assertEquals(17, dealer.getHand().getScore());
        assertEquals(2, dealer.getHand().getCards().size());
    }

    @Test
    void canBustWhileDrawing() {
        Dealer dealer = new Dealer();
        dealer.getHand().addCard(card(Card.Rank.TEN));
        dealer.getHand().addCard(card(Card.Rank.SIX));
        dealer.play(deck(card(Card.Rank.KING)));
        assertTrue(dealer.getHand().isBust());
    }
}
