package com.example.payment.controller;

import com.example.payment.dto.OrderItem;
import com.example.payment.dto.OrderRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*") // Cho phép Angular gọi API từ domain khác
public class OrderController {

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest order) {
        List<OrderItem> items = order.getItems();
        int total = items.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();

        // In thông tin đơn hàng (giả lập lưu/log)
        System.out.println("Khách hàng: " + order.getCustomerName());
        System.out.println("Địa chỉ: " + order.getAddress());
        System.out.println("Tổng tiền: " + total);

        return ResponseEntity.ok("Thanh toán thành công! Tổng cộng: " + total + "đ");
    }
}
