import urllib.request

url = "https://api.github.com/repos/Karthick691/pdd-App/actions/jobs/81357029356/logs"
headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
}

req = urllib.request.Request(url, headers=headers)
try:
    with urllib.request.urlopen(req) as response:
        content = response.read().decode("utf-8", errors="ignore")
        print("Success! Log length:", len(content))
        print("Log snippet:")
        print(content[-2000:])  # Print the end of the log
except Exception as e:
    print(f"Error fetching logs: {e}")
