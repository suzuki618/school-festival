package com.example.demo.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Ingredient;
import com.example.demo.entity.Item;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Recipe;
import com.example.demo.entity.SalesRecord;
import com.example.demo.repository.IngredientRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.RecipeRepository;
import com.example.demo.repository.SalesRecordRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class OrderController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private SalesRecordRepository salesRecordRepository;

    
    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;
    // 注文ページを表示
    @GetMapping("/order")
    public String showOrderPage(Model model) {
        List<Item> allItems = itemRepository.findAll();
        model.addAttribute("items", allItems);
        return "order";
    }

    // まとめて注文を処理
    @PostMapping("/order/submit")
    public String submitOrder(@RequestParam List<String> itemIds,
                              @RequestParam List<Integer> quantities,
                              @RequestParam String customerId,
                              HttpSession session) {

        Map<String, List<OrderItem>> customerOrders = (Map<String, List<OrderItem>>) session.getAttribute("customerOrders");
        if (customerOrders == null) {
            customerOrders = new HashMap<>();
        }

        List<OrderItem> orderList = new ArrayList<>();

        for (int i = 0; i < itemIds.size(); i++) {
            int quantity = quantities.get(i);
            if (quantity > 0) {
                Item item = itemRepository.findById(itemIds.get(i)).orElse(null);
                if (item != null) {
                    orderList.add(new OrderItem(item.getName(), quantity));

                    // 売上記録
                    SalesRecord record = new SalesRecord();
                    record.setItemId(item.getItemId());
                    record.setItemName(item.getName());
                    record.setQuantity(quantity);
                    record.setTotalPrice(item.getPrice() * quantity);
                    record.setDate(LocalDate.now());
                    salesRecordRepository.save(record);

                    // ▼ 材料を減らす処理
                    List<Recipe> recipes = recipeRepository.findByItemId(item.getItemId());
                    for (Recipe recipe : recipes) {
                        Ingredient ingredient = ingredientRepository.findById(recipe.getIngredientId()).orElse(null);
                        if (ingredient != null) {
                            float usedAmount = recipe.getQuantity() * quantity;
                            ingredient.setStock(ingredient.getStock() - usedAmount);
                            ingredientRepository.save(ingredient);
                        }
                    }
                }
            }
        }


        if (!orderList.isEmpty()) {
            customerOrders.put(customerId, orderList);
            session.setAttribute("customerOrders", customerOrders);
        }

        return "redirect:/dashboard";
    }
}