package org.example.hackathon_de07.tools;

import org.example.hackathon_de07.model.entity.FoodItem;
import org.example.hackathon_de07.model.entity.FoodCategory;
import org.example.hackathon_de07.repository.FoodItemRepository;
import org.example.hackathon_de07.repository.FoodCategoryRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SearchFoodByNameTool {

    private final FoodItemRepository foodItemRepository;
    private final FoodCategoryRepository foodCategoryRepository;

    public SearchFoodByNameTool(FoodItemRepository foodItemRepository, FoodCategoryRepository foodCategoryRepository) {
        this.foodItemRepository = foodItemRepository;
        this.foodCategoryRepository = foodCategoryRepository;
    }

    @Tool(name = "searchFoodByName", description = "Tìm kiếm món ăn theo tên hoặc từ khóa. Sử dụng khi khách hỏi về một món ăn cụ thể (ví dụ: 'Còn phở bò không?', 'Giá món bún chả bao nhiêu?'). Trả về danh sách món ăn khớp kèm tên, giá, tồn kho và danh mục.")
    public List<FoodItemInfo> searchFoodByName(@ToolParam(description = "Từ khóa tìm kiếm món ăn (tên món hoặc một phần tên)") String keyword) {
        List<FoodItem> items = foodItemRepository.findAll().stream()
                .filter(item -> item.getName().toLowerCase().contains(keyword.toLowerCase()))
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