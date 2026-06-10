from palindrome import is_palindrome

def main():
    text = input("Enter text to check if it's a palindrome: ")
    if is_palindrome(text):
        print(f"'{text}' is a palindrome!")
    else:
        print(f"'{text}' is not a palindrome.")


if __name__ == "__main__":
    main()
