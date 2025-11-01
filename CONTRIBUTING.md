# Contributing to no-tang-doc

Thank you for your interest in contributing to no-tang-doc! This document provides guidelines and instructions for contributing to the project.

---

## Table of Contents

- [Code Guidelines](#code-guidelines)
- [Branch Strategy](#branch-strategy)
- [Commit Messages](#commit-messages)
- [Pull Request Process](#pull-request-process)
- [Testing Requirements](#testing-requirements)
- [Development Environment](#development-environment)

---

## Code Guidelines

### General Principles

- Follow industry best practices
- High cohesion, low coupling
- Write clean, self-documenting code
- Include comprehensive tests
- Document public APIs and complex logic
- Review your own code before requesting reviews

### Agent Service Specific

- **Python Version**: 3.13.7+
- **Package Manager**: Use `uv` for all package management (no `pip` or `python` directly)
- **Code Quality**: Follow Ruff linting rules
- **Language**: All code and comments in English
- **Type Safety**: Use type hints for all functions
- **Test Coverage**: Maintain ≥95% code coverage

### Core Service Specific

- **Java Version**: 24
- **Framework**: Spring Boot 3.5.5
- **Build Tool**: Maven
- **Code Style**: Follow team's code style guidelines
- **Language**: All code and comments in English

### Web Service Specific

- **Language**: TypeScript
- **Framework**: React with Vite
- **Code Quality**: Follow ESLint rules
- **Language**: All code and comments in English

---

## Branch Strategy

### Protected Branches

- `main`: Production releases
- `dev`: Development integration
- `docs`: Documentation updates (merges to `dev`)
- `mod/*`: Module-specific main branches
  - `mod/agent`: Agent service
  - `mod/core`: Core service
  - `mod/web`: Web service

### Feature Branches

- `feat/{module}/*`: Feature development for specific modules
  - Example: `feat/agent/<feature-name>`
- `feat/*`: Project-wide features
  - Example: `feat/<feature-name>`

### Workflow

```
feat/{module}/* → mod/{module} → dev → main
      ↓ PR           ↓ PR        ↓ PR
  Feature Dev    Module Merge  Dev Test

feat/* (docs, etc.) → dev → main
      ↓ PR            ↓ PR
  Project Features  Dev Test
```

### Rules

- Protected branches (`main`, `dev`, `docs`, `mod/*`) require Pull Requests
- No direct pushes to protected branches
- CI must pass before merging
- At least one approval required
- Squash commits when appropriate

---

## Commit Messages

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification.

### Format

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `refactor`: Code refactoring (no functional changes)
- `docs`: Documentation changes
- `test`: Adding or updating tests
- `chore`: Maintenance tasks
- `ci`: CI/CD changes
- `perf`: Performance improvements
- `style`: Code style changes (formatting, etc.)

### Scope

Use the service name or module affected:
- `agent`: Agent service
- `core`: Core service
- `web`: Web service
- `infra`: Infrastructure
- `ci`: CI/CD
- Or leave empty for project-wide changes

### Examples

**Good commits:**
```
feat(agent): Add document export functionality

- Implement PDF export for documents
- Add CSV export for team data
- Update API documentation

Closes #123
```

```
fix(core): Resolve authentication token expiration issue

The token validation was not properly checking expiration time.
Now uses ZonedDateTime for accurate timezone handling.

Fixes #456
```

```
docs: Update installation instructions in README

- Add prerequisites section
- Update Docker commands
- Add troubleshooting tips
```

**Bad commits:**
```
fix bug
update code
WIP
asdfasdf
```

---

## Pull Request Process

### 1. Create Feature Branch

```bash
# For module-specific feature
git checkout -b feat/agent/<feature-name>

# For project-wide feature
git checkout -b feat/<feature-name>
```

### 2. Implement Changes

- Write clean, tested code
- Follow code guidelines
- Update documentation if needed
- Add tests for new functionality
- Ensure all tests pass locally

### 3. Commit Changes

```bash
git add .
git commit -m "feat(agent): Add your feature description"
```

### 4. Push to Remote

```bash
git push -u origin feat/agent/<feature-name>
```

### 5. Create Pull Request

Use GitHub CLI or web interface:

```bash
gh pr create --base mod/agent --title "feat(agent): Your feature title" --body "Description"
```

**PR Title**: Follow commit message format
**PR Description**: Include:
- Overview of changes
- Testing done
- Screenshots (if UI changes)
- Breaking changes (if any)
- Related issues/PRs

### 6. Code Review

- Respond to review comments promptly
- Make requested changes
- Push updates to the same branch
- Re-request review after addressing feedback

### 7. Merge

After approval and passing CI:
- Squash and merge (preferred for feature branches)
- Merge commit (for release branches)
- Delete branch after merging

---

## Testing Requirements

### Agent Service

**Minimum Requirements:**
- Unit test coverage: ≥95%
- All new features must have tests
- Run tests locally before pushing

**Running Tests:**
```bash
cd no-tang-doc-agent
uv run pytest tests/ \
  --cov=src/no_tang_doc_agent/mcp_server \
  --cov-report=xml \
  --cov-report=term-missing \
  --cov-report=html \
  --cov-fail-under=95 \
  --cov-branch
```

**Test Structure:**
```python
def test_feature_name():
    """Test description."""
    # Arrange
    setup_data = create_test_data()
    
    # Act
    result = function_under_test(setup_data)
    
    # Assert
    assert result == expected_value
```

### Core Service

Refer to [`no-tang-doc-core/README.md`](no-tang-doc-core/README.md) for Core service testing guidelines.

### Web Service

Refer to [`no-tang-doc-web/README.md`](no-tang-doc-web/README.md) for Web service testing guidelines.

---

## Development Environment

### Agent Service

**Prerequisites:**
- Python 3.13.7+
- uv package manager
- Docker (optional)

**Setup:**
```bash
cd no-tang-doc-agent
uv sync --all-extras --dev
uv run no-tang-doc-agent-mcp-server
```

**Linting:**
```bash
uv run ruff check src/ tests/ --output-format=github
uv run ruff format --check src/ tests/
```

**Auto-fix:**
```bash
uv run ruff check --fix src/ tests/
uv run ruff format src/ tests/
```

For detailed development guide, see [`no-tang-doc-agent/README.md`](no-tang-doc-agent/README.md).

### Core Service

Refer to [`no-tang-doc-core/README.md`](no-tang-doc-core/README.md) for Core service development setup.

### Web Service

Refer to [`no-tang-doc-web/README.md`](no-tang-doc-web/README.md) for Web service development setup.

---

## Code Review Guidelines

### For Authors

- Keep PRs focused and reasonably sized
- Provide clear description and context
- Respond to feedback constructively
- Test thoroughly before requesting review
- Update documentation as needed

### For Reviewers

- Review promptly (within 1-2 business days)
- Be constructive and respectful
- Ask questions if unclear
- Check for:
  - Code correctness
  - Test coverage
  - Documentation updates
  - Performance implications
  - Security concerns

---

## Getting Help

If you need help or have questions:

1. Check existing documentation in service READMEs
2. Search GitHub Issues for similar questions
3. Ask in team communication channels
4. Create a GitHub Issue with the `question` label

---

## License

By contributing to no-tang-doc, you agree that your contributions will be licensed under the project's license.

---

Thank you for contributing to no-tang-doc! 🎉
