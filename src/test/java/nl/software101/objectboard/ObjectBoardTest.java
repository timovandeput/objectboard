package nl.software101.objectboard;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class ObjectBoardTest {
    private static final Path PATH = Path.of("A");
    private static final int VALUE = 42;

    private final ObjectBoard board = new ObjectBoard();

    @Nested
    class Notifications {
        private final BoardListener listener = mock(BoardListener.class);
        private final BoardSubscription subscription = board.subscribe(PATH, listener);

        @Test
        void notifiesBoardChanges() {
            board.set(PATH, VALUE);
            board.unset(PATH);

            verify(listener).onSet(PATH, VALUE);
            verify(listener).onUnset(PATH, VALUE);
        }

        @Test
        void ignoresIfNothingChanged() {
            board.unset(PATH);

            verify(listener, never()).onUnset(any(), any());
        }

        @Test
        void synchronizesOnSubscribe() {
            board.unsubscribe(listener);
            board.set(PATH, VALUE);
            board.set(Path.of("other"), "Other value");

            board.subscribe(PATH, listener);

            verify(listener).onSet(PATH, VALUE);
            verify(listener, times(1)).onSet(any(), any());
        }

        @Test
        void unsubscribes() throws Exception {
            subscription.close();

            board.set(PATH, VALUE);
            board.unset(PATH);
            verify(listener, never()).onSet(any(), any());
            verify(listener, never()).onUnset(any(), any());
        }

        @Test
        void unsubscribesAllSubscriptionsForListener() {
            final var subscription = board.subscribe(Path.of(""), listener);

            board.unsubscribe(listener);

            board.set(PATH, VALUE);
            verify(listener, never()).onSet(any(), any());
        }

        @Test
        void notifiesEachListenerOnce() {
            board.subscribe(PATH, listener);
            board.subscribe(PATH, listener);

            board.set(PATH, VALUE);

            verify(listener, times(1)).onSet(any(), any());
        }

        @Test
        void synchronizesModifications() throws Exception {
            final var thread2 = new Thread(() -> board.unset(PATH));
            final var thread1 = new Thread(() -> board.set(PATH, VALUE));
            doAnswer((x) -> {
                Thread.sleep(100);
                return null;
            }).when(listener).onSet(any(), any());
            final var inOrder = Mockito.inOrder(listener);

            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();
            inOrder.verify(listener).onSet(PATH, VALUE);
            inOrder.verify(listener).onUnset(PATH, VALUE);
        }
    }
}
