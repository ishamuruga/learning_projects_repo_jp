# Calculate factorial of a number
# Factorial of n (denoted as n!) is the product of all positive integers less than or equal to n
# Example: 5! = 5 × 4 × 3 × 2 × 1 = 120

def factorial(n):
    """
    Calculate the factorial of a non-negative integer n.
    
    Args:
        n: A non-negative integer
        
    Returns:
        The factorial of n
        
    Raises:
        ValueError: If n is negative or not an integer
    """
    if not isinstance(n, int):
        raise ValueError("Input must be an integer")
    
    if n < 0:
        raise ValueError("Factorial is not defined for negative numbers")
    
    if n == 0 or n == 1:
        return 1
    
    result = 1
    for i in range(2, n + 1):
        result *= i
    
    return result


# Test cases
if __name__ == "__main__":
    print(f"factorial(0) = {factorial(0)}")    # 1
    print(f"factorial(1) = {factorial(1)}")    # 1
    print(f"factorial(5) = {factorial(5)}")    # 120
    print(f"factorial(10) = {factorial(10)}")  # 3628800
