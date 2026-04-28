
#pip install transformers accelerate huggingface_hub
#pip install fastapi nest-asyncio pyngrok uvicorn

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
from pyngrok import ngrok
import nest_asyncio

import json
from transformers import pipeline, AutoTokenizer

nest_asyncio.apply()

# -------------------------------
# FastAPI Setup
# -------------------------------
app = FastAPI(title="Claims API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# -------------------------------
# Load Model ONCE (important)
# -------------------------------
print("Loading model...")

tokenizer = AutoTokenizer.from_pretrained(
    "Qwen/Qwen2.5-1.5B-Instruct",
    padding_side='left'
)

pipe = pipeline(
    "text-generation",
    model="Qwen/Qwen2.5-1.5B-Instruct",
    tokenizer=tokenizer,
    torch_dtype="auto",
    device_map="auto",
    max_new_tokens=200
)

print("Model loaded!")

# -------------------------------
# Utility Functions
# -------------------------------
def chunk_text(text, chunk_size=800, overlap=100):
    words = text.split()
    chunks = []
    start = 0
    while start < len(words):
        chunks.append(" ".join(words[start:start + chunk_size]))
        start += chunk_size - overlap
    return chunks

def extract_after_keyword(text, keywords):
    if isinstance(keywords, str):
        keywords = [keywords]

    best_idx = -1
    for keyword in keywords:
        idx = text.rfind(keyword)
        if idx > best_idx:
            best_idx = idx

    if best_idx != -1:
        return text[best_idx + len(keyword):].strip()
    return text.strip()

def process_claims_chunk(chunk):
    prompt = (
        "You are helping YouTube gamers improve their content.\n\n"
        "Given the factual claims below, convert them into actionable recommendations.\n"
        "- Output bullet points ONLY\n\n"
        f"Claims:\n{chunk}\n\nRecommendations:"
    )
    output = pipe(prompt, max_new_tokens=300)
    raw = output[0]["generated_text"]
    return extract_after_keyword(raw, "Recommendations:")

def process_claims_trends_chunk(chunk):
    prompt = (
        "You are helping YouTube gamers improve their content.\n\n"
        "Identify upcoming trends.\n\n"
        f"Claims:\n{chunk}\n\nTrends:"
    )
    output = pipe(prompt, max_new_tokens=300)
    raw = output[0]["generated_text"]
    return extract_after_keyword(raw, "Trends:")

def claim_extraction(text):
    chunks = chunk_text(text)
    all_recommendations = []
    all_trends = []

    for chunk in chunks:
        all_recommendations.append(process_claims_chunk(chunk))
        all_trends.append(process_claims_trends_chunk(chunk))

    results = []

    for line in "\n".join(all_recommendations).splitlines():
        if line.strip():
            results.append({"type": "recommendation", "content": line.strip()})

    for line in "\n".join(all_trends).splitlines():
        if line.strip():
            results.append({"type": "trend", "content": line.strip()})

    return results

# -------------------------------
# API Endpoint
# -------------------------------
@app.get("/")
async def root():
    with open("video_claims_new.json", "r") as f:
        data = json.load(f)

    all_claims = " \n".join(
        claim for item in data for claim in item["claims"]
    )

    results = claim_extraction(all_claims)
    return {"results": results}


# -------------------------------
# Start Server + Ngrok
# -------------------------------
ngrok.set_auth_token(NGROK_TOKEN)

public_url = ngrok.connect(8000)
print("Public URL:", public_url)

config = uvicorn.Config(app, host="0.0.0.0", port=8000)
server = uvicorn.Server(config)

await server.serve()