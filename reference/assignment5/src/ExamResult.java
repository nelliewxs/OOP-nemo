public class ExamResult {
    private final String subject;
    private final int score;

    // Package-private: only Student creates exam results.
    ExamResult(String subject, int score) {
        this.subject = subject;
        this.score = score;
    }

    public String getSubject() {
        return subject;
    }

    public int getScore() {
        return score;
    }

    public boolean isPassed() {
        return score >= 50;
    }
}
