package storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class StatsStorage {
    private final Path path;

    public StatsStorage(Path path) {
        this.path = path.toAbsolutePath();
    }

    public GameStats loadStats() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        } catch (NoSuchFileException e) {
            return new GameStats(0, 0, 0);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid statistics file", e);
        }
        try {
            int wins = Integer.parseInt(properties.getProperty("wins"));
            int losses = Integer.parseInt(properties.getProperty("losses"));
            int pushes = Integer.parseInt(properties.getProperty("pushes"));
            int games = Integer.parseInt(properties.getProperty("games"));
            GameStats stats = new GameStats(wins, losses, pushes);
            if (games != stats.gamesPlayed()) {
                throw new IllegalArgumentException("Inconsistent game count");
            }
            return stats;
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid statistics file", e);
        }
    }

    public void saveStats(GameStats stats) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("games", Integer.toString(stats.gamesPlayed()));
        properties.setProperty("wins", Integer.toString(stats.wins()));
        properties.setProperty("losses", Integer.toString(stats.losses()));
        properties.setProperty("pushes", Integer.toString(stats.pushes()));
        Path temporary = Files.createTempFile(path.getParent(), "blackjack-stats-", ".tmp");
        try {
            try (OutputStream output = Files.newOutputStream(temporary)) {
                properties.store(output, null);
            }
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
