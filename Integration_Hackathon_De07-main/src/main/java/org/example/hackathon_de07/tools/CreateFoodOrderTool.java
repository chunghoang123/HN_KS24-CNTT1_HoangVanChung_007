package org.example.hackathon_de07.tools;

import org.example.hackathon_de07.service.OrderService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CreateFoodOrderTool {

    private final OrderService orderService;

    public CreateFoodOrderTool(OrderService orderService) {
        this.orderService = orderService;
    }

    @Tool(name = "createFoodOrder", description = "Tạo đơn hàng đồ ăn. Sử dụng khi khách muốn đặt món (ví dụ: 'Tôi muốn đặt 2 phở bò và 1 trà đá', 'Đặt món giúp em'). Cần thu thập: số điện thoại, tên khách, địa chỉ giao hàng, danh sách món ăn (tên món + số lượng). Trả về ID đơn hàng, tổng tiền và thông báo.")
    public OrderResult createFoodOrder(
            @ToolParam(description = "Số điện thoại khách hàng (dùng để tra cứu/tạo mới khách)") String dinerPhone,
            @ToolParam(description = "Tên đầy đủ của khách hàng") String dinerName,
            @ToolParam(description = "Địa chỉ giao hàng") String address,
            @ToolParam(description = "Danh sách món ăn đặt, mỗi món gồm tên món và số lượng") List<OrderItem> items) {
        
        List<OrderService.ItemRequest> itemRequests = items.stream()
                .map(item -> {
                    OrderService.ItemRequest req = new OrderService.ItemRequest();
                    req.setFoodName(item.foodName());
                    req.setQuantity(item.quantity());
                    return req;
                })
                .toList();

        OrderService.OrderResponse response = orderService.createOrder(dinerPhone, dinerName, address, itemRequests);
        
        return new OrderResult(response.getOrderId(), response.getPhone(), response.getTotalAmount(), response.getMessage());
    }

    public record OrderItem(
            @ToolParam(description = "Tên món ăn") String foodName,
            @ToolParam(description = "Số lượng") Integer quantity
    ) {}

    public record OrderResult(
            Long orderId,
            String phone,
            java.math.BigDecimal totalAmount,
            String message
    ) {}
}