package org.example.hackathon_de07.config;

import org.example.hackathon_de07.tools.CreateFoodOrderTool;
import org.example.hackathon_de07.tools.GetRestaurantInfoTool;
import org.example.hackathon_de07.tools.SearchFoodByCategoryTool;
import org.example.hackathon_de07.tools.SearchFoodByNameTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
    }

    @Bean
    public MessageChatMemoryAdvisor chatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                  SearchFoodByNameTool searchFoodByNameTool,
                                  SearchFoodByCategoryTool searchFoodByCategoryTool,
                                  GetRestaurantInfoTool getRestaurantInfoTool,
                                  CreateFoodOrderTool createFoodOrderTool) {
        return builder
                .defaultSystem("Bạn là trợ lý AI cho FoodHub Restaurant. Bạn có thể giúp khách với các việc:" +
                        "- Tra cứu món ăn theo tên hoặc danh mục (sử dụng từ khóa tìm kiếm)" +
                        "- Tra cứu thông tin nhà hàng (địa chỉ, giờ hoạt động, chính sách)" +
                        "- Đặt đơn hàng món ăn (cần số điện thoại, tên, địa chỉ, danh sách món)" +
                        "\n\n" +
                        "Hãy trả lời trực tiếp cho khách dựa trên thông tin khả dụng. " +
                        "Với yêu cầu đặt món, hãy thu thập số điện thoại, tên và địa chỉ nếu chưa có.")
                .defaultTools(searchFoodByNameTool, searchFoodByCategoryTool, getRestaurantInfoTool, createFoodOrderTool)
                .build();
    }
}