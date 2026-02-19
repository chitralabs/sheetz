# Contributing to Sheetz

Thank you for considering contributing to Sheetz! 🎉 All contributions are welcome —
from fixing a typo to implementing a new file format.

---

## 🚀 Your First Contribution

Never contributed to open source before? No problem. Here's how to make your first PR:

1. **Find an issue** labelled [`good first issue`](https://github.com/chitralabs/sheetz/issues?q=label%3A%22good+first+issue%22)
2. **Comment** "I'd like to work on this" — a maintainer will assign it to you within 24 hours
3. **Fork** the repo and create a branch: `git checkout -b feature/my-feature`
4. **Make your changes** and add tests
5. **Run tests**: `mvn test`
6. **Open a PR** — use the PR template and link the issue
7. **See your name** in the next CHANGELOG! 🎊

Expect review feedback within **48 hours**. We'll help you get it merged.

---

## 🐛 Reporting Bugs

Use the [Bug Report template](https://github.com/chitralabs/sheetz/issues/new?template=bug_report.md).
Include a minimal reproduction and your environment details.

---

## 💡 Requesting Features

Use the [Feature Request template](https://github.com/chitralabs/sheetz/issues/new?template=feature_request.md).
Check the [Roadmap](README.md#roadmap) — your idea may already be planned.

---

## 🔨 Development Setup

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/sheetz.git
cd sheetz

# Build and run tests
mvn clean install

# Run only tests
mvn test

# Check code coverage (requires JaCoCo)
mvn verify
open target/site/jacoco/index.html
```

**Requirements:** Java 11+, Maven 3.6+

---

## 📏 Code Standards

- Follow existing code style (no external formatter required)
- Add Javadoc for all public methods
- Maintain test coverage above 80% for new code
- No breaking changes to the public API without discussion

---

## 🧪 Testing Guidelines

- Every bug fix must include a test that **fails before** the fix and **passes after**
- Every new feature must include unit tests covering happy path + edge cases
- Use the existing test patterns in `src/test/java`
- Add sample files to `src/test/resources` if needed for file-based tests

---

## 📝 Commit Messages

Use conventional commits style:
```
feat: add ODS format support
fix: handle null values in LocalDate converter
docs: add streaming example to README
test: add validation edge cases for empty CSV
refactor: extract SAX parser into dedicated class
```

---

## 🏢 Adding Your Company to "Used By"

If Sheetz is used in production at your company or project, please open a PR to add it to
the README's "Used By" section. Include your company/project name and a link.

---

## 🙏 Recognition

All contributors are:
- Listed in `CHANGELOG.md` by name
- Added to the GitHub contributors graph
- Mentioned in release notes

Thank you for helping make Sheetz better! ⭐
