package com.example.demo.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Item;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.SalesRecord;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.SalesRecordRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class OrderController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private SalesRecordRepository salesRecordRepository;

    // 注文ページ表示
    @GetMapping("/order")
    public String showOrderPage(Model model) {
        List<Item> allItems = itemRepository.findAll();
        model.addAttribute("items", allItems);
        return "order";
    }

    // 注文処理（customerIdを自動採番）
    @PostMapping("/order/submit")
    public String submitOrder(@org.springframework.web.bind.annotation.RequestParam List<String> itemIds,
                              @org.springframework.web.bind.annotation.RequestParam List<Integer> quantities,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        List<OrderItem> orderList = new ArrayList<>();

        for (int i = 0; i < itemIds.size(); i++) {
            int quantity = quantities.get(i);
            if (quantity > 0) {
                Optional<Item> optionalItem = itemRepository.findById(itemIds.get(i));
                if (optionalItem.isPresent()) {
                    Item item = optionalItem.get();
                    orderList.add(new OrderItem(item.getName(), quantity, item.getPrice()));

                    // 売上記録
                    SalesRecord record = new SalesRecord();
                    record.setItemId(item.getItemId());
                    record.setItemName(item.getName());
                    record.setQuantity(quantity);
                    record.setTotalPrice(item.getPrice() * quantity);
                    record.setDate(LocalDate.now());
                    salesRecordRepository.save(record);
                }
            }
        }

        // 商品が1つも選択されていない場合は注文ページに戻る
        if (orderList.isEmpty()) {
            model.addAttribute("error", "1つ以上の商品を選択してください。");
            model.addAttribute("items", itemRepository.findAll());
            return "order";
        }

        // customerCounterからcustomerIdを生成
        Integer customerCounter = (Integer) session.getAttribute("customerCounter");
        if (customerCounter == null) {
            customerCounter = 1;
        } else {
            customerCounter++;
        }

        String customerId = String.valueOf(customerCounter);

        // セッションに保存
        Map<String, List<OrderItem>> customerOrders =
                (Map<String, List<OrderItem>>) session.getAttribute("customerOrders");

        if (customerOrders == null) {
            customerOrders = new HashMap<>();
        }

        customerOrders.put(customerId, orderList);
        session.setAttribute("customerOrders", customerOrders);
        session.setAttribute("customerCounter", customerCounter); // 更新

        // フラッシュメッセージで完了通知
        redirectAttributes.addFlashAttribute("message", "注文が完了しました。");

        return "redirect:/dashboard";
    }

    // お客様番号リセット用
    @PostMapping("/order/reset-customer-counter")
    public String resetCustomerCounter(HttpSession session, RedirectAttributes redirectAttributes) {
        session.setAttribute("customerCounter", 0);
        redirectAttributes.addFlashAttribute("message", "お客様番号をリセットしました。");
        return "redirect:/dashboard";
    }
}
