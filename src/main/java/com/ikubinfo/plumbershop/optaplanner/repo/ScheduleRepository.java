package com.ikubinfo.plumbershop.optaplanner.repo;

import com.ikubinfo.plumbershop.optaplanner.model.ScheduleDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ScheduleRepository extends MongoRepository<ScheduleDocument, String> {
}
