package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Hand {
    private final List<Card> cards = new ArrayList<>();

    public void addCard(Card card) {
        cards.add(Objects.requireNonNull(card));
    }

    public List<Card> getCards() {
        return List.copyOf(cards);
    }

    public int getScore() {
        int score = 0;
        int aces = 0;
        for (Card card : cards) {
            score += card.getValue();
            if (card.getRank() == Card.Rank.ACE) {
                aces++;
            }
        }
        if (aces > 0 && score + 10 <= 21) {
            score += 10;
        }
        return score;
    }

    public boolean isBust() {
        return getScore() > 21;
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && getScore() == 21;
    }
}
