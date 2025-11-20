import os
import subprocess
import json
from textwrap import dedent
from github import Github
import google.generativeai as genai

def run(cmd: str) -> str:
    result = subprocess.run(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    return result.stdout.strip()

def get_pr_info():
    event_path = os.environ["GITHUB_EVENT_PATH"]
    with open(event_path, "r") as f:
        event = json.load(f)

    pr_number = event["number"]
    repo_full_name = event["repository"]["full_name"]
    base_sha = event["pull_request"]["base"]["sha"]
    head_sha = event["pull_request"]["head"]["sha"]
    return pr_number, repo_full_name, base_sha, head_sha

def get_diff(base_sha, head_sha):
    diff = run(f"git diff {base_sha}...{head_sha}")
    return diff

def call_gemini_for_review(diff: str):
    genai.configure(api_key=os.environ["GEMINI_API_KEY"])

    prompt = dedent(f"""
    You are a senior backend engineer doing a code review.
    Review the following git diff:

    ```diff
    {diff}
    ```

    Provide:
    - Potential bugs
    - Performance issues
    - Security risks
    - Readability improvements
    - Testing considerations

    Respond in Markdown with clear bullet points.
    """).strip()

    model = genai.GenerativeModel("gemini-1.5-flash")
    response = model.generate_content(prompt)
    return response.text

def post_comment(repo_full_name, pr_number, review_text):
    gh = Github(os.environ["GITHUB_TOKEN"])
    repo = gh.get_repo(repo_full_name)
    pr = repo.get_pull(pr_number)
    pr.create_issue_comment(f"🤖 **Gemini 코드 리뷰 결과**\n\n{review_text}")

def main():
    pr_number, repo_full_name, base_sha, head_sha = get_pr_info()
    diff = get_diff(base_sha, head_sha)

    if not diff.strip():
        print("No diff to review.")
        return

    review = call_gemini_for_review(diff)
    post_comment(repo_full_name, pr_number, review)
    print("Review posted.")

if __name__ == "__main__":
    main()