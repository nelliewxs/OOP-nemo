package oopnemo.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import oopnemo.model.*;

/**
 * In-memory service boundary for a single-user console application.
 * CRUD keeps indexes, relationships and queued requests consistent.
 * This consistency does not imply thread safety or persistent storage.
 */
public final class CampusService {
    private final Map<String, Person> peopleById = new HashMap<>();
    private final Map<Role, Set<Person>> peopleByRole = new EnumMap<>(Role.class);
    private final Map<String, Hospital> hospitalsById = new HashMap<>();
    private final Map<String, PaymentRequest> requestsById = new HashMap<>();
    private static final Comparator<PaymentRequest> REQUEST_ORDER = Comparator
            .comparing(PaymentRequest::getPriority)
            .thenComparingLong(PaymentRequest::getSequence);
    private final Queue<PaymentRequest> requestQueue = new PriorityQueue<>(REQUEST_ORDER);
    private final PayrollService payroll = new PayrollService();
    private long nextSequence;

    public CampusService() {
        for (Role role : Role.values()) { peopleByRole.put(role, new LinkedHashSet<>()); }
    }

    public void addPerson(Person person) {
        Objects.requireNonNull(person, "Person");
        if (peopleById.containsKey(person.getId())) {
            throw new IllegalArgumentException("Person ID already exists: " + person.getId());
        }
        peopleById.put(person.getId(), person);
        peopleByRole.get(person.getRole()).add(person);
    }

    /** HashMap provides expected O(1) primary-key lookup. */
    public Optional<Person> findPerson(String id) {
        return Optional.ofNullable(peopleById.get(Validation.text(id, "Person ID")));
    }

    public Person requirePerson(String id) {
        return findPerson(id).orElseThrow(() -> new NoSuchElementException("Person not found: " + id));
    }

    public void updatePerson(String id, String name, int age, String email, String city) {
        // Update the canonical object; every collection contains that same reference.
        requirePerson(id).updateContactDetails(name, age, email, city);
    }

    public void removePerson(String id) {
        Person person = requirePerson(id);
        requestQueue.removeIf(request -> request.getPersonId().equals(person.getId()));
        requestsById.values().removeIf(request -> request.getPersonId().equals(person.getId()));
        if (person instanceof Doctor doctor) {
            for (Hospital hospital : hospitalsById.values()) { hospital.removeDoctor(doctor); }
        }
        peopleByRole.get(person.getRole()).remove(person);
        peopleById.remove(person.getId());
        // A removed student's owned results leave the managed graph with the student.
    }

    public List<Person> listPeople() { return sortedPeople(Comparator.naturalOrder()); }

    public List<Person> sortedPeople(Comparator<Person> comparator) {
        List<Person> result = new ArrayList<>(peopleById.values());
        result.sort(Objects.requireNonNull(comparator, "Comparator"));
        return List.copyOf(result);
    }

    public List<Person> peopleWithRole(Role role) {
        List<Person> result = new ArrayList<>(peopleByRole.get(Objects.requireNonNull(role, "Role")));
        result.sort(Comparator.naturalOrder());
        return List.copyOf(result);
    }

    public List<Person> filterPeople(Predicate<Person> predicate) {
        Objects.requireNonNull(predicate, "Predicate");
        return listPeople().stream().filter(predicate).toList();
    }

    public List<Person> searchByName(String query) {
        String key = Validation.text(query, "Search query").toLowerCase(Locale.ROOT);
        return filterPeople(person -> person.getName().toLowerCase(Locale.ROOT).contains(key));
    }

    public void addHospital(Hospital hospital) {
        Objects.requireNonNull(hospital, "Hospital");
        if (hospitalsById.containsKey(hospital.getId())) {
            throw new IllegalArgumentException("Hospital ID already exists: " + hospital.getId());
        }
        // Imported memberships must reference registered canonical doctors.
        for (Doctor doctor : hospital.getDoctors()) {
            if (peopleById.get(doctor.getId()) != doctor) {
                throw new IllegalArgumentException("Register the hospital's doctors first.");
            }
        }
        hospitalsById.put(hospital.getId(), hospital);
    }

    public Hospital requireHospital(String id) {
        Hospital hospital = hospitalsById.get(Validation.text(id, "Hospital ID"));
        if (hospital == null) { throw new NoSuchElementException("Hospital not found: " + id); }
        return hospital;
    }
    public void updateHospital(String id, String name) { requireHospital(id).setName(name); }
    public void removeHospital(String id) {
        Hospital hospital = requireHospital(id);
        hospitalsById.remove(hospital.getId());
    }
    public List<Hospital> listHospitals() {
        return hospitalsById.values().stream().sorted(Comparator.comparing(Hospital::getId)).toList();
    }
    public boolean assignDoctor(String hospitalId, String doctorId) {
        Person person = requirePerson(doctorId);
        if (!(person instanceof Doctor doctor)) {
            throw new IllegalArgumentException("Only doctors can be assigned to a hospital.");
        }
        return requireHospital(hospitalId).addDoctor(doctor);
    }
    public boolean unassignDoctor(String hospitalId, String doctorId) {
        Person person = requirePerson(doctorId);
        if (!(person instanceof Doctor doctor)) { throw new IllegalArgumentException("Not a doctor."); }
        return requireHospital(hospitalId).removeDoctor(doctor);
    }
    public ExamResult recordExam(String studentId, String subject, int score) {
        Person person = requirePerson(studentId);
        if (!(person instanceof Student student)) {
            throw new IllegalArgumentException("Only students can own exam results.");
        }
        return student.takeExam(subject, score);
    }

    public PaymentRequest submitRequest(String requestId, String personId, Priority priority) {
        String checkedId = Validation.text(requestId, "Request ID");
        if (requestsById.containsKey(checkedId)) {
            throw new IllegalArgumentException("Request ID already exists: " + checkedId);
        }
        Person person = requirePerson(personId);
        // One pending request per person prevents duplicate queued payouts.
        boolean alreadyPending = requestsById.values().stream().anyMatch(request ->
                request.getPersonId().equals(person.getId()) && request.getStatus() == RequestStatus.PENDING);
        if (alreadyPending) { throw new IllegalArgumentException("Person already has a pending request."); }
        PaymentRequest request = new PaymentRequest(checkedId, person.getId(), priority, nextSequence);
        nextSequence = Math.incrementExact(nextSequence);
        requestsById.put(checkedId, request);
        requestQueue.offer(request);
        return request;
    }

    public Optional<PaymentRequest> findRequest(String id) {
        return Optional.ofNullable(requestsById.get(Validation.text(id, "Request ID")));
    }
    public List<PaymentRequest> listRequests() {
        return requestsById.values().stream().sorted(Comparator.comparingLong(PaymentRequest::getSequence)).toList();
    }
    /** PriorityQueue iteration is not ordered; sort a COPY for the display. */
    public List<PaymentRequest> pendingRequests() {
        List<PaymentRequest> result = new ArrayList<>(requestQueue);
        result.sort(REQUEST_ORDER);
        return List.copyOf(result);
    }

    public void cancelRequest(String id) {
        PaymentRequest request = findRequest(id).orElseThrow(() ->
                new NoSuchElementException("Request not found: " + id));
        PaymentRequest cancelled = request.cancelled();
        requestQueue.remove(request);
        requestsById.put(request.getId(), cancelled);
    }

    public Optional<PaymentReceipt> processNextRequest() {
        PaymentRequest next = requestQueue.peek();
        if (next == null) { return Optional.empty(); }
        Person person = requirePerson(next.getPersonId());
        long amount = payroll.pay(person); // Compute before removing, so failures keep the request queued.
        PaymentRequest processed = next.processed();
        requestQueue.poll();
        requestsById.put(next.getId(), processed);
        return Optional.of(new PaymentReceipt(next.getId(), person.getId(), person.getName(), amount));
    }

    public Map<Role, Integer> countsByRole() {
        Map<Role, Integer> result = new EnumMap<>(Role.class);
        for (Role role : Role.values()) { result.put(role, peopleByRole.get(role).size()); }
        return Map.copyOf(result);
    }
    public long totalMonthlyPayments() {
        return peopleById.values().stream().mapToLong(Person::calculateMonthlyPayment).sum();
    }
    public double averageStudentGpa() {
        return peopleByRole.get(Role.STUDENT).stream().map(Student.class::cast)
                .mapToDouble(Student::getGpa).average().orElse(0);
    }
    public String summary() {
        long processed = requestsById.values().stream()
                .filter(request -> request.getStatus() == RequestStatus.PROCESSED).count();
        return String.format(Locale.ROOT,
                "People: %d | Students: %d | Teachers: %d | Doctors: %d%n"
                + "Hospitals: %d | Pending requests: %d | Processed requests: %d%n"
                + "Monthly payments: %d USD | Average student GPA: %.2f",
                peopleById.size(), peopleByRole.get(Role.STUDENT).size(),
                peopleByRole.get(Role.TEACHER).size(), peopleByRole.get(Role.DOCTOR).size(),
                hospitalsById.size(), requestQueue.size(), processed,
                totalMonthlyPayments(), averageStudentGpa());
    }
}
