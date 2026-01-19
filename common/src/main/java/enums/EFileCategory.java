package enums;

public enum EFileCategory {

    PROFILE("profile"),
    DOCUMENTS("documents"),
    EVENTS("events"),
    TICKETS("tickets"),
    RECEIPTS("receipts");

    private final String value;

    EFileCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
