package nl.software101.objectboard;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PathTest {
    private static final int VALUE = 42;

    @Test
    void createsInstance() {
        final var path = new Path("A", "B", "C");

        assertThat(path.toString()).isEqualTo("A/B/C");
    }

    @Test
    void createsInstanceFromEmptyString() {
        final var path = Path.of("");

        assertThat(path).isEqualTo(new Path());
    }

    @Test
    void createsInstanceFromString() {
        final var path = Path.of("A/B/C");

        assertThat(path).isEqualTo(new Path("A", "B", "C"));
    }

    @Test
    void matchesPlainSubPath() {
        assertThat(Path.of("").matches(Path.of("A"))).isFalse();
        assertThat(Path.of("A").matches(Path.of("B"))).isFalse();
        assertThat(Path.of("A/B").matches(Path.of("A/B"))).isTrue();
    }

    @Test
    void findsValue() {
        assertThat(new Path().in(VALUE)).contains(VALUE);
    }

    @Test
    void findsPathInTree() {
        final Object tree = Map.of("A", Map.of("A", "Nope", "B", VALUE));

        assertThat(Path.of("A/C").in(tree)).isEmpty();
        assertThat(Path.of("").in(tree)).contains(tree);
        assertThat(Path.of("A/B").in(tree)).contains(VALUE);
    }

    @Test
    void implementsEquals() {
        EqualsVerifier.forClass(Path.class).verify();
    }
}
