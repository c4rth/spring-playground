package org.c4rth.problem.service;

import org.c4rth.problem.entity.Product;
import org.c4rth.problem.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class DemoService {

    private final ProductRepository productRepository;

    public DemoService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
}