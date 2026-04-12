package com.realestate.portal.model;

public class Review {

    private String reviewID;
    private String propertyID;
    private String buyerName;
    private int rating;
    private String comment;


    public Review(String reviewID, String propertyID, String buyerName, int rating, String comment) {
        this.reviewID = reviewID;
        this.propertyID = propertyID;
        this.buyerName = buyerName;
        this.rating = rating;
        this.comment = comment;
    }


    public String getReviewID() { return reviewID; }
    public void setReviewID(String reviewID) { this.reviewID = reviewID; }

    public String getPropertyID() { return propertyID; }
    public void setPropertyID(String propertyID) { this.propertyID = propertyID; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public void displayReview() {
        System.out.println("Review by " + buyerName + ": [" + rating + "/5] " + comment);
    }

    public boolean isVerified() {
        return false; // By default, most reviews are not verified
    }
}