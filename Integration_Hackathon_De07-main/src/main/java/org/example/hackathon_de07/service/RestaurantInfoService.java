package org.example.hackathon_de07.service;

import org.example.hackathon_de07.model.entity.FoodItem;
import org.example.hackathon_de07.model.entity.FoodCategory;
import org.example.hackathon_de07.repository.FoodItemRepository;
import org.example.hackathon_de07.repository.FoodCategoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RestaurantInfoService {

    private final FoodItemRepository foodItemRepository;
    private final FoodCategoryRepository foodCategoryRepository;

    public RestaurantInfoService(FoodItemRepository foodItemRepository, FoodCategoryRepository foodCategoryRepository) {
        this.foodItemRepository = foodItemRepository;
        this.foodCategoryRepository = foodCategoryRepository;
    }

    public List<FoodItem> searchByName(String keyword) {
        return foodItemRepository.findAll().stream()
                .filter(item -> item.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<FoodItem> searchByCategory(String foodCategoryName) {
        Optional<FoodCategory> category = foodCategoryRepository.findAll().stream()
                .filter(c -> c.getName().toLowerCase().contains(foodCategoryName.toLowerCase()))
                .findFirst();
        if (category.isPresent()) {
            return foodItemRepository.findAll().stream()
                    .filter(item -> item.getFoodFoodCategory() != null && item.getFoodFoodCategory().getId().equals(category.get().getId()))
                    .collect(Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }

    public String getRestaurantInfo(String question) {
        return "Thông tin nhà hàng: " + question;
    }
}