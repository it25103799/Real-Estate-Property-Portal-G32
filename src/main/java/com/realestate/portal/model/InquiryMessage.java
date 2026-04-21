package com.realestate.portal.model;

public class InquiryMessage {
    private String threadId;
    private String timestamp;
    private String senderRole;
    private String senderName;
    private String content;

    public InquiryMessage() {}

    public InquiryMessage(String threadId, String timestamp, String senderRole, String senderName, String content) {
        this.threadId = threadId;
        this.timestamp = timestamp;
        this.senderRole = senderRole;
        this.senderName = senderName;
        this.content = content;
    }

    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
