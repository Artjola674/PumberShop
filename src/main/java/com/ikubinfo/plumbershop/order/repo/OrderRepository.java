package com.ikubinfo.plumbershop.order.repo;

import com.ikubinfo.plumbershop.order.model.OrderDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Optional;

public interface OrderRepository {

    OrderDocument save(OrderDocument document);

    Page<OrderDocument> findAll(Pageable pageable, Criteria criteria);

    Optional<OrderDocument> findById(String id);

    void delete(OrderDocument orderDocument);

    void deleteAll();


}
