
    package com.realestate.portal.model;


    public class PublicReview extends Review {

        public PublicReview(String reviewID, String propertyID, String buyerName, int rating, String comment) {

            super(reviewID, propertyID, buyerName, rating, comment);
        }

        @Override
        public void displayReview() {
            System.out.println("--- Public Review ---");
            super.displayReview();
        }
    }
