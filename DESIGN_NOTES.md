# PersonAI Design Notes

## Introduction

This document outlines the design principles and considerations for the PersonAI project. The aim is to keep the architecture and codebase as simple as possible while enabling powerful capabilities through the Language Learning Model (LLM).

## Design Principles

### KISS (Keep It Simple, Stupid)

- The codebase should be minimal and straightforward.
- Fail-fast to quickly alert the user to issues.

### Language and Environment

- Java is the language of choice for the MiniGW, as it's widely available and is the author's preferred language.
- The code level is set to 1.7 (Java 7) to ensure the code is readable rather than 'elegant'.
- Any supported JVM can be used, though JVM 11 is suggested. Avoid using End of Life versions.

### Contributions

- Contributors are welcome! But remember, KISS is the guiding principle.

### Role of the LLM

- The LLM will do all the heavy lifting, serving as the primary mechanism for fulfilling user needs and performing tasks.

## Conclusion

The design of PersonAI aims to be as simple as possible, serving merely as a bridge between the user and the LLM. The focus is on enabling the LLM to perform complex tasks, thereby democratizing access to AI capabilities.
