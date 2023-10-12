package com.ikubinfo.plumbershop.product.mapper;

import com.ikubinfo.plumbershop.product.dto.ProductDto;
import com.ikubinfo.plumbershop.product.model.ProductDocument;
import org.mapstruct.Mapper;

@Mapper
public interface ProductMapper {

    ProductDto productDocumentToProductDto(ProductDocument document);

    ProductDocument productDtoToProductDocument(ProductDto dto);


}
