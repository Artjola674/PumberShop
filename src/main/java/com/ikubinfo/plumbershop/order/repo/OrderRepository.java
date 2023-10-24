package com.ikubinfo.plumbershop.order.repo;

import com.ikubinfo.plumbershop.order.model.OrderDocument;

import java.util.Optional;

public interface OrderRepository {

    OrderDocument save(OrderDocument document);

    Optional<OrderDocument> findById(String id);
}
