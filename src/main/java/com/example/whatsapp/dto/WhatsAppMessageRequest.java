package com.example.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppMessageRequest {
    
    private String object;
    private List<Entry> entry;
    
    // Constructors
    public WhatsAppMessageRequest() {
    }
    
    public WhatsAppMessageRequest(String object, List<Entry> entry) {
        this.object = object;
        this.entry = entry;
    }
    
    // Getters
    public String getObject() {
        return object;
    }
    
    public List<Entry> getEntry() {
        return entry;
    }
    
    // Setters
    public void setObject(String object) {
        this.object = object;
    }
    
    public void setEntry(List<Entry> entry) {
        this.entry = entry;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {
        private String id;
        private List<Change> changes;
        
        // Constructors
        public Entry() {
        }
        
        public Entry(String id, List<Change> changes) {
            this.id = id;
            this.changes = changes;
        }
        
        // Getters
        public String getId() {
            return id;
        }
        
        public List<Change> getChanges() {
            return changes;
        }
        
        // Setters
        public void setId(String id) {
            this.id = id;
        }
        
        public void setChanges(List<Change> changes) {
            this.changes = changes;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Change {
        private String field;
        private Value value;
        
        // Constructors
        public Change() {
        }
        
        public Change(String field, Value value) {
            this.field = field;
            this.value = value;
        }
        
        // Getters
        public String getField() {
            return field;
        }
        
        public Value getValue() {
            return value;
        }
        
        // Setters
        public void setField(String field) {
            this.field = field;
        }
        
        public void setValue(Value value) {
            this.value = value;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        @JsonProperty("messaging_product")
        private String messagingProduct;
        
        private Metadata metadata;
        private List<Contact> contacts;
        private List<Message> messages;
        
        // Constructors
        public Value() {
        }
        
        public Value(String messagingProduct, Metadata metadata, List<Contact> contacts, List<Message> messages) {
            this.messagingProduct = messagingProduct;
            this.metadata = metadata;
            this.contacts = contacts;
            this.messages = messages;
        }
        
        // Getters
        public String getMessagingProduct() {
            return messagingProduct;
        }
        
        public Metadata getMetadata() {
            return metadata;
        }
        
        public List<Contact> getContacts() {
            return contacts;
        }
        
        public List<Message> getMessages() {
            return messages;
        }
        
        // Setters
        public void setMessagingProduct(String messagingProduct) {
            this.messagingProduct = messagingProduct;
        }
        
        public void setMetadata(Metadata metadata) {
            this.metadata = metadata;
        }
        
        public void setContacts(List<Contact> contacts) {
            this.contacts = contacts;
        }
        
        public void setMessages(List<Message> messages) {
            this.messages = messages;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        @JsonProperty("display_phone_number")
        private String displayPhoneNumber;
        
        @JsonProperty("phone_number_id")
        private String phoneNumberId;
        
        // Constructors
        public Metadata() {
        }
        
        public Metadata(String displayPhoneNumber, String phoneNumberId) {
            this.displayPhoneNumber = displayPhoneNumber;
            this.phoneNumberId = phoneNumberId;
        }
        
        // Getters
        public String getDisplayPhoneNumber() {
            return displayPhoneNumber;
        }
        
        public String getPhoneNumberId() {
            return phoneNumberId;
        }
        
        // Setters
        public void setDisplayPhoneNumber(String displayPhoneNumber) {
            this.displayPhoneNumber = displayPhoneNumber;
        }
        
        public void setPhoneNumberId(String phoneNumberId) {
            this.phoneNumberId = phoneNumberId;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Contact {
        private Profile profile;
        
        @JsonProperty("wa_id")
        private String waId;
        
        // Constructors
        public Contact() {
        }
        
        public Contact(Profile profile, String waId) {
            this.profile = profile;
            this.waId = waId;
        }
        
        // Getters
        public Profile getProfile() {
            return profile;
        }
        
        public String getWaId() {
            return waId;
        }
        
        // Setters
        public void setProfile(Profile profile) {
            this.profile = profile;
        }
        
        public void setWaId(String waId) {
            this.waId = waId;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {
        private String name;
        
        // Constructors
        public Profile() {
        }
        
        public Profile(String name) {
            this.name = name;
        }
        
        // Getters
        public String getName() {
            return name;
        }
        
        // Setters
        public void setName(String name) {
            this.name = name;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String from;
        private String id;
        private String timestamp;
        private String type;
        private Text text;
        private Image image;
        private Audio audio;
        private Video video;
        private Document document;
        private Location location;
        
        // Constructors
        public Message() {
        }
        
        public Message(String from, String id, String timestamp, String type, Text text, Image image, Audio audio, Video video, Document document, Location location) {
            this.from = from;
            this.id = id;
            this.timestamp = timestamp;
            this.type = type;
            this.text = text;
            this.image = image;
            this.audio = audio;
            this.video = video;
            this.document = document;
            this.location = location;
        }
        
        // Getters
        public String getFrom() { return from; }
        public String getId() { return id; }
        public String getTimestamp() { return timestamp; }
        public String getType() { return type; }
        public Text getText() { return text; }
        public Image getImage() { return image; }
        public Audio getAudio() { return audio; }
        public Video getVideo() { return video; }
        public Document getDocument() { return document; }
        public Location getLocation() { return location; }
        
        // Setters
        public void setFrom(String from) { this.from = from; }
        public void setId(String id) { this.id = id; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public void setType(String type) { this.type = type; }
        public void setText(Text text) { this.text = text; }
        public void setImage(Image image) { this.image = image; }
        public void setAudio(Audio audio) { this.audio = audio; }
        public void setVideo(Video video) { this.video = video; }
        public void setDocument(Document document) { this.document = document; }
        public void setLocation(Location location) { this.location = location; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Text {
        private String body;
        
        // Constructors
        public Text() {
        }
        
        public Text(String body) {
            this.body = body;
        }
        
        // Getters
        public String getBody() {
            return body;
        }
        
        // Setters
        public void setBody(String body) {
            this.body = body;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Image {
        private String id;
        private String caption;
        @JsonProperty("mime_type")
        private String mimeType;
        
        // Constructors
        public Image() {}
        public Image(String id, String caption, String mimeType) {
            this.id = id; this.caption = caption; this.mimeType = mimeType;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCaption() { return caption; }
        public void setCaption(String caption) { this.caption = caption; }
        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Audio {
        private String id;
        @JsonProperty("mime_type")
        private String mimeType;
        
        // Constructors
        public Audio() {}
        public Audio(String id, String mimeType) {
            this.id = id; this.mimeType = mimeType;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Video {
        private String id;
        private String caption;
        @JsonProperty("mime_type")
        private String mimeType;
        
        // Constructors
        public Video() {}
        public Video(String id, String caption, String mimeType) {
            this.id = id; this.caption = caption; this.mimeType = mimeType;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCaption() { return caption; }
        public void setCaption(String caption) { this.caption = caption; }
        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {
        private String id;
        private String caption;
        private String filename;
        @JsonProperty("mime_type")
        private String mimeType;
        
        // Constructors
        public Document() {}
        public Document(String id, String caption, String filename, String mimeType) {
            this.id = id; this.caption = caption; this.filename = filename; this.mimeType = mimeType;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCaption() { return caption; }
        public void setCaption(String caption) { this.caption = caption; }
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        private Double latitude;
        private Double longitude;
        private String name;
        private String address;
        
        // Constructors
        public Location() {}
        public Location(Double latitude, Double longitude, String name, String address) {
            this.latitude = latitude; this.longitude = longitude; this.name = name; this.address = address;
        }
        
        // Getters and Setters
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }
}