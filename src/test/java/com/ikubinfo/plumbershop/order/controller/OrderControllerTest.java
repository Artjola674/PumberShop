package com.ikubinfo.plumbershop.order.controller;

import com.ikubinfo.plumbershop.BaseTest;
import com.ikubinfo.plumbershop.CustomPageImpl;
import com.ikubinfo.plumbershop.common.dto.PageParams;
import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.order.dto.OrderDto;
import com.ikubinfo.plumbershop.order.dto.OrderItemDto;
import com.ikubinfo.plumbershop.order.dto.OrderRequest;
import com.ikubinfo.plumbershop.order.model.OrderDocument;
import com.ikubinfo.plumbershop.order.repo.OrderRepository;
import com.ikubinfo.plumbershop.product.dto.ProductDto;
import com.ikubinfo.plumbershop.product.dto.ProductRequest;
import com.ikubinfo.plumbershop.product.model.ProductDocument;
import com.ikubinfo.plumbershop.user.dto.Address;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDate;
import java.util.List;

import static com.ikubinfo.plumbershop.common.constants.BadRequest.ACTION_NOT_ALLOWED;
import static com.ikubinfo.plumbershop.common.constants.Constants.*;
import static com.ikubinfo.plumbershop.order.constants.OrderConstants.ORDER;
import static com.ikubinfo.plumbershop.product.constants.ProductConstants.PRODUCT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class OrderControllerTest extends BaseTest {

    private static final String ORDER_URL = BASE_URL + "/orders";

    private final OrderRepository orderRepository;

    @Autowired
    public OrderControllerTest(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @AfterEach
    void tearDown() {
        deleteUsers();
        orderRepository.deleteAll();
    }


    @Test
    void createOrder_pass() {

        OrderDto orderDto = createOrderDto();

        HttpHeaders headers = createHeaders(getTokenForUser());

        HttpEntity<OrderDto> entity = new HttpEntity<>(orderDto, headers);

        ResponseEntity<OrderDto> response = restTemplate.postForEntity(ORDER_URL,
                entity, OrderDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCustomer().getEmail()).isEqualTo("user@gmail.com");
        assertThat(response.getBody().getTotalPrice()).isEqualTo(120);
        assertThat(response.getBody().getEarnings()).isEqualTo(40);

    }

    @Test
    void createOrder_fail_nullAmount() {
        try {
            OrderDto orderDto = createOrderDto();
            orderDto.getOrderItems().get(0).setAmount(null);

            HttpHeaders headers = createHeaders(getTokenForUser());

            HttpEntity<OrderDto> entity = new HttpEntity<>(orderDto, headers);

            restTemplate.postForEntity(ORDER_URL,
                    entity, OrderDto.class);
            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        }catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(e.getMessage()).contains(INPUT_NOT_NULL);
        }

    }

    @Test
    void getAllOrders_withFilter() {
        LocalDate date = LocalDate.now();
        OrderDocument order1 = createOrderDocument(date.minusDays(1));
        orderRepository.save(order1);
        OrderDocument order2 = createOrderDocument(date.plusDays(1));
        orderRepository.save(order2);
        OrderDocument order3 = createOrderDocument(date.plusDays(2));
        orderRepository.save(order3);
        OrderDocument order4 = createOrderDocument(date.plusDays(3));
        orderRepository.save(order4);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setPageParams(new PageParams());
        orderRequest.setFromDate(date);
        orderRequest.setToDate(date.plusDays(2));

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        HttpEntity<OrderRequest> entity = new HttpEntity<>(orderRequest, headers);

        ResponseEntity<CustomPageImpl<OrderDto>> response = restTemplate.exchange(ORDER_URL+"/getAll", HttpMethod.POST,
                entity, new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent().size()).isEqualTo(2);
        assertThat(response.getBody().get().findAny().get().getDate()).isAfterOrEqualTo(date);
        assertThat(response.getBody().get().findAny().get().getDate()).isBeforeOrEqualTo(date.plusDays(2));

    }

    @Test
    void getAllOrders_withoutFilter() {
        LocalDate date = LocalDate.now();
        OrderDocument order1 = createOrderDocument(date.minusDays(1));
        orderRepository.save(order1);
        OrderDocument order2 = createOrderDocument(date.plusDays(1));
        orderRepository.save(order2);
        OrderDocument order3 = createOrderDocument(date.plusDays(2));
        orderRepository.save(order3);
        OrderDocument order4 = createOrderDocument(date.plusDays(3));
        orderRepository.save(order4);

        OrderRequest orderRequest = new OrderRequest();
        PageParams pageParams = new PageParams();
        pageParams.setPageSize(3);
        orderRequest.setPageParams(pageParams);

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        HttpEntity<OrderRequest> entity = new HttpEntity<>(orderRequest, headers);

        ResponseEntity<CustomPageImpl<OrderDto>> response = restTemplate.exchange(ORDER_URL+"/getAll", HttpMethod.POST,
                entity, new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent().size()).isEqualTo(3);
        assertThat(response.getBody().getTotalElements()).isEqualTo(4);
        assertThat(response.getBody().getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("If logged user is not seller or admin he will see only his orders. Otherwise he will see all orders")
    void getAllOrders_notAdminOrSeller() {
        LocalDate date = LocalDate.now();
        OrderDocument order1 = createOrderDocument(date.minusDays(1));
        orderRepository.save(order1);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setPageParams(new PageParams());

        HttpHeaders headers = createHeaders(getTokenForPlumber());

        HttpEntity<OrderRequest> entity = new HttpEntity<>(orderRequest, headers);

        ResponseEntity<CustomPageImpl<OrderDto>> response = restTemplate.exchange(ORDER_URL+"/getAll", HttpMethod.POST,
                entity, new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent().size()).isEqualTo(0);
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);

    }


    @Test
    void getOrderById_pass() {
        OrderDocument order = createOrderDocument(LocalDate.now());
        OrderDocument savedOrder = orderRepository.save(order);

        HttpHeaders headers = createHeaders(getTokenForSeller());

        ResponseEntity<OrderDto> response = restTemplate.exchange(
                ORDER_URL+"/id/"+savedOrder.getId(), HttpMethod.GET,
                new HttpEntity<>( headers), OrderDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDate()).isEqualTo(order.getDate());
    }

    @Test
    void getOrderById_fail_notFound() {
        try {
            HttpHeaders headers = createHeaders(getTokenForSeller());

            restTemplate.exchange(
                    ORDER_URL+"/id/"+ UtilClass.createRandomString(), HttpMethod.GET,
                    new HttpEntity<>( headers), ProductDto.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        }catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test
    void getOrderById_fail_accessDenied() {
        try {
            OrderDocument order = createOrderDocument(LocalDate.now());
            OrderDocument savedOrder = orderRepository.save(order);

            HttpHeaders headers = createHeaders(getTokenForUser());

            restTemplate.exchange(
                    ORDER_URL+"/id/"+ savedOrder.getId(), HttpMethod.GET,
                    new HttpEntity<>( headers), ProductDto.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        }catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(e.getMessage()).contains(ACTION_NOT_ALLOWED);
        }
    }

    @Test
    void deleteOrderById_pass() {
        OrderDocument order = createOrderDocument(LocalDate.now());
        OrderDocument savedOrder = orderRepository.save(order);

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        ResponseEntity<String> response = restTemplate.exchange(
                ORDER_URL + "/id/" + savedOrder.getId(), HttpMethod.DELETE,
                new HttpEntity<>(headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(DELETED_SUCCESSFULLY.replace(DOCUMENT, ORDER));
    }

    @Test
    void deleteOrderById_fail_notAdmin() {
        try {
            OrderDocument order = createOrderDocument(LocalDate.now());
            OrderDocument savedOrder = orderRepository.save(order);

            HttpHeaders headers = createHeaders(getTokenForSeller());

            restTemplate.exchange(
                    ORDER_URL + "/id/" + savedOrder.getId(), HttpMethod.DELETE,
                    new HttpEntity<>(headers), String.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpServerErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(e.getMessage()).contains("Access Denied");
        }

    }


    private OrderDto createOrderDto(){
        OrderDto orderDto = new OrderDto();
        orderDto.setId("1");
        orderDto.setDate(LocalDate.now());
        orderDto.setOrderItems(List.of(createOrderItem(),createOrderItem()));

        return orderDto;
    }

    private OrderItemDto createOrderItem() {
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setAmount(2);
        itemDto.setProduct(createProductDto());
        return itemDto;
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

    private OrderDocument createOrderDocument(LocalDate date){
        OrderDocument orderDocument = new OrderDocument();
        orderDocument.setDate(date);
//        orderDocument.setId("1");
        orderDocument.setCustomer(createUserDocument());
        orderDocument.setTotalPrice(120);
        orderDocument.setEarnings(40);
        return orderDocument;
    }

    private UserDocument createUserDocument() {
        Address address = new Address();
        address.setCity("city");
        address.setStreet("street");
        address.setPostalCode(1001);

        UserDocument userDocument = new UserDocument();
        userDocument.setFirstName("Artjola1");
        userDocument.setLastName("Kotorri1");
        userDocument.setEmail("artjolakotorri1@gmail.com");
        userDocument.setRole(Role.PLUMBER);
        userDocument.setPassword("1A@a2345678");
        userDocument.setId("1");
        userDocument.setAddress(address);
        userDocument.setDiscountPercentage(10);
        return userDocument;
    }
}