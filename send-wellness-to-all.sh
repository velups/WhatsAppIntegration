#!/bin/bash

# WhatsApp Wellness Check - Send to All Recipients Script
# This script sends wellness check messages to all configured recipients

# Configuration
BASE_URL="https://whatsappintegration-cbhz.onrender.com"
# For local testing, use: BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "========================================="
echo "WhatsApp Wellness Check - Batch Send"
echo "========================================="
echo ""

# Function to send wellness check to all enabled recipients
send_to_all_enabled() {
    echo -e "${YELLOW}Sending wellness checks to all ENABLED recipients...${NC}"
    
    response=$(curl -s -X POST "$BASE_URL/api/wellness/send-all")
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Response:${NC}"
        echo "$response" | python3 -m json.tool
    else
        echo -e "${RED}Failed to send wellness checks${NC}"
    fi
}

# Function to send test message to specific recipient
send_to_specific() {
    local phone="$1"
    local name="$2"
    local time_of_day="$3"
    
    echo -e "${YELLOW}Sending to $name ($phone)...${NC}"
    
    response=$(curl -s -X POST "$BASE_URL/api/wellness/send" \
        -H "Content-Type: application/json" \
        -d "{
            \"phoneNumber\": \"$phone\",
            \"name\": \"$name\",
            \"timeOfDay\": \"$time_of_day\"
        }")
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Sent successfully${NC}"
    else
        echo -e "${RED}✗ Failed to send${NC}"
    fi
}

# Function to get current statistics
get_stats() {
    echo -e "${YELLOW}Getting wellness check statistics...${NC}"
    
    response=$(curl -s -X GET "$BASE_URL/api/wellness/stats")
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Current Statistics:${NC}"
        echo "$response" | python3 -m json.tool
    else
        echo -e "${RED}Failed to get statistics${NC}"
    fi
}

# Function to list all recipients
list_recipients() {
    echo -e "${YELLOW}Fetching all recipients...${NC}"
    
    response=$(curl -s -X GET "$BASE_URL/api/wellness/recipients")
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Recipients:${NC}"
        echo "$response" | python3 -m json.tool
    else
        echo -e "${RED}Failed to fetch recipients${NC}"
    fi
}

# Main menu
echo "Select an option:"
echo "1. Send to ALL enabled recipients"
echo "2. Send test message to specific recipient"
echo "3. View statistics"
echo "4. List all recipients"
echo "5. Send to all recipients from JSON (manual)"
echo ""
read -p "Enter your choice (1-5): " choice

case $choice in
    1)
        send_to_all_enabled
        ;;
    2)
        read -p "Enter phone number (with +): " phone
        read -p "Enter recipient name: " name
        read -p "Enter time of day (morning/afternoon/evening): " time_of_day
        send_to_specific "$phone" "$name" "$time_of_day"
        ;;
    3)
        get_stats
        ;;
    4)
        list_recipients
        ;;
    5)
        echo -e "${YELLOW}Sending to all recipients from recipients.json...${NC}"
        echo ""
        
        # Sample recipients from your JSON (update with actual phone numbers)
        # Morning recipients
        send_to_specific "+1234567890" "Mary Johnson" "morning"
        sleep 2
        
        # Evening recipients  
        send_to_specific "+0987654321" "John Smith" "evening"
        sleep 2
        
        # Afternoon recipients
        send_to_specific "+1122334455" "Susan Chen" "afternoon"
        sleep 2
        
        # Another morning recipient
        send_to_specific "+6677889900" "Robert Wilson" "morning"
        
        echo ""
        echo -e "${GREEN}Batch send completed!${NC}"
        ;;
    *)
        echo -e "${RED}Invalid choice${NC}"
        exit 1
        ;;
esac

echo ""
echo "========================================="
echo "Operation completed"
echo "========================================="