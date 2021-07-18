package nl.software101.objectboard;

import java.io.Closeable;
import java.io.IOException;

/**
 * Subscription handle to board model changes.
 */
public class BoardSubscription implements Closeable {
    private final ObjectBoard board;
    private final String path;

    BoardSubscription(ObjectBoard board, String path) {
        this.board = board;
        this.path = path;
    }

    boolean matches(String path) {
        return path.startsWith(this.path);
    }

    @Override
    public void close() throws IOException {
        board.unsubscribe(this);
    }
}
