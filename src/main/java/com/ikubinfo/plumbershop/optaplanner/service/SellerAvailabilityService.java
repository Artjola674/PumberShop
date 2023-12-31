package com.ikubinfo.plumbershop.optaplanner.service;

import com.ikubinfo.plumbershop.common.dto.PageParams;
import com.ikubinfo.plumbershop.optaplanner.dto.SellerAvailabilityDto;
import com.ikubinfo.plumbershop.optaplanner.model.SellerAvailabilityDocument;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface SellerAvailabilityService {
    SellerAvailabilityDto save(SellerAvailabilityDto dto);

    List<SellerAvailabilityDocument> findAllByDates(LocalDateTime startDate, LocalDateTime endDate);

    Page<SellerAvailabilityDto> findAll(PageParams pageParams);

    SellerAvailabilityDto getById(String id);

    SellerAvailabilityDto updateById(String id, SellerAvailabilityDto availabilityDto);

    String deleteById(String id);
}
