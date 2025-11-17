# WhatsApp Integration Spring Boot API

A Spring Boot application that receives WhatsApp messages via webhook and responds with a static message.

## Features

- Webhook verification for WhatsApp Business API
- Receives and processes incoming WhatsApp messages
- Automatically responds with a configurable static message
- Supports text, image, audio, video, document, and location messages
- RESTful API design
- Comprehensive logging

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- WhatsApp Business Account
- Facebook Developer Account
- Meta Business App with WhatsApp product

## WhatsApp Setup

### 1. Create a Meta Business App

1. Go to [Facebook Developers](https://developers.facebook.com/)
2. Create a new app or use an existing one
3. Add WhatsApp product to your app
4. Note down your `Phone Number ID` and generate a permanent `Access Token`

### 2. Configure Webhook

1. In your WhatsApp product settings, go to Configuration
2. Set up webhook URL: `https://your-domain.com/api/whatsapp/webhook`
3. Set the verify token (must match the one in application.properties)
4. Subscribe to the `messages` webhook field

## Application Setup

### 1. Clone the repository

```bash
cd /Users/velu/Projects/WhatsAppIntegration
```

### 2. Configure application.properties

Update the following properties in `src/main/resources/application.properties`:

```properties
# Your webhook verification token (create a secure random string)
whatsapp.webhook.verify-token=YOUR_WEBHOOK_VERIFY_TOKEN_HERE

# Your WhatsApp access token from Meta Business App
whatsapp.api.access-token=YOUR_WHATSAPP_ACCESS_TOKEN_HERE

# Your WhatsApp phone number ID from Meta Business App
whatsapp.api.phone-number-id=YOUR_PHONE_NUMBER_ID_HERE

# Customize the static response message
whatsapp.response.static-message=Your custom automated response message here
```

### 3. Build and Run

```bash
# Build the application
mvn clean install

# Run the application
mvn spring-boot:run

# Or run the JAR file
java -jar target/whatsapp-integration-0.0.1-SNAPSHOT.jar
```

The application will start on port 8080 by default.

## API Endpoints

### 1. Health Check
```
GET /api/whatsapp/health
```
Returns: `WhatsApp Integration API is running`

### 2. Webhook Verification
```
GET /api/whatsapp/webhook?hub.mode=subscribe&hub.verify_token=YOUR_TOKEN&hub.challenge=CHALLENGE
```
Used by WhatsApp to verify the webhook URL.

### 3. Receive Messages
```
POST /api/whatsapp/webhook
```
Receives incoming WhatsApp messages and automatically sends a static response.

## Testing Locally

For local testing, you can use ngrok to expose your local server:

```bash
# Install ngrok
brew install ngrok  # On macOS

# Expose local port
ngrok http 8080
```

Use the ngrok HTTPS URL as your webhook URL in WhatsApp configuration.

## Message Flow

1. User sends a message to your WhatsApp Business number
2. WhatsApp sends a POST request to your webhook endpoint
3. The application processes the message
4. Automatically sends back the configured static response
5. Returns success response to WhatsApp

## Project Structure

```
src/main/java/com/example/whatsapp/
├── WhatsAppIntegrationApplication.java  # Main application class
├── config/
│   └── WebClientConfig.java            # WebClient configuration
├── controller/
│   └── WhatsAppWebhookController.java  # Webhook endpoints
├── dto/
│   ├── WhatsAppMessageRequest.java     # Incoming message structure
│   ├── WhatsAppMessageResponse.java    # API response structure
│   └── WhatsAppOutgoingMessage.java    # Outgoing message structure
└── service/
    └── WhatsAppService.java            # Business logic
```

## Logging

The application logs all incoming messages and responses. Check the console output or configure a log file in `application.properties`.

## Security Considerations

1. **Never commit sensitive tokens** - Use environment variables or external configuration
2. **Use HTTPS** - Always use HTTPS in production for webhook URLs
3. **Verify webhook token** - Ensure the verify token is strong and kept secret
4. **Rate limiting** - Consider implementing rate limiting for production use
5. **Input validation** - The application validates all incoming data

## Environment Variables (Alternative Configuration)

Instead of hardcoding values in application.properties, you can use environment variables:

```bash
export WHATSAPP_WEBHOOK_VERIFY_TOKEN=your_verify_token
export WHATSAPP_API_ACCESS_TOKEN=your_access_token
export WHATSAPP_API_PHONE_NUMBER_ID=your_phone_number_id

java -jar target/whatsapp-integration-0.0.1-SNAPSHOT.jar
```

Then update application.properties to use:
```properties
whatsapp.webhook.verify-token=${WHATSAPP_WEBHOOK_VERIFY_TOKEN}
whatsapp.api.access-token=${WHATSAPP_API_ACCESS_TOKEN}
whatsapp.api.phone-number-id=${WHATSAPP_API_PHONE_NUMBER_ID}
```

## Troubleshooting

### Webhook Verification Failed
- Check that the verify token in application.properties matches the one in WhatsApp configuration
- Ensure your application is running and accessible from the internet

### Messages Not Received
- Verify webhook subscription is active in WhatsApp configuration
- Check application logs for any errors
- Ensure the phone number is correctly configured

### Cannot Send Response
- Verify the access token is valid and not expired
- Check that the phone number ID is correct
- Ensure the recipient has initiated conversation (24-hour window for business-initiated messages)

## Next Steps

- Add database to store message history
- Implement dynamic response based on message content
- Add support for rich media responses (images, buttons, lists)
- Implement conversation flow management
- Add authentication and authorization
- Set up monitoring and alerting

## Support

For issues or questions, please check the application logs first. The application includes comprehensive logging that should help identify most problems.