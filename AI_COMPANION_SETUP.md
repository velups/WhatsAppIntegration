# AI Companion Feature - WhatsApp Integration

## Overview

Your WhatsApp Integration API now includes an AI-powered companion feature that provides warm, empathetic conversations with elderly users. The system greets users with "Hello Aunty, How are you doing today?" and maintains contextual conversations using OpenAI's GPT models.

## Key Features

### 1. Personalized Greetings
- First-time users receive a warm greeting: "Hello Aunty! 🌺 How are you doing today?"
- The AI remembers conversation context for ongoing chats

### 2. Empathetic AI Companion
- Designed specifically for elderly users
- Provides emotional support and companionship
- Asks thoughtful follow-up questions
- Shows genuine interest in their well-being
- Uses simple, clear language

### 3. Conversation Memory
- Maintains conversation history for 24 hours
- Remembers context across multiple messages
- Supports up to 20 messages per conversation

## Setup Instructions

### 1. Get OpenAI API Key

1. Sign up at [OpenAI Platform](https://platform.openai.com/)
2. Navigate to [API Keys](https://platform.openai.com/api-keys)
3. Create a new API key
4. Copy the key for configuration

### 2. Local Development Setup

Update `application.properties`:

```properties
# Enable AI features
whatsapp.ai.enabled=true

# OpenAI Configuration
openai.api.key=YOUR_OPENAI_API_KEY_HERE
openai.model=gpt-3.5-turbo  # or gpt-4 for better responses
openai.max-tokens=500
openai.temperature=0.8  # Higher = more creative, Lower = more focused
```

### 3. Render Deployment Setup

Add these environment variables in your Render dashboard:

| Variable | Description | Example |
|----------|-------------|---------|
| `OPENAI_API_KEY` | Your OpenAI API key | `sk-...` |
| `OPENAI_MODEL` | GPT model to use | `gpt-3.5-turbo` or `gpt-4` |
| `OPENAI_MAX_TOKENS` | Max response length | `500` |
| `OPENAI_TEMPERATURE` | Response creativity (0-1) | `0.8` |
| `WHATSAPP_AI_ENABLED` | Enable/disable AI | `true` |

### 4. Test the Application Locally

```bash
# Set environment variable for local testing
export OPENAI_API_KEY=your_api_key_here

# Build and run
mvn clean install
mvn spring-boot:run
```

## AI Model Options

### GPT-3.5-Turbo (Recommended for cost-effectiveness)
- Fast responses (~1-2 seconds)
- Good conversational quality
- Cost: ~$0.002 per 1K tokens
- Suitable for most companion conversations

### GPT-4 (Premium quality)
- Superior understanding and empathy
- Better context retention
- Cost: ~$0.03 per 1K tokens
- Recommended for complex emotional support

## Customizing the AI Personality

The AI companion's personality is defined in `OpenAIService.java`. You can modify the system prompt to adjust:

- Greeting style
- Level of formality
- Cultural sensitivity
- Response length
- Topics of interest

Current personality traits:
- Warm and caring
- Patient and understanding
- Asks follow-up questions
- Shows genuine interest
- Uses simple language
- Culturally respectful

## Conversation Flow

1. **First Message**: User sends any message
2. **Greeting**: AI responds with personalized greeting
3. **Conversation**: AI maintains context and responds empathetically
4. **Memory**: Conversation saved for 24 hours
5. **Cleanup**: Old conversations auto-deleted after 24 hours

## Cost Estimation

### OpenAI API Costs (GPT-3.5-Turbo)
- Input: ~$0.001 per 1K tokens
- Output: ~$0.002 per 1K tokens
- Average conversation: ~$0.01-0.02 per day per active user

### Example Monthly Costs
- 10 active users: ~$3-6/month
- 50 active users: ~$15-30/month
- 100 active users: ~$30-60/month

## Monitoring and Debugging

### Check Logs
```bash
# View Render logs
# Go to Render dashboard > Your Service > Logs
```

### Common Log Messages
- `Creating new conversation context for: [phone]` - New user
- `Generated AI response: [message]` - Successful AI response
- `OpenAI service not initialized` - Missing API key
- `Error generating AI response` - API or network issue

## Fallback Behavior

If OpenAI is unavailable or not configured:
1. System uses predefined fallback messages
2. Still maintains warm, caring tone
3. Logs indicate fallback mode active

## Security Considerations

1. **API Key Security**
   - Never commit API keys to git
   - Use environment variables only
   - Rotate keys regularly

2. **Data Privacy**
   - Conversations stored in memory only
   - Auto-deleted after 24 hours
   - No permanent storage of messages

3. **Content Filtering**
   - OpenAI has built-in content filtering
   - Inappropriate requests are rejected

## Troubleshooting

### AI Not Responding
1. Check `OPENAI_API_KEY` is set correctly
2. Verify `whatsapp.ai.enabled=true`
3. Check logs for error messages
4. Test OpenAI API key separately

### Slow Responses
1. Consider using GPT-3.5-Turbo instead of GPT-4
2. Reduce `max-tokens` setting
3. Check network latency

### Generic Responses
1. Increase `temperature` for more variety
2. Adjust system prompt for more personality
3. Ensure conversation history is working

## Testing the AI Companion

### Test Messages to Send

1. **First message**: "Hi"
   - Expected: Warm greeting with "Hello Aunty!"

2. **Health check**: "I'm feeling tired today"
   - Expected: Empathetic response with concern

3. **Family topic**: "My grandchildren visited yesterday"
   - Expected: Interest and follow-up questions

4. **Emotional support**: "I feel lonely sometimes"
   - Expected: Supportive, caring response

## Future Enhancements

Consider adding:
- Voice message support
- Multiple language support
- Personalized name recognition
- Daily check-in reminders
- Health monitoring questions
- Memory of user preferences
- Integration with family notifications

## Support

For issues:
1. Check application logs
2. Verify all environment variables
3. Test OpenAI API separately
4. Review this documentation

Remember: The AI companion is designed to provide emotional support and companionship, not medical or professional advice.