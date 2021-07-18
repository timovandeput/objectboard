package nl.software101.objectboard;

public class FieldNotFoundException extends ObjectBoardException {
    private final String field;

    public FieldNotFoundException(String field) {
        super("Field not found: '" + field + "'");
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
