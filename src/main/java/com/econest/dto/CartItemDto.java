package com.econest.dto;

import java.math.BigDecimal;

public class CartItemDto {

    private Long productId;
    private String productName;
    private String sku;
    private String imageUrl;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subtotal;
    private int availableStock;
    private boolean outOfStock;
    private boolean quantityExceedsStock;

    public CartItemDto() {
    }

    public CartItemDto(Long productId, String productName, String sku, String imageUrl,
                       BigDecimal unitPrice, int quantity, int availableStock) {
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.imageUrl = imageUrl;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.availableStock = availableStock;
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.outOfStock = availableStock <= 0;
        this.quantityExceedsStock = quantity > availableStock;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public boolean isOutOfStock() {
        return outOfStock;
    }

    public void setOutOfStock(boolean outOfStock) {
        this.outOfStock = outOfStock;
    }

    public boolean isQuantityExceedsStock() {
        return quantityExceedsStock;
    }

    public void setQuantityExceedsStock(boolean quantityExceedsStock) {
        this.quantityExceedsStock = quantityExceedsStock;
    }
}
