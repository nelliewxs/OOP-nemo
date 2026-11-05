public class Main {
    public static void main(String[] args) {
        // Inheritance: Student, Teacher and Doctor extend Person.
        Student student = new Student("Aizada Omurova", 19, "aizada@mail.com",
                "Osh", "CS-21", 3.8, 2);
        Teacher teacher = new Teacher("Nurlan Sadykov", 45, "nurlan@mail.com",
                "Bishkek", "Java Programming", 15, 900);
        Doctor doctor = new Doctor("Elena Petrova", 38, "elena@mail.com",
                "Karakol", "Cardiology", 120);

        System.out.println("1. Inheritance and polymorphism");
        Person[] people = {student, teacher, doctor};
        for (Person person : people) {
            person.displayInfo();

            System.out.println();
        }

        System.out.println("2. Composition: Student owns ExamResult objects");
        student.study(4);
        student.takeExam("Mathematics", 87);
        student.takeExam("Physics", 41);
        System.out.println("Exam results stored in " + student.getName() + ": "
                + student.getExamResults().size());
        System.out.println();

        System.out.println("3. Aggregation: one Doctor works in two Hospitals");
        Hospital cityHospital = new Hospital("City Hospital No. 1");
        Hospital privateClinic = new Hospital("Private Clinic");
        cityHospital.addDoctor(doctor);
        privateClinic.addDoctor(doctor);
        doctor.examinePatient("Marat Ismailov");
        System.out.println("Dr. " + doctor.getName() + " works in "
                + cityHospital.getName() + " and " + privateClinic.getName() + ".");
        System.out.println("Doctors in " + cityHospital.getName() + ": "
                + cityHospital.getDoctors().size());
        System.out.println();

        System.out.println("4. Realization and dependency: Payable");
        teacher.giveLecture("Class relationships");
        PayrollService payroll = new PayrollService();
        Payable[] payees = {student, teacher, doctor};
        int total = 0;
        for (Payable payee : payees) {
            total += payroll.pay(payee);
        }
        System.out.println("Total paid this month: " + total + " USD");
        System.out.println();

        System.out.println("5. Setters and validation");
        student.setGpa(5.5);

        student.setCourse(10);
        student.setAge(-5);
        student.setEmail("wrong-email");
        teacher.setSalary(-100);
        doctor.setPatientsCount(-3);
        doctor.setSpecialization("");
        System.out.println("Valid values:");
        student.setGpa(3.2);
        student.setCourse(3);
        student.setAge(20);
        teacher.setSalary(1000);
        System.out.println();
        student.displayInfo();
        System.out.println("Teacher salary now: " + teacher.getSalary() + " USD");
    }
}
