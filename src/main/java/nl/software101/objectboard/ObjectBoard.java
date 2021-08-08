package nl.software101.objectboard;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Implementation of the "blackboard" design pattern.
 * <p>
 * Values are stored in-memory in a hierarchy of named nodes. Clients receive notifications on modification of values.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Blackboard_(design_pattern)">Blackboard design pattern</a>
 */
public class ObjectBoard {
    private final Map<String, Object> model = new HashMap<>();
    private final Map<BoardSubscription, BoardListener> listeners = new HashMap<>();

    /**
     * Sets a value in the tree.
     * Subscribed listeners are notified of the new value.
     *
     * @param path  location to update
     * @param value new value
     */
    public synchronized void set(String path, Object value) {
        final var location = Path.of(path);
        final var modified = location.set(model, value);
        if (modified) {
            forMatching(location, listener -> listener.onSet(path, value));
        }
    }

    private void forMatching(Path path, Consumer<BoardListener> subscription) {
        listeners.entrySet().stream()
                .filter(e -> e.getKey().matches(path))
                .map(Map.Entry::getValue)
                .distinct()
                .forEach(subscription);
    }

    /**
     * Clears a value in the tree.
     * Subscribed listeners are notified of the cleared value.
     *
     * @param path location to clear
     */
    public synchronized void unset(String path) {
        final var location = Path.of(path);
        final var modified = location.set(model, null);
        if (modified) {
            forMatching(location, listener -> listener.onUnset(path));
        }
    }

    /**
     * Subscribes a listener to a path in the tree.
     * The same listener can subscribe to overlapping paths, but will only be notified once of a change.
     *
     * @param path     root location to subscribe
     * @param listener callback interface for notifications
     */
    public synchronized void subscribe(String path, BoardListener listener) {
        final var location = Path.of(path);
        final var subscription = new BoardSubscription(location);
        listeners.put(subscription, listener);
        location.in(model).forEach(listener::onSet);
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
        remove.forEach(listeners::remove);
    }

    private static class BoardSubscription {
        private final Path path;

        BoardSubscription(Path path) {
            this.path = path;
        }

        boolean matches(Path path) {
            return this.path.matches(path);
        }
    }
}
