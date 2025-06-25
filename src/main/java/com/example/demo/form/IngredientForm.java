package com.example.demo.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class IngredientForm {

    @NotBlank(message = "材料名は必須です")
    private String name;

    @NotBlank(message = "単位は必須です")
    private String unit;

    @NotNull(message = "使用量は必須です")
    @Min(value = 0, message = "数量は0以上でなければなりません")
    private Float quantity;

    // getter/setter

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Float getQuantity() {
        return quantity;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }
}
