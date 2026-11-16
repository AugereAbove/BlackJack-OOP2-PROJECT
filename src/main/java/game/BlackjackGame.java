package game;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class BlackjackGame {
    private final Supplier<Deck> deckSupplier;
    private Deck deck;
    private Hand player = new Hand();
    private Dealer dealer = new Dealer();
    private GameState.Status status = GameState.Status.NOT_STARTED;
    private GameState.Result result;

    public BlackjackGame() {
        this(() -> {
            Deck deck = new Deck();
            deck.shuffle();
            return deck;
        });
    }

    BlackjackGame(Supplier<Deck> deckSupplier) {
        this.deckSupplier = Objects.requireNonNull(deckSupplier);
    }

    public void startGame() {
        if (status == GameState.Status.PLAYER_TURN) {
            throw new IllegalStateException("The round is still in progress");
        }
        deck = Objects.requireNonNull(deckSupplier.get());
        player = new Hand();
        dealer = new Dealer();
        result = null;
        player.addCard(deck.deal());
        dealer.getHand().addCard(deck.deal());
        player.addCard(deck.deal());
        dealer.getHand().addCard(deck.deal());
        status = GameState.Status.PLAYER_TURN;
        if (player.isBlackjack() || dealer.getHand().isBlackjack()) {
            finishRound();
        }
    }

    public void hit() {
        requirePlayerTurn();
        player.addCard(deck.deal());
        if (player.isBust()) {
            result = GameState.Result.DEALER_WIN;
            status = GameState.Status.FINISHED;
        }
    }

    public void stand() {
        requirePlayerTurn();
        dealer.play(deck);
        finishRound();
    }

    public GameState getGameState() {
        List<Card> dealerCards = dealer.getHand().getCards();
        if (status == GameState.Status.PLAYER_TURN) {
            dealerCards = dealerCards.subList(0, 1);
        }
        return new GameState(status, result, player.getCards(), dealerCards, player.getScore());
    }

    private void requirePlayerTurn() {
        if (status != GameState.Status.PLAYER_TURN) {
            throw new IllegalStateException("No player turn is in progress");
        }
    }

    private void finishRound() {
        Hand dealerHand = dealer.getHand();
        if (player.isBlackjack() != dealerHand.isBlackjack()) {
            result = player.isBlackjack() ? GameState.Result.PLAYER_WIN : GameState.Result.DEALER_WIN;
        } else if (dealerHand.isBust() || player.getScore() > dealerHand.getScore()) {
            result = GameState.Result.PLAYER_WIN;
        } else if (player.getScore() < dealerHand.getScore()) {
            result = GameState.Result.DEALER_WIN;
        } else {
            result = GameState.Result.PUSH;
        }
        status = GameState.Status.FINISHED;
    }
}
