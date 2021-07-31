package nl.software101.objectboard;

import java.util.*;

/**
 * Node tree path specification.
 */
public final class Path {
    public static final Path EMPTY = new Path();
    private static final String WILDCARD = "*";

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
        if (!from.isEmpty() && !to.isEmpty()) {
            final var key = from.get(0);
            if (WILDCARD.equals(key) || key.equals(to.get(0))) {
                return matches(from.subList(1, from.size()), to.subList(1, to.size()));
            }
        }
        return false;
    }

    public Set<Object> in(Object object) {
        return in(segments, object);
    }

    private Set<Object> in(List<String> segments, Object object) {
        if (segments.isEmpty()) {
            return Set.of(object);
        }
        if (object instanceof Map) {
            final var map = (Map<?, ?>) object;
            final var segment = segments.get(0);
            if (WILDCARD.equals(segment)) {
                final var values = new HashSet<>();
                map.values().forEach(v -> {
                    final var set = in(segments.subList(1, segments.size()), v);
                    values.addAll(set);
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
