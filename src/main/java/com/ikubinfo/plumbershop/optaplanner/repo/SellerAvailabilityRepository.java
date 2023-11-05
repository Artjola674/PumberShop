package com.ikubinfo.plumbershop.optaplanner.repo;

import com.ikubinfo.plumbershop.optaplanner.model.SellerAvailabilityDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SellerAvailabilityRepository extends MongoRepository<SellerAvailabilityDocument, String> {


    List<SellerAvailabilityDocument> findByStartDateTimeGreaterThanEqualAndEndDateTimeLessThanEqual(LocalDateTime startDate, LocalDateTime endDate);
}
