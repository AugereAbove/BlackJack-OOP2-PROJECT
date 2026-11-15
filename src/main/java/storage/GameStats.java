package storage;

public record GameStats(int wins, int losses, int pushes) {
    public GameStats {
        if (wins < 0 || losses < 0 || pushes < 0 || (long) wins + losses + pushes > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid statistics");
        }
    }

    public int gamesPlayed() {
        return wins + losses + pushes;
    }
}
