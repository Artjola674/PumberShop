package com.ikubinfo.plumbershop.product.controller;

import com.ikubinfo.plumbershop.product.dto.ProductDto;
import com.ikubinfo.plumbershop.product.dto.ProductRequest;
import com.ikubinfo.plumbershop.product.service.ProductService;
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
    public ResponseEntity<ProductDto> saveProduct(@RequestBody ProductDto productDto){
        return ResponseEntity.ok(productService.save(productDto));
    }

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getProducts(@Valid @RequestBody ProductRequest request){
        return ResponseEntity.ok(productService.getAll(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable String id){
        return ResponseEntity.ok(productService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SELLER')")
    public ResponseEntity<ProductDto> updateProductById(@PathVariable String id,
                                                        @RequestBody ProductDto productDto){
        return ResponseEntity.ok(productService.updateById(id,productDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SELLER')")
    public ResponseEntity<String> deleteProductById(@PathVariable String id){
        return ResponseEntity.ok(productService.deleteById(id));
    }
}
