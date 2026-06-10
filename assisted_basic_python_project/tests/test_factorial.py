import sys
import os
from pathlib import Path

# Add parent directory to path so we can import factorial
sys.path.insert(0, str(Path(__file__).parent.parent))

import pytest
from factorial import factorial


class TestHappyPath:
    """Tests for valid factorial calculations (happy path)"""
    
    def test_factorial_of_one(self):
        assert factorial(1) == 1
    
    def test_factorial_of_two(self):
        assert factorial(2) == 2
    
    def test_factorial_of_three(self):
        assert factorial(3) == 6
    
    def test_factorial_of_five(self):
        assert factorial(5) == 120
    
    def test_factorial_of_ten(self):
        assert factorial(10) == 3628800
    
    def test_factorial_of_fifteen(self):
        assert factorial(15) == 1307674368000


class TestEdgeValues:
    """Tests for edge cases"""
    
    def test_factorial_of_zero(self):
        """0! should equal 1 by mathematical definition"""
        assert factorial(0) == 1
    
    def test_factorial_of_one_is_one(self):
        """1! should equal 1"""
        assert factorial(1) == 1


class TestBoundaryValues:
    """Tests for boundary conditions"""
    
    def test_factorial_of_large_number(self):
        """Test with a reasonably large number"""
        assert factorial(20) == 2432902008176640000
    
    def test_factorial_of_very_large_number(self):
        """Test with a very large number (Python handles big integers)"""
        result = factorial(100)
        assert result == 93326215443944152681699238856266700490715968264381621468592963895217599993229915608941463976156518286253697920827223758251185210916864000000000000000000000000
    
    def test_factorial_sequential_values(self):
        """Test several sequential values"""
        assert factorial(4) == 24
        assert factorial(5) == 120
        assert factorial(6) == 720


class TestUnhappyPathInvalidTypes:
    """Tests for invalid input types (unhappy path)"""
    
    def test_factorial_with_float(self):
        """Should raise ValueError for float input"""
        with pytest.raises(ValueError):
            factorial(5.5)
    
    def test_factorial_with_string(self):
        """Should raise ValueError for string input"""
        with pytest.raises(ValueError):
            factorial("5")
    
    def test_factorial_with_none(self):
        """Should raise ValueError for None input"""
        with pytest.raises(ValueError):
            factorial(None)
    
    def test_factorial_with_list(self):
        """Should raise ValueError for list input"""
        with pytest.raises(ValueError):
            factorial([5])
    
    def test_factorial_with_dict(self):
        """Should raise ValueError for dict input"""
        with pytest.raises(ValueError):
            factorial({"n": 5})


class TestUnhappyPathNegativeNumbers:
    """Tests for negative numbers (unhappy path)"""
    
    def test_factorial_with_negative_one(self):
        """Should raise ValueError for negative input"""
        with pytest.raises(ValueError):
            factorial(-1)
    
    def test_factorial_with_negative_five(self):
        """Should raise ValueError for negative input"""
        with pytest.raises(ValueError):
            factorial(-5)
    
    def test_factorial_with_large_negative_number(self):
        """Should raise ValueError for large negative input"""
        with pytest.raises(ValueError):
            factorial(-1000)


class TestMathematicalProperties:
    """Tests to verify mathematical properties of factorial"""
    
    def test_factorial_property_n_equals_n_minus_1_times_n(self):
        """Test that n! = (n-1)! × n"""
        assert factorial(6) == factorial(5) * 6
        assert factorial(10) == factorial(9) * 10
    
    def test_factorial_is_always_positive(self):
        """Factorial should always return a positive number"""
        for i in range(0, 11):
            assert factorial(i) > 0
    
    def test_factorial_increases_monotonically(self):
        """Each factorial should be greater than or equal to the previous"""
        prev = factorial(0)
        for i in range(1, 11):
            curr = factorial(i)
            assert curr >= prev
            prev = curr
