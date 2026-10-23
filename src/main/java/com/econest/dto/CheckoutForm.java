package com.econest.dto;

import com.econest.entity.PaymentMethod;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CheckoutForm {

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 120, message = "Name must be between 2 and 120 characters")
    private String customerName;

    @NotBlank(message = "Email is required for order confirmation")
    @Email(message = "Please provide a valid email address")
    private String customerEmail;

    @NotBlank(message = "Phone number is required for delivery coordination")
    @Size(min = 7, max = 25, message = "Phone must be between 7 and 25 characters")
    private String customerPhone;

    @NotBlank(message = "Shipping address is required")
    @Size(min = 5, max = 255, message = "Address must be at least 5 characters")
    private String shippingAddress;

    @NotBlank(message = "City is required")
    private String city;

    private String state;

    @NotBlank(message = "Postal / ZIP code is required")
    private String postalCode;

    @NotBlank(message = "Country is required")
    private String country = "United States";

    @NotNull(message = "Please select a payment method")
    private PaymentMethod paymentMethod = PaymentMethod.MOCK_ONLINE;

    private String notes;
    private String promoCode;

    public CheckoutForm() {
    }

    // Getters and Setters
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }
}
