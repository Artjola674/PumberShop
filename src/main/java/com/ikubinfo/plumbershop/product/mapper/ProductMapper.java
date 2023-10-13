package com.ikubinfo.plumbershop.product.mapper;

import com.ikubinfo.plumbershop.product.dto.ProductDto;
import com.ikubinfo.plumbershop.product.model.ProductDocument;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface ProductMapper {

    ProductDto toProductDto(ProductDocument document);

    ProductDocument toProductDocument(ProductDto dto);

    ProductDocument updateProductFromDto(ProductDto dto,@MappingTarget ProductDocument document);


}
