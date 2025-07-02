package com.example.demo.entity;

public class OrderItem {
    private String itemName;
    private int quantity;
    private int price; // ← 追加

    public OrderItem() {}

    public OrderItem(String itemName, int quantity) {
        this.itemName = itemName;
        this.quantity = quantity;
    }

    // ✅ 追加：3つの引数を受け取るコンストラクタ
    public OrderItem(String itemName, int quantity, int price) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // ✅ getter / setter for price
    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
