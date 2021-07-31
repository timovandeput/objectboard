package nl.software101.objectboard;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Node tree path specification.
 */
public final class Path {
    public static final Path EMPTY = new Path();
    private static final String WILDCARD = "*";
    private static final String GLOB = "**";

    private final List<String> segments;

    Path(String... segments) {
        this.segments = Arrays.asList(segments);
    }

    public static Path of(String path) {
        if (path.isBlank()) {
            return new Path();
        }
        return new Path(path.split("/"));
    }

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

    public Set<Object> in(Object object) {
        return in(segments, object);
    }

    private Set<Object> in(List<String> segments, Object object) {
        if (segments.isEmpty()) {
            return (object instanceof Map) ? Set.of() : Set.of(object);
        }
        if (segments.equals(List.of(GLOB))) {
            return Set.of(object);
        }
        if (object instanceof Map) {
            final var map = (Map<?, ?>) object;
            final var segment = segments.get(0);
            if (WILDCARD.equals(segment)) {
                final var values = new HashSet<>();
                map.values().forEach(v -> {
                    values.addAll(in(segments.subList(1, segments.size()), v));
                });
                return values;
            }
            if (GLOB.equals(segment)) {
                final var values = new HashSet<>();
                map.forEach((k, v) -> {
                    String next = segments.get(1);
                    if (k.equals(next) || WILDCARD.equals(next) || GLOB.equals(next)) {
                        values.addAll(in(segments.subList(1, segments.size()), map));
                    } else {
                        values.addAll(in(segments, v));
                    }
                });
                return values;
            }
            final var value = map.get(segment);
            if (value != null) {
                return in(segments.subList(1, segments.size()), value);
            }
        }
        return Set.of();
    }

    public void set(Map<String, Object> map, @Nullable Object value) {
        set(segments, map, value);
    }

    private void set(List<String> segments, Map<String, Object> map, @Nullable Object value) {
        if (segments.isEmpty()) {
            throw new ObjectBoardException("Cannot update an empty path");
        }
        final var key = segments.get(0);
        if (segments.size() > 1) {
            var target = map.get(key);
            if (!(target instanceof Map)) {
                if (value == null) {
                    return;
                }
                target = new HashMap<>();
                map.put(key, target);
            }
            //noinspection unchecked
            set(segments.subList(1, segments.size()), (Map<String, Object>) target, value);
            return;
        }
        if (value != null || map.containsKey(key)) {
            map.put(key, value);
        }
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
