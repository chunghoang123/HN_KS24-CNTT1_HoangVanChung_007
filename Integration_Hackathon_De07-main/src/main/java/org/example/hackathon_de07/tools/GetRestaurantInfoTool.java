package org.example.hackathon_de07.tools;

import org.example.hackathon_de07.service.RestaurantInfoService;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class  GetRestaurantInfoTool {

    private final VectorStore vectorStore;
    private final RestaurantInfoService restaurantInfoService;

    public GetRestaurantInfoTool(VectorStore vectorStore, RestaurantInfoService restaurantInfoService) {
        this.vectorStore = vectorStore;
        this.restaurantInfoService = restaurantInfoService;
    }

    @Tool(name = "getRestaurantInfo", description = "Tra cứu thông tin nhà hàng (địa chỉ, giờ hoạt động, chính sách đặt/huỷ món, phí giao hàng, thanh toán, FAQ...). Sử dụng khi khách hỏi chung về nhà hàng không liên quan đến món ăn cụ thể (ví dụ: 'Địa chỉ chi nhánh Q1?', 'Giờ mở cửa?', 'Đặt đơn dưới 200k có phí ship không?', 'Chính sách huỷ đơn như thế nào?'). Thực hiện similarity search trên vector store.")
    public String getRestaurantInfo(@ToolParam(description = "Câu hỏi của khách về thông tin nhà hàng") String question) {
        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(3)
                .build();

        List<Document> results = vectorStore.similaritySearch(request);
        
        if (results.isEmpty()) {
            return "Không tìm thấy thông tin liên quan.";
        }

        return results.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));
    }
}