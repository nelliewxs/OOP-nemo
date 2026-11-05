package oopnemo.service;

/** A returned result, not a stored domain entity. */
public record PaymentReceipt(String requestId, String personId, String name, long amount) {
    @Override public String toString() {
        return requestId + " | " + name + " (" + personId + ") | " + amount + " USD";
    }
}
