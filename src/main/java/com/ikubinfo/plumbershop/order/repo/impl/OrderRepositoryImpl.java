package com.ikubinfo.plumbershop.order.repo.impl;

import com.ikubinfo.plumbershop.order.model.OrderDocument;
import com.ikubinfo.plumbershop.order.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public OrderDocument save(OrderDocument document){

        return mongoTemplate.save(document);

    }

    @Override
    public Page<OrderDocument> findAll(Pageable pageable, Criteria criteria) {


        Query query = new Query(criteria);

        long total = mongoTemplate.count(query, OrderDocument.class);

        query.skip(pageable.getOffset());
        query.limit(pageable.getPageSize());

        List<OrderDocument> results = mongoTemplate.find(query, OrderDocument.class);

        return new PageImpl<>(results, pageable, total);

    }

    @Override
    public Optional<OrderDocument> findById(String id) {
        return Optional.ofNullable(
                mongoTemplate.findById(id, OrderDocument.class));
    }

    @Override
    public void delete(OrderDocument orderDocument) {
        mongoTemplate.remove(orderDocument);
    }
}
