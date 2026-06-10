import sys
import os
from pathlib import Path

# Add parent directory to path so we can import palindrome
sys.path.insert(0, str(Path(__file__).parent.parent))

import pytest
from palindrome import is_palindrome


class TestNormalPalindromes:
    """Tests for simple palindromes"""
    
    def test_single_character(self):
        assert is_palindrome("a")
    
    def test_simple_palindrome(self):
        assert is_palindrome("racecar")
    
    def test_two_same_characters(self):
        assert is_palindrome("aa")
    
    def test_three_character_palindrome(self):
        assert is_palindrome("aba")


class TestMixedCasePalindromes:
    """Tests for palindromes with mixed case"""
    
    def test_uppercase_palindrome(self):
        assert is_palindrome("RACECAR")
    
    def test_mixed_case_palindrome(self):
        assert is_palindrome("RaCeCaR")
    
    def test_mixed_case_example(self):
        assert is_palindrome("Madam")


class TestPalindromesWithSpaces:
    """Tests for palindromes containing spaces"""
    
    def test_palindrome_with_leading_space(self):
        assert is_palindrome(" racecar")
    
    def test_palindrome_with_trailing_space(self):
        assert is_palindrome("racecar ")
    
    def test_palindrome_with_internal_spaces(self):
        assert is_palindrome("race car")
    
    def test_palindrome_with_multiple_spaces(self):
        assert is_palindrome("a b c b a")


class TestPalindromesWithPunctuation:
    """Tests for palindromes with punctuation"""
    
    def test_palindrome_with_comma(self):
        assert is_palindrome("A man, a plan, a canal, Panama")
    
    def test_palindrome_with_apostrophe(self):
        assert is_palindrome("No 'x' in Nixon")
    
    def test_palindrome_with_exclamation(self):
        assert is_palindrome("Never odd or even!")
    
    def test_palindrome_with_mixed_punctuation(self):
        assert is_palindrome("Was it a car or a cat I saw?")
    
    def test_palindrome_with_hyphens(self):
        assert is_palindrome("A-man-a-plan-a-canal-Panama")


class TestEmptyAndWhitespace:
    """Tests for edge cases with empty strings and whitespace"""
    
    def test_empty_string(self):
        assert is_palindrome("")
    
    def test_single_space(self):
        assert is_palindrome(" ")
    
    def test_multiple_spaces_only(self):
        assert is_palindrome("   ")
    
    def test_whitespace_and_punctuation_only(self):
        assert is_palindrome(" . , ! ")


class TestNegativeCases:
    """Tests for non-palindromes"""
    
    def test_simple_non_palindrome(self):
        assert not is_palindrome("hello")
    
    def test_almost_palindrome(self):
        assert not is_palindrome("racecar1")
    
    def test_reversed_but_not_palindrome(self):
        assert not is_palindrome("abc")
    
    def test_non_palindrome_with_spaces(self):
        assert not is_palindrome("hello world")
    
    def test_non_palindrome_with_punctuation(self):
        assert not is_palindrome("Hello, World!")
    
    def test_completely_different_strings(self):
        assert not is_palindrome("python")
    
    def test_partially_palindromic(self):
        assert not is_palindrome("abcde")


class TestComplexCases:
    """Tests for complex real-world examples"""
    
    def test_dog_god(self):
        assert is_palindrome("A Santa at NASA")
    
    def test_numbers_as_text(self):
        assert is_palindrome("12321")
    
    def test_mixed_alphanumeric(self):
        assert is_palindrome("A1B2B1A")
    
    def test_phrase_with_numbers(self):
        assert not is_palindrome("ABA12")
        # assert is_palindrome("One1 two 2 three 3 two 2 one1")
