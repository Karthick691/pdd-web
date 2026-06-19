import urllib.request
import json

url = "https://api.github.com/repos/Karthick691/pdd-App/actions/runs/27527240578/jobs"
headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
}

req = urllib.request.Request(url, headers=headers)
try:
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode("utf-8"))
        for job in data.get("jobs", []):
            print(f"Job Name: {job.get('name')}")
            print(f"Job ID: {job.get('id')}")
            print(f"Status: {job.get('status')}")
            print(f"Conclusion: {job.get('conclusion')}")
            print(f"HTML URL: {job.get('html_url')}")
            print("-" * 50)
except Exception as e:
    print(f"Error fetching jobs: {e}")
