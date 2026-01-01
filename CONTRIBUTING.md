# Contributing to MedTime 💊

First off, thank you for considering contributing to MedTime! It's people like you that make MedTime such a great tool for medication management.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Process](#development-process)
- [Style Guidelines](#style-guidelines)
- [Commit Messages](#commit-messages)
- [Pull Request Process](#pull-request-process)

## Code of Conduct

This project and everyone participating in it is governed by our commitment to providing a welcoming and inspiring community for all. Please be respectful and constructive in all interactions.

### Our Standards

- Using welcoming and inclusive language
- Being respectful of differing viewpoints and experiences
- Gracefully accepting constructive criticism
- Focusing on what is best for the community
- Showing empathy towards other community members

## Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally
3. **Set up the development environment** (see README.md)
4. **Create a branch** for your changes
5. **Make your changes**
6. **Test your changes**
7. **Submit a pull request**

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Git
- Firebase account (for backend features)
- Gemini API key (for AI features)

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check existing issues to avoid duplicates. When you create a bug report, include as many details as possible:

- **Use a clear and descriptive title**
- **Describe the exact steps to reproduce the problem**
- **Provide specific examples**
- **Describe the behavior you observed** and what you expected
- **Include screenshots** if applicable
- **Note your environment** (Android version, device model, app version)

**Bug Report Template:**
```markdown
**Description:**
A clear and concise description of the bug.

**Steps to Reproduce:**
1. Go to '...'
2. Click on '...'
3. Scroll down to '...'
4. See error

**Expected Behavior:**
What you expected to happen.

**Actual Behavior:**
What actually happened.

**Screenshots:**
If applicable, add screenshots.

**Environment:**
- Device: [e.g., Pixel 6]
- Android Version: [e.g., Android 13]
- App Version: [e.g., 1.0.0]

**Additional Context:**
Any other context about the problem.
```

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion:

- **Use a clear and descriptive title**
- **Provide a detailed description** of the suggested enhancement
- **Explain why this enhancement would be useful**
- **List any similar features** in other apps if applicable
- **Include mockups or examples** if possible

### Your First Code Contribution

Unsure where to begin? You can start by looking through these issues:

- **Good First Issue** - Issues labeled as good for newcomers
- **Help Wanted** - Issues that need assistance
- **Documentation** - Improvements or additions to documentation

## Development Process

### 1. Setting Up Your Development Environment

```bash
# Fork and clone the repository
git clone https://github.com/YOUR-USERNAME/MedTime.git
cd MedTime

# Add upstream remote
git remote add upstream https://github.com/Riasat38/MedTime.git

# Install dependencies (done automatically by Gradle)
```

### 2. Create a Branch

```bash
# Update your local main branch
git checkout main
git pull upstream main

# Create a new branch
git checkout -b feature/your-feature-name
# or
git checkout -b fix/your-bug-fix
```

Branch naming conventions:
- `feature/` - New features
- `fix/` - Bug fixes
- `docs/` - Documentation changes
- `refactor/` - Code refactoring
- `test/` - Adding or updating tests
- `chore/` - Maintenance tasks

### 3. Make Your Changes

- Write clean, readable code
- Follow the existing code style
- Add comments for complex logic
- Update documentation if needed
- Write or update tests

### 4. Test Your Changes

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run lint checks
./gradlew lint
```

Ensure:
- All tests pass
- No new lint warnings
- App builds successfully
- Features work as expected on a real device or emulator

### 5. Commit Your Changes

```bash
git add .
git commit -m "Type: Brief description"
```

See [Commit Messages](#commit-messages) for guidelines.

### 6. Push to Your Fork

```bash
git push origin feature/your-feature-name
```

### 7. Open a Pull Request

- Go to your fork on GitHub
- Click "Compare & pull request"
- Fill in the PR template
- Submit the pull request

## Style Guidelines

### Kotlin Code Style

Follow the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// Good
class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    
    suspend fun getUser(userId: String): Result<User> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                Result.Success(document.toObject(User::class.java))
            } else {
                Result.Error("User not found")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
}
```

**Key Points:**
- Use 4 spaces for indentation (not tabs)
- Maximum line length: 120 characters
- Use meaningful variable and function names
- Prefer `val` over `var` when possible
- Use type inference when the type is obvious
- Add blank lines between logical sections

### Jetpack Compose Guidelines

```kotlin
// Good - Clear, composable function with proper naming
@Composable
fun MedicationCard(
    medication: Medication,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = medication.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = medication.dosage,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
```

**Best Practices:**
- Composable function names start with uppercase
- Use `Modifier` parameter for flexibility
- Provide default values for optional parameters
- Keep composables small and focused
- Use `remember` for state that survives recomposition
- Hoist state when necessary

### File Organization

```
FeatureName/
├── FeatureScreen.kt          # UI screen
├── FeatureViewModel.kt       # ViewModel
├── FeatureRepository.kt      # Data layer
└── components/
    ├── FeatureCard.kt        # Reusable components
    └── FeatureDialog.kt
```

### Documentation

- Add KDoc comments for public APIs
- Document complex algorithms
- Include usage examples for utilities

```kotlin
/**
 * Analyzes a prescription image using Google Gemini AI.
 *
 * @param imageBitmap The prescription image to analyze
 * @param context Additional context for the AI model
 * @return GeminiResult containing extracted medication information
 * @throws Exception if the analysis fails
 */
suspend fun extractMedicationInfo(
    imageBitmap: Bitmap,
    context: String = "Extract medication information"
): GeminiResult
```

## Commit Messages

Follow the conventional commits specification:

### Format
```
Type: Brief description (50 chars or less)

More detailed explanation if needed (wrap at 72 characters).
Include motivation for the change and contrast with previous behavior.

- Bullet points are okay
- Use present tense: "Add" not "Added"
- Reference issues: Fixes #123
```

### Types

- **Add**: New feature or functionality
- **Fix**: Bug fix
- **Update**: Update existing feature
- **Remove**: Remove code or files
- **Refactor**: Code refactoring (no functional changes)
- **Docs**: Documentation changes
- **Test**: Adding or updating tests
- **Chore**: Maintenance tasks (dependencies, config, etc.)
- **Style**: Code style/formatting changes
- **Perf**: Performance improvements

### Examples

```
Add: Medication intake tracking feature

Implement ability to mark medications as taken or skipped.
Includes UI updates and database schema changes.

Fixes #45
```

```
Fix: Notification not showing on Android 13+

Update notification channel configuration to handle
Android 13+ permission requirements.

Closes #78
```

## Pull Request Process

### Before Submitting

- [ ] Code follows the style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] No new warnings generated
- [ ] Tests added/updated and passing
- [ ] Manual testing completed
- [ ] Commits are clean and well-organized

### PR Template

When opening a PR, include:

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Changes Made
- Change 1
- Change 2
- Change 3

## Testing
- [ ] Unit tests pass
- [ ] Manual testing completed
- [ ] Tested on Android version: X.X

## Screenshots (if applicable)
Before: [image]
After: [image]

## Related Issues
Fixes #123
Relates to #456

## Checklist
- [ ] My code follows the project style guidelines
- [ ] I have performed a self-review
- [ ] I have commented my code where necessary
- [ ] I have updated the documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix/feature works
- [ ] New and existing tests pass locally
```

### Review Process

1. **Automated checks** must pass (build, tests, lint)
2. **Code review** by maintainers
3. **Address feedback** if requested
4. **Approval** from at least one maintainer
5. **Merge** by maintainers

### After Your PR is Merged

- Delete your branch (if not needed)
- Update your local repository
- Celebrate! 🎉

```bash
git checkout main
git pull upstream main
git branch -d feature/your-feature-name
```

## Questions?

Don't hesitate to ask questions:

- Open an issue labeled "question"
- Start a discussion on GitHub Discussions
- Check existing documentation

## Recognition

Contributors will be recognized in:
- README.md acknowledgments section
- Release notes
- Project website (when available)

---

Thank you for contributing to MedTime! Your efforts help make medication management easier and safer for everyone. 💙

