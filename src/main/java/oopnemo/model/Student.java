package oopnemo.model;

import java.util.ArrayList;
import java.util.List;

public final class Student extends Person {
    private String group;
    private double gpa;
    private int course;
    private final List<ExamResult> examResults = new ArrayList<>();
    private long nextExamNumber = 1;

    public Student(String id, String name, int age, String email, String city,
                   String group, double gpa, int course) {
        super(id, name, age, email, city);
        setGroup(group);
        setGpa(gpa);
        setCourse(course);
    }

    @Override public Role getRole() { return Role.STUDENT; }
    public String getGroup() { return group; }
    public double getGpa() { return gpa; }
    public int getCourse() { return course; }
    public void setGroup(String group) { this.group = Validation.text(group, "Group"); }
    public void setGpa(double gpa) {
        if (!Double.isFinite(gpa) || gpa < 0 || gpa > 4) {
            throw new IllegalArgumentException("GPA must be finite and between 0 and 4.");
        }
        this.gpa = gpa;
    }
    public void setCourse(int course) { this.course = Validation.range(course, 1, 6, "Course"); }
    public String study(int hours) {
        Validation.range(hours, 1, Integer.MAX_VALUE, "Study hours");
        return getName() + " studied for " + hours + " hours.";
    }
    public ExamResult takeExam(String subject, int score) {
        ExamResult result = new ExamResult(getId() + "-EXAM-" + nextExamNumber, subject, score);
        examResults.add(result);
        nextExamNumber++;
        return result;
    }
    /** A snapshot prevents callers from adding or removing owned results. */
    public List<ExamResult> getExamResults() { return List.copyOf(examResults); }
    public long calculateScholarship() {
        if (gpa >= 3.5) { return 100L + course * 10L; }
        if (gpa >= 3.0) { return 50L + course * 5L; }
        return 0;
    }
    @Override public long calculateMonthlyPayment() { return calculateScholarship(); }
}
