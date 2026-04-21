package com.realestate.portal.model;

import java.util.ArrayList;
import java.util.List;

public class InquiryThread {
    private String id;
    private String propertyId;
    private String propertyTitle;
    private String sellerName;
    private String buyerAccountName;
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
    private String createdDate;
    private String status;
    private List<InquiryMessage> messages = new ArrayList<>();

    public InquiryThread() {}

    public InquiryThread(String id, String propertyId, String propertyTitle, String sellerName,
                         String buyerAccountName, String buyerName, String buyerEmail, String buyerPhone,
                         String createdDate, String status) {
        this.id = id;
        this.propertyId = propertyId;
        this.propertyTitle = propertyTitle;
        this.sellerName = sellerName;
        this.buyerAccountName = buyerAccountName;
        this.buyerName = buyerName;
        this.buyerEmail = buyerEmail;
        this.buyerPhone = buyerPhone;
        this.createdDate = createdDate;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPropertyId() { return propertyId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }

    public String getPropertyTitle() { return propertyTitle; }
    public void setPropertyTitle(String propertyTitle) { this.propertyTitle = propertyTitle; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getBuyerAccountName() { return buyerAccountName; }
    public void setBuyerAccountName(String buyerAccountName) { this.buyerAccountName = buyerAccountName; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }

    public String getBuyerPhone() { return buyerPhone; }
    public void setBuyerPhone(String buyerPhone) { this.buyerPhone = buyerPhone; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<InquiryMessage> getMessages() { return messages; }
    public void setMessages(List<InquiryMessage> messages) { this.messages = messages; }
}
