package game;

import org.junit.jupiter.api.Test;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import static game.Card.Rank.*;
import static game.GameState.Result.*;
import static game.GameState.Status.*;
import static org.junit.jupiter.api.Assertions.*;

class BlackjackGameTest {
    private BlackjackGame game(Card.Rank... ranks) {
        return new BlackjackGame(() -> new Deck() {
            private final Queue<Card.Rank> cards = new ArrayDeque<>(Arrays.asList(ranks));
            @Override public Card deal() { return new Card(cards.remove(), Card.Suit.CLUBS); }
        });
    }

    @Test
    void hidesDealerCardUntilPlayerBusts() {
        BlackjackGame game = game(TEN, NINE, SIX, SEVEN, KING);
        game.startGame();
        GameState snapshot = game.getGameState();
        assertEquals(PLAYER_TURN, snapshot.getStatus());
        assertEquals(1, snapshot.getDealerCards().size());
        assertNull(snapshot.getResult());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.getPlayerCards().clear());
        game.hit();
        assertEquals(DEALER_WIN, game.getGameState().getResult());
        assertEquals(2, game.getGameState().getDealerCards().size());
        assertEquals(1, snapshot.getDealerCards().size());
        assertThrows(IllegalStateException.class, game::hit);
    }

    @Test
    void dealerDrawsAndBustsAfterStand() {
        BlackjackGame game = game(TEN, TEN, SEVEN, SIX, KING);
        game.startGame();
        game.stand();
        assertEquals(PLAYER_WIN, game.getGameState().getResult());
        assertEquals(3, game.getGameState().getDealerCards().size());
    }

    @Test
    void scoreComparisonAndTies() {
        BlackjackGame win = game(TEN, TEN, NINE, EIGHT);
        win.startGame(); win.stand();
        assertEquals(PLAYER_WIN, win.getGameState().getResult());
        BlackjackGame loss = game(TEN, TEN, SEVEN, NINE);
        loss.startGame(); loss.stand();
        assertEquals(DEALER_WIN, loss.getGameState().getResult());
        BlackjackGame tie = game(TEN, TEN, EIGHT, EIGHT);
        tie.startGame(); tie.stand();
        assertEquals(PUSH, tie.getGameState().getResult());
    }

    @Test
    void initialNaturalsEndRoundAndTakePrecedence() {
        BlackjackGame player = game(ACE, TEN, KING, NINE);
        player.startGame();
        assertEquals(PLAYER_WIN, player.getGameState().getResult());
        BlackjackGame dealer = game(TEN, ACE, NINE, KING);
        dealer.startGame();
        assertEquals(DEALER_WIN, dealer.getGameState().getResult());
        BlackjackGame both = game(ACE, ACE, KING, KING);
        both.startGame();
        assertEquals(PUSH, both.getGameState().getResult());
        assertEquals(FINISHED, both.getGameState().getStatus());
    }

    @Test
    void rejectsInvalidActionsAndResetsAfterRound() {
        BlackjackGame game = game(TEN, TEN, SEVEN, SEVEN);
        assertThrows(IllegalStateException.class, game::stand);
        game.startGame();
        assertThrows(IllegalStateException.class, game::startGame);
        game.stand();
        assertThrows(IllegalStateException.class, game::stand);
        game.startGame();
        assertEquals(PLAYER_TURN, game.getGameState().getStatus());
        assertNull(game.getGameState().getResult());
        assertEquals(2, game.getGameState().getPlayerCards().size());
        assertEquals(1, game.getGameState().getDealerCards().size());
    }
    @Test
    void hitUpdatesScoreAndDealerRemainsHidden() {
        BlackjackGame game = game(ACE, TEN, FIVE, SIX, NINE, FIVE);
        game.startGame();
        GameState before = game.getGameState();
        assertEquals(16, before.getPlayerScore());
        game.hit();
        GameState after = game.getGameState();
        assertEquals(15, after.getPlayerScore());
        assertEquals(3, after.getPlayerCards().size());
        assertEquals(1, after.getDealerCards().size());
        assertEquals(2, before.getPlayerCards().size());
        assertThrows(UnsupportedOperationException.class, () -> after.getDealerCards().clear());
        game.stand();
        assertEquals(DEALER_WIN, game.getGameState().getResult());
        assertEquals(3, game.getGameState().getDealerCards().size());
    }

    @Test
    void threeCardTwentyOneCanPushDealerTwentyOne() {
        BlackjackGame game = game(TEN, TEN, FIVE, SIX, SIX, FIVE);
        game.startGame();
        game.hit();
        assertEquals(21, game.getGameState().getPlayerScore());
        game.stand();
        assertEquals(PUSH, game.getGameState().getResult());
    }

    @Test
    void newGameHasNoCardsAndRejectsActions() {
        BlackjackGame game = game(TEN, NINE, SEVEN, EIGHT);
        GameState state = game.getGameState();
        assertEquals(NOT_STARTED, state.getStatus());
        assertEquals(0, state.getPlayerScore());
        assertTrue(state.getPlayerCards().isEmpty());
        assertTrue(state.getDealerCards().isEmpty());
        assertNull(state.getResult());
        assertThrows(IllegalStateException.class, game::hit);
    }
}
