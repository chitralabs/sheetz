# Contributing to Sheetz

Thank you for your interest in contributing to Sheetz! This document provides guidelines and instructions for contributing.

## How to Contribute

### Reporting Bugs

- Check existing [issues](https://github.com/chitralabs/sheetz/issues) first to avoid duplicates
- Use the bug report template and include: Java version, OS, Sheetz version, minimal reproducible example

### Suggesting Features

- Open a [feature request](https://github.com/chitralabs/sheetz/issues/new) with your use case
- Explain why existing functionality doesn't cover your need

### Pull Requests

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Write tests for your changes
4. Ensure all tests pass: `mvn clean test`
5. Follow existing code style and conventions
6. Submit a pull request against `main`

## Development Setup

### Prerequisites

- Java 11+
- Maven 3.8+

### Build & Test

```bash
# Clone
git clone https://github.com/chitralabs/sheetz.git
cd sheetz

# Build
mvn clean install

# Run tests only
mvn test

# Generate coverage report
mvn test jacoco:report
# View: target/site/jacoco/index.html
```

## Code Style

- Use 4-space indentation
- Follow standard Java naming conventions
- Add Javadoc to all public classes and methods
- Keep methods focused and short
- Prefer immutability where possible

## Testing

- All new features must have corresponding unit tests
- Use JUnit 5 and AssertJ for assertions
- Place tests in the matching package under `src/test/java`
- Aim for meaningful assertions, not just coverage

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.
