package oopnemo.app;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import oopnemo.model.*;
import oopnemo.service.*;

/** Deterministic, executable evidence of the assignment requirements. */
public final class Demo {
    private Demo() { }
    public static void run() {
        CampusService service = DemoData.create();
        System.out.println("OOP NEMO | SIW1 | University and hospital administration");
        section("1. Original classes, relationships and payment rules");
        service.listPeople().forEach(person -> System.out.println(person.displayInfo()));
        Student student = (Student) service.requirePerson("S001");
        System.out.println("Student-owned results: " + student.getExamResults());
        System.out.println(((Doctor) service.requirePerson("D001")).examinePatient("Demo patient"));
        for (Hospital hospital : service.listHospitals()) {
            System.out.println(hospital + " -> " + hospital.getDoctors().stream()
                    .map(Doctor::getId).sorted().toList());
        }

        section("2. Business identity, primary lookup and role index");
        Person sameId = new Student("S001", "Other name", 20, "other@example.com", "Osh", "CS-21", 3.0, 1);
        Set<Person> unique = new HashSet<>(List.of(student, sameId));
        System.out.println("Same ID equals: " + student.equals(sameId));
        System.out.println("HashSet size for two equal IDs: " + unique.size());
        System.out.println("Lookup D001: " + service.requirePerson("D001").getName());
        System.out.println("Doctors from role index: " + ids(service.peopleWithRole(Role.DOCTOR)));
        System.out.println("City filter Osh: " + ids(service.filterPeople(p -> p.getCity().equals("Osh"))));

        section("3. Natural ordering and three custom comparators");
        System.out.println("Natural ID order: " + ids(service.listPeople()));
        System.out.println("Name ascending: " + ids(service.sortedPeople(PersonComparators.BY_NAME)));
        System.out.println("Age ascending: " + ids(service.sortedPeople(PersonComparators.BY_AGE)));
        System.out.println("Payment descending: " + ids(service.sortedPeople(PersonComparators.BY_PAYMENT_DESC)));

        section("4. Applicant and payroll operator priority workflow");
        service.submitRequest("R-LOW", "S001", Priority.LOW);
        service.submitRequest("R-HIGH-1", "D001", Priority.HIGH);
        service.submitRequest("R-MEDIUM", "T001", Priority.MEDIUM);
        service.submitRequest("R-HIGH-2", "D002", Priority.HIGH);
        System.out.println("Submission order: LOW, HIGH, MEDIUM, HIGH");
        System.out.println("Processing order: priority first; FIFO within equal priority");
        service.pendingRequests().forEach(System.out::println);
        while (!service.pendingRequests().isEmpty()) {
            System.out.println("Processed: " + service.processNextRequest().orElseThrow());
        }
        System.out.println("Empty queue returns Optional.empty: " + service.processNextRequest().isEmpty());

        section("5. CRUD consistency and edge cases");
        service.updatePerson("D001", "Elena Petrova Updated", 39, "elena.updated@example.com", "Bishkek");
        System.out.println("Updated name in hospital: " + service.requireHospital("H001").getDoctors()
                .stream().filter(d -> d.getId().equals("D001")).findFirst().orElseThrow().getName());
        service.submitRequest("R-DELETE", "D001", Priority.HIGH);
        service.removePerson("D001");
        System.out.println("Deleted D001: primary lookup empty = " + service.findPerson("D001").isEmpty());
        System.out.println("Role index after deletion: " + ids(service.peopleWithRole(Role.DOCTOR)));
        System.out.println("H001 memberships after deletion: " + service.requireHospital("H001").getDoctors().size());
        System.out.println("Pending requests after deletion: " + service.pendingRequests().size());
        System.out.println("Deleted person's request removed: " + service.findRequest("R-DELETE").isEmpty());
        try { service.addPerson(student); }
        catch (IllegalArgumentException ex) { System.out.println("Duplicate rejected: " + ex.getMessage()); }
        try { student.setGpa(Double.NaN); }
        catch (IllegalArgumentException ex) { System.out.println("Invalid GPA rejected: " + ex.getMessage()); }
        try { service.requirePerson("MISSING"); }
        catch (java.util.NoSuchElementException ex) { System.out.println("Missing ID handled: " + ex.getMessage()); }

        section("6. Aggregated report after CRUD");
        System.out.println(service.summary());
        System.out.println("Demo finished successfully. Payments are simulated; data is in memory.");
    }
    private static List<String> ids(List<Person> people) { return people.stream().map(Person::getId).toList(); }
    private static void section(String title) { System.out.println("\n=== " + title + " ==="); }
}
