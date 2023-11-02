package com.ikubinfo.plumbershop.optaplanner.mapper;

import com.ikubinfo.plumbershop.optaplanner.dto.SellerAvailabilityDto;
import com.ikubinfo.plumbershop.optaplanner.model.SellerAvailabilityDocument;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface SellerAvailabilityMapper {

    SellerAvailabilityDto toDto(SellerAvailabilityDocument document);

    SellerAvailabilityDocument toDocument(SellerAvailabilityDto dto);

    SellerAvailabilityDocument updateAvailabilityFromDto(SellerAvailabilityDto dto, @MappingTarget SellerAvailabilityDocument document);
}
