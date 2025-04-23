package com.example.payment.controller;

import com.example.payment.entity.Product;
import com.example.payment.response.ApiResponse;
import com.example.payment.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProduct() {
        try {
            List<Product> productList = productService.getAll();
            ApiResponse<List<Product>> response = new ApiResponse<>(
                    "Lấy danh sách sản phẩm thành công",
                    productList,
                    HttpStatus.OK.value()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse<>("No products found", null, HttpStatus.NOT_FOUND.value()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>("Lỗi hệ thống", null, HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
