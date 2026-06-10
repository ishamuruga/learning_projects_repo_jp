# Create a function that checks whether a string is a palindrome.
# Ignore spaces, punctuation, and case.
# Return True if palindrome, otherwise False.

Main.py
# Create a command line program that asks the user for text
# and prints whether it is a palindrome.



tests/test_palindrome.py

# Write pytest test cases for the palindrome checker.
# Cover normal palindromes, mixed case, spaces,
# punctuation, empty strings, and negative cases.

uv add --dev pytest


uv run pytest

how to enable code coverage report

================================

# Project 04 - Python Utilities

A Python project with utility functions including palindrome checker and factorial calculator.

## Project Structure

- `main.py` - Command line program that prompts users to check if text is a palindrome
- `palindrome.py` - Palindrome checker function (ignores spaces, punctuation, and case)
- `factorial.py` - Factorial calculator function
- `tests/` - Test suite for all modules

## Installation

Install dependencies:
```bash
uv sync
```

Or manually add dev dependencies:
```bash
uv add --dev pytest pytest-cov
```

## Running Tests

### Run all tests
```bash
uv run pytest
```

### Run specific test file
```bash
uv run pytest tests/test_palindrome.py
uv run pytest tests/test_factorial.py
```

### Run with verbose output
```bash
uv run pytest -v
```

## Code Coverage

### Generate coverage report (HTML)
```bash
uv run pytest --cov=. --cov-report=html
```
Then open `htmlcov/index.html` in your browser.

### Display coverage in terminal
```bash
uv run pytest --cov=. --cov-report=term-missing
```

### Coverage for specific modules
```bash
uv run pytest --cov=palindrome --cov=factorial --cov-report=html
```

## Running the Program

Run the palindrome checker:
```bash
uv run python main.py
```