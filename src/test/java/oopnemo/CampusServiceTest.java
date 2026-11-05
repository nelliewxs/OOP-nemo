package oopnemo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import oopnemo.app.DemoData;
import oopnemo.model.*;
import oopnemo.service.*;

/** Dependency-free regression suite. Every check throws on failure; -ea is not required. */
public final class CampusServiceTest {
    private static int checks;
    private CampusServiceTest() { }

    public static void main(String[] args) {
        run("Business identity and collection contracts", CampusServiceTest::identity);
        run("Empty collections and missing keys", CampusServiceTest::empty);
        run("CRUD and atomic validation", CampusServiceTest::crud);
        run("Composition, aggregation and many-to-many", CampusServiceTest::relationships);
        run("Priority order, FIFO ties and cancellation", CampusServiceTest::queue);
        run("Cascade removal across every managed reference", CampusServiceTest::cascade);
        run("Sorting, filters, analytics and snapshots", CampusServiceTest::reports);
        run("Validation and payment arithmetic", CampusServiceTest::validation);
        run("Repeated CRUD consistency with 100 people", CampusServiceTest::bulk);
        run("Interactive console with valid, invalid and ended input", CampusServiceTest::console);
        System.out.println("PASS: 10 test groups; " + checks + " assertions.");
    }

    private static Student student(String id) {
        return new Student(id, "Student " + id, 20, id + "@example.com", "Osh", "CS-21", 3.8, 2);
    }
    private static Doctor doctor(String id) {
        return new Doctor(id, "Doctor " + id, 40, id + "@example.com", "Bishkek", "Cardiology", 10);
    }
    private static void identity() {
        Person a = student("P1");
        Person b = student("P1");
        Person c = doctor("P1");
        check(a.equals(a) && a.equals(b) && b.equals(a) && b.equals(c) && a.equals(c), "Equality contract across person subclasses");
        check(!a.equals(null) && !a.equals("P1") && !a.equals(student("P2")), "Distinct and unrelated objects");
        equal(a.hashCode(), b.hashCode());
        equal(a.compareTo(c), 0);
        Set<Person> hash = new HashSet<>(List.of(a, b, c));
        Set<Person> tree = new TreeSet<>(List.of(a, b, c));
        equal(hash.size(), 1);
        equal(tree.size(), 1);
        int oldHash = a.hashCode();
        a.updateContactDetails("Renamed", 21, "new@example.com", "Karakol");
        equal(a.hashCode(), oldHash);
        check(hash.contains(a), "Mutable contact details do not break hash lookup");
        check(new Hospital("H1", "A").equals(new Hospital("H1", "B")), "Hospital key equality");
        PaymentRequest r = new PaymentRequest("R1", "P1", Priority.LOW, 0);
        PaymentRequest other = new PaymentRequest("R1", "P2", Priority.HIGH, 99);
        check(r.equals(other) && r.hashCode() == other.hashCode(), "Request key equality");
        equal(r.processed().getStatus(), RequestStatus.PROCESSED);
        equal(r.getStatus(), RequestStatus.PENDING);
        throwsType(IllegalStateException.class, () -> r.processed().cancelled());
        Student s1 = student("S1");
        Student s2 = student("S1");
        ExamResult e1 = s1.takeExam("Math", 80);
        ExamResult e2 = s2.takeExam("Other", 20);
        check(e1.equals(e2) && e1.hashCode() == e2.hashCode(), "Exam result uses its student-scoped key");
    }
    private static void empty() {
        CampusService s = new CampusService();
        check(s.listPeople().isEmpty(), "Empty registry");
        check(s.peopleWithRole(Role.DOCTOR).isEmpty(), "Empty role index");
        check(s.findPerson("X").isEmpty(), "Missing lookup");
        check(s.processNextRequest().isEmpty(), "Empty queue");
        equal(s.totalMonthlyPayments(), 0L);
        equal(s.averageStudentGpa(), 0.0);
        check(s.listHospitals().isEmpty() && s.listRequests().isEmpty(), "Empty other registries");
        throwsType(NoSuchElementException.class, () -> s.removePerson("X"));
        throwsType(NoSuchElementException.class, () -> s.requireHospital("X"));
        throwsType(NoSuchElementException.class, () -> s.cancelRequest("X"));
        throwsType(NoSuchElementException.class, () -> s.submitRequest("R", "X", Priority.HIGH));
        throwsType(IllegalArgumentException.class, () -> s.findPerson(" "));
        throwsType(NullPointerException.class, () -> s.addPerson(null));
    }
    private static void crud() {
        CampusService s = new CampusService();
        Student person = student("S1");
        s.addPerson(person);
        throwsType(IllegalArgumentException.class, () -> s.addPerson(doctor("S1")));
        equal(s.listPeople().size(), 1);
        equal(s.peopleWithRole(Role.STUDENT).size(), 1);
        equal(s.peopleWithRole(Role.DOCTOR).size(), 0);
        s.updatePerson(" S1 ", "Updated", 30, "updated@example.com", "Naryn");
        equal(s.peopleWithRole(Role.STUDENT).get(0).getName(), "Updated");
        check(s.requirePerson("S1") == person, "Canonical instance is retained");
        throwsType(IllegalArgumentException.class, () -> s.updatePerson("S1", "Wrong partial value", 50, "invalid", "Osh"));
        equal(person.getName(), "Updated");
        equal(person.getAge(), 30);
        s.removePerson(" S1 ");
        check(s.findPerson("S1").isEmpty() && s.peopleWithRole(Role.STUDENT).isEmpty(), "All person indexes are cleaned");
        s.addPerson(student("S1"));
        equal(s.listPeople().size(), 1);
    }
    private static void relationships() {
        CampusService s = new CampusService();
        Student student = student("S1");
        Doctor d1 = doctor("D1");
        Doctor d2 = doctor("D2");
        s.addPerson(student); s.addPerson(d1); s.addPerson(d2);
        s.addHospital(new Hospital("H1", "One"));
        s.addHospital(new Hospital("H2", "Two"));
        check(s.assignDoctor("H1", "D1"), "First membership added");
        check(!s.assignDoctor("H1", "D1"), "Duplicate membership rejected by Set");
        s.assignDoctor("H2", "D1"); s.assignDoctor("H2", "D2");
        equal(s.requireHospital("H2").getDoctors().size(), 2);
        check(s.requireHospital("H1").getDoctors().contains(d1), "Shared doctor reference");
        throwsType(IllegalArgumentException.class, () -> s.assignDoctor("H1", "S1"));
        throwsType(UnsupportedOperationException.class, () -> s.requireHospital("H1").getDoctors().clear());
        s.updateHospital("H1", "Renamed hospital");
        equal(s.requireHospital("H1").getName(), "Renamed hospital");
        s.removeHospital("H1");
        check(s.findPerson("D1").isPresent(), "Deleting aggregate retains its doctor");
        equal(s.requireHospital("H2").getDoctors().size(), 2);
        check(s.unassignDoctor("H2", "D2") && !s.unassignDoctor("H2", "D2"), "Unassignment is idempotent");
        ExamResult first = s.recordExam("S1", "Math", 87);
        ExamResult second = s.recordExam("S1", "Math", 41);
        check(!first.equals(second), "Retakes have distinct business IDs");
        check(first.isPassed() && !second.isPassed(), "Exam threshold");
        throwsType(UnsupportedOperationException.class, () -> student.getExamResults().clear());
        throwsType(IllegalArgumentException.class, () -> s.recordExam("D1", "Math", 80));
        throwsType(IllegalArgumentException.class, () -> s.recordExam("S1", "Math", 101));
        equal(student.getExamResults().size(), 2);
        Hospital unmanaged = new Hospital("BAD", "Bad membership");
        unmanaged.addDoctor(doctor("D1"));
        throwsType(IllegalArgumentException.class, () -> s.addHospital(unmanaged));
        equal(s.listHospitals().size(), 1);
        throwsType(IllegalArgumentException.class, () -> s.addHospital(new Hospital("H2", "Duplicate")));
    }
    private static void queue() {
        CampusService s = new CampusService();
        for (String id : List.of("P1", "P2", "P3", "P4")) { s.addPerson(student(id)); }
        s.submitRequest("LOW", "P1", Priority.LOW);
        s.submitRequest("Z-HIGH", "P2", Priority.HIGH);
        s.submitRequest("MEDIUM", "P3", Priority.MEDIUM);
        s.submitRequest("A-HIGH", "P4", Priority.HIGH);
        equal(s.pendingRequests().stream().map(PaymentRequest::getId).toList(), List.of("Z-HIGH", "A-HIGH", "MEDIUM", "LOW"));
        throwsType(IllegalArgumentException.class, () -> s.submitRequest("OTHER", "P1", Priority.HIGH));
        throwsType(IllegalArgumentException.class, () -> s.submitRequest("LOW", "P2", Priority.HIGH));
        equal(s.pendingRequests().size(), 4);
        equal(s.processNextRequest().orElseThrow().requestId(), "Z-HIGH");
        equal(s.findRequest("Z-HIGH").orElseThrow().getStatus(), RequestStatus.PROCESSED);
        throwsType(IllegalStateException.class, () -> s.cancelRequest("Z-HIGH"));
        equal(s.processNextRequest().orElseThrow().requestId(), "A-HIGH");
        s.cancelRequest("MEDIUM");
        equal(s.findRequest("MEDIUM").orElseThrow().getStatus(), RequestStatus.CANCELLED);
        throwsType(IllegalStateException.class, () -> s.cancelRequest("MEDIUM"));
        equal(s.processNextRequest().orElseThrow().requestId(), "LOW");
        check(s.processNextRequest().isEmpty(), "Queue exhausted without errors");
        equal(s.listRequests().size(), 4);
        s.submitRequest("NEXT", "P3", Priority.LOW);
        equal(s.pendingRequests().size(), 1);
    }
    private static void cascade() {
        CampusService s = DemoData.create();
        s.submitRequest("R1", "D001", Priority.HIGH);
        s.processNextRequest();
        s.submitRequest("R2", "D001", Priority.LOW);
        s.submitRequest("KEEP", "S001", Priority.MEDIUM);
        s.removePerson("D001");
        check(s.findPerson("D001").isEmpty(), "Removed from primary index");
        check(s.peopleWithRole(Role.DOCTOR).stream().noneMatch(p -> p.getId().equals("D001")), "Removed from role index");
        check(s.listHospitals().stream().allMatch(h -> h.getDoctors().stream().noneMatch(d -> d.getId().equals("D001"))), "Removed from every hospital");
        check(s.listRequests().stream().noneMatch(r -> r.getPersonId().equals("D001")), "Removed from processed and pending request index");
        equal(s.pendingRequests().size(), 1);
        equal(s.processNextRequest().orElseThrow().requestId(), "KEEP");
        s.removePerson("S001");
        check(s.peopleWithRole(Role.STUDENT).stream().noneMatch(p -> p.getId().equals("S001")), "Composed result owner leaves registry");
        equal(s.listRequests().size(), 0);
    }
    private static void reports() {
        CampusService s = DemoData.create();
        equal(s.listPeople().stream().map(Person::getId).toList(), List.of("D001", "D002", "S001", "S002", "T001"));
        equal(s.sortedPeople(PersonComparators.BY_NAME).get(0).getId(), "S001");
        equal(s.sortedPeople(PersonComparators.BY_AGE).get(0).getId(), "S001");
        equal(s.sortedPeople(PersonComparators.BY_PAYMENT_DESC).get(0).getId(), "D001");
        equal(s.countsByRole().get(Role.STUDENT), 2);
        equal(s.totalMonthlyPayments(), 6220L);
        check(Math.abs(s.averageStudentGpa() - 3.5) < 0.000001, "Average GPA");
        equal(s.searchByName("ELENA").size(), 1);
        equal(s.filterPeople(p -> p.getCity().equals("Osh")).size(), 2);
        throwsType(UnsupportedOperationException.class, () -> s.listPeople().clear());
        throwsType(UnsupportedOperationException.class, () -> s.countsByRole().clear());
        List<Person> snapshot = s.listPeople();
        s.addPerson(student("NEW"));
        equal(snapshot.size(), 5);
        equal(s.listPeople().size(), 6);
        Student first = student("B"); Student second = student("A");
        first.setName("Same"); second.setName("same");
        check(PersonComparators.BY_NAME.compare(first, second) > 0, "Comparator ties resolved by ID");
        equal(s.summary().contains("People: 6"), true);
    }
    private static void validation() {
        Student student = student("S");
        throwsType(IllegalArgumentException.class, () -> student.setGpa(Double.NaN));
        throwsType(IllegalArgumentException.class, () -> student.setGpa(Double.POSITIVE_INFINITY));
        throwsType(IllegalArgumentException.class, () -> student.setCourse(0));
        throwsType(IllegalArgumentException.class, () -> student.setAge(121));
        throwsType(IllegalArgumentException.class, () -> student.setName("  "));
        throwsType(IllegalArgumentException.class, () -> student.setEmail("a@@b"));
        throwsType(IllegalArgumentException.class, () -> student.setEmail("@b"));
        equal(student.getGpa(), 3.8);
        equal(student.getAge(), 20);
        Teacher teacher = new Teacher("T", "Teacher", 50, "t@example.com", "Osh", "Java", 60, Integer.MAX_VALUE);
        check(teacher.calculateMonthlyPayment() > Integer.MAX_VALUE, "Payment arithmetic uses long");
        Doctor doctor = doctor("D");
        throwsType(IllegalArgumentException.class, () -> doctor.setPatientsCount(-1));
        throwsType(IllegalArgumentException.class, () -> doctor.examinePatient(""));
        equal(doctor.getPatientsCount(), 10);
        doctor.setPatientsCount(Integer.MAX_VALUE);
        throwsType(ArithmeticException.class, () -> doctor.examinePatient("Test"));
        equal(doctor.getPatientsCount(), Integer.MAX_VALUE);
        CampusService s = new CampusService(); s.addPerson(teacher);
        s.submitRequest("R", "T", Priority.HIGH);
        teacher.setSalary(1000);
        equal(s.processNextRequest().orElseThrow().amount(), 1600L);
        student.setGpa(3.0); equal(student.calculateScholarship(), 60L);
        student.setGpa(2.9); equal(student.calculateScholarship(), 0L);
    }
    private static void bulk() {
        CampusService s = new CampusService();
        for (int i = 0; i < 100; i++) {
            s.addPerson(student("P" + i));
            s.submitRequest("R" + i, "P" + i, Priority.values()[i % 3]);
        }
        for (int i = 0; i < 100; i += 2) { s.removePerson("P" + i); }
        equal(s.listPeople().size(), 50);
        equal(s.peopleWithRole(Role.STUDENT).size(), 50);
        equal(s.pendingRequests().size(), 50);
        equal(s.listRequests().size(), 50);
        Set<String> paid = new HashSet<>();
        Optional<PaymentReceipt> receipt;
        while ((receipt = s.processNextRequest()).isPresent()) {
            check(paid.add(receipt.orElseThrow().personId()), "Each survivor processed exactly once");
        }
        equal(paid.size(), 50);
        check(paid.stream().allMatch(id -> Integer.parseInt(id.substring(1)) % 2 == 1), "Deleted applicants never processed");
    }
    private static void console() {
        String output = runConsole("2\nS001\n10\nR-CLI\nS001\nHIGH\n11\n14\n2\nMISSING\n21\nS001\nGPA\nNaN\n14\n0\n");
        check(output.contains("Aizada Omurova"), "Lookup through UI");
        check(output.contains("Processed: R-CLI"), "Submit and process through UI");
        check(output.contains("Processed requests: 1"), "UI report reflects processing");
        check(output.contains("Error: Person not found: MISSING"), "Missing ID handled through UI");
        check(output.contains("GPA must be finite"), "Invalid UI field handled");
        check(output.contains("Goodbye"), "Clean exit");
        String created = runConsole("3\nSTUDENT\nS999\nConsole Student\n19\nconsole@example.com\nOsh\nCS-25\n3.5\n1\n2\nS999\n5\nS999\n2\nS999\n0\n");
        check(created.contains("Person added: S999") && created.contains("Console Student"), "Create through UI");
        check(created.contains("Error: Person not found: S999"), "Delete through UI");
        check(runConsole("4\nS001\n").contains("Input ended"), "EOF during operation exits cleanly");
        check(runConsole("bad\n0\n").contains("Unknown menu option"), "Unknown option handled");
        check(runConsole("21\nD001\nPATIENTS\n2147483647\n21\nD001\nEXAMINE\nDemo\n14\n0\n")
                .contains("Error: integer overflow"), "Checked arithmetic failure handled through UI");
    }
    private static String runConsole(String input) {
        try {
            String executable = Path.of(System.getProperty("java.home"), "bin", "java").toString();
            Process process = new ProcessBuilder(executable, "-cp", System.getProperty("java.class.path"),
                    "oopnemo.app.Main").redirectErrorStream(true).start();
            try (var stream = process.getOutputStream()) { stream.write(input.getBytes(StandardCharsets.UTF_8)); }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            equal(process.waitFor(), 0);
            return output;
        } catch (IOException ex) { throw new AssertionError("Cannot run console", ex); }
        catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new AssertionError(ex); }
    }
    private static void run(String name, Runnable test) {
        test.run();
        System.out.println("PASS " + name);
    }
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) { throw new AssertionError(message); }
    }
    private static void equal(Object actual, Object expected) {
        check(Objects.equals(actual, expected), "Expected " + expected + " but got " + actual);
    }
    private static void throwsType(Class<? extends Throwable> type, Runnable action) {
        try { action.run(); }
        catch (Throwable ex) {
            check(type.isInstance(ex), "Expected " + type.getSimpleName() + " but got " + ex);
            return;
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
}
