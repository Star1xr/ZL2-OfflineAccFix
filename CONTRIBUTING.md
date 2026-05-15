# Contributing to Zalith Launcher 2+

Thank you for your interest in contributing to **Zalith Launcher 2+**! This is a community-driven fork, and we welcome contributions from developers of all skill levels.

## Before You Start

- This is an **unofficial fork** of [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2)
- Please review the [upstream project](https://github.com/ZalithLauncher/ZalithLauncher2) to understand the original codebase
- All contributions are governed by the **[GPL-3.0 license](LICENSE)**
- By contributing, you agree to license your changes under GPL-3.0

## Types of Contributions We Welcome

-  **Bug Fixes** - Help us squash bugs and improve stability
-  **Features** - New features and enhancements (discuss in issues first!)
-  **Documentation** - Improve README, comments, and guides
-  **Translations** - Help translate the app (consider contributing upstream)
-  **Code Review** - Review pull requests and provide feedback
-  **Ideas** - Suggest improvements via GitHub Issues

## Getting Started

### 1. Fork and Clone

```bash
# Fork the repository on GitHub, then clone your fork
git clone https://github.com/YOUR_USERNAME/ZalithLauncher2Plus.git
cd ZalithLauncher2Plus

# Add upstream as a remote to stay synced
git remote add upstream https://github.com/ZalithLauncher/ZalithLauncher2.git
```

### 2. Set Up Development Environment

**Requirements:**
- Android Studio **Bumblebee** or newer
- Android SDK (API level 26-35)
- JDK 11
- Git

**Setup Steps:**
```bash
# Ensure you're on the latest code
git fetch upstream
git checkout main  # or your default branch

# Open in Android Studio and let Gradle sync
```

### 3. Create a Branch

```bash
# Create a descriptive branch name
git checkout -b feature/your-feature-name
# or
git checkout -b fix/bug-description
```

## Development Guidelines

### Code Style

- Follow **Android/Kotlin conventions**
- Use **meaningful variable and function names**
- Keep functions small and focused (single responsibility principle)
- Add comments for complex logic
- Use proper indentation (4 spaces)

### Kotlin Specific

- Prefer immutability: use `val` over `var`
- Use Coroutines for async operations
- Leverage extension functions for clarity
- Follow [Kotlin Conventions](https://kotlinlang.org/docs/coding-conventions.html)

### Git Commits

Write clear, descriptive commit messages:

```
[TYPE] Brief description (50 chars max)

Detailed explanation of what and why, not how.
- Use bullet points for multiple changes
- Reference issue numbers: "Fixes #123"
- Keep body to 72 characters width
```

**Types:**
- `[FEAT]` - New feature
- `[FIX]` - Bug fix
- `[REFACTOR]` - Code reorganization (no functional change)
- `[DOCS]` - Documentation updates
- `[STYLE]` - Code style fixes (formatting, semicolons, etc.)
- `[PERF]` - Performance improvements
- `[TEST]` - Adding or updating tests
- `[CI]` - CI/CD changes

**Examples:**
```
[FEAT] Add offline account authentication support

- Implement local authentication fallback
- Cache account credentials securely
- Fixes #42
```

```
[FIX] Prevent crash on missing launcher files

- Add null safety checks in FileManager
- Provide user-friendly error message
- Fixes #89
```

## Before Submitting a Pull Request

### 1. Sync with Upstream

```bash
git fetch upstream
git rebase upstream/main
# or merge if you prefer
git merge upstream/main
```

### 2. Test Your Changes

- Test on **multiple Android API levels** (26+)
- Test on **different device sizes**
- Verify no crashes or crashes in logs
- Check performance impact

### 3. Code Review Checklist

- [ ] Code follows style guidelines
- [ ] Comments added for complex logic
- [ ] No debug logs or console prints left
- [ ] Changes are tested thoroughly
- [ ] Commit messages are clear and descriptive
- [ ] No breaking changes (or documented)
- [ ] License headers preserved/added where needed

## Submitting a Pull Request

### 1. Push to Your Fork

```bash
git push origin feature/your-feature-name
```

### 2. Create Pull Request

When creating a PR on GitHub:

**Title Format:**
```
[TYPE] Brief description

Examples:
[FEAT] Add support for offline authentication
[FIX] Crash when launcher files missing
```

**Description Template:**
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix (non-breaking)
- [ ] New feature (non-breaking)
- [ ] Breaking change
- [ ] Documentation update

## Related Issues
Fixes #(issue number)

## Testing
- [ ] Tested on API 26
- [ ] Tested on API 35
- [ ] Tested on tablet
- [ ] Tested on phone

## Screenshots/Videos
If applicable, add media here

## Additional Notes
Any other relevant information
```

### 3. Address Feedback

- Respond to comments respectfully
- Make requested changes and push to the same branch
- Use force push sparingly (only if needed)
- Engage in discussion constructively

## Reporting Issues

### Bug Reports

**Title:** Clear, concise description

**Template:**
```markdown
## Description
What went wrong?

## Steps to Reproduce
1. Step 1
2. Step 2
3. Step 3

## Expected Behavior
What should happen

## Actual Behavior
What actually happened

## Environment
- Device: Model/API Level
- Android Version: (e.g., 12)
- App Version: (if applicable)

## Logs
Relevant error messages or logcat output

## Screenshots
If applicable
```

### Feature Requests

**Title:** What feature would you like?

```markdown
## Description
What should the feature do?

## Use Case
Why is this needed?

## Proposed Solution
How should it work?

## Alternative Solutions
Any other approaches?
```

## Review Process

1. **Automated Checks** - CI/CD pipeline runs tests
2. **Code Review** - Maintainers review your changes
3. **Changes Requested** - Update code if needed
4. **Approval** - Get at least one approval
5. **Merge** - Maintainer merges to main branch

Typical turnaround: **1-2 weeks**, depending on complexity

## Community Standards

### Be Respectful
- Treat all contributors with respect
- Welcome diverse perspectives
- Assume good intentions
- Provide constructive feedback

### Be Helpful
- Help other contributors when possible
- Answer questions in issues/PRs
- Share knowledge and experience
- Mentor newer developers

### Be Responsible
- Test changes thoroughly
- Document your code
- Keep security in mind
- Report vulnerabilities responsibly

## Compliance & Legal

### License
All contributions must comply with **GPL-3.0**:
- Preserve copyright notices
- Include license headers in new files
- Document modifications

### Upstream Compatibility
- Stay compatible with upstream when possible
- Clearly document any breaking changes
- Consider contributing fixes upstream

### Security
- Don't commit secrets (API keys, tokens, etc.)
- Use `.gitignore` for sensitive files
- Report security issues privately to maintainers
- Follow secure coding practices

## Running Tests

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run specific test
./gradlew test:TestClassName
```

## Useful Resources

- **Android Documentation:** https://developer.android.com/docs
- **Kotlin Documentation:** https://kotlinlang.org/docs/home.html
- **Jetpack Compose:** https://developer.android.com/develop/ui/compose
- **Upstream Project:** https://github.com/ZalithLauncher/ZalithLauncher2
- **PojavLauncher:** https://github.com/PojavLauncherTeam/PojavLauncher

## Getting Help

-  Check existing issues and discussions
-  Comment on relevant issues for clarification
-  Contact maintainers for private concerns
-  See README.md for more resources

## Recognition

Contributors are recognized through:
- Commit history on GitHub
- Contributors section in README (if applicable)
- Acknowledgments in release notes
- Community appreciation

## Questions?

-  Open a GitHub Discussion
-  Create an Issue (if a bug)
-  Comment on related PRs/Issues

---

Thank you for making Zalith Launcher 2+ better! 🚀
