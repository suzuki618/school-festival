package com.example.demo.form;

import java.util.List;

public class OrderForm {
    private String name;
    private int price;
    private List<IngredientForm> ingredients;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public List<IngredientForm> getIngredients() {
		return ingredients;
	}
	public void setIngredients(List<IngredientForm> ingredients) {
		this.ingredients = ingredients;
	}

    // getter/setter
    
}
