#!/bin/bash

# Test Groq API directly
# You need to set your GROQ_API_KEY environment variable

if [ -z "$GROQ_API_KEY" ]; then
    echo "Please set GROQ_API_KEY environment variable"
    echo "export GROQ_API_KEY=gsk_..."
    exit 1
fi

echo "Testing Groq API with key: ${GROQ_API_KEY:0:10}..."
echo ""

# Test with the current model
curl -X POST "https://api.groq.com/openai/v1/chat/completions" \
  -H "Authorization: Bearer $GROQ_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {
        "role": "system",
        "content": "You are a helpful assistant."
      },
      {
        "role": "user",
        "content": "Hello, how are you? Please respond in one sentence."
      }
    ],
    "model": "llama-3.3-70b-versatile",
    "max_tokens": 100,
    "temperature": 0.8
  }' | jq .

echo ""
echo "Testing complete!"