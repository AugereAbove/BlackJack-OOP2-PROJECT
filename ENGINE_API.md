# Engine integration proposal

The `game` package is independent of the server and client. `BlackjackGame` provides `startGame()`, `hit()`, `stand()`, and `getGameState()`. Invalid actions throw `IllegalStateException`. A new round may start after the previous round finishes.

`GameState` is a snapshot: `getStatus()` returns `NOT_STARTED`, `PLAYER_TURN`, or `FINISHED`; `getResult()` returns `PLAYER_WIN`, `DEALER_WIN`, or `PUSH` after a round and is otherwise null. `getPlayerCards()` and `getPlayerScore()` describe the player. `getDealerCards()` exposes only the first card during the player's turn, then the full hand when finished. Card lists cannot be modified. A natural Blackjack ends the round immediately.

This API remains a proposal for Zahran to review; server integration and the complete application are still pending. The Java version and build should be revisited if course requirements require changes.
