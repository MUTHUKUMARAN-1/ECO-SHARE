package com.econest.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class CartDto {

    private List<CartItemDto> items = new ArrayList<>();
    private int totalQuantity = 0;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal shippingFee = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private boolean hasStockIssues = false;
    private String promoCode;
    private boolean qualifiesForFreeShipping = false;
    private BigDecimal freeShippingThreshold = new BigDecimal("50.00");
    private BigDecimal amountNeededForFreeShipping = BigDecimal.ZERO;

    public CartDto() {
    }

    public void recalculate(BigDecimal discount, String code) {
        this.totalQuantity = items.stream().mapToInt(CartItemDto::getQuantity).sum();
        this.subtotal = items.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.hasStockIssues = items.stream()
                .anyMatch(i -> i.isOutOfStock() || i.isQuantityExceedsStock());

        if (this.subtotal.compareTo(freeShippingThreshold) >= 0 || items.isEmpty()) {
            this.shippingFee = BigDecimal.ZERO;
            this.qualifiesForFreeShipping = true;
            this.amountNeededForFreeShipping = BigDecimal.ZERO;
        } else {
            this.shippingFee = new BigDecimal("4.99");
            this.qualifiesForFreeShipping = false;
            this.amountNeededForFreeShipping = freeShippingThreshold.subtract(this.subtotal);
        }

        this.discountAmount = discount != null ? discount : BigDecimal.ZERO;
        this.promoCode = code;

        BigDecimal discountedSubtotal = this.subtotal.subtract(this.discountAmount);
        if (discountedSubtotal.compareTo(BigDecimal.ZERO) < 0) {
            discountedSubtotal = BigDecimal.ZERO;
        }

        // 5% eco-friendly local tax estimate
        this.taxAmount = discountedSubtotal.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);

        this.totalAmount = discountedSubtotal.add(this.shippingFee).add(this.taxAmount).setScale(2, RoundingMode.HALF_UP);
    }

    // Getters and Setters
    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean isHasStockIssues() {
        return hasStockIssues;
    }

    public void setHasStockIssues(boolean hasStockIssues) {
        this.hasStockIssues = hasStockIssues;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public boolean isQualifiesForFreeShipping() {
        return qualifiesForFreeShipping;
    }

    public void setQualifiesForFreeShipping(boolean qualifiesForFreeShipping) {
        this.qualifiesForFreeShipping = qualifiesForFreeShipping;
    }

    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) {
        this.freeShippingThreshold = freeShippingThreshold;
    }

    public BigDecimal getAmountNeededForFreeShipping() {
        return amountNeededForFreeShipping;
    }

    public void setAmountNeededForFreeShipping(BigDecimal amountNeededForFreeShipping) {
        this.amountNeededForFreeShipping = amountNeededForFreeShipping;
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
}
