# OOP-nemo

Independent Work 1: extending the Assignment 5 university and hospital model with the **Java Collection Framework**.

The project preserves `Person`, `Student`, `Teacher`, `Doctor`, `Payable`, `ExamResult`, `Hospital`, `PayrollService` and the original payment rules. The extension adds indexed registries, role grouping, consistent CRUD, sorting, priority requests and analytics.

## Run on Windows

Install **JDK 17 or newer**, open PowerShell in this folder, then run:

```powershell
.\scripts\run.ps1        # Interactive menu, with demo data
.\scripts\run.ps1 -Demo  # Complete deterministic demonstration
.\scripts\test.ps1       # Compile and run regression tests
```

There are no external Java dependencies. Build scripts target Java 17 with UTF-8, all compiler warnings enabled, and warnings treated as errors. The local verification used JDK 23; GitHub Actions is configured to test Java 17 and 23 on Windows and Linux.

On Linux/macOS:

```sh
sh scripts/run.sh
sh scripts/run.sh --demo
sh scripts/test.sh
```

The program stores data **in memory**. Exiting discards session changes. Payments are simulated.

## Open in IntelliJ IDEA

1. Open this repository folder and select a project SDK of Java 17 or newer.
2. Mark `src/main/java` as **Sources Root** and `src/test/java` as **Test Sources Root** if IntelliJ does not detect them.
3. Run `oopnemo.app.Main`; use the program argument `--demo` for the demonstration.
4. Run `oopnemo.CampusServiceTest` for the dependency-free regression suite.

## Features

- Immutable business IDs and correct `equals` / `hashCode` for domain entities.
- `HashMap` primary registries and an `EnumMap<Role, Set<Person>>` index.
- Student exam history in `ArrayList`; hospital doctor membership in `LinkedHashSet`.
- Natural ordering by ID and three custom comparators: name, age and payment.
- `PriorityQueue` request processing: HIGH, MEDIUM, LOW; FIFO within equal priority.
- Person CRUD, hospital CRUD, doctor assignments, exams and payment request lifecycle.
- Cascading removal across related indexes, hospital memberships and requests.
- ID/name/role queries, criteria filters, sorted views and aggregated statistics.
- Invalid input, duplicates, missing keys, empty queues and EOF handling.

The menu models applicants and a payroll operator; it does not implement account authentication. Mutating registries, memberships and requests should go through `CampusService`.

## Submission materials

- [Technical report with execution screenshots](output/pdf/SIW1-Technical-Report.pdf) - English, 8 pages.
- [Updated UML class diagram](docs/uml/class-diagram.pdf) - domain and service views.
- [Editable UML source](docs/uml/class-diagram.puml).
- [Requirement coverage and collection choices](docs/requirements.md).
- [Oral defense preparation in Russian](docs/defense-ru.md).
- [Actual demo output](docs/evidence/demo-output.txt) and [test results](docs/evidence/test-output.txt).
- [Original Assignment 5 source](reference/assignment5/README.md) - historical baseline, compiled separately.

The evidence JPEGs are browser screenshots of captured Java execution logs, not screenshots of IntelliJ. The full logs and viewer HTML are included for reproducibility.

## Repository structure

```text
src/main/java/oopnemo/
  model/       Domain entities, interfaces and enums
  service/     Registries, workflows, sorting and payroll
  app/         Console menu, sample data and deterministic demo
src/test/java/oopnemo/   Regression suite
reference/assignment5/ Original nine Java files from the supplied PDF
docs/uml/               UML source and rendered diagrams
docs/evidence/          Execution logs and screenshots
output/pdf/             Technical report
scripts/                Build, run, test and artifact generation helpers
```

## Regenerate the report

The Java app does not require Python. Rebuilding the documentation additionally needs `pypdf`, `reportlab` and `pypdfium2`:

1. Capture `Main --demo` and `scripts/test.ps1` output into the corresponding files under `docs/evidence/`.
2. Run `python scripts/build_report.py --prepare` to rebuild the UML and evidence HTML.
3. Serve only `docs/evidence/` locally. Move the pointer into the outer page padding, then capture the complete `main` content block of `architecture.html`, `workflow.html` and `tests.html` as matching JPEG files. Exclude the outer padding so the pointer is outside each capture.
4. Run `python scripts/build_report.py` to generate the report.

## Local credentials

The user's `.env` is excluded from Git and from submission archives. GitHub credentials are for repository administration and are not needed to run the application.
