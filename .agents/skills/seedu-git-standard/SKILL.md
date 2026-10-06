---
name: seedu-git-standard
description: Apply SE-EDU Git conventions when proposing, writing, or reviewing commit messages and naming branches in this project.
---

# SE-EDU Git standard

Follow the [SE-EDU Git conventions](https://se-education.org/guides/conventions/git.html).

## Commit messages

- Write an imperative subject with an initial capital and no final period. Aim for 50 characters; never exceed 72. A relevant scope or category prefix is optional, not mandatory.
- For a nontrivial change, add a body after one blank line. Wrap body lines at 72 characters and separate paragraphs with blank lines. Use bullets when helpful.
- Explain the change and its motivation rather than narrating implementation details or duplicating code comments. Give enough context for a reviewer to assess the rationale without reading the diff.
- Organize the body around the existing situation in present tense, the need for change, the proposed action in imperative mood, the reasoning for that approach, and relevant additional information. Avoid redundant time qualifiers such as "currently" or "originally". "Let's" may introduce the action.
- If the explanation becomes unwieldy, consider smaller focused commits.

## Branch names

Use descriptive kebab-case keywords. For an issue branch, prefix those keywords with the issue number, for example `42-inventory-validation`.

## Project workflow

Read AGENTS.md before Git work. Commit or push only when the user explicitly requests it; this skill does not authorize either action. Use lightweight tags unless annotated tags are requested.

Before an authorized commit, inspect the working tree and staged diff. Include only the intended changes, preserve unrelated work and staging, and ensure the message accurately describes the staged result. Do not rewrite existing commits or rename existing branches merely to apply this standard retroactively.

When proposing a message, apply these rules immediately. Before creating a commit, check subject and body lengths and ensure the rationale is clear. Include relevant validation results without claiming checks that were not run. For Java changes, also follow the project's Java skill and required Java 25 Gradle check.
