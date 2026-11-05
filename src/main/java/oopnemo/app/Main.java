package oopnemo.app;

import java.util.Comparator;
import java.util.Locale;
import java.util.Scanner;
import oopnemo.model.*;
import oopnemo.service.*;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--demo")) { Demo.run(); return; }
        if (args.length != 0) {
            System.err.println("Usage: java -cp build/classes oopnemo.app.Main [--demo]");
            System.exit(2);
        }
        CampusService service = DemoData.create();
        System.out.println("OOP NEMO | University and hospital administration");
        System.out.println("Demo data loaded. In-memory console application; payments are simulated.");
        try (Scanner input = new Scanner(System.in)) {
            while (true) {
                menu();
                if (!input.hasNextLine()) { break; }
                String choice = input.nextLine().trim();
                if (choice.equals("0")) { break; }
                try { execute(choice, service, input); }
                catch (IllegalArgumentException | java.util.NoSuchElementException | IllegalStateException | ArithmeticException ex) {
                    System.out.println("Error: " + ex.getMessage());
                }
            }
        }
        System.out.println("Goodbye. Session data was not saved.");
    }

    private static void menu() {
        System.out.println("\n1 List people    2 Find by ID    3 Add person    4 Update contacts");
        System.out.println("5 Delete person  6 Search name   7 Filter role   8 Sort people");
        System.out.println("9 List hospitals 10 Submit payment 11 Process next 12 Cancel request");
        System.out.println("13 Record exam   14 Statistics   15 Run separate demo");
        System.out.println("16 Add hospital  17 Rename hospital 18 Delete hospital");
        System.out.println("19 Assign doctor 20 Unassign doctor 21 Update role details 22 List requests");
        System.out.print("0 Exit\nChoice: ");
    }

    private static void execute(String choice, CampusService service, Scanner in) {
        switch (choice) {
            case "1" -> print(service.listPeople());
            case "2" -> System.out.println(service.requirePerson(ask(in, "Person ID")));
            case "3" -> addPerson(service, in);
            case "4" -> {
                String id = ask(in, "Person ID");
                service.updatePerson(id, ask(in, "Name"), number(in, "Age"), ask(in, "Email"), ask(in, "City"));
                System.out.println("Contact details updated.");
            }
            case "5" -> { service.removePerson(ask(in, "Person ID")); System.out.println("Person and related references removed."); }
            case "6" -> print(service.searchByName(ask(in, "Name contains")));
            case "7" -> print(service.peopleWithRole(Role.valueOf(ask(in, "Role STUDENT/TEACHER/DOCTOR").toUpperCase(Locale.ROOT))));
            case "8" -> {
                String key = ask(in, "Sort ID/NAME/AGE/PAYMENT").toUpperCase(Locale.ROOT);
                Comparator<Person> comparator = switch (key) {
                    case "ID" -> Comparator.naturalOrder();
                    case "NAME" -> PersonComparators.BY_NAME;
                    case "AGE" -> PersonComparators.BY_AGE;
                    case "PAYMENT" -> PersonComparators.BY_PAYMENT_DESC;
                    default -> throw new IllegalArgumentException("Unknown sort key.");
                };
                print(service.sortedPeople(comparator));
            }
            case "9" -> service.listHospitals().forEach(hospital -> {
                System.out.println(hospital);
                hospital.getDoctors().stream().sorted().forEach(d -> System.out.println("  " + d));
            });
            case "10" -> System.out.println(service.submitRequest(ask(in, "Request ID"), ask(in, "Applicant person ID"),
                    Priority.valueOf(ask(in, "Priority HIGH/MEDIUM/LOW").toUpperCase(Locale.ROOT))));
            case "11" -> System.out.println(service.processNextRequest().map(r -> "Processed: " + r).orElse("Queue is empty."));
            case "12" -> { service.cancelRequest(ask(in, "Request ID")); System.out.println("Request cancelled."); }
            case "13" -> System.out.println(service.recordExam(ask(in, "Student ID"), ask(in, "Subject"), number(in, "Score 0-100")));
            case "14" -> System.out.println(service.summary());
            case "15" -> Demo.run();
            case "16" -> { service.addHospital(new Hospital(ask(in, "Hospital ID"), ask(in, "Name"))); System.out.println("Hospital added."); }
            case "17" -> { service.updateHospital(ask(in, "Hospital ID"), ask(in, "New name")); System.out.println("Hospital updated."); }
            case "18" -> { service.removeHospital(ask(in, "Hospital ID")); System.out.println("Hospital removed; doctors retained."); }
            case "19" -> System.out.println(service.assignDoctor(ask(in, "Hospital ID"), ask(in, "Doctor ID")) ? "Doctor assigned." : "Doctor already assigned.");
            case "20" -> System.out.println(service.unassignDoctor(ask(in, "Hospital ID"), ask(in, "Doctor ID")) ? "Doctor unassigned." : "Doctor was not assigned.");
            case "21" -> updateRoleDetails(service, in);
            case "22" -> { print(service.listRequests()); System.out.println("Pending processing order:"); print(service.pendingRequests()); }
            default -> System.out.println("Unknown menu option.");
        }
    }

    private static void addPerson(CampusService service, Scanner in) {
        Role role = Role.valueOf(ask(in, "Role STUDENT/TEACHER/DOCTOR").toUpperCase(Locale.ROOT));
        String id = ask(in, "Person ID");
        String name = ask(in, "Name");
        int age = number(in, "Age");
        String email = ask(in, "Email");
        String city = ask(in, "City");
        Person person = switch (role) {
            case STUDENT -> new Student(id, name, age, email, city, ask(in, "Group"),
                    Double.parseDouble(ask(in, "GPA 0-4")), number(in, "Course 1-6"));
            case TEACHER -> new Teacher(id, name, age, email, city, ask(in, "Subject"),
                    number(in, "Experience years 0-60"), number(in, "Salary in whole USD"));
            case DOCTOR -> new Doctor(id, name, age, email, city, ask(in, "Specialization"), number(in, "Patients count"));
        };
        service.addPerson(person);
        System.out.println("Person added: " + person.getId());
    }

    private static void updateRoleDetails(CampusService service, Scanner in) {
        Person person = service.requirePerson(ask(in, "Person ID"));
        // One selected attribute is changed per operation, so failed validation leaves it unchanged.
        if (person instanceof Student student) {
            switch (ask(in, "Field GROUP/GPA/COURSE").toUpperCase(Locale.ROOT)) {
                case "GROUP" -> student.setGroup(ask(in, "Group"));
                case "GPA" -> student.setGpa(Double.parseDouble(ask(in, "GPA")));
                case "COURSE" -> student.setCourse(number(in, "Course"));
                default -> throw new IllegalArgumentException("Unknown student field.");
            }
        } else if (person instanceof Teacher teacher) {
            switch (ask(in, "Field SUBJECT/EXPERIENCE/SALARY").toUpperCase(Locale.ROOT)) {
                case "SUBJECT" -> teacher.setSubject(ask(in, "Subject"));
                case "EXPERIENCE" -> teacher.setExperienceYears(number(in, "Experience"));
                case "SALARY" -> teacher.setSalary(number(in, "Salary"));
                default -> throw new IllegalArgumentException("Unknown teacher field.");
            }
        } else if (person instanceof Doctor doctor) {
            switch (ask(in, "Field SPECIALIZATION/PATIENTS/EXAMINE").toUpperCase(Locale.ROOT)) {
                case "SPECIALIZATION" -> doctor.setSpecialization(ask(in, "Specialization"));
                case "PATIENTS" -> doctor.setPatientsCount(number(in, "Patients"));
                case "EXAMINE" -> System.out.println(doctor.examinePatient(ask(in, "Patient name")));
                default -> throw new IllegalArgumentException("Unknown doctor field.");
            }
        }
        System.out.println("Role details updated.");
    }

    private static String ask(Scanner in, String prompt) {
        System.out.print(prompt + ": ");
        if (!in.hasNextLine()) { throw new IllegalStateException("Input ended."); }
        return in.nextLine().trim();
    }
    private static int number(Scanner in, String prompt) { return Integer.parseInt(ask(in, prompt)); }
    private static void print(Iterable<?> items) {
        boolean empty = true;
        for (Object item : items) { System.out.println(item); empty = false; }
        if (empty) { System.out.println("No records."); }
    }
}
