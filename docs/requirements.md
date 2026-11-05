# SIW1 requirements and continuity

## Authoritative inputs

- `SIW1.pdf`: the current assignment, including the five criteria worth five points each.
- `Assignment 5 ZNZ.pdf`: the prior implementation, its complete nine Java listings and UML diagram.
- Assignment 4 contains a different employee model and is not merged into this project.
- The previously shared real-estate chat is outside this project's scope.

The original PDFs are retained by the user outside this repository. Recovered baseline source is in `reference/assignment5/src`.

## Migration from Assignment 5

| Original element | Preserved behavior | SIW1 extension |
| --- | --- | --- |
| `Person` | Name, age, email, city; abstract person hierarchy | Immutable ID; ID-based equality and natural order; atomic contact updates |
| `Student` | Group, GPA, course; scholarship; study and exams | Validated, uniquely identified exam results; immutable list snapshots |
| `Teacher` | Subject, experience, salary; lectures and raise | Validated setters; payment arithmetic widened to `long` |
| `Doctor` | Specialization, patient count; examinations and income | Validated setters; checked patient increment; shared hospital membership |
| `Payable` | Name and monthly payment contract | Implemented in abstract `Person`, inherited and fulfilled by each subclass; return type widened to `long` |
| `ExamResult` | Subject, score, pass threshold; student ownership | Immutable business ID, score validation, equality and hashing |
| `Hospital` | Independent doctors and aggregation | Immutable ID; `LinkedHashSet<Doctor>` prevents duplicate membership |
| `PayrollService` | Calculates payment through `Payable` | Used by request processing; payment remains a simulation |
| `Main` | Demonstrates all roles and original relationships | Interactive CRUD menu plus a deterministic `--demo` mode |

The original `Person[]` registry is replaced by a primary map and indexed sets. No parallel list of all people is maintained: sorted lists are generated on demand. Existing list/set fields inside domain entities provide the required relationship storage.

The original print-only validation is replaced by exceptions caught at the console boundary. Existing calculation and activity methods return values/text instead of printing inside entities, so they can be tested without parsing console output.

## Requirement coverage

| SIW1 requirement | Implementation | Verification |
| --- | --- | --- |
| List / Set entity storage | `Student.examResults` ArrayList; `Hospital.doctors` LinkedHashSet; role-index sets | Composition, aggregation, duplicate membership and snapshot tests |
| Business-key equality and hashing | Person, Hospital, ExamResult, PaymentRequest | HashSet / TreeSet identity tests and mutation-stable IDs |
| Primary-key Map | HashMap registries in CampusService | Lookup, duplicate rejection and missing-key tests |
| Grouping Map | EnumMap of Role to Set of Person | Role counts before and after CRUD |
| Comparable | Person compares immutable String IDs lexicographically | Natural sort and equality consistency tests |
| Three comparators | Name ascending, age ascending, payment descending; ID tie breakers | Sorting and tie tests |
| Queue processing | PriorityQueue of immutable PaymentRequest; offer / peek / poll | Mixed priorities, FIFO ties, cancellation and empty queue |
| Existing 1:N, N:M, composition, aggregation | Student results; hospital doctor sets; same doctor in multiple hospitals | Shared-reference and lifecycle tests |
| Role workflow | Student/teacher/doctor submits; payroll operator processes via service | Interactive subprocess test and demo |
| Consistent CRUD | Person/contact CRUD; hospital CRUD; cascading request and membership cleanup | Atomic validation, cascade and 100-person regression |
| Search, filter, analytics | ID/name/role/city queries; sort views; payment totals and average GPA | Reports and console tests |
| Edge cases | Invalid input, duplicate IDs, NaN GPA, missing entities, empty queues and EOF | Validation and interactive tests |

## Collection choices and complexity

- HashMap primary lookup has expected O(1) cost, assuming suitable key dispersion. It is not a worst-case guarantee.
- EnumMap stores the fixed role categories; indexed role sets avoid scanning every person for membership selection.
- ArrayList provides compact, ordered storage for a student's exam history; append has amortized O(1) cost.
- LinkedHashSet prevents duplicate hospital membership and preserves insertion order internally. Public snapshots do not promise set iteration order; the UI explicitly sorts.
- PriorityQueue.offer and poll cost O(log q); peek costs O(1). Equal-priority requests use a monotonic sequence to enforce FIFO ties.
- A queue snapshot is explicitly sorted in O(q log q). Iterating PriorityQueue directly does not yield processing order.
- Submission also checks all registered requests for an existing pending request for the person, so the complete submit operation is O(r), not just O(log q).
- Deletion scans hospitals and requests to remove related references. Consistency is deliberately preferred to an unnecessary web of secondary indexes.
- The three custom comparators are for sorted list views. Only the ID-based natural ordering is used for identity-sensitive sorted sets; mutable display fields must not be used as permanent TreeSet keys.

## Scope and invariants

This is a single-user, in-memory educational console program. "Synchronized CRUD" means that related collections are updated together; no multithreading guarantee is claimed. There is no database, authentication server or transfer of real funds.

Use CampusService to register/remove people and hospitals, change hospital membership, and manage requests. Role, primary IDs, and queue ordering keys cannot change. Contact and role-detail setters only change attributes that are not registry keys. Returned collection structures cannot be edited, but person objects are shared canonical instances.

Amounts are calculated when a request is processed, using the current person details. Only one request per person may be pending. Processed/cancelled requests are retained for the current session; person deletion removes that person's request history as well as pending items. This is a bounded classroom model, not a monthly payroll ledger.

Composition means a removed student's exam results leave the managed object graph with the student. Java may retain externally referenced objects until they are unreachable; the application does not promise immediate memory destruction.

## Deliverables

- Java application and regression suite under `src/`.
- UML source (`.puml`), rendered diagrams (`.png`), and a standalone class-diagram PDF under `docs/uml/`.
- Technical report in `output/pdf/SIW1-Technical-Report.pdf`.
- Execution evidence and logs under `docs/evidence/`.
- Russian oral-defense preparation in `docs/defense-ru.md`.

## References

- Oracle Java 17: [HashMap](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/HashMap.html).
- Oracle Java 17: [PriorityQueue](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/PriorityQueue.html).
- Oracle Java 17: [Comparable](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Comparable.html).
- Oracle Java 17: [Comparator](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Comparator.html).
