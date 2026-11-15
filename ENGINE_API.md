# Engine integration proposal

The `game` package is independent of the server and client. The proposed server entry point is `BlackjackGame` with `startGame()`, `hit()`, `stand()`, and `getGameState()`. The state should expose the player's cards and score, the dealer's visible card during play, and the result and full dealer hand after the round. Invalid actions should throw `IllegalStateException`. This contract is a proposal for Zahran to review, not an agreed final API.

Next steps: agree on the state and result types, implement round and dealer logic, test deterministic rounds, and integrate with the server. The Java version and build should be revisited if course requirements or Zahran's existing work require changes.
