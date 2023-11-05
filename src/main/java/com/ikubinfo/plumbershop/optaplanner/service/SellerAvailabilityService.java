package com.ikubinfo.plumbershop.optaplanner.service;

import com.ikubinfo.plumbershop.common.dto.Filter;
import com.ikubinfo.plumbershop.optaplanner.dto.SellerAvailabilityDto;
import com.ikubinfo.plumbershop.optaplanner.model.SellerAvailabilityDocument;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface SellerAvailabilityService {
    SellerAvailabilityDto create(SellerAvailabilityDto dto);

    List<SellerAvailabilityDocument> findAllByDates(LocalDateTime startDate, LocalDateTime endDate);

    Page<SellerAvailabilityDto> getAllAvailabilities(Filter filter);

    SellerAvailabilityDto getById(String id);

    SellerAvailabilityDto updateById(String id, SellerAvailabilityDto availabilityDto);

    String deleteById(String id);
}
