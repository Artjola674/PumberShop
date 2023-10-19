package com.ikubinfo.plumbershop.order.repo.impl;

import com.ikubinfo.plumbershop.order.model.OrderDocument;
import com.ikubinfo.plumbershop.order.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public OrderDocument save(OrderDocument document){

        return mongoTemplate.save(document);

    }
}
