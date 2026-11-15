package storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StatsStorageTest {
    @TempDir Path directory;

    @Test
    void missingFileStartsEmptyAndSavedStatsSurviveReload() throws IOException {
        Path path = directory.resolve("stats.properties");
        StatsStorage storage = new StatsStorage(path);
        assertEquals(new GameStats(0, 0, 0), storage.loadStats());
        storage.saveStats(new GameStats(3, 2, 1));
        assertEquals(new GameStats(3, 2, 1), new StatsStorage(path).loadStats());
        storage.saveStats(new GameStats(4, 2, 1));
        assertEquals(7, storage.loadStats().gamesPlayed());
    }

    @Test
    void invalidFilesAreRejectedWithoutChangingThem() throws IOException {
        Path path = directory.resolve("stats.properties");
        for (String contents : new String[] {
                "games=1\nwins=bad\nlosses=0\npushes=0\n",
                "games=1\nwins=-1\nlosses=0\npushes=0\n",
                "games=2\nwins=1\nlosses=0\npushes=0\n",
                "games=0\nwins=0\n"}) {
            Files.writeString(path, contents);
            assertThrows(IOException.class, () -> new StatsStorage(path).loadStats());
            assertEquals(contents, Files.readString(path));
        }
    }

    @Test
    void rejectsNegativeCountsAndOverflow() {
        assertThrows(IllegalArgumentException.class, () -> new GameStats(-1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new GameStats(Integer.MAX_VALUE, 1, 0));
    }
}
