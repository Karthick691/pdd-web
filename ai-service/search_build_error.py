import json
import os

transcript_path = r"C:\Users\karth\.gemini\antigravity-ide\brain\1ed09a4b-0ea3-4b0f-92ce-bbbe93a5e182\.system_generated\logs\transcript.jsonl"

if os.path.exists(transcript_path):
    print("Transcript exists. Searching...")
    with open(transcript_path, "r", encoding="utf-8") as f:
        for i, line in enumerate(f):
            try:
                obj = json.loads(line)
                content = str(obj.get("content", ""))
                # Check for build failure keywords
                keywords = ["assembleDebug", "Build with Gradle", "FAILED", "exit code 1", "gradlew"]
                if any(kw.lower() in content.lower() for kw in keywords):
                    print(f"\n--- Line {i} matches ---")
                    print(f"Source: {obj.get('source')}, Type: {obj.get('type')}")
                    # Print content, up to 1000 characters
                    print(content[:1000])
                    print("-" * 40)
            except Exception as e:
                pass
else:
    print("Transcript does not exist.")
