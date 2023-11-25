package com.ikubinfo.plumbershop.order.service;

import com.ikubinfo.plumbershop.common.dto.Filter;
import com.ikubinfo.plumbershop.email.EmailService;
import com.ikubinfo.plumbershop.exception.BadRequestException;
import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;
import com.ikubinfo.plumbershop.order.dto.OrderDto;
import com.ikubinfo.plumbershop.order.dto.OrderItemDto;
import com.ikubinfo.plumbershop.order.dto.OrderRequest;
import com.ikubinfo.plumbershop.order.mapper.OrderMapper;
import com.ikubinfo.plumbershop.order.model.OrderDocument;
import com.ikubinfo.plumbershop.order.repo.OrderRepository;
import com.ikubinfo.plumbershop.order.service.impl.OrderServiceImpl;
import com.ikubinfo.plumbershop.product.dto.ProductDto;
import com.ikubinfo.plumbershop.product.model.ProductDocument;
import com.ikubinfo.plumbershop.product.repo.ProductRepository;
import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.ikubinfo.plumbershop.user.dto.Address;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import com.ikubinfo.plumbershop.user.dto.UserRequest;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.service.UserService;
import com.itextpdf.text.DocumentException;
import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.ikubinfo.plumbershop.common.constants.BadRequest.ACTION_NOT_ALLOWED;
import static com.ikubinfo.plumbershop.common.constants.Constants.*;
import static com.ikubinfo.plumbershop.order.constants.OrderConstants.ORDER;
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
class OrderServiceTest {

    @Autowired
    private OrderService underTest;

    @Mock
    private OrderRepository orderRepository;

    private OrderMapper orderMapper = Mappers.getMapper((OrderMapper.class));
    @Mock
    private UserService userService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private EmailService emailService;

    @Value("${documents.folder}")
    private String documentPath;

    @BeforeEach
    void setUp() {
        underTest = new OrderServiceImpl(orderRepository,userService,productRepository,emailService);
    }

    @Test
    void save_success() throws MessagingException, IOException, DocumentException {
        OrderDto orderDto = createOrderDto();
        OrderDocument orderDocument = orderMapper.toOrderDocument(orderDto);
        UserDocument loggedUser = createUserDocument();
        orderDocument.setCustomer(loggedUser);

        ReflectionTestUtils.setField(underTest, "documentPath", documentPath);
        given(userService.getUserByEmail(loggedUser.getEmail())).willReturn(loggedUser);

        underTest.save(orderDto, fromUserDocumentToCustomUserDetails(loggedUser));

        verify(emailService).sendEmailWhenOrderIsCreated(any());
        verify(orderRepository).save(any(OrderDocument.class));

    }

    @Test
    void getAllOrders() {

        UserDocument loggedUser = createUserDocument();

        OrderDocument orderDocument = createOrderDocument();
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setFilter(new Filter());
        orderRequest.setCustomerId(orderDocument.getCustomer().getId());
        Page<OrderDocument> mockedPage = new PageImpl<>(List.of(orderDocument));
        when(orderRepository.findAll(any(Pageable.class), any(Criteria.class)))
                .thenReturn(mockedPage);

        Page<OrderDto> result = underTest.getAllOrders(fromUserDocumentToCustomUserDetails(loggedUser), orderRequest);

        assertThat(result.getContent().get(0).getCustomer().getId()).isEqualTo(orderDocument.getCustomer().getId());
    }

    @Test
    void getById_success() {
        UserDocument loggedUser = createUserDocument();

        OrderDocument orderDocument = createOrderDocument();

        given(orderRepository.findById(orderDocument.getId())).willReturn(Optional.of(orderDocument));

        OrderDto result = underTest.getById(orderDocument.getId(), fromUserDocumentToCustomUserDetails(loggedUser));

        assertThat(result).isEqualTo(orderMapper.toOrderDto(orderDocument));
    }

    @Test
    void getById_throwException_notFound() {
        UserDocument loggedUser = createUserDocument();

        OrderDocument orderDocument = createOrderDocument();

        given(orderRepository.findById(orderDocument.getId())).willReturn(Optional.ofNullable(null));

        assertThatThrownBy(() ->underTest.getById(orderDocument.getId(), fromUserDocumentToCustomUserDetails(loggedUser)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("%s not found with %s : '%s' ",ORDER,ID, orderDocument.getId());
    }

    @Test
    void getById_throwException_canNotAccess() {
        UserDocument loggedUser = createUserDocument();
        loggedUser.setId("2");

        OrderDocument orderDocument = createOrderDocument();

        given(orderRepository.findById(orderDocument.getId())).willReturn(Optional.of(orderDocument));

        assertThatThrownBy(() ->underTest.getById(orderDocument.getId(), fromUserDocumentToCustomUserDetails(loggedUser)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(ACTION_NOT_ALLOWED);
    }

    @Test
    void deleteById_success() {
        UserDocument loggedUser = createUserDocument();
        loggedUser.setRole(Role.ADMIN);

        OrderDocument orderDocument = createOrderDocument();

        given(orderRepository.findById(orderDocument.getId())).willReturn(Optional.of(orderDocument));

        String result = underTest.deleteById(orderDocument.getId(), fromUserDocumentToCustomUserDetails(loggedUser));

        verify(orderRepository).delete(orderDocument);

        assertThat(result).isEqualTo(DELETED_SUCCESSFULLY.replace(DOCUMENT,ORDER));
    }

    @Test
    void deleteById_throwException_notAllowed() {
        UserDocument loggedUser = createUserDocument();

        OrderDocument orderDocument = createOrderDocument();

        given(orderRepository.findById(orderDocument.getId())).willReturn(Optional.of(orderDocument));

        assertThatThrownBy(() ->underTest.deleteById(orderDocument.getId(), fromUserDocumentToCustomUserDetails(loggedUser)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(ACTION_NOT_ALLOWED);

        verify(orderRepository,never()).delete(any(OrderDocument.class));

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

    private OrderDocument createOrderDocument(){
        OrderDocument orderDocument = new OrderDocument();
        orderDocument.setDate(LocalDate.now());
        orderDocument.setId("1");
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