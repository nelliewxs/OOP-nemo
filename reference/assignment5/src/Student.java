import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Student extends Person implements Payable {
    private String group = "No group";
    private double gpa = 0;
    private int course = 1;
    private final List<ExamResult> examResults = new ArrayList<>();

    public Student(String name, int age, String email, String city,
                   String group, double gpa, int course) {
        super(name, age, email, city);
        setGroup(group);

        setGpa(gpa);
        setCourse(course);
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        if (group == null || group.trim().isEmpty()) {
            System.out.println("Error: group can not be empty.");
        } else {
            this.group = group;
        }
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        if (gpa < 0 || gpa > 4) {
            System.out.println("Error: GPA must be between 0 and 4, got " + gpa);
        } else {
            this.gpa = gpa;
        }
    }

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
        if (course < 1 || course > 6) {
            System.out.println("Error: course must be from 1 to 6, got " + course);
        } else {

            this.course = course;
        }
    }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Role: Student");
        System.out.println("Group: " + group);
        System.out.println("GPA: " + gpa);
        System.out.println("Course: " + course);
    }

    public void study(int hours) {
        System.out.println(getName() + " studied for " + hours + " hours.");
    }

    // Composition: Student creates and owns its ExamResult objects.
    public void takeExam(String subject, int score) {
        ExamResult result = new ExamResult(subject, score);
        examResults.add(result);
        String verdict = result.isPassed() ? "passed" : "failed";
        System.out.println(getName() + " " + verdict + " " + subject
                + " with " + score + " points.");
    }

    public List<ExamResult> getExamResults() {
        return Collections.unmodifiableList(examResults);
    }

    public int calculateScholarship() {
        if (gpa >= 3.5) {
            return 100 + course * 10;
        } else if (gpa >= 3.0) {
            return 50 + course * 5;
        }

        return 0;
    }

    @Override
    public int calculateMonthlyPayment() {
        return calculateScholarship();
    }
}
