import os
import requests
from openai import OpenAI

MAX_DIFF_LENGTH = 12000  # prevent token overflow


def load_diff() -> str:
    with open("diff.txt", "r", encoding="utf-8") as diff_file:
        diff = diff_file.read().strip()

    if len(diff) > MAX_DIFF_LENGTH:
        print("Diff too large, truncating...")
        diff = diff[:MAX_DIFF_LENGTH]

    return diff


def generate_review(code_diff: str) -> str:
    try:
        client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

        response = client.chat.completions.create(
            model="gpt-4.1-mini",
            messages=[
                {
                    "role": "system",
                    "content": (
                        "You are a strict code reviewer. Return concise findings with severity, "
                        "impact, and suggested fix. If there are no issues, say so clearly."
                    ),
                },
                {
                    "role": "user",
                    "content": (
                        "Review this pull request diff for correctness, bugs, regressions, "
                        f"maintainability, and missing tests.\n\n{code_diff}"
                    ),
                },
            ],
        )

        return response.choices[0].message.content or "No review content returned."

    except Exception as e:
        return f"AI review failed: {str(e)}"


def post_pr_comment(review_text: str) -> None:
    repository = os.getenv("GITHUB_REPOSITORY")
    pr_number = os.getenv("PR_NUMBER")
    token = os.getenv("GITHUB_TOKEN")

    if not repository or not pr_number or not token:
        print("Missing GitHub context. Review output:\n")
        print(review_text)
        return

    url = f"https://api.github.com/repos/{repository}/issues/{pr_number}/comments"

    body = {
        "body": f"## 🤖 AI Code Review\n\n{review_text}"
    }

    headers = {
        "Accept": "application/vnd.github+json",
        "Authorization": f"Bearer {token}",
    }

    try:
        response = requests.post(url, json=body, headers=headers, timeout=30)
        response.raise_for_status()
        print("Posted AI review to the pull request.")
    except Exception as e:
        print("Failed to post comment:", str(e))


def main() -> None:
    code_diff = load_diff()

    if not code_diff:
        print("No diff content found. Skipping review.")
        return

    review_text = generate_review(code_diff)
    post_pr_comment(review_text)


if __name__ == "__main__":
    main()