package com.example.demo.controller;

import java.time.LocalDate;
import java.util.Comparator;
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
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(14);

        List<SalesRecord> salesRecords = salesRecordRepository.findByDateBetween(startDate, endDate);

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
        model.addAttribute("salesPeriod", startDate + " ～ " + endDate);
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
     * 期間指定で売上をリセット（オプション）
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
