package game;

class Dealer {
    private final Hand hand = new Hand();

    Hand getHand() {
        return hand;
    }

    void play(Deck deck) {
        while (hand.getScore() < 17) {
            hand.addCard(deck.deal());
        }
    }
}
