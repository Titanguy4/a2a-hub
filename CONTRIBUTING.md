# Contributing to A2A-Hub

First off, thank you for considering contributing to A2A-Hub! It's people like you that make open-source such a great community. 

## 1. Where do I go from here?

If you've noticed a bug or have a feature request, make sure to check our [Issues](https://github.com/oscarbol09/a2a-hub/issues) to see if someone else has already created a ticket. If not, go ahead and [make one](https://github.com/oscarbol09/a2a-hub/issues/new)!

## 2. Fork & create a branch

If this is something you think you can fix, then [fork A2A-Hub](https://github.com/oscarbol09/a2a-hub/fork) and create a branch with a descriptive name.

A good branch name would be (where issue #325 is the ticket you're working on):

```sh
git checkout -b fix/325-agent-health-check
```

## 3. Implement your fix or feature

### Backend Guidelines (Java/Spring Boot)
- **Style:** We follow standard Java conventions. Avoid overly nested blocks.
- **Testing:** Add JUnit 5 tests for your logic. Integration tests should use Testcontainers.
- **Virtual Threads:** Be mindful of thread pinning. Avoid `synchronized` blocks around long network/I/O calls; use `ReentrantLock` if necessary.

### Frontend Guidelines (Vue 3/Tailwind)
- **Composition API:** Use `<script setup lang="ts">`.
- **Types:** Always provide types or interfaces for API responses and component props. No `any` without justification.
- **Styling:** Use Tailwind utility classes. For complex components, abstract into reusable components in `src/components`.

## 4. Conventional Commits

We use [Conventional Commits](https://www.conventionalcommits.org/). This means your commit messages should be formatted like:

- `feat(frontend): add search capability to discovery view`
- `fix(backend): resolve SSRF vulnerability in agent registry`
- `docs: update setup instructions in README`
- `chore: update dependencies`

## 5. Make a Pull Request

At this point, you should switch back to your master branch and make sure it's up to date with A2A-Hub's master branch.

Then push your branch to your fork and submit a pull request!

Please fill out the PR template completely. A maintainer will review your code, potentially ask for changes, and merge it!

## 6. Code of Conduct

Please note that this project is released with a [Contributor Code of Conduct](CODE_OF_CONDUCT.md). By participating in this project you agree to abide by its terms.
