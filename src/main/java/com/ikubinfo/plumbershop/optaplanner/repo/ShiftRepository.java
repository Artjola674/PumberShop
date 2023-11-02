package com.ikubinfo.plumbershop.optaplanner.repo;

import com.ikubinfo.plumbershop.optaplanner.model.ShiftDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ShiftRepository extends MongoRepository<ShiftDocument, String> {
}
