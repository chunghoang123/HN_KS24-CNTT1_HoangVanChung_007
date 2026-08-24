package org.example.hackathon_de07.service;

import org.example.hackathon_de07.model.entity.*;
import org.example.hackathon_de07.model.constant.FoodOrderStatus;
import org.example.hackathon_de07.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final DinerRepository dinerRepository;
    private final FoodItemRepository foodItemRepository;
    private final FoodOrderRepository foodOrderRepository;

    public OrderService(DinerRepository dinerRepository, FoodItemRepository foodItemRepository, FoodOrderRepository foodOrderRepository) {
        this.dinerRepository = dinerRepository;
        this.foodItemRepository = foodItemRepository;
        this.foodOrderRepository = foodOrderRepository;
    }

    @Transactional
    public OrderResponse createOrder(String phone, String name, String address, List<ItemRequest> items) {
        // 1. Tìm hoặc tạo Diner
        Diner diner = dinerRepository.findByPhone(phone)
                .orElseGet(() -> {
                    Diner newDiner = new Diner();
                    newDiner.setPhone(phone);
                    newDiner.setFullName(name);
                    newDiner.setAddress(address);
                    return dinerRepository.save(newDiner);
                });

        // 2. Tính tổng tiền và kiểm tra tồn kho - xử lý hoàn toàn ngoài lambda
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Đầu tiên, collect tất cả food items cần kiểm tra
        List<FoodItem> foundItems = items.stream()
                .map(itemReq -> {
                    FoodItem foodItem = foodItemRepository.findAll().stream()
                            .filter(item -> item.getName().toLowerCase().equals(itemReq.getFoodName().toLowerCase()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn: " + itemReq.getFoodName()));
                    return foodItem;
                })
                .collect(Collectors.toList());

        // Kiểm tra tồn kho và tính tổng
        for (FoodItem foodItem : foundItems) {
            // Tìm quantity tương ứng từ items gốc
            ItemRequest itemReq = items.stream()
                    .filter(i -> i.getFoodName().equals(foodItem.getName()))
                    .findFirst()
                    .orElseThrow();

            if (foodItem.getStock() < itemReq.getQuantity()) {
                throw new RuntimeException("Món '" + foodItem.getName() + "' chỉ còn " + foodItem.getStock() + " tồn kho");
            }

            foodItem.setStock(foodItem.getStock() - itemReq.getQuantity());
            foodItemRepository.save(foodItem);

            BigDecimal itemTotal = foodItem.getPrice().multiply(new BigDecimal(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        // 3. Tạo FoodOrder
        FoodOrder order = new FoodOrder();
        order.setDiner(diner);
        order.setFoodFoodOrderDate(LocalDateTime.now());
        order.setStatus(FoodOrderStatus.PENDING);
        order.setTotalAmount(totalAmount);
        order.setNote("Đặt qua AI Chatbot");

        FoodOrder savedOrder = foodOrderRepository.save(order);

        // 4. Tạo FoodFoodOrderItem và gán order
        List<FoodFoodOrderItem> orderItems = items.stream().map(itemReq -> {
            FoodItem foodItem = foundItems.stream()
                    .filter(f -> f.getName().toLowerCase().equals(itemReq.getFoodName().toLowerCase()))
                    .findFirst()
                    .orElseThrow();

            FoodFoodOrderItem orderItem = new FoodFoodOrderItem();
            orderItem.setFoodItem(foodItem);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setUnitPrice(foodItem.getPrice());
            orderItem.setFoodFoodOrder(savedOrder);
            return orderItem;
        }).collect(Collectors.toList());

        return new OrderResponse(savedOrder.getId(), savedOrder.getDiner().getPhone(), savedOrder.getTotalAmount(), "Đặt hàng thành công");
    }

    public static class ItemRequest {
        private String foodName;
        private Integer quantity;

        public String getFoodName() { return foodName; }
        public void setFoodName(String foodName) { this.foodName = foodName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class OrderResponse {
        private Long orderId;
        private String phone;
        private BigDecimal totalAmount;
        private String message;

        public OrderResponse() {}

        public OrderResponse(Long orderId, String phone, BigDecimal totalAmount, String message) {
            this.orderId = orderId;
            this.phone = phone;
            this.totalAmount = totalAmount;
            this.message = message;
        }

        public Long getOrderId() { return orderId; }
        public String getPhone() { return phone; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public String getMessage() { return message; }
    }
}