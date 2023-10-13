package com.ikubinfo.plumbershop.product.service;

import com.ikubinfo.plumbershop.product.dto.ProductDto;
import com.ikubinfo.plumbershop.product.dto.ProductRequest;
import org.springframework.data.domain.Page;

public interface ProductService {
    ProductDto save(ProductDto productDto);

    Page<ProductDto> getAll(ProductRequest request);

    ProductDto getById(String id);

    ProductDto updateById(String id, ProductDto productDto);

    String deleteById(String id);

}
