# Assignment 5 baseline

These nine source files were recovered from the complete source listings in the supplied `Assignment 5 ZNZ.pdf` (pages 3-15). They preserve the previous coursework for comparison and are not part of the new application's build.

The original PDF also contains the UML diagram (page 3). The updated diagrams are under `docs/uml/`.

To compile and run this historical version separately:

```powershell
New-Item -ItemType Directory -Force build/baseline | Out-Null
javac --release 17 -encoding UTF-8 -d build/baseline reference/assignment5/src/*.java
java -cp build/baseline Main
```

The new implementation preserves the original person hierarchy, fields, scholarship and salary formulas, exam ownership, shared hospital memberships, and `PayrollService` dependency on `Payable`. See `docs/requirements.md` for the migration details.
