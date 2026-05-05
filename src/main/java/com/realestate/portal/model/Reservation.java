package com.realestate.portal.model;

/**
 * Model class representing a Property Booking / Reservation.
 *
 * Flat-file column order (pipe-delimited in bookings.txt):
 *   0  bookingId
 *   1  propertyId
 *   2  propertyTitle
 *   3  sellerUsername
 *   4  buyerUsername
 *   5  buyerName
 *   6  buyerEmail
 *   7  buyerPhone
 *   8  bookingDate   (yyyy-MM-dd)
 *   9  returnDate    (yyyy-MM-dd)
 *  10  status        (RESERVED | OVERDUE | COMPLETED | CANCELLED)
 */
public class Reservation {

    private String bookingId;
    private String propertyId;
    private String propertyTitle;
    private String sellerUsername;
    private String buyerUsername;
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
    private String bookingDate;
    private String returnDate;
    private String status;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Reservation() {}

    public Reservation(String bookingId, String propertyId, String propertyTitle,
                       String sellerUsername, String buyerUsername,
                       String buyerName, String buyerEmail, String buyerPhone,
                       String bookingDate, String returnDate, String status) {
        this.bookingId     = bookingId;
        this.propertyId    = propertyId;
        this.propertyTitle = propertyTitle;
        this.sellerUsername = sellerUsername;
        this.buyerUsername  = buyerUsername;
        this.buyerName     = buyerName;
        this.buyerEmail    = buyerEmail;
        this.buyerPhone    = buyerPhone;
        this.bookingDate   = bookingDate;
        this.returnDate    = returnDate;
        this.status        = status;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getBookingId()      { return bookingId; }
    public void   setBookingId(String bookingId)      { this.bookingId = bookingId; }

    public String getPropertyId()     { return propertyId; }
    public void   setPropertyId(String propertyId)    { this.propertyId = propertyId; }

    public String getPropertyTitle()  { return propertyTitle; }
    public void   setPropertyTitle(String propertyTitle) { this.propertyTitle = propertyTitle; }

    public String getSellerUsername() { return sellerUsername; }
    public void   setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }

    public String getBuyerUsername()  { return buyerUsername; }
    public void   setBuyerUsername(String buyerUsername) { this.buyerUsername = buyerUsername; }

    public String getBuyerName()      { return buyerName; }
    public void   setBuyerName(String buyerName)      { this.buyerName = buyerName; }

    public String getBuyerEmail()     { return buyerEmail; }
    public void   setBuyerEmail(String buyerEmail)    { this.buyerEmail = buyerEmail; }

    public String getBuyerPhone()     { return buyerPhone; }
    public void   setBuyerPhone(String buyerPhone)    { this.buyerPhone = buyerPhone; }

    public String getBookingDate()    { return bookingDate; }
    public void   setBookingDate(String bookingDate)  { this.bookingDate = bookingDate; }

    public String getReturnDate()     { return returnDate; }
    public void   setReturnDate(String returnDate)    { this.returnDate = returnDate; }

    public String getStatus()         { return status; }
    public void   setStatus(String status)            { this.status = status; }

    /**
     * Serialize to a pipe-delimited line suitable for appending to bookings.txt.
     * No trailing newline — callers should use println().
     */
    public String toFileLine() {
        return String.join("|",
                bookingId, propertyId, propertyTitle, sellerUsername,
                buyerUsername, buyerName, buyerEmail, buyerPhone,
                bookingDate, returnDate, status);
    }

    @Override
    public String toString() {
        return "Reservation{bookingId='" + bookingId + "', propertyId='" + propertyId +
                "', buyerUsername='" + buyerUsername + "', status='" + status + "'}";
    }
}
