package nl.software101.objectboard;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Node tree path specification.
 */
public final class Path {
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
        if (from.isEmpty()) {
            return true;
        }
        if (!to.isEmpty()) {
            return from.get(0).equals(to.get(0)) && matches(from.subList(1, from.size()), to.subList(1, to.size()));
        }
        return false;
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
