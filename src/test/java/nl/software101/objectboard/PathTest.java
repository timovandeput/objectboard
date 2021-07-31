package nl.software101.objectboard;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PathTest {
    private static final int VALUE = 42;
    private static final String OTHER_VALUE = "Other";

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
    void matchesWildcardSubPath() {
        assertThat(Path.of("A/*").matches(Path.of("A/B"))).isTrue();
        assertThat(Path.of("A/*").matches(Path.of("A/B/C"))).isFalse();
        assertThat(Path.of("A/*/C").matches(Path.of("A/B/C"))).isTrue();
        assertThat(Path.of("A/*/C").matches(Path.of("A/C"))).isFalse();
        assertThat(Path.of("A/*/C").matches(Path.of("A/B/B/C"))).isFalse();
    }

    @Test
    void matchesGlobSubPath() {
        assertThat(Path.of("**").matches(Path.of("A/B/C"))).isTrue();
        assertThat(Path.of("**").matches(Path.of(""))).isTrue();
        assertThat(Path.of("A/**").matches(Path.of("B"))).isFalse();
        assertThat(Path.of("A/**").matches(Path.of("A"))).isTrue();
        assertThat(Path.of("A/**").matches(Path.of("A/B/C"))).isTrue();
        assertThat(Path.of("A/**/Z").matches(Path.of("A/B"))).isFalse();
        assertThat(Path.of("A/**/Z").matches(Path.of("A/Z"))).isTrue();
        assertThat(Path.of("A/**/Z").matches(Path.of("A/B/Y/Z"))).isTrue();
    }

    @Test
    void findsValue() {
        assertThat(new Path().in(VALUE)).contains(VALUE);
    }

    @Test
    void findsPathInTree() {
        final Object tree = Map.of("A", Map.of("A", "Nope", "B", VALUE));

        assertThat(Path.of("A/C").in(tree)).isEmpty();
        assertThat(Path.of("").in(tree)).isEmpty();
        assertThat(Path.of("A/B").in(tree)).contains(VALUE);
    }

    @Test
    void findsWildcardPathsInTree() {
        final Map<?, ?> subtree = Map.of("X", "Nope", "B", VALUE);
        final Object tree = Map.of("A", subtree, "B", OTHER_VALUE);

        assertThat(Path.of("*/B").in(tree)).containsExactly(VALUE);
        assertThat(Path.of("*/Z").in(tree)).isEmpty();
        assertThat(Path.of("A/*").in(tree)).containsAll(subtree.values());
        assertThat(Path.of("*").in(tree)).containsExactly(OTHER_VALUE);
    }

    @Test
    void findsGlobPathsInTree() {
        final Map<?, ?> subtree = Map.of("A", OTHER_VALUE, "B", VALUE);
        final Object tree = Map.of("A", subtree, "B", VALUE);

        assertThat(Path.of("**").in(tree)).containsExactly(tree);
        assertThat(Path.of("**/A").in(tree)).isEmpty();
        assertThat(Path.of("**/B").in(tree)).containsExactly(VALUE);
        assertThat(Path.of("A/**").in(tree)).containsExactly(subtree);
        assertThat(Path.of("A/**/B").in(tree)).containsExactly(VALUE);
        assertThat(Path.of("A/**/**/B").in(tree)).containsExactly(VALUE);
        assertThat(Path.of("B/**").in(tree)).containsExactly(VALUE);
    }

    @Test
    void implementsEquals() {
        EqualsVerifier.forClass(Path.class).verify();
    }
}
