package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Recipe;

public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
    List<Recipe> findByItemId(String itemId);

	void deleteByItemId(String itemId);
	
	List<Recipe> findByIngredientId(String ingredientId);
}
