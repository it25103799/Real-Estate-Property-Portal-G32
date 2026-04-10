package com.realestate.portal.model;

public class VerifiedReview extends Review {

    public VerifiedReview(String reviewID, String propertyID, String buyerName, int rating, String comment) {
        super(reviewID, propertyID, buyerName, rating, comment);
    }

    @Override
    public void displayReview() {
        System.out.println("--- Verified Purchase Review ✅ ---");
        super.displayReview();
    }
}