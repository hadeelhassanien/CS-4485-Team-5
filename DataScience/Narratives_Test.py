import pandas as pd
from transformers import pipeline

pipe = pipeline(
    "text-generation",
    model="Qwen/Qwen2.5-1.5B-Instruct",
    torch_dtype="auto",
    device_map="auto",
)

with open("claims_from_sarah.txt", "r", encoding="utf-8") as f:
    claims_text = f.read()

def chunk_text(text, chunk_size=800, overlap=100):
    words = text.split()
    chunks = []
    start = 0
    while start < len(words):
        end = start + chunk_size
        chunk = " ".join(words[start:end])
        chunks.append(chunk)
        start += chunk_size - overlap  # overlap keeps context between chunks
    return chunks

def process_claims_chunk(chunk, pipe):
    prompt = (
        "You are helping YouTube gamers improve their content.\n\n"
        "Given the factual claims below, convert them into upcoming trends and actionable recommendations, split into two sections \n"
        "Rules:\n"
        "- Output bullet points\n"
        "- Be concise\n"
        "- Merge similar ideas\n"
        "- Focus on advice (not raw claims)\n"
        "- Show creators what kind of trends has been seen in gaming videos coming up\n"
        "- Give creators advice on what type of games they should play (not suggestions to improve games)\n\n"
        f"Claims:\n{chunk}"
    )

    output = pipe(prompt, max_new_tokens=300)
    return output[0]["generated_text"]

def deduplicate_recommendations(text, pipe):
    prompt = (
        "Clean and deduplicate these recommendations and trends.\n"
        "Merge similar points and keep the clearest version.\n"
        "Return only bullet points.\n\n"
        f"{text}"
    )

    output = pipe(prompt, max_new_tokens=500)
    return output[0]["generated_text"]

def claim_extraction(text, pipe):
    chunks = chunk_text(text, chunk_size=800, overlap=100)
    all_claims = []

    for i, chunk in enumerate(chunks):
        print(f"  Chunk {i+1}/{len(chunks)}...", end=" ", flush=True)
        claims = extract_claims_from_chunk(chunk, pipe)
        all_claims.append(claims)
        print("done")

    combined = "\n".join(all_claims)

    if len(chunks) > 1:
        print("  Deduplicating...")
        combined = deduplicate_claims(combined, pipe)

    return combined

chunks = chunk_text(claims_text, chunk_size=800, overlap=100)

all_outputs = []

for i, chunk in enumerate(chunks):
    print(f"Chunk {i+1}/{len(chunks)}...")
    result = process_claims_chunk(chunk, pipe)
    all_outputs.append(result)

combined = "\n".join(all_outputs)

if len(chunks) > 1:
    print("Deduplicating...")
    combined = deduplicate_recommendations(combined, pipe)

print("\nFinal Recommendations:\n")
print(combined)