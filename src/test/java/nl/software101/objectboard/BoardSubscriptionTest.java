package nl.software101.objectboard;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoardSubscriptionTest {
    private static final String PATH = "A.B";

    private final ObjectBoard board = new ObjectBoard();
    private final BoardSubscription subscription = new BoardSubscription(board, PATH);

    @Test
    void matchesSubPath() {
        assertThat(subscription.matches("A")).isFalse();
        assertThat(subscription.matches("B")).isFalse();
        assertThat(subscription.matches("A.B")).isTrue();
        assertThat(subscription.matches("A.B.C")).isTrue();
    }
}
