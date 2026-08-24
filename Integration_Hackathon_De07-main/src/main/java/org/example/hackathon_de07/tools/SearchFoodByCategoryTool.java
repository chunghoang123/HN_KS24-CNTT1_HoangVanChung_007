package org.example.hackathon_de07.tools;

import org.example.hackathon_de07.model.entity.FoodItem;
import org.example.hackathon_de07.model.entity.FoodCategory;
import org.example.hackathon_de07.repository.FoodItemRepository;
import org.example.hackathon_de07.repository.FoodCategoryRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SearchFoodByCategoryTool {

    private final FoodItemRepository foodItemRepository;
    private final FoodCategoryRepository foodCategoryRepository;

    public SearchFoodByCategoryTool(FoodItemRepository foodItemRepository, FoodCategoryRepository foodCategoryRepository) {
        this.foodItemRepository = foodItemRepository;
        this.foodCategoryRepository = foodCategoryRepository;
    }

    @Tool(name = "searchFoodByCategory", description = "Tìm kiếm món ăn theo danh mục. Sử dụng khi khách hỏi về nhóm món ăn (ví dụ: 'Có những món khai vị nào?', 'Món chính có gì?', 'Danh mục đồ uống'). Trả về danh sách món ăn thuộc danh mục kèm giá và tồn kho.")
    public List<FoodItemInfo> searchFoodByCategory(@ToolParam(description = "Tên danh mục món ăn (ví dụ: 'Khai vị', 'Món chính', 'Đồ uống', 'Tráng miệng')") String foodCategoryName) {
        Optional<FoodCategory> category = foodCategoryRepository.findAll().stream()
                .filter(c -> c.getName().toLowerCase().contains(foodCategoryName.toLowerCase()))
                .findFirst();

        if (category.isEmpty()) {
            return List.of();
        }

        List<FoodItem> items = foodItemRepository.findAll().stream()
                .filter(item -> item.getFoodFoodCategory() != null && item.getFoodFoodCategory().getId().equals(category.get().getId()))
                .collect(Collectors.toList());

        return items.stream().map(this::toInfo).collect(Collectors.toList());
    }

    private FoodItemInfo toInfo(FoodItem item) {
        String categoryName = (item.getFoodFoodCategory() != null) ? item.getFoodFoodCategory().getName() : "N/A";
        return new FoodItemInfo(item.getName(), item.getPrice(), item.getStock(), categoryName, item.getDescription());
    }

    public record FoodItemInfo(
            String name,
            java.math.BigDecimal price,
            Integer stock,
            String category,
            String description
    ) {}
}