package com.ikubinfo.plumbershop.product.controller;

import com.ikubinfo.plumbershop.product.dto.ProductDto;
import com.ikubinfo.plumbershop.product.dto.ProductRequest;
import com.ikubinfo.plumbershop.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SELLER')")
    @Operation(summary = "create a new product")
    public ResponseEntity<ProductDto> saveProduct(@RequestBody ProductDto productDto){
        return ResponseEntity.ok(productService.save(productDto));
    }

    @GetMapping
    @Operation(summary = "Get all products")
    public ResponseEntity<Page<ProductDto>> getProducts(@Valid @RequestBody ProductRequest request){
        return ResponseEntity.ok(productService.getAll(request));
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Get a product by ID")
    public ResponseEntity<ProductDto> getProductById(@PathVariable String id){
        return ResponseEntity.ok(productService.getById(id));
    }

    @PutMapping("/id/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SELLER')")
    @Operation(summary = "Update a product by ID")
    public ResponseEntity<ProductDto> updateProductById(@PathVariable String id,
                                                        @RequestBody ProductDto productDto){
        return ResponseEntity.ok(productService.updateById(id,productDto));
    }

    @DeleteMapping("/id/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SELLER')")
    @Operation(summary = "Delete a product by ID")
    public ResponseEntity<String> deleteProductById(@PathVariable String id){
        return ResponseEntity.ok(productService.deleteById(id));
    }
}
