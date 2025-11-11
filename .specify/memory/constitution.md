<!--
Sync Impact Report:
- Version change: none -> 1.0.0
- Added sections:
  - I. Code Quality & Modern Android Development
  - II. Comprehensive Testing
  - III. Consistent User Experience
  - IV. Performance & Optimization
  - Development Workflow
- Templates requiring updates:
  - ✅ .specify/templates/plan-template.md
  - ✅ .specify/templates/spec-template.md
  - ✅ .specify/templates/tasks-template.md
-->
# Pasbuk Enhanced Android Constitution

## Core Principles

### I. Code Quality & Modern Android Development
All new code MUST be written in Kotlin, following the official Kotlin coding conventions. The UI MUST be built with Jetpack Compose. Asynchronous operations MUST be handled using Kotlin Coroutines and Flow. Dependency Injection MUST be implemented using a recognized framework like Hilt or Koin. The application architecture MUST align with the official "Guide to app architecture," clearly separating UI, Domain, and Data Layers.

### II. Comprehensive Testing
TDD is strongly encouraged. Unit tests MUST be written for all business logic, including ViewModels, UseCases, and Repositories, aiming for a minimum of 70% test coverage. Integration tests SHOULD be implemented for interactions between different layers of the application. UI tests MUST be created for critical user flows using Jetpack Compose testing APIs to ensure stability and correctness from the user's perspective.

### III. Consistent User Experience (UX)
The application MUST adhere to Material Design 3 guidelines to ensure a modern and predictable user interface. UI components SHOULD be designed for reusability to maintain consistency across the application. The app MUST provide a responsive and adaptive experience across different screen sizes and orientations. Accessibility standards, including TalkBack support and sufficient color contrast, are non-negotiable and MUST be met.

### IV. Performance & Optimization
The application MUST deliver a smooth user experience, free of jank or freezes. Application startup time SHOULD be minimized. Memory usage MUST be actively monitored and optimized to prevent memory leaks and out-of-memory crashes. Network operations MUST be efficient, with appropriate caching strategies implemented to reduce latency and data consumption.

## Development Workflow

All code changes MUST be submitted through a Pull Request (PR) on the project's version control system. Every PR MUST be reviewed and approved by at least one other team member before being merged. All PRs MUST pass the full suite of continuous integration (CI) checks, including static analysis (lint), all tests (unit, integration, UI), and a successful build.

## Governance

This constitution is the single source of truth for development standards and practices within the Pasbuk Enhanced Android project. Amendments to this constitution require a formal proposal via a PR, which must be approved by the project maintainers. All development activities, code reviews, and architectural decisions must align with these principles. Any intentional deviation requires explicit, documented justification and approval within the corresponding PR.

**Version**: 1.0.0 | **Ratified**: 2025-11-11 | **Last Amended**: 2025-11-11
