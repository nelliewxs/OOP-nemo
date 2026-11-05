package oopnemo.model;

import java.util.Objects;

/** Immutable queue sort keys; lifecycle is managed by PayrollService. */
public final class PaymentRequest {
    private final String id;
    private final String personId;
    private final Priority priority;
    private final long sequence;
    private final RequestStatus status;

    public PaymentRequest(String id, String personId, Priority priority, long sequence) {
        this(id, personId, priority, sequence, RequestStatus.PENDING);
    }

    private PaymentRequest(String id, String personId, Priority priority, long sequence,
                           RequestStatus status) {
        this.id = Validation.text(id, "Request ID");
        this.personId = Validation.text(personId, "Person ID");
        this.priority = Objects.requireNonNull(priority, "Priority");
        if (sequence < 0) { throw new IllegalArgumentException("Sequence must not be negative."); }
        this.sequence = sequence;
        this.status = status;
    }
    public String getId() { return id; }
    public String getPersonId() { return personId; }
    public Priority getPriority() { return priority; }
    public long getSequence() { return sequence; }
    public RequestStatus getStatus() { return status; }
    public PaymentRequest processed() { return finish(RequestStatus.PROCESSED); }
    public PaymentRequest cancelled() { return finish(RequestStatus.CANCELLED); }
    private PaymentRequest finish(RequestStatus next) {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can change status.");
        }
        return new PaymentRequest(id, personId, priority, sequence, next);
    }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof PaymentRequest request && id.equals(request.id);
    }
    @Override public int hashCode() { return id.hashCode(); }
    @Override public String toString() {
        return id + " | person " + personId + " | " + priority + " | " + status;
    }
}
