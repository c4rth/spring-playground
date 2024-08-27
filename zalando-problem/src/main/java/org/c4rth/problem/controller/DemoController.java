package org.c4rth.problem.controller;

import lombok.extern.slf4j.Slf4j;
import org.c4rth.problem.entity.Product;
import org.c4rth.problem.exception.ProductNotFoundException;
import org.c4rth.problem.service.DemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product")
@Slf4j
public class DemoController {

    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        log.info("Get product by id: {}", id);
        var result = demoService.getProductById(id);
        if (result.isEmpty()) {
            throw new ProductNotFoundException(id);
        }
        return ResponseEntity.ok(result.get());
    }
}