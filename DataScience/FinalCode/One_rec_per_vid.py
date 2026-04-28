from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import nest_asyncio
import httpx
import asyncio
import subprocess
import threading
import re
import socket
from transformers import pipeline, AutoTokenizer

nest_asyncio.apply()

# -------------------------------
# Config
# -------------------------------
INPUT_URL = "https://cyclist-frustrate-enlighten.ngrok-free.dev/api/narratives/page-data"

# -------------------------------
# Load Model
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
)
print("Model loaded!")

# -------------------------------
# Helper Functions
# -------------------------------
def extract_after_keyword(text: str, keyword: str) -> str:
    if keyword in text:
        return text.split(keyword, 1)[-1].strip()
    return text.strip()

def trim_to_first_sentence(text: str) -> str:
    idx = text.find(".")
    return text[:idx + 1].strip() if idx != -1 else text.strip()

def process_claims_chunk(chunk: str) -> str:
    prompt = (
        "You are helping YouTube gamers improve their content. You are not giving advice on how to improve games, you are giving advice on improving videos that gamers upload.\n\n"
        "Given the factual claims below, convert them into ONE single actionable recommendation.\n"
        "- Output exactly ONE bullet point ONLY. Do not add explanation or extra text.\n\n"
        f"Claims:\n{chunk}\n\nRecommendation:"
    )
    output = pipe(prompt, max_new_tokens=80)
    raw = output[0]["generated_text"]
    return trim_to_first_sentence(extract_after_keyword(raw, "Recommendation:"))

def fetch_and_process(data: dict) -> list:
    results = []
    for section in data.get("aiGameplayOverview", []):
        title = section.get("title", "")
        items = section.get("items", [])
        chunk = "\n".join(items)
        recommendation = process_claims_chunk(chunk)
        results.append({"title": title, "recommendation": recommendation})
    return results

def find_free_port():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind(('', 0))
        return s.getsockname()[1]

PORT = find_free_port()
print(f"Using port: {PORT}")
# -------------------------------
# FastAPI App
# -------------------------------
app = FastAPI(title="Claims API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
async def root():
    try:
        async with httpx.AsyncClient(timeout=30) as client:
            response = await client.get(INPUT_URL)
            response.raise_for_status()
            data = response.json()

        results = await asyncio.get_event_loop().run_in_executor(
            None, fetch_and_process, data
        )

        return {"results": results}

    except httpx.HTTPError as e:
        raise HTTPException(status_code=502, detail=f"Upstream fetch failed: {e}")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# -------------------------------
# Install + Start Cloudflared
# -------------------------------
def start_cloudflared():
    proc = subprocess.Popen(
        ["cloudflared", "tunnel", "--url", f"http://localhost:{PORT}"],
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT
    )
    for line in proc.stdout:
        line = line.decode()
        match = re.search(r"https://[a-z0-9\-]+\.trycloudflare\.com", line)
        if match:
            print("\n✅ Your OUTPUT URL:", match.group(0))
            break

threading.Thread(target=start_cloudflared, daemon=True).start()

config = uvicorn.Config(app, host="0.0.0.0", port=PORT, loop="asyncio")
server = uvicorn.Server(config)
await server.serve()