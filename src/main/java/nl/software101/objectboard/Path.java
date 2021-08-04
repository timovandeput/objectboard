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
    private static final String WILDCARD = "*";
    private static final String GLOB = "**";

    private final List<String> segments;

    /**
     * Constructs a path from named segments.
     *
     * @param segments
     */
    Path(String... segments) {
        this.segments = Arrays.asList(segments);
    }

    /**
     * @param path specification
     * @return path from a specification using <code>/</code> as separator
     */
    public static Path of(String path) {
        if (path.isBlank()) {
            return new Path();
        }
        return new Path(path.split("/"));
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
            final var key = from.get(0);
            if (!to.isEmpty() && (WILDCARD.equals(key) || key.equals(to.get(0)))) {
                return matches(from.subList(1, from.size()), to.subList(1, to.size()));
            }
            if (GLOB.equals(key)) {
                return matches(from.subList(1, from.size()), to)
                        || (!to.isEmpty() && matches(from, to.subList(1, to.size())))
                        || (!to.isEmpty() && matches(from.subList(1, from.size()), to.subList(1, to.size())));
            }
        }
        return false;
    }

    /**
     * Finds all data in the model matching this path.
     *
     * @param model
     * @return map of paths with the value at this path
     */
    public Map<String, Object> in(Object model) {
        final var map = new HashMap<String, Object>();
        in(model, "", segments, map::put);
        return map;
    }

    private void in(Object model, String prefix, List<String> segments, BiConsumer<String, Object> found) {
        if (segments.isEmpty() || segments.equals(List.of(GLOB))) {
            found.accept(prefix, model);
        } else if (model instanceof Map) {
            final var map = (Map<?, ?>) model;
            final var segment = segments.get(0);
            if (WILDCARD.equals(segment)) {
                map.forEach((k, v) -> in(v, combine(prefix, k), segments.subList(1, segments.size()), found));
            } else if (GLOB.equals(segment)) {
                in(map, prefix, segments.subList(1, segments.size()), found);
                final var next = segments.get(1);
                map.entrySet().stream()
                        .filter(e -> !e.getKey().equals(next))
                        .forEach(e -> in(e.getValue(), combine(prefix, e.getKey()), segments.subList(1, segments.size()), found));
            } else {
                final var value = map.get(segment);
                if (value != null) {
                    in(value, combine(prefix, segments.get(0)), segments.subList(1, segments.size()), found);
                }
            }
        }
    }

    private String combine(String path, Object segment) {
        return path.isEmpty() ? segment.toString() : path + '/' + segment;
    }

    public boolean set(Map<String, Object> map, @Nullable Object value) {
        return set(segments, map, value);
    }

    private boolean set(List<String> segments, Map<String, Object> map, @Nullable Object value) {
        if (segments.isEmpty()) {
            throw new ObjectBoardException("Cannot update an empty path");
        }
        final var key = segments.get(0);
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
            return set(segments.subList(1, segments.size()), (Map<String, Object>) target, value);
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
        return String.join("/", segments);
    }
}
