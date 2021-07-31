package nl.software101.objectboard;

import java.io.Closeable;

/**
 * Subscription handle to board model changes.
 */
public class BoardSubscription implements Closeable {
    private final ObjectBoard board;
    private final Path path;

    BoardSubscription(ObjectBoard board, Path path) {
        this.board = board;
        this.path = path;
    }

    boolean matches(Path path) {
        return this.path.matches(path);
    }

    @Override
    public void close() {
        board.unsubscribe(this);
    }
}
