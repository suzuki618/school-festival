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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Ingredient;
import com.example.demo.repository.IngredientRepository;
import com.example.demo.repository.RecipeRepository;

@Controller
public class IngredientController {

    @Autowired
    private IngredientRepository ingredientRepository;
    @Autowired
    private RecipeRepository recipeRepository;

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
    @PostMapping("/ingredients/delete/{ingredientId}")
    public String deleteIngredient(@PathVariable String ingredientId, Model model, RedirectAttributes redirectAttributes) {
        if (!recipeRepository.findByIngredientId(ingredientId).isEmpty()) {
            // 材料が使われている場合、リダイレクト時にエラーメッセージを渡す
            redirectAttributes.addFlashAttribute("deleteError", "この材料はレシピで使用されているため削除できません。");
            return "redirect:/ingredients";
        }

        ingredientRepository.deleteById(ingredientId);
        return "redirect:/ingredients";
    }
}
