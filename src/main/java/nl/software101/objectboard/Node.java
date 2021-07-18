package nl.software101.objectboard;

import java.util.*;

/**
 * Element in a hierarchical model of nodes.
 */
public class Node {
    private final Map<String, Object> fields = new HashMap<>();

    /**
     * @return all fields of this node
     */
    public Map<String, Object> fields() {
        return Collections.unmodifiableMap(fields);
    }

    /**
     * @param name name of the field
     * @return (if present) value of the field
     */
    public Optional<Object> field(String name) {
        return Optional.ofNullable(fields.get(name));
    }

    /**
     * Adds or updates a named field.
     *
     * @param path  name of the field
     * @param value value of the field (which can be a new Node)
     * @return the node containing the value
     */
    public Node set(String path, Object value) {
        final var list = toPathList(path);
        final var node = resolve(list.subList(0, list.size() - 1));
        node.fields.put(list.get(list.size() - 1), value);
        return node;
    }

    /**
     * Clears a named field.
     *
     * @param path name of the field
     * @return the prior value of the field
     */
    public Optional<Object> unset(String path) {
        final var list = toPathList(path);
        final var node = resolve(list.subList(0, list.size() - 1));
        final var value = node.fields.remove(list.get(list.size() - 1));
        return Optional.ofNullable(value);
    }

    /**
     * @param path period-delimited (".") relative path of field names
     * @return node indicated by the path
     * @throws FieldNotFoundException if there is no node at this path
     */
    public Node resolve(String path) {
        final var list = toPathList(path);
        return resolve(list);
    }

    private List<String> toPathList(String path) {
        return (path.contains("."))
                ? Arrays.asList(path.split("\\."))
                : List.of(path);
    }

    private Node resolve(List<String> path) {
        if (path.isEmpty()) {
            return this;
        }

        String name = path.get(0);
        final var next = (Node) fields.get(name);
        if (next == null) {
            throw new FieldNotFoundException(name);
        }

        if (path.size() > 1) {
            try {
                return next.resolve(path.subList(1, path.size()));
            } catch (FieldNotFoundException e) {
                throw new FieldNotFoundException(name + '.' + e.getField());
            }
        }
        return next;
    }
}

