package oopnemo.model;

/** Shared validation; invalid updates throw before changing the old value. */
public final class Validation {
    private Validation() { }

    public static String text(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
        return value.trim();
    }

    public static int range(int value, int min, int max, String field) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(field + " must be between " + min + " and " + max + ".");
        }
        return value;
    }

    public static String email(String value) {
        String result = text(value, "Email");
        int at = result.indexOf('@');
        if (at <= 0 || at == result.length() - 1 || result.indexOf('@', at + 1) >= 0
                || result.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("Email must contain a local part and a domain separated by @.");
        }
        return result;
    }
}
