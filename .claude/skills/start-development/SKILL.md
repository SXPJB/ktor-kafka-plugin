---
name: start-development
description: Rules for creating branches and starting any new development in ktor-kafka-plugin. Use before writing code for a new feature, fix, refactor, or chore, when asked to "start a task", "create a branch", "begin work on X", or when currently on master with changes to make.
---

# Start development

Follow these steps in order before writing code. Read `AGENTS.md` first for architecture and conventions.

## 1. Sync master
```bash
git switch master
git pull --ff-only origin master
```
If the working tree is dirty, stop and ask the user what to do with the changes. Never commit on `master`.

## 2. Pick the branch name
Format: `<type>/<short-kebab-description>`, lowercase, 2-5 words, no ticket or author in the name.

| Type | Use for |
|---|---|
| `feature/` | New user-facing capability or public DSL/API (e.g. `feature/add-producer-support`) |
| `task/` | Internal work: refactors, tests, CI, docs, tooling (e.g. `task/add-ci-cd`) |
| `fix/` | Bug fixes (e.g. `fix/consumer-config-precedence`) |

Check the name is free and spelled correctly:
```bash
git branch -a --list "*<description>*"
```

## 3. Create the branch
```bash
git switch -c <type>/<description>
```

## 4. Plan before coding
- Restate the goal in one or two sentences and list the files to touch.
- Check the "Known issues" section of `AGENTS.md`; mention any that affect the task.
- If the change alters public API, Maven coordinates, or dependency versions, confirm with the user first.

## 5. Implement
- Follow `AGENTS.md` conventions (constants for messages, `logger()`, KDoc on public types, sealed `KtorKafkaSpec` dispatch).
- Add or update Kotest `DescribeSpec` tests mirroring the main package path.
- Keep each commit focused on one logical change.

## 6. Verify
```bash
./gradlew ktlintFormat
./gradlew ktlintCheck
./gradlew test
```
Both checks must pass. Report failures with their output; do not claim success otherwise.

## 7. Commit
Title format: `<Type> | SXPJB | <Imperative summary>` with Type in `Feature`, `Task`, `Fix`, `Chore`.
Add a short body explaining why when it is not obvious.

## 8. Push and PR (only when the user asks)
```bash
git push -u origin <branch>
gh pr create --base master --title "<same as commit title>" --body "<summary + test notes>"
```
CI runs lint and tests on the PR. Update `AGENTS.md` and `README.md` in the same PR if architecture, public DSL, or commands changed.
