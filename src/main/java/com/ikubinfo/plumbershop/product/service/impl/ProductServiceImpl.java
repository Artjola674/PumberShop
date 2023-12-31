package com.ikubinfo.plumbershop.product.service.impl;

import com.ikubinfo.plumbershop.common.dto.PageParams;
import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;
import com.ikubinfo.plumbershop.product.dto.ProductDto;
import com.ikubinfo.plumbershop.product.dto.ProductRequest;
import com.ikubinfo.plumbershop.product.mapper.ProductMapper;
import com.ikubinfo.plumbershop.product.model.ProductDocument;
import com.ikubinfo.plumbershop.product.model.QProductDocument;
import com.ikubinfo.plumbershop.product.repo.ProductRepository;
import com.ikubinfo.plumbershop.product.service.ProductService;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static com.ikubinfo.plumbershop.common.constants.Constants.*;
import static com.ikubinfo.plumbershop.product.constants.ProductConstants.PRODUCT;
import static com.ikubinfo.plumbershop.product.constants.ProductConstants.PRODUCT_DOCUMENT;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
        productMapper = Mappers.getMapper(ProductMapper.class);
    }

    @Override
    public ProductDto save(ProductDto productDto) {
        ProductDocument document =
                productRepository.save(productMapper.toProductDocument(productDto));
        return productMapper.toProductDto(document);
    }

    @Override
    public Page<ProductDto> getAll(ProductRequest request) {

        PageParams pageParams = request.getPageParams();

        Pageable pageable = PageRequest.of(pageParams.getPageNumber(), pageParams.getPageSize(),
                Sort.by(Sort.Direction.valueOf(pageParams.getSortType()),
                        UtilClass.getSortField(ProductDocument.class, pageParams.getSortBy())));

        QProductDocument qProduct = new QProductDocument(PRODUCT_DOCUMENT);

        BooleanExpression predicate = hasName(request.getName(), qProduct)
                .and(hasCode(request.getCode(),qProduct));
        return productRepository.findAll(predicate, pageable)
                .map(productMapper::toProductDto);

    }

    @Override
    public ProductDto getById(String id) {
        ProductDocument document = getProductDocumentById(id);
        return productMapper.toProductDto(document);
    }

    @Override
    public ProductDto updateById(String id, ProductDto productDto) {
        ProductDocument document = getProductDocumentById(id);
        ProductDocument updatedDocument = productMapper.updateProductFromDto(productDto,document);

        return productMapper.toProductDto(productRepository.save(updatedDocument));
    }

    @Override
    public String deleteById(String id) {
        productRepository.deleteById(id);
        return DELETED_SUCCESSFULLY.replace(DOCUMENT, PRODUCT);
    }

    private ProductDocument getProductDocumentById(String id) {
        return productRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException(PRODUCT,ID, id));
    }


    private BooleanExpression hasName(String name, QProductDocument qProduct){
        return name == null ? qProduct.id.isNotNull(): qProduct.name.eq(name);
    }

    private BooleanExpression hasCode(String code, QProductDocument qProduct){
        return code == null ? qProduct.id.isNotNull(): qProduct.code.eq(code);
    }

}
