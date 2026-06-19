import urllib.request
import json

url = "https://api.github.com/repos/Karthick691/pdd-App/actions/runs?per_page=5"
headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
}

req = urllib.request.Request(url, headers=headers)
try:
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode("utf-8"))
        for run in data.get("workflow_runs", []):
            print(f"Run ID: {run.get('id')}")
            print(f"Event: {run.get('event')}")
            print(f"Status: {run.get('status')}")
            print(f"Conclusion: {run.get('conclusion')}")
            print(f"Commit Message: {run.get('head_commit', {}).get('message')}")
            print(f"URL: {run.get('html_url')}")
            print("-" * 50)
except Exception as e:
    print(f"Error fetching runs: {e}")
