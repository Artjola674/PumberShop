package com.ikubinfo.plumbershop.optaplanner.repo;

import com.ikubinfo.plumbershop.optaplanner.model.SellerAvailability;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SellerAvailabilityRepository extends MongoRepository<SellerAvailability, String> {


    List<SellerAvailability> findByStartDateTimeGreaterThanEqualAndEndDateTimeLessThanEqual(LocalDateTime startDate, LocalDateTime endDate);
}
