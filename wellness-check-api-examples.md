# Wellness Check API - Example Requests

## 🔍 Quick Reference

Base URL: 
- Production: `https://whatsappintegration-cbhz.onrender.com`
- Local: `http://localhost:8080`

## 📨 Send Wellness Check

### Send to Single Recipient
```bash
curl -X POST https://whatsappintegration-cbhz.onrender.com/api/wellness/send \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+6591234567",
    "name": "Mary Johnson",
    "timeOfDay": "morning",
    "customMessage": "Good morning {name}! 🌅 Hope you slept well. How are you feeling today?"
  }'
```

### Send to All Enabled Recipients
```bash
curl -X POST https://whatsappintegration-cbhz.onrender.com/api/wellness/send-all
```

## 👥 Recipient Management

### Get All Recipients
```bash
curl https://whatsappintegration-cbhz.onrender.com/api/wellness/recipients
```

### Get Only Enabled Recipients
```bash
curl https://whatsappintegration-cbhz.onrender.com/api/wellness/recipients/enabled
```

### Get Recipients Due for Check
```bash
curl https://whatsappintegration-cbhz.onrender.com/api/wellness/recipients/due
```

### Add New Recipient
```bash
curl -X POST https://whatsappintegration-cbhz.onrender.com/api/wellness/recipients \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+6598765432",
    "name": "Grandma Lee",
    "preferredTimeOfDay": "morning",
    "customMessage": "Good morning {name}! 🌸 Hope you had a peaceful night.",
    "enabled": true,
    "timezone": "Asia/Singapore",
    "relationship": "grandmother",
    "notes": "Loves flower emojis and morning chats"
  }'
```

### Enable/Disable Recipient
```bash
# Enable
curl -X PUT "https://whatsappintegration-cbhz.onrender.com/api/wellness/recipients/%2B6591234567/enable?enabled=true"

# Disable
curl -X PUT "https://whatsappintegration-cbhz.onrender.com/api/wellness/recipients/%2B6591234567/enable?enabled=false"
```

Note: Phone number with '+' needs to be URL encoded as '%2B'

### Remove Recipient
```bash
curl -X DELETE https://whatsappintegration-cbhz.onrender.com/api/wellness/recipients/%2B6591234567
```

## 📊 Statistics & Monitoring

### Get Statistics
```bash
curl https://whatsappintegration-cbhz.onrender.com/api/wellness/stats
```

Response:
```json
{
  "totalRecipients": 10,
  "enabledRecipients": 5,
  "recipientsDueForCheck": 2,
  "schedulerEnabled": true,
  "nextCheckHours": [9, 14, 19]
}
```

### Health Check
```bash
curl https://whatsappintegration-cbhz.onrender.com/api/wellness/health
```

Response:
```json
{
  "status": "healthy",
  "service": "Wellness Check Service",
  "enabledRecipients": 5,
  "timestamp": 1705308000000
}
```

## 🔄 JavaScript/Fetch Examples

### Send Wellness Check (JavaScript)
```javascript
async function sendWellnessCheck() {
  const response = await fetch('https://whatsappintegration-cbhz.onrender.com/api/wellness/send', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      phoneNumber: '+6591234567',
      name: 'Mary Johnson',
      timeOfDay: 'morning',
      customMessage: 'Good morning {name}! Hope you are doing well today.'
    })
  });
  
  const data = await response.json();
  console.log('Wellness check sent:', data);
}
```

### Add Recipient (JavaScript)
```javascript
async function addRecipient() {
  const recipient = {
    phoneNumber: '+6598765432',
    name: 'John Smith',
    preferredTimeOfDay: 'evening',
    enabled: true,
    timezone: 'Asia/Singapore',
    relationship: 'uncle',
    notes: 'Prefers evening messages'
  };

  const response = await fetch('https://whatsappintegration-cbhz.onrender.com/api/wellness/recipients', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(recipient)
  });
  
  const data = await response.json();
  console.log('Recipient added:', data);
}
```

## 🐍 Python Examples

### Send Wellness Check (Python)
```python
import requests
import json

url = "https://whatsappintegration-cbhz.onrender.com/api/wellness/send"
payload = {
    "phoneNumber": "+6591234567",
    "name": "Mary Johnson",
    "timeOfDay": "morning",
    "customMessage": "Good morning {name}! Hope you're feeling great today!"
}

response = requests.post(url, json=payload)
print(f"Status: {response.status_code}")
print(f"Response: {response.json()}")
```

### Get All Recipients (Python)
```python
import requests

url = "https://whatsappintegration-cbhz.onrender.com/api/wellness/recipients"
response = requests.get(url)

recipients = response.json()
for recipient in recipients:
    print(f"{recipient['name']}: {recipient['phoneNumber']} - Enabled: {recipient['enabled']}")
```

## 📱 Postman Collection

Import this collection into Postman:

```json
{
  "info": {
    "name": "WhatsApp Wellness Check API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Send Wellness Check",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"phoneNumber\": \"+6591234567\",\n  \"name\": \"Mary Johnson\",\n  \"timeOfDay\": \"morning\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/wellness/send",
          "host": ["{{baseUrl}}"],
          "path": ["api", "wellness", "send"]
        }
      }
    },
    {
      "name": "Get All Recipients",
      "request": {
        "method": "GET",
        "url": {
          "raw": "{{baseUrl}}/api/wellness/recipients",
          "host": ["{{baseUrl}}"],
          "path": ["api", "wellness", "recipients"]
        }
      }
    },
    {
      "name": "Add Recipient",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"phoneNumber\": \"+6598765432\",\n  \"name\": \"John Smith\",\n  \"preferredTimeOfDay\": \"evening\",\n  \"enabled\": true,\n  \"timezone\": \"Asia/Singapore\",\n  \"relationship\": \"friend\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/wellness/recipients",
          "host": ["{{baseUrl}}"],
          "path": ["api", "wellness", "recipients"]
        }
      }
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "https://whatsappintegration-cbhz.onrender.com",
      "type": "string"
    }
  ]
}
```

## 🔐 Authentication (If Implemented)

If authentication is added in the future:

### With API Key
```bash
curl -H "X-API-Key: your-api-key" \
  https://whatsappintegration-cbhz.onrender.com/api/wellness/recipients
```

### With Bearer Token
```bash
curl -H "Authorization: Bearer your-jwt-token" \
  https://whatsappintegration-cbhz.onrender.com/api/wellness/recipients
```

## ⚠️ Important Notes

1. **Phone Number Format**: Always include country code with '+' (e.g., +6591234567)
2. **URL Encoding**: When using phone numbers in URLs, encode '+' as '%2B'
3. **Rate Limiting**: Allow 1-2 seconds between bulk operations
4. **Timezone**: Use standard timezone identifiers (e.g., "Asia/Singapore", "America/New_York")
5. **Custom Messages**: Use `{name}` placeholder for recipient name substitution

## 🧪 Test Scenarios

### Morning Wellness Check
```bash
curl -X POST https://whatsappintegration-cbhz.onrender.com/api/wellness/send \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+6591234567",
    "name": "Test User",
    "timeOfDay": "morning"
  }'
```

### Afternoon Check with Custom Message
```bash
curl -X POST https://whatsappintegration-cbhz.onrender.com/api/wellness/send \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+6591234567",
    "name": "Test User",
    "timeOfDay": "afternoon",
    "customMessage": "Hi {name}! 🌻 Just checking in this lovely afternoon. How has your day been so far?"
  }'
```

### Evening Check
```bash
curl -X POST https://whatsappintegration-cbhz.onrender.com/api/wellness/send \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+6591234567",
    "name": "Test User",
    "timeOfDay": "evening"
  }'
```