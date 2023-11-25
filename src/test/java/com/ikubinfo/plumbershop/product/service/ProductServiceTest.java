package com.ikubinfo.plumbershop.product.service;

import com.ikubinfo.plumbershop.common.dto.Filter;
import com.ikubinfo.plumbershop.email.EmailService;
import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;
import com.ikubinfo.plumbershop.order.mapper.OrderMapper;
import com.ikubinfo.plumbershop.order.repo.OrderRepository;
import com.ikubinfo.plumbershop.order.service.OrderService;
import com.ikubinfo.plumbershop.order.service.impl.OrderServiceImpl;
import com.ikubinfo.plumbershop.product.dto.ProductDto;
import com.ikubinfo.plumbershop.product.dto.ProductRequest;
import com.ikubinfo.plumbershop.product.mapper.ProductMapper;
import com.ikubinfo.plumbershop.product.model.ProductDocument;
import com.ikubinfo.plumbershop.product.repo.ProductRepository;
import com.ikubinfo.plumbershop.product.service.impl.ProductServiceImpl;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import com.ikubinfo.plumbershop.user.dto.UserRequest;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.service.UserService;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static com.ikubinfo.plumbershop.common.constants.Constants.*;
import static com.ikubinfo.plumbershop.product.constants.ProductConstants.PRODUCT;
import static com.ikubinfo.plumbershop.security.CustomUserDetails.fromUserDocumentToCustomUserDetails;
import static com.ikubinfo.plumbershop.user.constants.UserConstants.USER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class ProductServiceTest {

    @Autowired
    private ProductService underTest;

    @Mock
    private ProductRepository productRepository;

    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        underTest = new ProductServiceImpl(productRepository);
        productMapper = Mappers.getMapper(ProductMapper.class);
    }

    @Test
    void saveProduct_success() {

        ProductDto productDto = createProductDto();
        ProductDocument productDocument = productMapper.toProductDocument(productDto);

        given(productRepository.save(productDocument)).willReturn(productDocument);

        ProductDto result = underTest.save(productDto);

        assertThat(result).isEqualTo(productDto);
    }

    @Test
    void getAll_success() {

        ProductDocument productDocument = createProductDocument();
        ProductRequest productRequest = new ProductRequest();
        productRequest.setFilter(new Filter());
        productRequest.setName(productDocument.getName());

        Page<ProductDocument> mockedPage = new PageImpl<>(List.of(productDocument));
        given(productRepository.findAll(any(BooleanExpression.class), any(Pageable.class)))
                .willReturn(mockedPage);

        Page<ProductDto> result = underTest.getAll(productRequest);

        assertThat(result.getContent().get(0).getName()).isEqualTo(productDocument.getName());
    }

    @Test
    void getById_success() {
        ProductDto productDto = createProductDto();
        ProductDocument productDocument = productMapper.toProductDocument(productDto);

        given(productRepository.findById(productDto.getId())).willReturn(Optional.of(productDocument));

        ProductDto result = underTest.getById(productDto.getId());

        assertThat(result).isEqualTo(productDto);
    }

    @Test
    void getById_throwException_NotFound() {
        String id = "1";

        given(productRepository.findById(id)).willReturn(Optional.ofNullable(null));

        assertThatThrownBy(() ->underTest.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("%s not found with %s : '%s' ",PRODUCT,ID, id);

    }

    @Test
    void updateById_success() {
        ProductDto productDto = createProductDto();

        ProductDocument productDocument = createProductDocument();
        String name = productDocument.getName();

        given(productRepository.findById(productDto.getId())).willReturn(Optional.of(productDocument));

        ProductDocument updatedDocument = productMapper.updateProductFromDto(productDto,productDocument);

        given(productRepository.save(updatedDocument)).willReturn(updatedDocument);
        ProductDto result = underTest.updateById(productDocument.getId(),productDto);

        assertThat(result.getName()).isNotEqualTo(name);
        assertThat(result.getCount()).isEqualTo(productDocument.getCount());

    }

    @Test
    void updateById_throwException_notFound() {
        ProductDto productDto = createProductDto();

        given(productRepository.findById(productDto.getId())).willReturn(Optional.ofNullable(null));

        assertThatThrownBy(() ->underTest.updateById(productDto.getId(), productDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("%s not found with %s : '%s' ",PRODUCT,ID, productDto.getId());

        verify(productRepository,never()).save(any());

    }


    @Test
    void deleteById_success() {
        String id = "1";
        String result = underTest.deleteById(id);

        verify(productRepository).deleteById(id);

        assertThat(result).isEqualTo(DELETED_SUCCESSFULLY.replace(DOCUMENT,PRODUCT));
    }

    private ProductDto createProductDto() {
        ProductDto productDto = new ProductDto();
        productDto.setId("1");
        productDto.setName("Name");
        productDto.setCode("1001");
        productDto.setBuyingPrice(20);
        productDto.setSellingPrice(30);
        productDto.setCount(5);
        return productDto;
    }

    private ProductDocument createProductDocument() {
        ProductDocument productDocument = new ProductDocument();
        productDocument.setName("Name1");
        productDocument.setCode("1002");
        productDocument.setBuyingPrice(20);
        productDocument.setSellingPrice(30);
        productDocument.setCount(5);
        return productDocument;
    }

}