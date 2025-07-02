package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.SalesRecord;

public interface SalesRecordRepository extends JpaRepository<SalesRecord, Long> {

    // すでにあるメソッド
    List<SalesRecord> findByDateBetween(LocalDate start, LocalDate end);

    // ✅ 追加：指定した itemId を持つ販売記録が存在するか
    boolean existsByItemId(String itemId);
}
