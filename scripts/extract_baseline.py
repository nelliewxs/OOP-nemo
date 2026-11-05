"""Recover the nine original Java listings from the supplied Assignment 5 PDF.

Usage: python scripts/extract_baseline.py PATH_TO_ASSIGNMENT_5.pdf
Requires pypdf. The original PDF is not published in the repository.
"""
import re
import sys
from pathlib import Path
from pypdf import PdfReader

root = Path(__file__).resolve().parents[1]
reader = PdfReader(sys.argv[1])
text = "\n".join(page.extract_text() for page in reader.pages)
text = re.sub(r"(?m)^Assignment 5 \| \d+\s*$", "", text).replace("\xa0", " ")
headings = list(re.finditer(r"(?m)^4\.[1-9] (\w+\.java)\s*$", text))
assert len(headings) == 9, "Expected all nine original listings"
destination = root / "reference" / "assignment5" / "src"
destination.mkdir(parents=True, exist_ok=True)
for i, heading in enumerate(headings):
    end = headings[i + 1].start() if i + 1 < len(headings) else text.index("5. Program Results", heading.end())
    code = text[heading.end():end].strip() + "\n"
    code = "\n".join(line.rstrip() for line in code.splitlines()) + "\n"
    (destination / heading.group(1)).write_text(code, encoding="utf-8")
print("Recovered nine original Java files into reference/assignment5/src")
