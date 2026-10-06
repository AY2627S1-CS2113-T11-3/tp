# Project context

This repository is a starter template for a greenfield Java project used in an introductory software engineering course in an undergraduate computer science program. Students use it as the starting point for their own projects.

# Default user context

Unless the user says otherwise, assume that you are assisting a student working on a project in this repository. If the user identifies themselves as an instructor or another project stakeholder, adapt your response to that role.

# Student profile

* Prior knowledge: Basic Java and OOP concepts.
* Level of programming experience: [to be filled]
* IDE and level of expertise: [to be filled]

# Guidance for interacting with users

* Explain the rationale for significant actions: what you did and why.
* Keep explanations brief but instructive, supporting learning through responsible use of AI. For example:

  * When suggesting a Git command, briefly explain what it does.
  * Add explanatory Javadoc comments to all classes and to nontrivial methods and fields when their purpose or behavior is not obvious.
  * Make generated code as self-explanatory as possible, and include explanatory comments where they improve understanding.
  * When faced with a design choice, choose the simplest option that is sufficient for the requirements, while briefly explaining relevant more advanced alternatives.

# Project-specific requirements

## Mandatory coding standard

All Java code in this project, including tests, must follow the SE-EDU basic and intermediate coding standard.
Before writing, modifying, or reviewing Java code, read and apply the project skill
[seedu-java-coding-standard](.agents/skills/seedu-java-coding-standard/SKILL.md).
Review rules that Checkstyle cannot enforce manually, and run the Gradle wrapper's `check` task with Java 25 after changes.
These requirements apply to all agents working in this repository.

## Java version:

Ensure that Java 25 is used when running the application or build tasks. On macOS, use `sdk use java 25.0.3.fx-zulu` to switch to Java 25 if needed.

## Git

All future commits must follow the SE-EDU Git conventions. Before proposing, writing, or reviewing commit messages,
read and apply [seedu-git-standard](.agents/skills/seedu-git-standard/SKILL.md).
Apply its branch naming rules when creating branches. These requirements apply to all agents in this repository.

Use lightweight tags unless the user requests an annotated tag.
When proposing or creating a commit message, include enough detail to explain the rationale for the change.
Do not commit or push unless explicitly asked.
