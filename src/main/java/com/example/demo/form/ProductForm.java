package com.example.demo.form;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductForm {

    private String itemId;

    @NotBlank(message = "商品名は必須です")
    private String name;

    @NotNull(message = "価格は必須です")
    @Min(value = 0, message = "価格は0以上でなければなりません")
    private Integer price;

    @Valid  // IngredientFormのバリデーションを有効にするため必須
    @NotNull(message = "材料情報は必須です")
    private List<IngredientForm> ingredients;

    // getter/setter

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public List<IngredientForm> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<IngredientForm> ingredients) {
        this.ingredients = ingredients;
    }
}
