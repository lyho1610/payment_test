package com.example.payment.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class OrderRequest {
    private String customerName;
    private String address;
    private List<OrderItem> items;
    private Long amount;
}
