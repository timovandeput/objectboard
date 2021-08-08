package nl.software101.objectboard;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Node tree path specification.
 * <p>
 * Supports wildcard segments encoded as <code>*</code>.
 * Supports segment globs encoded as <code>**</code>.
 */
public final class Path {
    private static final String SEPARATOR = "/";
    private static final String WILDCARD = "*";
    private static final String GLOB = "**";

    private final List<String> segments;

    /**
     * Constructs a path from named segments.
     *
     * @param segments
     */
    Path(String... segments) {
        this(Arrays.asList(segments));
    }

    private Path(List<String> segments) {
        this.segments = segments;
    }

    /**
     * @param path specification
     * @return path from a specification using <code>/</code> as separator
     */
    public static Path of(String path) {
        return new Path(segments(path));
    }

    private static List<String> segments(String path) {
        if (path.isBlank()) {
            return List.of();
        }
        return Arrays.asList(path.split(SEPARATOR));
    }

    /**
     * @return true if this path is a sub-path o the other path, taking wildcards and globs into account
     */
    public boolean matches(Path other) {
        return matches(segments, other.segments);
    }

    private boolean matches(List<String> from, List<String> to) {
        if (from.isEmpty() && to.isEmpty()) {
            return true;
        }
        if (!from.isEmpty()) {
            final var key = head(from);
            if (!to.isEmpty() && (WILDCARD.equals(key) || key.equals(head(to)))) {
                return matches(tail(from), tail(to));
            }
            if (GLOB.equals(key)) {
                return matches(tail(from), to)
                        || (!to.isEmpty() && matches(from, tail(to)))
                        || (!to.isEmpty() && matches(tail(from), tail(to)));
            }
        }
        return false;
    }

    /**
     * Finds all data in the model matching this path.
     *
     * @param model (hierarchical) data structure
     * @return map of paths with the value at this path
     */
    public Map<String, Object> in(Object model) {
        return in("", model);
    }

    /**
     * Finds all data in the prefixed model matching this path.
     *
     * @param prefix path to the model
     * @param model  (hierarchical) data structure
     * @return map of paths with the value at this path
     */
    public Map<String, Object> in(String prefix, Object model) {
        final var map = new HashMap<String, Object>();
        in("", model, segments, segments(prefix), map::put);
        return map;
    }

    private void in(String prefix, Object model, List<String> from, List<String> to, BiConsumer<String, Object> found) {
        if (from.isEmpty() || to.isEmpty()) {
            in(prefix, model, from, found);
        } else {
            final var key = head(from);
            if (WILDCARD.equals(key) || key.equals(head(to))) {
                in(combine(prefix, head(to)), model, tail(from), tail(to), found);
            } else if (GLOB.equals(key)) {
                in(prefix, model, tail(from), to, found);
                in(combine(prefix, head(to)), model, from, tail(to), found);
                in(combine(prefix, head(to)), model, tail(from), tail(to), found);
            }
        }
    }

    private void in(String prefix, Object model, List<String> segments, BiConsumer<String, Object> found) {
        if (segments.isEmpty() || segments.equals(List.of(GLOB))) {
            found.accept(prefix, model);
        } else if (model instanceof Map) {
            final var map = (Map<?, ?>) model;
            final var head = head(segments);
            if (head.equals(WILDCARD)) {
                map.forEach((k, v) -> in(combine(prefix, k), v, tail(segments), found));
            } else if (head.equals(GLOB)) {
                in(prefix, map, tail(segments), found);
                final var next = segments.get(1);
                map.entrySet().stream()
                        .filter(e -> !e.getKey().equals(next))
                        .forEach(e -> in(combine(prefix, e.getKey()), e.getValue(), tail(segments), found));
            } else {
                final var value = map.get(head);
                if (value != null) {
                    in(combine(prefix, head(segments)), value, tail(segments), found);
                }
            }
        }
    }

    private String combine(String path, Object segment) {
        return path.isEmpty() ? segment.toString() : path + SEPARATOR + segment;
    }

    private String head(List<String> list) {
        return list.get(0);
    }

    private List<String> tail(List<String> list) {
        return list.subList(1, list.size());
    }

    public boolean set(Map<String, Object> map, @Nullable Object value) {
        return set(segments, map, value);
    }

    private boolean set(List<String> segments, Map<String, Object> map, @Nullable Object value) {
        if (segments.isEmpty()) {
            throw new ObjectBoardException("Cannot update an empty path");
        }
        final var key = head(segments);
        if (segments.size() > 1) {
            var target = map.get(key);
            if (!(target instanceof Map)) {
                if (value == null) {
                    return false;
                }
                target = new HashMap<>();
                map.put(key, target);
            }
            //noinspection unchecked
            return set(tail(segments), (Map<String, Object>) target, value);
        }
        final var current = map.get(key);
        if (Objects.deepEquals(value, current)) {
            return false;
        }
        map.put(key, value);
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(segments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path = (Path) o;
        return segments.equals(path.segments);
    }

    @Override
    public String toString() {
        return String.join(SEPARATOR, segments);
    }
}
