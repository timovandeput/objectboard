package nl.software101.objectboard;

/**
 * Notification API for changes in an object board.
 */
public interface BoardListener {
    /**
     * Notifies a value has been set.
     *
     * @param path  path of the value
     * @param value the added or updated value
     */
    void onSet(Path path, Object value);

    /**
     * Notifies a value has been cleared.
     *
     * @param path  path of the value
     * @param value the prior value
     */
    void onUnset(Path path, Object value);
}
