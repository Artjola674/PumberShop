package com.ikubinfo.plumbershop.product.controller;

import com.ikubinfo.plumbershop.BaseTest;
import com.ikubinfo.plumbershop.CustomPageImpl;
import com.ikubinfo.plumbershop.auth.dto.AuthRequest;
import com.ikubinfo.plumbershop.auth.dto.AuthResponse;
import com.ikubinfo.plumbershop.common.dto.Filter;
import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.product.dto.ProductDto;
import com.ikubinfo.plumbershop.product.dto.ProductRequest;
import com.ikubinfo.plumbershop.product.mapper.ProductMapper;
import com.ikubinfo.plumbershop.product.model.ProductDocument;
import com.ikubinfo.plumbershop.product.repo.ProductRepository;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

import static com.ikubinfo.plumbershop.common.constants.Constants.DELETED_SUCCESSFULLY;
import static com.ikubinfo.plumbershop.common.constants.Constants.DOCUMENT;
import static com.ikubinfo.plumbershop.product.constants.ProductConstants.PRODUCT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class ProductControllerTest extends BaseTest {

    private static final String PATH = "/products";
    private static final String PRODUCT_URL = BASE_URL + PATH;

    private final ProductRepository productRepository;

    @Autowired
    public ProductControllerTest(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @AfterEach
    void tearDown() {
        deleteUsers();
        productRepository.deleteAll();
    }

    @Test
    void saveProduct_pass() {
        ProductDto productDto = createProductDto("Name", "Code");

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        HttpEntity<ProductDto> entity = new HttpEntity<>(productDto, headers);

        ResponseEntity<ProductDto> response = restTemplate.postForEntity(PRODUCT_URL,
                entity, ProductDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo(productDto.getName());

    }

    @Test
    void saveProduct_fail_nullName() {
        try {
            ProductDto productDto = createProductDto(null, "Code");

            HttpHeaders headers = createHeaders(getTokenForAdmin());

            HttpEntity<ProductDto> entity = new HttpEntity<>(productDto, headers);

            restTemplate.postForEntity(PRODUCT_URL, entity, ProductDto.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

    }

    @Test
    void getProducts_withFilter() {
        ProductDocument product1 = createProductDocument("Name", "Code");
        productRepository.save(product1);
        ProductDocument product2 = createProductDocument("Name", "Code");
        productRepository.save(product2);
        ProductDocument product3 = createProductDocument("Name", "Code3");
        productRepository.save(product3);

        ProductRequest productRequest = new ProductRequest();
        productRequest.setFilter(new Filter());
        productRequest.setName(product1.getName());
        productRequest.setCode(product1.getCode());


        HttpHeaders headers = createHeaders(getTokenForUser());

        HttpEntity<ProductRequest> entity = new HttpEntity<>(productRequest, headers);

        ResponseEntity<CustomPageImpl<ProductDto>> response = restTemplate.exchange(PRODUCT_URL+"/getAll", HttpMethod.POST,
                entity, new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent().size()).isEqualTo(2);
        assertThat(response.getBody().get().findAny().get().getName()).isEqualTo(product1.getName());
        assertThat(response.getBody().get().findAny().get().getCode()).isNotEqualTo(product3.getCode());

    }

    @Test
    void getProducts_withoutFilter() {
        ProductDocument product1 = createProductDocument("Name", "Code");
        productRepository.save(product1);
        ProductDocument product2 = createProductDocument("Name", "Code");
        productRepository.save(product2);
        ProductDocument product3 = createProductDocument("Name", "Code3");
        productRepository.save(product3);

        ProductRequest productRequest = new ProductRequest();
        Filter filter = new Filter();
        filter.setPageSize(2);
        productRequest.setFilter(filter);

        HttpHeaders headers = createHeaders(getTokenForUser());

        HttpEntity<ProductRequest> entity = new HttpEntity<>(productRequest, headers);

        ResponseEntity<CustomPageImpl<ProductDto>> response = restTemplate.exchange(PRODUCT_URL+"/getAll", HttpMethod.POST,
                entity, new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
        assertThat(response.getBody().getTotalPages()).isEqualTo(2);

    }

    @Test
    void getProductById_pass() {
        ProductDocument product1 = createProductDocument("Name", "Code");
        ProductDocument savedProduct = productRepository.save(product1);

        HttpHeaders headers = createHeaders(getTokenForUser());

        ResponseEntity<ProductDto> response = restTemplate.exchange(
                PRODUCT_URL+"/id/"+savedProduct.getId(), HttpMethod.GET,
        new HttpEntity<>( headers), ProductDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCode()).isEqualTo(product1.getCode());
    }

    @Test
    void getProductById_fail() {
        try {
            HttpHeaders headers = createHeaders(getTokenForUser());

            restTemplate.exchange(
                    PRODUCT_URL+"/id/"+ UtilClass.createRandomString(), HttpMethod.GET,
                    new HttpEntity<>( headers), ProductDto.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        }catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }


    @Test
    void updateProductById_pass() {
        ProductDocument product = createProductDocument("Name", "Code");
        ProductDocument savedProduct = productRepository.save(product);

        ProductDto productDto = createProductDto("new name", "new code");

        HttpHeaders headers = createHeaders(getTokenForSeller());
        HttpEntity<ProductDto> entity = new HttpEntity<>(productDto, headers);

        ResponseEntity<ProductDto> response = restTemplate.exchange(
                PRODUCT_URL+"/id/"+savedProduct.getId(), HttpMethod.PUT,
                entity, ProductDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCode()).isEqualTo(productDto.getCode());
        assertThat(response.getBody().getCode()).isNotEqualTo(product.getCode());
    }

    @Test
    void updateProductById_fail_nullCode() {
        try {
            ProductDocument product = createProductDocument("Name", "Code");
            ProductDocument savedProduct = productRepository.save(product);

            ProductDto productDto = createProductDto("new name", null);

            HttpHeaders headers = createHeaders(getTokenForSeller());
            HttpEntity<ProductDto> entity = new HttpEntity<>(productDto, headers);

            restTemplate.exchange(
                    PRODUCT_URL+"/id/"+savedProduct.getId(), HttpMethod.PUT,
                    entity, ProductDto.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

    }


    @Test
    void deleteProductById_pass() {

        ProductDocument product = createProductDocument("Name", "Code");
        ProductDocument savedProduct = productRepository.save(product);

        HttpHeaders headers = createHeaders(getTokenForSeller());

        ResponseEntity<String> response = restTemplate.exchange(
                PRODUCT_URL + "/id/" + savedProduct.getId(), HttpMethod.DELETE,
                new HttpEntity<>(headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(DELETED_SUCCESSFULLY.replace(DOCUMENT, PRODUCT));
    }

    private ProductDto createProductDto(String name, String code) {
        ProductDto productDto = new ProductDto();
        productDto.setId("1");
        productDto.setName(name);
        productDto.setCode(code);
        productDto.setBuyingPrice(20);
        productDto.setSellingPrice(30);
        productDto.setCount(5);
        return productDto;
    }

    private ProductDocument createProductDocument(String name, String code) {
        ProductDocument productDocument = new ProductDocument();
        productDocument.setName(name);
        productDocument.setCode(code);
        productDocument.setBuyingPrice(20);
        productDocument.setSellingPrice(30);
        productDocument.setCount(5);
        return productDocument;
    }
}