package com.example.payment.serviceImpl;

import com.example.payment.entity.Product;
import com.example.payment.repository.ProductRepository;
import com.example.payment.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> getAll() {
        List<Product> productList = productRepository.findAll();
        if (productList.isEmpty()) {
            throw new IllegalArgumentException("Không có dữ liệu product");
        }
        return productList;
    }
}
