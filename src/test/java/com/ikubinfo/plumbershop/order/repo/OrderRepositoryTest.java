package com.ikubinfo.plumbershop.order.repo;

import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.order.model.OrderDocument;
import com.ikubinfo.plumbershop.order.repo.impl.OrderRepositoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepositoryImpl underTest;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void save() {
        OrderDocument order = createOrder(LocalDate.now());

        OrderDocument savedOrder = underTest.save(order);

        Optional<OrderDocument> result = underTest.findById(savedOrder.getId());

        assertThat(result.get().getId()).isEqualTo(savedOrder.getId());

    }

    @Test
    void findAllOrderByDate() {
        Pageable pageable = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.valueOf("DESC"),
                        UtilClass.getSortField(OrderDocument.class, "date")));

        Criteria criteria = new Criteria();

        LocalDate date = LocalDate.now();

        OrderDocument order1 = createOrder(date);
        OrderDocument order2 = createOrder(date.plusDays(1));
        OrderDocument order3 = createOrder(date.minusDays(1));

        underTest.save(order1);
        underTest.save(order2);
        underTest.save(order3);

        Page<OrderDocument> result = underTest.findAll(pageable, criteria);

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.get().findFirst().get().getDate()).isEqualTo(date.plusDays(1));
    }

    @Test
    void findAllOrderByDate_returnNoContent() {
        Pageable pageable = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.valueOf("DESC"),
                        UtilClass.getSortField(OrderDocument.class, "date")));

        Criteria criteria = new Criteria();

        Page<OrderDocument> result = underTest.findAll(pageable, criteria);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
    }

    @Test
    void canFindById() {

        OrderDocument order = createOrder(LocalDate.now());

        OrderDocument savedOrder = underTest.save(order);

        Optional<OrderDocument> result = underTest.findById(savedOrder.getId());

        assertThat(result.get().getId()).isEqualTo(savedOrder.getId());

    }

    @Test
    void findByIdAndReturnNull() {

        Optional<OrderDocument> result = underTest.findById("1");

        assertThat(result).isEmpty();

    }

    @Test
    void delete() {
        OrderDocument order = createOrder(LocalDate.now());

        OrderDocument savedOrder = underTest.save(order);

        underTest.delete(savedOrder);

        Optional<OrderDocument> result = underTest.findById(savedOrder.getId());

        assertThat(result).isEmpty();

    }


    @Test
    void deleteAll() {
        OrderDocument order = createOrder(LocalDate.now());
        OrderDocument savedOrder = underTest.save(order);

        underTest.deleteAll();

        Optional<OrderDocument> result = underTest.findById(savedOrder.getId());

        assertThat(result).isEmpty();

    }

    private static OrderDocument createOrder(LocalDate date) {
        return OrderDocument.builder()
                .date(date)
                .build();
    }
}