package com.ikubinfo.plumbershop.product.controller;

import com.ikubinfo.plumbershop.product.dto.ProductDto;
import com.ikubinfo.plumbershop.product.dto.ProductRequest;
import com.ikubinfo.plumbershop.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductDto> saveProduct(@RequestBody ProductDto productDto){
        return ResponseEntity.ok(productService.save(productDto));
    }

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getProducts(@Valid @RequestBody ProductRequest request){
        return ResponseEntity.ok(productService.getAll(request));
    }
}
