# 🌟 Wellness Check Feature - Automated Daily Care

## Overview

Your WhatsApp Integration API now includes a **Wellness Check** feature that automatically sends personalized care messages to elderly recipients on a daily schedule. Perfect for family members, caregivers, and support networks to stay connected!

## ✨ Key Features

### 🤖 **Automated Scheduling**
- **Daily wellness checks** at configurable times
- **Smart timing**: Morning (9 AM), Afternoon (2 PM), Evening (7 PM)
- **Personalized schedules** per recipient
- **Time zone support** for global recipients

### 👥 **Recipient Management**
- **JSON configuration** for easy recipient management
- **Individual preferences** (name, preferred time, custom messages)
- **Enable/disable** recipients easily
- **Relationship tracking** (family, friend, neighbor, etc.)

### 💬 **Personalized Messages**
- **Uses recipient names** in all messages
- **Time-appropriate greetings** (morning/afternoon/evening)
- **Custom messages** per recipient
- **Emoji support** for warm, friendly tone
- **Variety of message templates** to avoid repetition

### 🔧 **Management APIs**
- **Manual send** wellness checks
- **Bulk send** to all enabled recipients
- **Add/remove/manage** recipients via API
- **Real-time statistics** and monitoring

## 📋 **API Endpoints**

### **Send Single Wellness Check**
```http
POST /api/wellness/send
Content-Type: application/json

{
  "phoneNumber": "+1234567890",
  "name": "Mary",
  "customMessage": "Good morning {name}! Hope you're doing well today. 💖",
  "timeOfDay": "morning"
}
```

### **Send to All Enabled Recipients**
```http
POST /api/wellness/send-all
```

### **Manage Recipients**
```http
# Get all recipients
GET /api/wellness/recipients

# Get enabled recipients only
GET /api/wellness/recipients/enabled

# Add new recipient
POST /api/wellness/recipients
{
  "phoneNumber": "+1234567890",
  "name": "Mary Johnson",
  "preferredTimeOfDay": "morning",
  "customMessage": "Good morning {name}! 🌅 Hope you slept well!",
  "enabled": true,
  "timezone": "America/New_York",
  "relationship": "daughter",
  "notes": "Loves morning conversations"
}

# Enable/disable recipient
PUT /api/wellness/recipients/+1234567890/enable?enabled=true

# Remove recipient
DELETE /api/wellness/recipients/+1234567890
```

### **Statistics & Monitoring**
```http
GET /api/wellness/stats
GET /api/wellness/health
```

## ⚙️ **Configuration Setup**

### **1. Update Recipients List**

Edit `src/main/resources/recipients.json`:

```json
[
  {
    "phoneNumber": "+65XXXXXXXX",
    "name": "Grandma Lee",
    "preferredTimeOfDay": "morning", 
    "customMessage": "Good morning {name}! 🌸 Hope you slept peacefully. How are you feeling today?",
    "enabled": true,
    "timezone": "Asia/Singapore",
    "relationship": "grandmother", 
    "notes": "Loves morning chats and flower emojis"
  },
  {
    "phoneNumber": "+1XXXXXXXXX",
    "name": "Uncle Bob",
    "preferredTimeOfDay": "evening",
    "customMessage": null,
    "enabled": true,
    "timezone": "America/New_York",
    "relationship": "uncle",
    "notes": "Prefers evening check-ins, use default messages"
  }
]
```

### **2. Configure Timing (Optional)**

In `application.properties`:

```properties
# Wellness Check Schedule
wellness.scheduler.enabled=true
wellness.scheduler.morning-hour=9    # 9 AM
wellness.scheduler.afternoon-hour=14 # 2 PM  
wellness.scheduler.evening-hour=19   # 7 PM
```

### **3. For Render Deployment**

Add environment variables:

```
WELLNESS_SCHEDULER_ENABLED=true
WELLNESS_MORNING_HOUR=9
WELLNESS_AFTERNOON_HOUR=14
WELLNESS_EVENING_HOUR=19
```

## 💬 **Sample Messages**

### **Morning Messages**
- "Good morning {name}! 🌅 Hope you slept well. How are you feeling today?"
- "Morning {name}! ☀️ Wishing you a wonderful day ahead. How are you doing?"
- "Hello {name}! 🌞 Hope you're having a lovely morning. How are you today?"

### **Afternoon Messages** 
- "Good afternoon {name}! 🌤️ Hope your day is going well. How are you feeling?"
- "Hello {name}! 😊 Just wanted to check in this afternoon. How has your day been?"
- "Hi {name}! 🌻 Hope you're having a pleasant afternoon. How are you doing?"

### **Evening Messages**
- "Good evening {name}! 🌙 Hope you had a wonderful day. How are you feeling?"
- "Evening {name}! ⭐ Just checking in to see how your day went. How are you?"
- "Hello {name}! 🌆 Hope your evening is peaceful. How has your day been?"

## 🕐 **How Scheduling Works**

1. **Hourly Check**: Every hour, the system checks if it's time to send wellness messages
2. **Time Matching**: Compares current time with configured morning/afternoon/evening hours
3. **Recipient Filtering**: Only sends to recipients who:
   - Are **enabled**
   - Haven't received a message in the **last 20 hours** 
   - Have **matching time preference** (or no preference)
4. **Personalized Delivery**: Sends appropriate message with recipient's name
5. **Rate Limiting**: 2-second delay between messages to avoid WhatsApp limits

## 🎯 **Use Cases**

### **Family Care**
- Daily check-ins with elderly parents/grandparents
- Medication reminders with caring tone
- Holiday and special occasion greetings

### **Professional Caregiving**
- Wellness checks for care home residents  
- Client check-ins for home care services
- Community support network coordination

### **Friendship Networks**
- Senior community mutual support
- Regular connection for isolated individuals
- Group care coordination for neighbors

## 📊 **Monitoring & Statistics**

### **GET /api/wellness/stats** Response:
```json
{
  "totalRecipients": 5,
  "enabledRecipients": 3, 
  "recipientsDueForCheck": 1,
  "schedulerEnabled": true,
  "nextCheckHours": [9, 14, 19]
}
```

### **Logs to Monitor:**
- `"Starting X wellness checks at Y"` - Scheduled run
- `"Sent wellness check to Name (phone): messageId"` - Successful send
- `"Failed to send wellness check to Name"` - Delivery failure
- `"Wellness check scheduler is disabled"` - Configuration check

## 🛡️ **Safety Features**

### **Privacy Protection**
- Recipients disabled by default
- Phone numbers in configuration files only
- No persistent storage of message content

### **Rate Limiting**
- 2-second delays between messages
- Hourly scheduling prevents spam
- 20-hour cooldown between messages per recipient

### **Error Handling**
- Graceful failure for individual messages
- Continues processing remaining recipients
- Detailed error logging for troubleshooting

## 🚀 **Deployment Steps**

### **1. Configure Recipients**
```bash
# Edit the recipients file
vim src/main/resources/recipients.json

# Add real phone numbers and names
# Set "enabled": true for recipients you want to activate
```

### **2. Deploy to Render**
```bash
git add .
git commit -m "Add wellness check feature for daily care messages"
git push

# Add environment variables in Render dashboard:
# WELLNESS_SCHEDULER_ENABLED=true
```

### **3. Test the Feature**
```bash
# Test single message
curl -X POST https://your-app.onrender.com/api/wellness/send \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+your-number",
    "name": "Test User", 
    "timeOfDay": "morning"
  }'

# Check statistics
curl https://your-app.onrender.com/api/wellness/stats
```

## 🔧 **Customization Options**

### **Message Templates**
Modify message arrays in `WellnessCheckService.java`:
- Add more message variations
- Include cultural/language preferences
- Add seasonal or holiday messages

### **Scheduling Frequency**
Change cron expression in `@Scheduled` annotation:
- `"0 0 */1 * * *"` - Every hour (current)
- `"0 0 9,14,19 * * *"` - Only at 9 AM, 2 PM, 7 PM
- `"0 0 10 * * *"` - Only at 10 AM daily

### **Time Zones**
Recipients can have individual time zones:
- Messages sent based on recipient's local time
- Supports all standard timezone identifiers
- Future enhancement for timezone-aware scheduling

## 💡 **Pro Tips**

1. **Start Small**: Enable 1-2 recipients first to test
2. **Personalize Messages**: Custom messages get better responses  
3. **Monitor Logs**: Check Render logs for delivery confirmation
4. **Family Coordination**: Share API with family members for manual sends
5. **Backup Plans**: Keep alternative contact methods for emergencies

## 🆘 **Troubleshooting**

### **Messages Not Sending**
- Check `wellness.scheduler.enabled=true`
- Verify recipients have `"enabled": true`
- Confirm WhatsApp API credentials are working
- Check if current time matches configured hours

### **Wrong Timing**
- Verify timezone settings in recipients.json
- Check server timezone (Render uses UTC)
- Adjust hour settings for your timezone

### **API Errors**
- Validate phone number format (+country code)
- Ensure recipient names don't contain special characters
- Check WhatsApp Business API rate limits

The wellness check feature transforms your WhatsApp bot into a caring companion that never forgets to check on the people who matter most! 💝