package com.example.demo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.entity.Ingredient;
import com.example.demo.entity.Item;
import com.example.demo.entity.Recipe;
import com.example.demo.form.IngredientForm;
import com.example.demo.form.ProductForm;
import com.example.demo.repository.IngredientRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.RecipeRepository;
import com.example.demo.repository.SalesRecordRepository;

import jakarta.validation.Valid;

@Controller
public class RegistrationController {

    @Autowired
    private ItemRepository itemRepo;

    @Autowired
    private IngredientRepository ingredientRepo;

    @Autowired
    private RecipeRepository recipeRepo;
    
    @Autowired
    private SalesRecordRepository salesRecordRepo;
    
    
    @GetMapping("/")
    public String index() {
        return "redirect:/products"; // 初期ページを商品一覧にリダイレクトするなど
    }


    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        ProductForm form = new ProductForm();
        form.setIngredients(List.of(new IngredientForm())); // 最初の1つをセット
        model.addAttribute("productForm", form);
        return "registration";
    }

    @PostMapping("/register")
    public String registerProduct(@Valid @ModelAttribute ProductForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("productForm", form);
            return "registration";
        }

        // Item登録
        Item item = new Item();
        item.setItemId(UUID.randomUUID().toString());
        item.setName(form.getName());
        item.setPrice(form.getPrice());
        itemRepo.save(item);

        // Recipe登録（Ingredientも無ければ作成）
        for (IngredientForm ing : form.getIngredients()) {
            Ingredient ingredient = ingredientRepo.findByName(ing.getName())
                .orElseGet(() -> {
                    Ingredient newIng = new Ingredient();
                    newIng.setIngredientId(UUID.randomUUID().toString());
                    newIng.setName(ing.getName());
                    newIng.setUnit(ing.getUnit());
                    newIng.setStock(0);
                    return ingredientRepo.save(newIng);
                });

            Recipe recipe = new Recipe();
            recipe.setItemId(item.getItemId());
            recipe.setIngredientId(ingredient.getIngredientId());
            recipe.setQuantity(ing.getQuantity());
            recipeRepo.save(recipe);
        }

        return "redirect:/products";
    }

    @GetMapping("/products")
    public String listProducts(Model model) {
        List<Item> items = itemRepo.findAll();
        model.addAttribute("items", items);
        return "products";
    }

    @GetMapping("/products/edit/{itemId}")
    public String showEditForm(@PathVariable String itemId, Model model) {
        Item item = itemRepo.findById(itemId).orElse(null);
        if (item == null) return "redirect:/products?error=notfound";

        List<Recipe> recipes = recipeRepo.findByItemId(itemId);
        List<IngredientForm> ingredientForms = recipes.stream().map(recipe -> {
            Ingredient ing = ingredientRepo.findById(recipe.getIngredientId()).orElse(null);
            IngredientForm form = new IngredientForm();
            if (ing != null) {
                form.setName(ing.getName());
                form.setUnit(ing.getUnit());
            }
            form.setQuantity(recipe.getQuantity());
            return form;
        }).toList();

        ProductForm form = new ProductForm();
        form.setItemId(item.getItemId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setIngredients(ingredientForms);

        model.addAttribute("productForm", form);
        return "edit_item";
    }

    @PostMapping("/products/edit/{itemId}")
    @Transactional
    public String updateItem(
        @PathVariable String itemId,
        @Valid @ModelAttribute ProductForm form,
        BindingResult bindingResult,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("productForm", form);
            return "edit_item";
        }

        itemRepo.findById(itemId).ifPresent(existingItem -> {
            existingItem.setName(form.getName());
            existingItem.setPrice(form.getPrice());
            itemRepo.save(existingItem);
        });

        recipeRepo.deleteByItemId(itemId);

        for (IngredientForm ing : form.getIngredients()) {
            Ingredient ingredient = ingredientRepo.findByName(ing.getName())
                .orElseGet(() -> {
                    Ingredient newIng = new Ingredient();
                    newIng.setIngredientId(UUID.randomUUID().toString());
                    newIng.setName(ing.getName());
                    newIng.setUnit(ing.getUnit());
                    newIng.setStock(0);
                    return ingredientRepo.save(newIng);
                });

            Recipe recipe = new Recipe();
            recipe.setItemId(itemId);
            recipe.setIngredientId(ingredient.getIngredientId());
            recipe.setQuantity(ing.getQuantity());
            recipeRepo.save(recipe);
        }

        return "redirect:/products";
    }

    @PostMapping("/products/delete/{itemId}")
    @Transactional
    public String deleteItem(@PathVariable String itemId, Model model) {
        boolean hasSales = salesRecordRepo.existsByItemId(itemId);
        if (hasSales) {
            model.addAttribute("error", "この商品は販売記録があるため削除できません。");
            return "redirect:/products";
        }
        recipeRepo.deleteByItemId(itemId);
        itemRepo.deleteById(itemId);
        return "redirect:/products";
    }

}
