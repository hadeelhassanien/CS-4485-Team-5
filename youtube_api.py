
import os
from dotenv import load_dotenv
from googleapiclient.discovery import build
import json
import requests
import pandas as pd

load_dotenv()

api_key= os.getenv("API_KEY")


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

df.to_excel("initial_analysis.xlsx", index=False)

print("saved")
