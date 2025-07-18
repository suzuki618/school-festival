package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, String> {
    Optional<Ingredient> findByName(String name);
    
}
