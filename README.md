testtest
test new

## build and run

use java 17 or later. build with `./gradlew classes` (windows: `gradlew.bat classes`).

in separate terminals, start the server and client:

```
java -cp build/classes/java/main server.BlackjackServer
java -cp build/classes/java/main client.BlackjackClient
```

the server uses port 5000 by default. pass a port to the server and a host and port to the client to change it. enter `START`, `HIT`, `STAND`, `STATS`, or `QUIT` in the client. statistics are saved in `.blackjack-stats.properties` in the server user's home directory.

## tests

run `./gradlew test` (windows: `gradlew.bat test`).
