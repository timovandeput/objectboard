package nl.software101.objectboard;

import java.util.*;

/**
 * Node tree path specification.
 */
public final class Path {
    public static final Path EMPTY = new Path();

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
        return Objects.deepEquals(segments, other.segments);
    }

    public Optional<Object> in(Object object) {
        return in(segments, object);
    }

    private Optional<Object> in(List<String> segments, Object object) {
        if (segments.isEmpty()) {
            return Optional.of(object);
        }
        if (object instanceof Map) {
            final var segment = segments.get(0);
            final var value = ((Map<?, ?>) object).get(segment);
            if (value != null) {
                return in(segments.subList(1, segments.size()), value);
            }
        }
        return Optional.empty();
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
