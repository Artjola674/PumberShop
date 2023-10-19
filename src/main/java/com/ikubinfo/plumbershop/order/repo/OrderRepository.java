package com.ikubinfo.plumbershop.order.repo;

import com.ikubinfo.plumbershop.order.model.OrderDocument;

public interface OrderRepository {

    OrderDocument save(OrderDocument document);
}
