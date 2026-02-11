# Security Policy

## Supported Versions

| Version | Supported          |
|---------|--------------------|
| 1.0.x   | :white_check_mark: |

## Reporting a Vulnerability

If you discover a security vulnerability in Sheetz, please report it responsibly.

**Do not open a public GitHub issue.** Instead, send an email to **security@chitralabs.github.io** with
the following information:

1. A description of the vulnerability and the conditions needed to trigger it.
2. Steps to reproduce the issue or a minimal proof of concept.
3. The Sheetz version(s) affected.
4. Any suggested fix or mitigation you are aware of.

We will acknowledge your report within **48 hours** and aim to provide a fix or
mitigation plan within **7 business days**. Once a fix is available, we will
coordinate a disclosure timeline with you before making the details public.

## Security Measures

Sheetz takes the following steps to protect users:

- **XXE Protection** — The SAX parser used for XLSX streaming is configured with
  external entity processing disabled and doctype declarations disallowed,
  preventing XML External Entity injection attacks from malicious spreadsheet
  files.
- **Dependency Scanning** — We track upstream security advisories for Apache POI,
  OpenCSV, and SLF4J and update promptly when patches are available.
- **Minimal Permissions** — The library performs only file I/O on paths explicitly
  provided by the caller and does not open network connections, execute system
  commands, or access environment variables.

## Scope

This policy covers the `io.github.chitralabs.sheetz:sheetz-core` artifact published to
Maven Central. Third-party forks, downstream wrappers, and applications that
embed Sheetz are outside the scope of this policy.

Thank you for helping keep Sheetz and its users safe.
