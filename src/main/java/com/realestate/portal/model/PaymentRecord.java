package com.realestate.portal.model;

/**
 * Model class representing a payment record for completed rental bookings.
 *
 * Flat-file column order (pipe-delimited in payments.txt):
 *   0  paymentId
 *   1  bookingId
 *   2  propertyId
 *   3  sellerId
 *   4  rentalFee         (base rental amount: days × daily rate)
 *   5  penaltyFee        (overdue penalty: overduedays × daily rate)
 *   6  totalAmount       (rentalFee + penaltyFee)
 *   7  paymentStatus     (PAID | PENDING)
 *   8  paymentDate       (yyyy-MM-dd)
 *   9  bookingReturnDate (yyyy-MM-dd)
 */
public class PaymentRecord {

    private String paymentId;
    private String bookingId;
    private String propertyId;
    private String sellerId;
    private double rentalFee;
    private double penaltyFee;
    private double totalAmount;
    private String paymentStatus;
    private String paymentDate;
    private String bookingReturnDate;

    // ── Constructors ──────────────────────────────────────────────────────────

    public PaymentRecord() {}

    public PaymentRecord(String paymentId, String bookingId, String propertyId,
                        String sellerId, double rentalFee, double penaltyFee,
                        double totalAmount, String paymentStatus,
                        String paymentDate, String bookingReturnDate) {
        this.paymentId         = paymentId;
        this.bookingId         = bookingId;
        this.propertyId        = propertyId;
        this.sellerId          = sellerId;
        this.rentalFee         = rentalFee;
        this.penaltyFee        = penaltyFee;
        this.totalAmount       = totalAmount;
        this.paymentStatus     = paymentStatus;
        this.paymentDate       = paymentDate;
        this.bookingReturnDate = bookingReturnDate;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getPaymentId()       { return paymentId; }
    public void   setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getBookingId()       { return bookingId; }
    public void   setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getPropertyId()      { return propertyId; }
    public void   setPropertyId(String propertyId) { this.propertyId = propertyId; }

    public String getSellerId()        { return sellerId; }
    public void   setSellerId(String sellerId) { this.sellerId = sellerId; }

    public double getRentalFee()       { return rentalFee; }
    public void   setRentalFee(double rentalFee) { this.rentalFee = rentalFee; }

    public double getPenaltyFee()      { return penaltyFee; }
    public void   setPenaltyFee(double penaltyFee) { this.penaltyFee = penaltyFee; }

    public double getTotalAmount()     { return totalAmount; }
    public void   setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentStatus()   { return paymentStatus; }
    public void   setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentDate()     { return paymentDate; }
    public void   setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    public String getBookingReturnDate() { return bookingReturnDate; }
    public void   setBookingReturnDate(String bookingReturnDate) { this.bookingReturnDate = bookingReturnDate; }

    /**
     * Serialize to a pipe-delimited line suitable for appending to payments.txt.
     * No trailing newline — callers should use println().
     */
    public String toFileLine() {
        return String.join("|",
                paymentId, bookingId, propertyId, sellerId,
                String.valueOf(rentalFee), String.valueOf(penaltyFee),
                String.valueOf(totalAmount), paymentStatus,
                paymentDate, bookingReturnDate);
    }

    @Override
    public String toString() {
        return "PaymentRecord{paymentId='" + paymentId + "', bookingId='" + bookingId +
                "', sellerId='" + sellerId + "', totalAmount=" + totalAmount + "}";
    }
}

