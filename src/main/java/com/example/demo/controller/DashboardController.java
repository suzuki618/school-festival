package com.example.demo.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.OrderItem;
import com.example.demo.entity.SalesRecord;
import com.example.demo.repository.SalesRecordRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {

    @Autowired
    private SalesRecordRepository salesRecordRepository;

    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpSession session) {
        LocalDate today = LocalDate.now();

        // 最古日付を取得
        LocalDate firstDate = salesRecordRepository.findAll().stream()
                .map(SalesRecord::getDate)
                .min(LocalDate::compareTo)
                .orElse(today);

        // フォーマットを yyyy/MM/dd に変換
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String formattedStartDate = firstDate.format(formatter);
        String formattedToday = today.format(formatter);

        // 売上期間の文字列
        String salesPeriod = formattedStartDate + " ～ " + formattedToday;

        // 期間の売上データを取得
        List<SalesRecord> salesRecords = salesRecordRepository.findByDateBetween(firstDate, today);

        int totalSales = salesRecords.stream()
                .mapToInt(SalesRecord::getTotalPrice)
                .sum();

        Map<LocalDate, Integer> dailySalesMap = salesRecords.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::getDate,
                        Collectors.summingInt(SalesRecord::getTotalPrice)
                ));

        List<DailySales> dailySales = dailySalesMap.entrySet().stream()
                .map(entry -> new DailySales(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DailySales::getDate))
                .collect(Collectors.toList());

        Map<String, List<OrderItem>> customerOrders = (Map<String, List<OrderItem>>) session.getAttribute("customerOrders");

        model.addAttribute("totalSales", totalSales);
        model.addAttribute("salesPeriod", salesPeriod);
        model.addAttribute("dailySales", dailySales);
        model.addAttribute("customerOrders", customerOrders);

        return "dashboard";
    }

    /**
     * 売上全リセット処理
     */
    @PostMapping("/dashboard/reset-sales")
    @ResponseBody
    public String resetSales() {
        salesRecordRepository.deleteAll();
        return "売上をすべてリセットしました";
    }

    /**
     * 期間指定で売上をリセット
     */
    @PostMapping("/dashboard/reset-sales-range")
    @ResponseBody
    public String resetSalesByRange(@RequestParam String start, @RequestParam String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        List<SalesRecord> recordsToDelete = salesRecordRepository.findByDateBetween(startDate, endDate);
        salesRecordRepository.deleteAll(recordsToDelete);
        return "指定期間の売上をリセットしました";
    }

    /**
     * お客様番号をリセット
     */
    @PostMapping("/dashboard/reset-customers")
    @ResponseBody
    public String resetCustomers(HttpSession session) {
        // 顧客注文情報をクリア
        session.removeAttribute("customerOrders");

        // お客様番号カウンタもリセット（OrderControllerと統一）
        session.setAttribute("customerCounter", 0);

        return "お客様番号をリセットしました";
    }

    /**
     * 顧客注文から特定の商品を削除し、売上情報も更新
     */
    @PostMapping("/order/delete-item")
    public String deleteOrderItem(@RequestParam String customerId,
                                  @RequestParam String itemName,
                                  HttpSession session) {

        Map<String, List<OrderItem>> customerOrders =
                (Map<String, List<OrderItem>>) session.getAttribute("customerOrders");

        if (customerOrders != null && customerOrders.containsKey(customerId)) {
            List<OrderItem> items = customerOrders.get(customerId);
            Iterator<OrderItem> iterator = items.iterator();

            while (iterator.hasNext()) {
                OrderItem item = iterator.next();
                if (item.getItemName().equals(itemName)) {
                    // 削除対象商品の合計金額を取得
                    int itemTotalPrice = item.getPrice();

                    iterator.remove();

                    // 売上情報の更新
                    LocalDate todayDate = LocalDate.now();
                    List<SalesRecord> records = salesRecordRepository.findByDateBetween(todayDate, todayDate);

                    if (!records.isEmpty()) {
                        SalesRecord record = records.get(0);
                        int updatedPrice = record.getTotalPrice() - itemTotalPrice;
                        record.setTotalPrice(Math.max(updatedPrice, 0)); // 0未満は防止
                        salesRecordRepository.save(record);
                    }

                    break; // 1件削除したらループ抜け
                }
            }

            // 顧客の注文リストが空なら顧客自体も削除
            if (items.isEmpty()) {
                customerOrders.remove(customerId);
            } else {
                customerOrders.put(customerId, items);
            }
            session.setAttribute("customerOrders", customerOrders);
        }

        return "redirect:/dashboard";
    }

    /**
     * 日別売上表示用内部クラス
     */
    public static class DailySales {
        private final LocalDate date;
        private final int amount;

        public DailySales(LocalDate date, int amount) {
            this.date = date;
            this.amount = amount;
        }

        public LocalDate getDate() {
            return date;
        }

        public int getAmount() {
            return amount;
        }
    }
}
