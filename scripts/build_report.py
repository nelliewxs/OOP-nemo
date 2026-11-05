"""Build UML, execution viewer and technical report from checked-in evidence.

Requires reportlab and pypdfium2. Run --prepare before capturing the HTML views.
The application itself and its Java tests need no Python packages.
"""
import argparse
import html
import re
from pathlib import Path
from reportlab.pdfgen import canvas
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.styles import ParagraphStyle
from reportlab.platypus import Paragraph, Table, TableStyle
import pypdfium2 as pdfium

ROOT = Path(__file__).resolve().parents[1]
UML = ROOT / "docs/uml"
EVIDENCE = ROOT / "docs/evidence"
OUTPUT = ROOT / "output/pdf"
INK = colors.HexColor("#17202d")
BLUE = colors.HexColor("#e9f0f8")
GRAY = colors.HexColor("#d9d9d9")


class Diagram:
    def __init__(self, pdf):
        self.c = pdf
        self.height = 1100

    def text(self, x, y, text, size=22, bold=False):
        self.c.setFillColor(INK)
        self.c.setFont("Helvetica-Bold" if bold else "Helvetica", size)
        self.c.drawString(x, self.height - y, text)

    def box(self, x, y, w, title, fields, methods, stereotype=None):
        head = 78 if stereotype else 52
        h = head + 28 * len(fields) + 16 + 28 * len(methods) + 14
        self.c.setStrokeColor(INK)
        self.c.setLineWidth(1.6)
        self.c.setFillColor(colors.white)
        self.c.rect(x, self.height - y - h, w, h, fill=1, stroke=1)
        self.c.setFillColor(BLUE)
        self.c.rect(x, self.height - y - head, w, head, fill=1, stroke=1)
        if stereotype:
            self.text(x + 14, y + 26, stereotype, 19)
        self.text(x + 14, y + head - 17, title, 25, True)
        offset = y + head + 28
        for line in fields:
            self.text(x + 14, offset, line, 20)
            offset += 28
        self.line([(x, offset - 12), (x + w, offset - 12)])
        offset += 8
        for line in methods:
            self.text(x + 14, offset, line, 20)
            offset += 28
        return h

    def line(self, points, dashed=False):
        c = self.c
        c.setStrokeColor(INK)
        c.setLineWidth(1.8)
        c.setDash(8, 6) if dashed else c.setDash()
        p = c.beginPath()
        p.moveTo(points[0][0], self.height - points[0][1])
        for x, y in points[1:]:
            p.lineTo(x, self.height - y)
        c.drawPath(p)
        c.setDash()

    def triangle_up(self, x, y):
        c = self.c
        p = c.beginPath()
        p.moveTo(x, self.height - y)
        p.lineTo(x - 12, self.height - y - 22)
        p.lineTo(x + 12, self.height - y - 22)
        p.close()
        c.setFillColor(colors.white)
        c.drawPath(p, fill=1, stroke=1)

    def diamond(self, x, y, filled=False):
        p = self.c.beginPath()
        p.moveTo(x, self.height - y)
        p.lineTo(x - 10, self.height - y - 17)
        p.lineTo(x, self.height - y - 34)
        p.lineTo(x + 10, self.height - y - 17)
        p.close()
        self.c.setFillColor(INK if filled else colors.white)
        self.c.drawPath(p, fill=1, stroke=1)


def diagrams():
    UML.mkdir(parents=True, exist_ok=True)
    c = canvas.Canvas(str(UML / "class-diagram.pdf"), pagesize=(1600, 1100))
    c.setTitle("OOP nemo SIW1 UML class diagram")
    d = Diagram(c)
    d.text(50, 40, "OOP nemo  |  Domain model", 29, True)
    d.text(50, 72, "Selected members shown. Getters, setters and enum classes omitted for readability.", 20)
    d.box(600, 100, 400, "Payable", [], ["+ getName(): String", "+ calculateMonthlyPayment(): long"], "<<interface>>")
    d.box(550, 310, 500, "Person", ["- id: String {readOnly}", "- name, email, city: String; age: int"],
          ["+ getRole(): Role", "+ compareTo(Person): int", "+ equals(Object): boolean; hashCode(): int"], "<<abstract>> implements Comparable<Person>")
    d.line([(800, 310), (800, 286)], True)
    d.triangle_up(800, 264)
    # Generalization spine from the three concrete classes to Person.
    d.line([(270, 650), (270, 610), (800, 610), (800, 580)])
    d.triangle_up(800, 558)
    d.line([(800, 650), (800, 610)])
    d.line([(1330, 650), (1330, 610), (800, 610)])
    h_student = d.box(60, 650, 420, "Student", ["- group: String; gpa: double; course: int", "- examResults: List<ExamResult>"],
                      ["+ takeExam(String, int): ExamResult", "+ calculateScholarship(): long"])
    d.box(590, 650, 420, "Teacher", ["- subject: String", "- experienceYears, salary: int"], ["+ calculateRaise(): long", "+ giveLecture(String): String"])
    h_doctor = d.box(1120, 650, 420, "Doctor", ["- specialization: String", "- patientsCount: int"], ["+ calculateMonthlyIncome(): long", "+ examinePatient(String): String"])
    d.box(60, 920, 420, "ExamResult", ["- id, subject: String {readOnly}", "- score: int {readOnly}"], ["+ isPassed(): boolean"])
    d.box(1120, 920, 420, "Hospital", ["- id: String; name: String", "- doctors: Set<Doctor>"], ["+ addDoctor(Doctor): boolean"])
    d.line([(270, 650 + h_student), (270, 920)])
    d.diamond(270, 650 + h_student, True)
    d.text(290, 872, "1", 18)
    d.text(290, 911, "0..*", 18)
    d.line([(1330, 650 + h_doctor), (1330, 920)])
    d.diamond(1330, 886)
    d.text(1350, 870, "0..*", 18)
    d.text(1350, 912, "0..*", 18)
    d.box(590, 920, 420, "PaymentRequest", ["- id, personId: String", "- priority; sequence; status {readOnly}"], ["+ processed() / cancelled(): new request"])
    # ID-backed association; routed outside the middle class column.
    d.line([(550, 425), (515, 425), (515, 985), (590, 985)])
    d.text(520, 410, "1", 18)
    d.text(532, 970, "0..*", 18)
    d.text(525, 895, "by ID", 17)
    c.showPage()
    d.text(50, 45, "OOP nemo  |  Service layer and collection ownership", 29, True)
    d.text(50, 78, "Main and Demo call CampusService. PersonComparators defines the three selectable sort views.", 20)
    d.box(60, 150, 800, "CampusService", [
        "- peopleById: Map<String, Person> = HashMap",
        "- peopleByRole: Map<Role, Set<Person>> = EnumMap",
        "- hospitalsById: Map<String, Hospital> = HashMap",
        "- requestsById: Map<String, PaymentRequest> = HashMap",
        "- requestQueue: Queue<PaymentRequest> = PriorityQueue",
        "- payroll: PayrollService"], [
        "+ addPerson(Person) / updatePerson(...) / removePerson(id)",
        "+ findPerson(id): Optional<Person>",
        "+ peopleWithRole(Role): List<Person>",
        "+ sortedPeople(Comparator) / filterPeople(Predicate)",
        "+ hospital CRUD; assignDoctor(...) / unassignDoctor(...)",
        "+ recordExam(...): ExamResult",
        "+ submitRequest(...): PaymentRequest",
        "+ cancelRequest(id): void",
        "+ processNextRequest(): Optional<PaymentReceipt>",
        "+ summary(): String"])
    d.box(1000, 150, 540, "PayrollService", [], ["+ pay(payee: Payable): long"])
    d.box(1000, 420, 540, "Payable", [], ["+ calculateMonthlyPayment(): long"], "<<interface>>")
    d.box(1000, 650, 540, "Person", ["- immutable ID; mutable contact details"], ["+ calculateMonthlyPayment(): long"], "<<abstract>>")
    d.box(1000, 910, 540, "PersonComparators", [], ["{static} BY_NAME", "{static} BY_AGE", "{static} BY_PAYMENT_DESC"])
    d.line([(860, 220), (1000, 220)])
    d.text(875, 200, "owns", 18)
    d.line([(1270, 260), (1270, 420)], True)
    d.line([(1258, 402), (1270, 420), (1282, 402)])
    d.text(1290, 350, "uses parameter", 18)
    d.line([(1270, 650), (1270, 578)], True)
    d.triangle_up(1270, 556)
    d.line([(860, 630), (930, 630), (930, 715), (1000, 715)])
    d.text(940, 697, "0..*", 18)
    d.line([(1270, 910), (1270, 814)], True)
    d.line([(1258, 832), (1270, 814), (1282, 832)])
    d.text(1290, 890, "compares", 18)
    d.text(70, 890, "Collection implementations", 25, True)
    d.text(70, 930, "Student owns ArrayList<ExamResult>.", 20)
    d.text(70, 965, "Hospital aggregates LinkedHashSet<Doctor>.", 20)
    d.text(70, 1000, "Requests: HIGH, MEDIUM, LOW; FIFO within one priority.", 20)
    d.text(70, 1035, "CRUD cleans indexes, memberships and related requests.", 20)
    d.text(70, 1070, "Single-user, in-memory application. Payments are simulated.", 20)
    c.save()
    pdf = pdfium.PdfDocument(str(UML / "class-diagram.pdf"))
    for i, name in enumerate(["domain-model.png", "service-layer.png"]):
        pdf[i].render(scale=1.5).to_pil().save(UML / name)


def viewers():
    text = (EVIDENCE / "demo-output.txt").read_text(encoding="utf-8")
    a, b = text.split("=== 4. Applicant", 1)
    sections = {
        "architecture": ("Domain model, identity and sorting", a.strip()),
        "workflow": ("Priority workflow, CRUD and analytics", "=== 4. Applicant" + b),
        "tests": ("Regression suite", (EVIDENCE / "test-output.txt").read_text(encoding="utf-8")),
    }
    for name, (title, output) in sections.items():
        (EVIDENCE / f"{name}.html").write_text(f'''<!doctype html>
<html lang="en"><meta charset="utf-8"><title>OOP nemo execution evidence</title>
<style>*{{box-sizing:border-box}}body{{margin:0;background:#f2f4f7;padding:28px;font-family:Arial,sans-serif;color:#111827}}
main{{max-width:1540px;margin:auto}}h1{{font-size:27px;margin:0 0 8px}}p{{font-size:16px;color:#475569;margin:0 0 18px}}
pre{{margin:0;background:#122032;color:#eff5fc;padding:24px;border-radius:8px;font:18px/1.42 Consolas,monospace;white-space:pre-wrap;overflow-wrap:anywhere}}
</style><main><h1>{html.escape(title)}</h1>
<p>Actual Java execution captured on 5 November 2026. Command: <code>{'scripts/test.ps1' if name == 'tests' else 'java -cp build/classes oopnemo.app.Main --demo'}</code></p>
<pre>{html.escape(output.strip())}</pre></main></html>''', encoding="utf-8")


class Report:
    def __init__(self):
        self.c = canvas.Canvas(str(OUTPUT / "SIW1-Technical-Report.pdf"), pagesize=A4)
        self.c.setTitle("SIW1 Java Collection Framework technical report")
        self.c.setAuthor("Nuriya Zhumanalieva")
        self.number = 0
        self.style = ParagraphStyle("body", fontName="Helvetica", fontSize=10.5, leading=15,
                                    textColor=INK, spaceAfter=9)

    def page(self, title, wide=False):
        if self.number:
            self.c.showPage()
        self.number += 1
        self.w, self.h = landscape(A4) if wide else A4
        self.c.setPageSize((self.w, self.h))
        self.y = self.h - 48
        self.c.setFont("Helvetica", 9)
        self.c.setFillColor(colors.HexColor("#596579"))
        self.c.drawString(45, self.y, "INDEPENDENT WORK 1   /   JAVA COLLECTION FRAMEWORK")
        self.y -= 33
        self.c.setFont("Helvetica-Bold", 21)
        self.c.setFillColor(colors.black)
        self.c.drawString(45, self.y, title)
        self.y -= 26
        self.c.setFont("Helvetica", 9)
        self.c.drawRightString(self.w - 45, 25, str(self.number))

    def p(self, text, size=None):
        style = self.style if size is None else ParagraphStyle("small", parent=self.style, fontSize=size, leading=size * 1.4)
        p = Paragraph(text, style)
        _, h = p.wrap(self.w - 90, self.h)
        if self.y - h < 45:
            raise ValueError(f"Report page {self.number} overflows with {text[:50]}")
        p.drawOn(self.c, 45, self.y - h)
        self.y -= h + 10

    def heading(self, text):
        self.y -= 9
        self.c.setFont("Helvetica-Bold", 13)
        self.c.setFillColor(colors.black)
        self.c.drawString(45, self.y, text)
        self.y -= 20

    def image(self, path, max_height):
        from PIL import Image
        with Image.open(path) as im:
            iw, ih = im.size
        factor = min((self.w - 90) / iw, max_height / ih)
        w, h = iw * factor, ih * factor
        self.c.drawImage(str(path), (self.w - w) / 2, self.y - h, w, h)
        self.y -= h + 10

    def table(self, rows, widths):
        style = ParagraphStyle("cell", parent=self.style, fontSize=9, leading=12)
        data = [[Paragraph(html.escape(str(cell)), style) for cell in row] for row in rows]
        table = Table(data, colWidths=widths, hAlign="LEFT")
        table.setStyle(TableStyle([
            ("BACKGROUND", (0, 0), (-1, 0), BLUE),
            ("GRID", (0, 0), (-1, -1), 0.5, GRAY),
            ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
            ("LEFTPADDING", (0, 0), (-1, -1), 9),
            ("RIGHTPADDING", (0, 0), (-1, -1), 9),
            ("TOPPADDING", (0, 0), (-1, -1), 8),
            ("BOTTOMPADDING", (0, 0), (-1, -1), 8),
        ]))
        _, height = table.wrap(self.w - 90, self.h)
        if self.y - height < 45:
            raise ValueError("Table overflows")
        table.drawOn(self.c, 45, self.y - height)
        self.y -= height + 15


def report():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    for name in ["architecture", "workflow", "tests"]:
        if not (EVIDENCE / f"{name}.jpg").exists():
            raise FileNotFoundError(f"Capture {name}.html as {name}.jpg before building the report")
    test_text = (EVIDENCE / "test-output.txt").read_text(encoding="utf-8")
    result = re.search(r"PASS: (\d+) test groups; (\d+) assertions", test_text)
    if not result:
        raise ValueError("Evidence must contain a successful regression run")
    groups, assertions = result.groups()
    r = Report()
    r.page("University and hospital administration")
    r.p("<b>Technical report</b><br/>Student: Nuriya Zhumanalieva<br/>Assignment: Independent Work 1<br/>Project: OOP-nemo<br/>Date: 5 November 2026")
    r.heading("Project purpose")
    r.p("This work extends the Person, Student, Teacher and Doctor system from Assignment 5 with the Java Collection Framework. It preserves the original domain relationships and payment formulas while adding indexed registries, sorting, request processing and consistent CRUD operations.")
    r.p("The result is a runnable Java console application with a deterministic demonstration, an interactive menu and a regression suite. All " + assertions + " assertions in " + groups + " test groups passed. The original nine Java listings were recovered separately, compiled and executed for comparison.")
    r.heading("Continuity with the previous project")
    r.p("Person remains abstract. Student, Teacher and Doctor retain their original fields and activities. Student creates and owns exam results; hospitals share independently created doctors. PayrollService still calculates payments through the Payable contract. Person now implements that common contract for the hierarchy.")
    r.p("The original Person[] array is replaced by HashMap and role-indexed sets. Immutable business IDs were added to entities. Payment methods use long to avoid int arithmetic overflow, and validation failures throw exceptions handled by the console. The original source is retained under reference/assignment5/src.")
    r.heading("Scope")
    r.p("This is an in-memory, single-user coursework application. Requests represent simulated payouts, not bank transfers. Data is reset on restart. Persistence, login infrastructure and concurrent access are outside the assignment scope.")
    r.p('Repository: <link href="https://github.com/nelliewxs/OOP-nemo" color="#1e4674">github.com/nelliewxs/OOP-nemo</link>')

    r.page("Domain model and relationships", True)
    r.image(UML / "domain-model.png", 370)
    r.p("Figure 1. Updated UML domain model. The filled diamond denotes Student-owned ExamResult composition. The hollow diamond denotes Hospital-Doctor aggregation; both ends permit multiple memberships. Requests identify their applicant by immutable personId. Role is STUDENT, TEACHER or DOCTOR; priorities are HIGH, MEDIUM and LOW; statuses are PENDING, PROCESSED and CANCELLED.", 9)

    r.page("Service layer and collection ownership", True)
    r.image(UML / "service-layer.png", 370)
    r.p("Figure 2. CampusService owns the registries and queue, coordinates CRUD and calls PayrollService. Main provides the applicant and payroll-operator actions. PersonComparators supplies three temporary sorted views. Collection types and selected operations match the implementation; routine accessors and validation utilities are omitted.", 9)

    r.page("Collection choices and contracts")
    r.table([
        ["Collection", "Purpose and justification"],
        ["HashMap<String, Person>", "Primary registry; expected O(1) key lookup. No duplicate general-purpose list is maintained."],
        ["EnumMap<Role, Set<Person>>", "Categorization index. LinkedHashSet role members enforce uniqueness by immutable person ID."],
        ["ArrayList<ExamResult>", "Ordered student-owned exam history; amortized O(1) append. Each retake gets a new result ID."],
        ["LinkedHashSet<Doctor>", "Hospital membership without duplicates. A doctor can be shared across hospitals."],
        ["HashMap for hospitals and requests", "Direct lookup of hospitals and current-session request history by business key."],
        ["PriorityQueue<PaymentRequest>", "Priority processing with FIFO submission sequence for equal priorities. Sort keys are immutable."],
    ], [175, A4[0] - 265])
    r.heading("Equality and ordering")
    r.p("Person, Hospital, ExamResult and PaymentRequest implement equals and hashCode using their immutable business IDs. All Person subclasses share one ID namespace. Changes to names, ages or payment attributes do not change identity or hash buckets.")
    r.p("Person implements Comparable by lexicographic ID, consistent with equality. Three custom comparators provide name ascending, age ascending and monthly payment descending, with ID tie breakers. They sort temporary lists; mutable display attributes are not stored as TreeSet ordering keys.")
    r.p("Collection getters return unmodifiable snapshots. Person objects remain shared canonical references, so changing a person's contact data is visible through the role index and hospital membership.")

    r.page("CRUD and role workflows")
    r.heading("Consistent CRUD")
    r.p("Creation validates an entity and rejects an existing ID before changing registries. Contact updates validate every field before assigning any value, preventing partial changes. A person's role and ID do not change. The same object is referenced by the primary index, role set and hospital membership.")
    r.p("Deleting a person removes the primary entry, role membership, pending queue entries and all of that person's request history. Deleting a doctor also removes that doctor from every hospital. Removing a hospital leaves its independently registered doctors intact. Student-owned exam results leave the managed graph with their owner.")
    r.heading("Applicant and operator workflow")
    r.p("A registered student, teacher or doctor submits a uniquely identified payment request. The service permits only one pending request per person. The payroll operator processes the next request: HIGH before MEDIUM before LOW, and earlier submissions first within one priority. The amount is calculated from the person's current details, then poll removes the queued request and the request index receives an immutable PROCESSED replacement.")
    r.p("Cancellation similarly replaces the request with a CANCELLED version and removes it from the queue. A processed or cancelled request cannot be cancelled again. An empty queue returns Optional.empty. If payment calculation fails, the request remains queued because calculation precedes removal.")
    r.heading("Complexity and error handling")
    r.p("HashMap key lookup has expected O(1) cost under suitable hash distribution. PriorityQueue offer and poll cost O(log q); peek is O(1). Complete submission also scans existing requests for a pending duplicate, so it is O(r). Filtering and cascade deletion require scans; sorting costs O(n log n). Queue display sorts a copy because iterator order is unspecified.")
    r.p("Validation covers blank fields, email structure, finite GPA, age/course ranges, negative amounts, duplicate keys, missing records and wrong role operations. The menu handles invalid numeric input and end-of-input. Consistent CRUD is not a claim of thread safety.")

    r.page("Execution evidence for the domain model", True)
    r.image(EVIDENCE / "architecture.jpg", 365)
    r.p("Figure 3. Browser screenshot of captured Java --demo output. This is actual execution text, rendered in the evidence viewer. It demonstrates the original hierarchy, composition and aggregation, ID-based equality, primary and role lookup, filtering, natural order and all three custom sort views. Raw output is retained as docs/evidence/demo-output.txt.", 9)

    r.page("Execution evidence for queue and CRUD", True)
    r.image(EVIDENCE / "workflow.jpg", 365)
    r.p("Figure 4. Captured Java execution shows two HIGH requests processed in submission order, followed by MEDIUM and LOW. Updating the doctor is reflected in hospital membership; deletion then cleans the role index, memberships and requests. The final totals reflect the four remaining people. Three processed requests remain because the deleted doctor's history was removed.", 9)

    r.page("Verification and reproducible execution")
    r.image(EVIDENCE / "tests.jpg", 230)
    r.p(f"Figure 5. All {assertions} assertions in {groups} groups passed. Tests cover equality, atomic updates, relationships, queue order and cancellation, cascade cleanup, analytics, validation, 100-person CRUD and interactive console subprocesses.", 9)
    r.heading("How to run")
    r.p("Install JDK 17 or newer. On Windows, run <b>scripts/run.ps1</b> for the menu, <b>scripts/run.ps1 -Demo</b> for the complete demonstration, or <b>scripts/test.ps1</b> for the regression suite. On Linux/macOS, use sh scripts/run.sh --demo and sh scripts/test.sh. The Java application needs no external libraries.")
    r.p("Local verification used JDK 23 with javac --release 17, UTF-8 and -Xlint:all -Werror. The generated bytecode targets Java 17. Windows scripts and the actual menu were exercised locally. GitHub Actions is configured to run the same suite on Java 17 and 23, on Windows and Linux; its run history records remote results.")
    r.heading("Sources and submission materials")
    r.p("Course materials: SIW1.pdf and Assignment 5 ZNZ.pdf. Technical references: Oracle Java SE 17 API documentation for HashMap, PriorityQueue, Comparable and Comparator. Direct links and a detailed requirement matrix are in docs/requirements.md.", 9)
    r.p("Submit the repository link, this report and docs/uml/class-diagram.pdf. The editable UML source and screenshots are included. docs/defense-ru.md provides oral-defense questions and a demonstration sequence. Oral understanding must be demonstrated by the student.", 9)
    r.c.save()
    print("Created output/pdf/SIW1-Technical-Report.pdf")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--prepare", action="store_true")
    args = parser.parse_args()
    if args.prepare:
        diagrams()
        viewers()
        print("Prepared UML diagrams and HTML execution viewers")
    else:
        report()
