from youtube_transcript_api import YouTubeTranscriptApi
from transformers import pipeline
import os
from dotenv import load_dotenv
from googleapiclient.discovery import build
import json
import requests
import pandas as pd
import re

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
    
def duration_to_seconds(duration):
    pattern = r'PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?'
    match = re.match(pattern, duration)

    h = int(match.group(1)) if match.group(1) else 0
    m = int(match.group(2)) if match.group(2) else 0
    s = int(match.group(3)) if match.group(3) else 0

    return h*3600 + m*60 + s

youtube = build('youtube', 'v3', developerKey=api_key)

request = youtube.videos().list(
    part='localizations,statistics,topicDetails,contentDetails',
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

    video_duration = item.get("contentDetails",{}).get("duration")

    videos_data.append({
        "Video ID": video_id,
        "Video Title": title,
        "Description": description,
        "Views": view_count,
        "Likes": like_count,
        "Number of Comments": comment_count,
        "Topic Categories": topics_str,
        "Duration": duration_to_seconds(video_duration)
    
    })


df = pd.DataFrame(videos_data)
df["Transcript"] = df["Video ID"].apply(get_transcript)
df.to_excel("initial_analysis.xlsx", index=False)

print("saved")
