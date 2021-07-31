package nl.software101.objectboard;

/**
 * Notification API for changes in an object board.
 */
public interface BoardListener {
    /**
     * Notifies that a value has been set.
     *
     * @param path  path of the value
     * @param value the added or updated value
     */
    void onSet(Path path, Object value);

    /**
     * Notifies that a value has been cleared.
     *
     * @param path path of the value
     */
    void onUnset(Path path);
}
