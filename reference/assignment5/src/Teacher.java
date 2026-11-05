public class Teacher extends Person implements Payable {
    private String subject = "Not assigned";
    private int experienceYears = 0;
    private int salary = 300;

    public Teacher(String name, int age, String email, String city,
                   String subject, int experienceYears, int salary) {
        super(name, age, email, city);
        setSubject(subject);
        setExperienceYears(experienceYears);
        setSalary(salary);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            System.out.println("Error: subject can not be empty.");
        } else {
            this.subject = subject;
        }
    }

    public int getExperienceYears() {
        return experienceYears;

    }

    public void setExperienceYears(int experienceYears) {
        if (experienceYears < 0 || experienceYears > 60) {
            System.out.println("Error: experience " + experienceYears
                    + " is not valid.");
        } else {
            this.experienceYears = experienceYears;
        }
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        if (salary < 0) {
            System.out.println("Error: salary can not be negative.");
        } else {
            this.salary = salary;
        }
    }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Role: Teacher");
        System.out.println("Subject: " + subject);
        System.out.println("Experience: " + experienceYears + " years");
        System.out.println("Salary: " + salary + " USD");
    }

    public void giveLecture(String topic) {
        System.out.println(getName() + " is giving a lecture on \""
                + topic + "\" (" + subject + ").");
    }

    // 1 percent of the salary for every year of experience.
    public int calculateRaise() {
        return salary * experienceYears / 100;
    }

    @Override
    public int calculateMonthlyPayment() {
        return salary + calculateRaise();
    }
}
