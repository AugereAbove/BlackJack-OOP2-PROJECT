package game;

import java.util.List;

public class GameState {
    public enum Status { NOT_STARTED, PLAYER_TURN, FINISHED }
    public enum Result { PLAYER_WIN, DEALER_WIN, PUSH }

    private final Status status;
    private final Result result;
    private final List<Card> playerCards;
    private final List<Card> dealerCards;
    private final int playerScore;

    GameState(Status status, Result result, List<Card> playerCards,
              List<Card> dealerCards, int playerScore) {
        this.status = status;
        this.result = result;
        this.playerCards = List.copyOf(playerCards);
        this.dealerCards = List.copyOf(dealerCards);
        this.playerScore = playerScore;
    }

    public Status getStatus() { return status; }
    public Result getResult() { return result; }
    public List<Card> getPlayerCards() { return playerCards; }
    public List<Card> getDealerCards() { return dealerCards; }
    public int getPlayerScore() { return playerScore; }
}
