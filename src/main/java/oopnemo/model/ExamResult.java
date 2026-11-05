package oopnemo.model;

/** Immutable value owned by one Student; created only by Student's API. */
public final class ExamResult {
    private final String id;
    private final String subject;
    private final int score;

    ExamResult(String id, String subject, int score) {
        this.id = Validation.text(id, "Exam result ID");
        this.subject = Validation.text(subject, "Subject");
        this.score = Validation.range(score, 0, 100, "Score");
    }

    public String getId() { return id; }
    public String getSubject() { return subject; }
    public int getScore() { return score; }
    public boolean isPassed() { return score >= 50; }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof ExamResult result && id.equals(result.id);
    }
    @Override public int hashCode() { return id.hashCode(); }
    @Override public String toString() {
        return subject + ": " + score + (isPassed() ? " PASS" : " FAIL");
    }
}
