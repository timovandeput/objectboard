package nl.software101.objectboard;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NodeTest {
    private static final String NAME = "Name";
    private static final String VALUE = "Value";

    private final Node root = new Node();

    @Nested
    class NodeNavigation {
        @Test
        void traversesPathToNode() {
            final var target = new Node();
            root.set("A", new Node().set("B", new Node().set("C", target)));

            final var node = root.resolve("A.B.C");

            assertThat(node).isSameAs(target);
        }

        @Test
        void throws_pathDoesNotExist() {
            root.set("A", new Node());

            assertThatThrownBy(() -> root.resolve("A.B.C"))
                    .isInstanceOf(FieldNotFoundException.class)
                    .hasMessageContaining("'A.B'");
        }

        @Test
        void throws_pathDoesNotReferenceNode() {
            root.set("A", new Node().set("B", VALUE));

            assertThatThrownBy(() -> root.resolve("A.B"))
                    .isInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class FieldValues {
        @Test
        void setsFieldValue() {
            final var value = root.set(NAME, VALUE);

            assertThat(value).isSameAs(root);
            assertThat(root.fields()).isEqualTo(Map.of(NAME, VALUE));
            assertThat(root.field(NAME)).contains(VALUE);
        }

        @Test
        void setsValueOnPath() {
            final var target = new Node();
            root.set("A", new Node().set("B", target));

            final var node = root.set("A.B.C", VALUE);

            assertThat(target.field("C")).contains(VALUE);
            assertThat(node).isSameAs(target);
        }

        @Test
        void removesField() {
            root.set(NAME, VALUE);

            final var value = root.unset(NAME);

            assertThat(value).contains(VALUE);
            assertThat(root.field(NAME)).isEmpty();
            assertThat(root.fields()).isEmpty();
        }

        @Test
        void removesUnknownField() {
            assertThat(root.unset(NAME)).isEmpty();
        }

        @Test
        void removesValueOnPath() {
            root.set("A", new Node().set("B", VALUE));

            final var value = root.unset("A.B");

            assertThat(root.field("A.B")).isEmpty();
            assertThat(value).contains(VALUE);
        }
    }
}
