from youtube_transcript_api import YouTubeTranscriptApi
from transformers import pipeline
import os
from dotenv import load_dotenv
from googleapiclient.discovery import build
import json
import requests
import pandas as pd

load_dotenv()

api_key= os.getenv("API_KEY")

yta = YouTubeTranscriptApi()

def get_transcript(video_id):
    try:
        transcript = yta.fetch(video_id)
        transcript_text = " ".join(entry.text for entry in transcript)
        return transcript_text
    except Exception:
        return None

youtube = build('youtube', 'v3', developerKey=api_key)

request = youtube.videos().list(
    part='localizations,statistics,topicDetails',
    chart='mostPopular',
    regionCode='us',
    videoCategoryId='20',
    maxResults='50'

)

response = request.execute()

videos_data = []

for item in response["items"]:
    video_id = item.get("id")

    stats = item.get("statistics")
    view_count = stats.get("viewCount")
    like_count = stats.get("likeCount")
    comment_count = stats.get("commentCount")

    topics = item.get("topicDetails", {}).get("topicCategories", [])
    topics_str = ", ".join(topics)

    localization = item.get("localizations",{}).get("en", {})
    title = localization.get("title")
    description = localization.get("description")

    videos_data.append({
        "Video ID": video_id,
        "Video Title": title,
        "Description": description,
        "Views": view_count,
        "Likes": like_count,
        "Number of Comments": comment_count,
        "Topic Categories": topics_str
    
    })


df = pd.DataFrame(videos_data)
df["Transcript"] = df["Video ID"].apply(get_transcript)
df.to_excel("initial_analysis.xlsx", index=False)

print("saved")


pipe = pipeline(
    "text-generation",
    model="Qwen/Qwen2.5-1.5B-Instruct",
    torch_dtype="auto",
    device_map="auto",
    max_new_tokens=100
)

sample_data = df['Video Title'].str.cat()


# Function to extract claims
def claim_extraction(text):
    # Create message
    messages = [
        {"role": "system", "content": "You are a helpful assistant that extracts factual claims from text."},
        {"role": "user", "content": f"Extract all factual claims as bullet points.\n\nText: {text}"}
    ]
    # Send message to Qwen model
    output = pipe(messages, max_length=None)
    # Get claim from Qwen response
    result = output[0]["generated_text"][-1]["content"]
    return result

# Test extract claims for sample data
print(f"Claims Extracted:\n{claim_extraction(sample_data)}\n")
