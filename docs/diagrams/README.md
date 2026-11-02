# Editing the guide diagrams

Every PNG embedded in the Developer Guide has a matching `.puml` source in this directory.
`loan-style.puml` supplies shared styling and does not produce an image.

Use Java 25 and [PlantUML 1.2025.10](https://repo.maven.apache.org/maven2/net/sourceforge/plantuml/plantuml/1.2025.10/).
Download `plantuml-1.2025.10.jar` into `build/diagram-tools/` (create the directory first).
The renderer is a local build tool and is not committed. PlantUML uses Graphviz for class layouts;
install [Graphviz](https://graphviz.org/download/) if `java -jar build/diagram-tools/plantuml-1.2025.10.jar -testdot`
reports that it is unavailable. Avoid Smetana for class diagrams because this renderer version omits
association label direction markers with that layout engine.

From the repository root, regenerate the images in either PowerShell or a POSIX shell:

```text
java "-Djava.awt.headless=true" -jar build/diagram-tools/plantuml-1.2025.10.jar -charset UTF-8 -tpng -o ../images "docs/diagrams/*.puml"
```

The warning that `loan-style.puml` has no image is expected. Inspect every changed PNG before including it
with its source change. Compare relationships and operation signatures with the Java code.

Follow these conventions:

- Keep an attribute compartment and an operation compartment in every class, even when either is empty.
  Explicit `--` separators preserve the empty compartments. Members use `{field}` to preserve their manual
  grouping; operations still have parentheses and appear below the separator.
- Show retained references as navigable solid associations and temporary use as dashed dependencies.
  Show each reference once: use an association role instead of repeating it in the attribute compartment.
  Association labels include a reading-direction triangle (`>`), separate from the navigability arrowhead.
- Use a filled diamond only for exclusive ownership, such as `Session` owning its `Preparation` or `Loan`
  owning its private item snapshot. `Inventory.addItem` stores the supplied reference, so its item association
  does not promise exclusive ownership.
- Sequence participants use plain, un-underlined names. End an activation when a call returns.
  Use `destroy` (an `X`) only when the interaction actually destroys an object. A returned constructor or
  completed command does not destroy a stored item, session, request, report, or manager. Those lifelines
  remain alive; they do not receive an `X` merely because the diagram ends.
- Check guide links and section anchors after renaming or numbering headings. Commit the regenerated PNGs
  with their source changes when a commit is requested.

See the [SE-EDU PlantUML tutorial](https://se-education.org/guides/tutorials/plantUml.html) and
[PlantUML class notation](https://plantuml.com/class-diagram) for editing syntax.
