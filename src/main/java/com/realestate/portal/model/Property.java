package com.realestate.portal.model;

public class Property {
    private String id;
    private String title;
    private double price;
    private String location;
    private String type;
    private String status;
    private String sellerName;
    private String imageUrl;
    private int bedrooms;
    private int bathrooms;
    private String description; // New field

    // Default Constructor
    public Property() {}

    // Upgraded Constructor
    public Property(String id, String title, double price, String location, String type, String status, String sellerName, String imageUrl, int bedrooms, int bathrooms, String description) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.location = location;
        this.type = type;
        this.status = status;
        this.sellerName = sellerName;
        this.imageUrl = imageUrl;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.description = description; // Initialize new field
    }

    // Getters
    public String getImageUrl() { return imageUrl; }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public double getPrice() { return price; }
    public String getLocation() { return location; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getSellerName() { return sellerName; }
    public int getBedrooms() { return bedrooms; }
    public int getBathrooms() { return bathrooms; }
    public String getDescription() { return description; } // New getter

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setPrice(double price) { this.price = price; }
    public void setLocation(String location) { this.location = location; }
    public void setType(String type) { this.type = type; }
    public void setStatus(String status) { this.status = status; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setBedrooms(int bedrooms) { this.bedrooms = bedrooms; }
    public void setBathrooms(int bathrooms) { this.bathrooms = bathrooms; }
    public void setDescription(String description) { this.description = description; } // New setter
}