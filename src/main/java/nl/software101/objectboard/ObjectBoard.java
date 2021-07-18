package nl.software101.objectboard;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Implementation of the "blackboard" design pattern.
 * <p>
 * Values are stored in a network of {@link Node} objects that can be updated using a path down the nodes by providing
 * a period-separated (".") hierarchical path.
 * Subscribers are automatically notified about changes in the tree.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Blackboard_(design_pattern)">Blackboard design pattern</a>
 */
public class ObjectBoard {
    private final Node model = new Node();
    private final Map<BoardSubscription, BoardListener> listeners = new HashMap<>();

    /**
     * Sets a value in the tree.
     * Subscribed listeners are notified of the change.
     *
     * @param path  location to update
     * @param value new value
     */
    public synchronized void set(String path, Object value) {
        model.set(path, value);
        forMatching(path, listener -> listener.onSet(path, value));
    }

    private void forMatching(String path, Consumer<BoardListener> subscription) {
        listeners.entrySet().stream()
                .filter(e -> e.getKey().matches(path))
                .map(Map.Entry::getValue)
                .distinct()
                .forEach(subscription);
    }

    /**
     * Clears a value in the tree.
     * Subscribed listeners are notified of the change
     *
     * @param path location to clear
     */
    public synchronized void unset(String path) {
        model.unset(path).ifPresent(value ->
                forMatching(path, listener -> listener.onUnset(path, value))
        );
    }

    /**
     * Subscribes a listener to a path in the tree.
     * The same listener can subscribe to overlapping paths, but will only be notified once of a change.
     *
     * @param path     root location to subscribe
     * @param listener callback interface for notifications
     * @return subscription
     */
    synchronized BoardSubscription subscribe(String path, BoardListener listener) {
        final var subscription = new BoardSubscription(this, path);
        listeners.put(subscription, listener);
        model.field(path).ifPresent(value -> listener.onSet(path, value));
        return subscription;
    }

    /**
     * Unsubscribes all subscriptions for a single listener.
     *
     * @param listener (possibly shared) notification callback
     */
    public synchronized void unsubscribe(BoardListener listener) {
        final var remove = listeners.entrySet().stream()
                .filter(e -> e.getValue() == listener)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        remove.forEach(this::unsubscribe);
    }

    /**
     * Unsubscribes a single path.
     *
     * @param subscription handle that was created during subscription.
     */
    synchronized void unsubscribe(BoardSubscription subscription) {
        listeners.remove(subscription);
    }
}
