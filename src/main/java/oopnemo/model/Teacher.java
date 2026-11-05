package oopnemo.model;

public final class Teacher extends Person {
    private String subject;
    private int experienceYears;
    private int salary;

    public Teacher(String id, String name, int age, String email, String city,
                   String subject, int experienceYears, int salary) {
        super(id, name, age, email, city);
        setSubject(subject);
        setExperienceYears(experienceYears);
        setSalary(salary);
    }

    @Override public Role getRole() { return Role.TEACHER; }
    public String getSubject() { return subject; }
    public int getExperienceYears() { return experienceYears; }
    public int getSalary() { return salary; }
    public void setSubject(String subject) { this.subject = Validation.text(subject, "Subject"); }
    public void setExperienceYears(int years) {
        experienceYears = Validation.range(years, 0, 60, "Experience");
    }
    public void setSalary(int salary) {
        this.salary = Validation.range(salary, 0, Integer.MAX_VALUE, "Salary");
    }
    public String giveLecture(String topic) {
        return getName() + " is giving a lecture on " + Validation.text(topic, "Topic") + ".";
    }
    public long calculateRaise() { return (long) salary * experienceYears / 100; }
    @Override public long calculateMonthlyPayment() { return salary + calculateRaise(); }
}
