package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Ingredient;
import com.example.demo.repository.IngredientRepository;

@Controller
public class IngredientController {

    @Autowired
    private IngredientRepository ingredientRepository;

    @GetMapping("/ingredients")
    public String showIngredients(Model model) {
        List<Ingredient> ingredients = ingredientRepository.findAll();
        model.addAttribute("ingredients", ingredients);
        return "ingredients";
    }

    // 在庫を追加する形の更新
    @PostMapping("/ingredients/update/{id}")
    public String addToStock(@PathVariable("id") String id,
                             @RequestParam("stock") int addedStock) {
        ingredientRepository.findById(id).ifPresent(ingredient -> {
            float newStock = ingredient.getStock() + addedStock;
            ingredient.setStock(newStock);
            
            ingredientRepository.save(ingredient);
        });
        return "redirect:/ingredients";
    }

    // 新しい材料を追加する機能（必要であれば）
    @PostMapping("/ingredients/add")
    public String addIngredient(@ModelAttribute Ingredient ingredient) {
        ingredientRepository.save(ingredient);
        return "redirect:/ingredients";
    }
}
