# OOP-nemo

Java object-oriented programming coursework: Independent Work 1, extending a domain model with the Java Collection Framework (JCF).

## Status

Repository initialized. Application implementation has not started. The previous coursework's Java source and UML diagram are needed to preserve its domain, inheritance, relationships, and user roles. If no previous project exists, a new domain must be agreed before implementation.

## Assignment requirements

- Store domain entities in `List` or `Set` collections.
- Implement `equals()` and `hashCode()` using unique business keys.
- Provide a primary-key `Map` index and a category/group index.
- Implement `Comparable` on the main domain class and at least three custom comparators.
- Model a real domain workflow with a `Queue` and process requests using `poll()`.
- Preserve the existing UML relationships and interactions between user roles.
- Implement CRUD that keeps entity collections, indexes, relationships, and pending requests consistent.
- Provide primary-key lookup, criteria-based filtering, sorting, and aggregated reports.
- Handle invalid input, duplicate identifiers, missing entities, and empty collections or queues.

## Deliverables

1. Java source code under `src/`, with run instructions.
2. Updated UML class diagram showing collections and the service layer.
3. Technical report in PDF or DOCX with the domain description, collection choices, and execution screenshots.
4. Preparation for an oral code defense covering equality contracts, ordering, queue processing, and map complexity.

## Implementation sequence

1. Review the existing project and map its classes, roles, and relationships to the assignment.
2. Choose collection implementations and define identity and ordering contracts.
3. Implement service operations, consistent indexes, queue workflows, and analytics.
4. Verify the complete role scenarios and collection edge cases.
5. Update the UML, capture execution screenshots, and write the report.

The assignment specifies submission before midterm assessment week, without a calendar date. Source: the supplied `SIW1.pdf`.

## Local configuration

Local credentials are kept in `.env`, which is excluded from Git. They are not part of the Java assignment or required for running the eventual application.
