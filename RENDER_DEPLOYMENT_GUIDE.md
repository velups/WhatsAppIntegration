# Deploy WhatsApp Integration API to Render

This guide will walk you through deploying your WhatsApp Integration Spring Boot API to Render.

## Prerequisites

- GitHub account
- Render account (sign up at https://render.com)
- WhatsApp Business Account configured
- Meta Business App with WhatsApp product

## Step-by-Step Deployment Guide

### Step 1: Prepare Your Code for Deployment

1. **Initialize Git repository** (if not already done):
   ```bash
   cd /Users/velu/Projects/WhatsAppIntegration
   git init
   git add .
   git commit -m "Initial commit - WhatsApp Integration API"
   ```

2. **Create a GitHub repository**:
   - Go to https://github.com/new
   - Name it `WhatsAppIntegration`
   - Keep it public (or private if you have a paid Render account)
   - Don't initialize with README (we already have one)

3. **Push to GitHub**:
   ```bash
   git remote add origin https://github.com/YOUR_USERNAME/WhatsAppIntegration.git
   git branch -M main
   git push -u origin main
   ```

### Step 2: Update render.yaml

1. Edit `render.yaml` and replace `YOUR_GITHUB_USERNAME` with your actual GitHub username:
   ```yaml
   repo: https://github.com/YOUR_GITHUB_USERNAME/WhatsAppIntegration
   ```

2. Commit and push the change:
   ```bash
   git add render.yaml
   git commit -m "Update GitHub username in render.yaml"
   git push
   ```

### Step 3: Deploy on Render

#### Option A: Using Render Dashboard (Recommended for first-time)

1. **Log in to Render**: Go to https://dashboard.render.com

2. **Create New Web Service**:
   - Click "New +" button
   - Select "Web Service"

3. **Connect GitHub**:
   - Click "Connect GitHub" if not already connected
   - Authorize Render to access your repositories
   - Select your `WhatsAppIntegration` repository

4. **Configure the Service**:
   - **Name**: `whatsapp-integration-api` (or your preferred name)
   - **Region**: Choose closest to your location
   - **Branch**: `main`
   - **Runtime**: `Docker`
   - **Instance Type**: Free (or your preferred plan)

5. **Add Environment Variables**:
   Click "Advanced" and add these environment variables:
   
   | Key | Value |
   |-----|-------|
   | `WHATSAPP_API_ACCESS_TOKEN` | Your WhatsApp access token from Meta |
   | `WHATSAPP_API_PHONE_NUMBER_ID` | Your Phone Number ID from Meta |
   | `WHATSAPP_WEBHOOK_VERIFY_TOKEN` | Generate a secure random string |
   | `SPRING_PROFILES_ACTIVE` | `production` |

   **Note**: Save the `WHATSAPP_WEBHOOK_VERIFY_TOKEN` value - you'll need it for WhatsApp configuration.

6. **Create Web Service**:
   - Click "Create Web Service"
   - Render will start building and deploying your application

#### Option B: Using Blueprint (Render YAML)

1. Go to https://dashboard.render.com/blueprints

2. Click "New Blueprint Instance"

3. Connect your GitHub repository

4. Render will automatically detect `render.yaml`

5. Configure environment variables as shown above

6. Click "Apply"

### Step 4: Wait for Deployment

1. **Monitor the build**:
   - Render will show build logs
   - First deployment may take 5-10 minutes
   - Wait for "Live" status

2. **Get your URL**:
   - Once deployed, Render provides a URL like:
   - `https://whatsapp-integration-api.onrender.com`
   - Note this URL for WhatsApp configuration

3. **Test the health endpoint**:
   ```bash
   curl https://your-app-name.onrender.com/api/whatsapp/health
   ```
   Should return: `WhatsApp Integration API is running`

### Step 5: Configure WhatsApp Webhook

1. **Go to Meta for Developers**:
   - Navigate to your app dashboard
   - Go to WhatsApp > Configuration

2. **Configure Webhook**:
   - **Callback URL**: `https://your-app-name.onrender.com/api/whatsapp/webhook`
   - **Verify Token**: Use the same value you set in `WHATSAPP_WEBHOOK_VERIFY_TOKEN`
   - Click "Verify and Save"

3. **Subscribe to Webhook Fields**:
   - After verification succeeds, subscribe to `messages` field
   - This enables your app to receive messages

### Step 6: Test Your Integration

1. **Send a test message**:
   - Send any message to your WhatsApp Business number
   - You should receive the automated response

2. **Check logs in Render**:
   - Go to your service dashboard on Render
   - Click on "Logs" to see incoming messages and responses

## Important Notes

### Free Tier Limitations

If using Render's free tier:
- Service spins down after 15 minutes of inactivity
- First request after spin-down takes 30-50 seconds
- Limited to 750 hours/month
- Consider upgrading for production use

### Environment Variables Reference

| Variable | Description | Required |
|----------|-------------|----------|
| `WHATSAPP_WEBHOOK_VERIFY_TOKEN` | Token for webhook verification | Yes |
| `WHATSAPP_API_ACCESS_TOKEN` | Meta/WhatsApp API access token | Yes |
| `WHATSAPP_API_PHONE_NUMBER_ID` | Your WhatsApp phone number ID | Yes |
| `WHATSAPP_RESPONSE_STATIC_MESSAGE` | Custom response message | No (has default) |
| `SPRING_PROFILES_ACTIVE` | Set to `production` | Recommended |

### Updating Your Application

1. **Make changes locally**
2. **Commit and push to GitHub**:
   ```bash
   git add .
   git commit -m "Your update message"
   git push
   ```
3. **Render auto-deploys** (if enabled) or manually trigger deployment

### Monitoring & Logs

- **View Logs**: Dashboard > Your Service > Logs
- **Metrics**: Dashboard > Your Service > Metrics
- **Set up alerts**: Dashboard > Your Service > Settings > Health & Alerts

### Troubleshooting

#### Webhook Verification Fails
- Ensure `WHATSAPP_WEBHOOK_VERIFY_TOKEN` in Render matches the token in Meta
- Check service is running: test health endpoint
- Review logs for verification attempts

#### Messages Not Received
- Verify webhook is subscribed to `messages` field
- Check Render logs for incoming POST requests
- Ensure service is not sleeping (free tier issue)

#### Cannot Send Response
- Verify `WHATSAPP_API_ACCESS_TOKEN` is valid
- Check `WHATSAPP_API_PHONE_NUMBER_ID` is correct
- Ensure user initiated conversation within 24 hours

#### Build Failures
- Check Java version compatibility (requires Java 17)
- Verify all dependencies in pom.xml
- Review build logs for specific errors

### Security Best Practices

1. **Never commit tokens** to GitHub
2. **Use strong webhook verify token**
3. **Enable HTTPS only** (Render does this by default)
4. **Regularly rotate access tokens**
5. **Monitor logs for suspicious activity**

## Next Steps

After successful deployment:

1. **Test thoroughly** with different message types
2. **Set up monitoring** and alerts
3. **Implement error handling** for production
4. **Consider upgrading** Render plan for better performance
5. **Add custom domain** if needed

## Support Resources

- **Render Documentation**: https://render.com/docs
- **WhatsApp Business API**: https://developers.facebook.com/docs/whatsapp
- **Application Logs**: Check Render dashboard for debugging
- **Health Check**: `GET /api/whatsapp/health`

## Cost Considerations

### Render Pricing
- **Free**: Limited hours, auto-sleep
- **Starter** ($7/month): No sleep, better performance
- **Professional**: Custom domains, more resources

### WhatsApp Pricing
- **Message costs**: Check Meta's pricing for your region
- **Conversation-based pricing**: Charges per 24-hour conversation window

Remember to monitor your usage and costs on both platforms!