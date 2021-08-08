package nl.software101.objectboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class ObjectBoardTest {
    private static final String PATH = "A/B";
    private static final String SUBSCRIPTION = "A/*";
    private static final int VALUE = 42;

    private final ObjectBoard board = new ObjectBoard();

    @Nested
    class Notifications {
        private final BoardListener listener = mock(BoardListener.class);

        @BeforeEach
        void setUp() {
            board.subscribe(SUBSCRIPTION, listener);
        }

        @Test
        void synchronizesOnSubscribe() {
            board.unsubscribe(listener);
            board.set(PATH, VALUE);
            board.set("Other path", "Other value");

            board.subscribe(SUBSCRIPTION, listener);

            verify(listener).onSet(PATH, VALUE);
            verify(listener, times(1)).onSet(any(), any());
        }

        @Test
        void notifiesValueUpdate() {
            board.set(PATH, VALUE);

            verify(listener).onSet(PATH, VALUE);
        }

        @Test
        void ignoresOverwriteOfEqualValue() {
            board.set(PATH, VALUE);
            board.set(PATH, VALUE);

            verify(listener, times(1)).onSet(any(), any());
        }

        @Test
        void notifiesNestedValueUpdate() {
            board.set(PATH, VALUE);
        }

        @Test
        void notifiesValueRemove() {
            board.set(PATH, VALUE);
            board.unset(PATH);

            verify(listener).onUnset(PATH);
        }

        @Test
        void ignoresRemovingNonExistingValue() {
            board.unset(PATH);

            verify(listener, never()).onUnset(any());
        }

        @Test
        void unsubscribes() {
            board.unsubscribe(listener);

            board.set(PATH, VALUE);
            board.unset(PATH);
            verify(listener, never()).onSet(any(), any());
            verify(listener, never()).onUnset(any());
        }

        @Test
        void unsubscribesAllSubscriptionsForListener() {
            board.subscribe("**", listener);
            Mockito.reset(listener);

            board.unsubscribe(listener);

            board.set(PATH, VALUE);
            verify(listener, never()).onSet(any(), any());
        }

        @Test
        void notifiesEachListenerOnce() {
            board.subscribe(SUBSCRIPTION, listener);
            board.subscribe(SUBSCRIPTION, listener);

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
            inOrder.verify(listener).onUnset(PATH);
        }
    }
}
