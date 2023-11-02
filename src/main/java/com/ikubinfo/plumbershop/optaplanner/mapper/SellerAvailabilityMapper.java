package com.ikubinfo.plumbershop.optaplanner.mapper;

import com.ikubinfo.plumbershop.optaplanner.dto.SellerAvailabilityDto;
import com.ikubinfo.plumbershop.optaplanner.model.SellerAvailability;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface SellerAvailabilityMapper {

    SellerAvailabilityDto toDto(SellerAvailability document);

    SellerAvailability toDocument(SellerAvailabilityDto dto);

    SellerAvailability updateAvailabilityFromDto(SellerAvailabilityDto dto, @MappingTarget SellerAvailability document);
}
