package nl.software101.objectboard;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoardSubscriptionTest {
    private static final Path PATH = Path.of("A/**");
    private static final Path MATCHING = Path.of("A/B/C");
    private static final Path NOT_MATCHING = Path.of("Z");

    private final ObjectBoard board = new ObjectBoard();
    private final BoardSubscription subscription = new BoardSubscription(board, PATH);

    @Test
    void matchesSubPath() {
        assertThat(subscription.matches(MATCHING)).isTrue();
        assertThat(subscription.matches(NOT_MATCHING)).isFalse();
    }
}
