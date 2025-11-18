#!/bin/bash

# Send wellness check to configured recipients in recipients.json

# Configuration
BASE_URL="https://whatsappintegration-cbhz.onrender.com"
# For local testing, use: BASE_URL="http://localhost:8080"

echo "========================================="
echo "Sending Wellness Checks to Configured Recipients"
echo "========================================="
echo ""

# Method 1: Use the API endpoint that sends to all enabled recipients
echo "Sending to all enabled recipients via API..."
echo ""

curl -X POST "$BASE_URL/api/wellness/send-all" \
  -H "Content-Type: application/json"

echo ""
echo ""
echo "========================================="
echo "Alternative: Send Individual Messages"
echo "========================================="
echo ""

# Send to QC (first recipient)
echo "Sending morning wellness check to QC..."
curl -X POST "$BASE_URL/api/wellness/send" \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+6588098559",
    "name": "QC",
    "timeOfDay": "morning",
    "customMessage": "Good morning QC! 🌅 Hope you slept well and are ready for a beautiful day. How are you feeling today?"
  }'

echo ""
echo "Waiting 2 seconds..."
sleep 2

# Send to Velu (second recipient)
echo "Sending morning wellness check to Velu..."
curl -X POST "$BASE_URL/api/wellness/send" \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+6583824052",
    "name": "Velu",
    "timeOfDay": "morning",
    "customMessage": "Good morning Velu! 🌙 Hope you had a wonderful rest. Just checking in to see how you're doing today."
  }'

echo ""
echo ""
echo "========================================="
echo "Wellness checks sent successfully!"
echo "========================================="