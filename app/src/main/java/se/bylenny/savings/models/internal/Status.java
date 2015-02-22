package se.bylenny.savings.models.internal;

public enum Status {
    active("active"),
    deleted("deleted");
    private final String status;

    private Status(String status) {
        this.status = status;
    }
}