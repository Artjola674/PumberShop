package com.ikubinfo.plumbershop.product.repo;

import com.ikubinfo.plumbershop.product.model.ProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ProductRepository extends  MongoRepository<ProductDocument, String>, QuerydslPredicateExecutor<ProductDocument> {
}
