package nl.software101.objectboard;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PathTest {
    private static final Object VALUE = 42;
    private static final Object OTHER_VALUE = "Other";

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
        assertThat(new Path().in(VALUE)).isEqualTo(Map.of("", VALUE));
    }

    @Test
    void findsPathInTree() {
        final Object tree = Map.of("A", Map.of("A", OTHER_VALUE, "B", VALUE));

        assertThat(Path.of("A/C").in(tree)).isEmpty();
        assertThat(Path.of("").in(tree)).isEqualTo(Map.of("", tree));
        assertThat(Path.of("A/B").in(tree)).isEqualTo(Map.of("A/B", VALUE));
    }

    @Test
    void findsWildcardPathsInTree() {
        final Map<?, ?> subtree = Map.of("X", OTHER_VALUE, "B", VALUE);
        final Object tree = Map.of("A", subtree, "B", OTHER_VALUE);

        assertThat(Path.of("*/B").in(tree)).isEqualTo(Map.of("A/B", VALUE));
        assertThat(Path.of("*/Z").in(tree)).isEmpty();
        assertThat(Path.of("A/*").in(tree)).isEqualTo(Map.of("A/B", VALUE, "A/X", OTHER_VALUE));
        assertThat(Path.of("*").in(tree)).isEqualTo(Map.of("A", subtree, "B", OTHER_VALUE));
    }

    @Test
    void findsGlobPathsInTree() {
        final Map<?, ?> subtree = Map.of("A", OTHER_VALUE, "B", VALUE);
        final Object tree = Map.of("A", subtree, "B", VALUE);

        assertThat(Path.of("**").in(tree)).isEqualTo(Map.of("", tree));
        assertThat(Path.of("**/A").in(tree)).isEqualTo(Map.of("A", subtree));
        assertThat(Path.of("**/B").in(tree)).isEqualTo(Map.of("B", VALUE, "A/B", VALUE));
        assertThat(Path.of("A/**").in(tree)).isEqualTo(Map.of("A", subtree));
        assertThat(Path.of("A/**/B").in(tree)).isEqualTo(Map.of("A/B", VALUE));
        assertThat(Path.of("A/**/**/B").in(tree)).isEqualTo(Map.of("A/B", VALUE));
        assertThat(Path.of("B/**").in(tree)).isEqualTo(Map.of("B", VALUE));
    }

    @Test
    void throws_addWithoutPath() {
        assertThatThrownBy(() -> Path.of("").set(Map.of(), VALUE))
                .isInstanceOf(ObjectBoardException.class)
                .hasMessageContaining("empty path");
    }

    @Test
    void addsValueToExistingTree() {
        //noinspection MismatchedQueryAndUpdateOfCollection
        final var subtree = new HashMap<>();
        final var tree = new HashMap<String, Object>();
        tree.put("A", subtree);

        Path.of("A/B").set(tree, VALUE);

        assertThat(tree.get("A")).isSameAs(subtree);
        assertThat(subtree.get("B")).isEqualTo(VALUE);
    }

    @Test
    void createsNewTreeNode() {
        final var tree = new HashMap<String, Object>();
        tree.put("A", VALUE);

        final var modified = Path.of("A/B").set(tree, VALUE);

        assertThat(modified).isTrue();
        assertThat(tree).isEqualTo(Map.of("A", Map.of("B", VALUE)));
    }

    @Test
    void ignores_clearNonExistingPath() {
        final var tree = Map.of("A", VALUE);

        final var modified = Path.of("A/B/C").set(tree, null);

        assertThat(modified).isFalse();
    }

    @Test
    void ignores_clearNonExistingValue() {
        // Would throw if modified
        final var tree = Map.<String, Object>of();

        final var modified = Path.of("A").set(tree, null);

        assertThat(modified).isFalse();
    }

    @Test
    void implementsEquals() {
        EqualsVerifier.forClass(Path.class).verify();
    }
}
