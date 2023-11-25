package com.ikubinfo.plumbershop.optaplanner.service.impl;

import com.ikubinfo.plumbershop.common.dto.Filter;
import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;
import com.ikubinfo.plumbershop.optaplanner.dto.SellerAvailabilityDto;
import com.ikubinfo.plumbershop.optaplanner.mapper.SellerAvailabilityMapper;
import com.ikubinfo.plumbershop.optaplanner.model.SellerAvailabilityDocument;
import com.ikubinfo.plumbershop.optaplanner.repo.SellerAvailabilityRepository;
import com.ikubinfo.plumbershop.optaplanner.service.SellerAvailabilityService;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.ikubinfo.plumbershop.common.constants.Constants.*;
import static com.ikubinfo.plumbershop.optaplanner.constants.Constants.SELLER_AVAILABILITY;

@Service
public class SellerAvailabilityServiceImpl implements SellerAvailabilityService {

    private final SellerAvailabilityRepository sellerAvailabilityRepository;
    private final SellerAvailabilityMapper mapper;

    public SellerAvailabilityServiceImpl(SellerAvailabilityRepository sellerAvailabilityRepository) {
        this.sellerAvailabilityRepository = sellerAvailabilityRepository;
        this.mapper = Mappers.getMapper(SellerAvailabilityMapper.class);
    }

    @Override
    public SellerAvailabilityDto save(SellerAvailabilityDto dto) {
        SellerAvailabilityDocument doc = mapper.toDocument(dto);
        sellerAvailabilityRepository.save(doc);
        return mapper.toDto(doc);
    }

    @Override
    public List<SellerAvailabilityDocument> findAllByDates(LocalDateTime startDate, LocalDateTime endDate) {
        return sellerAvailabilityRepository
                .findByStartDateTimeGreaterThanEqualAndEndDateTimeLessThanEqual(startDate,endDate);
    }

    @Override
    public Page<SellerAvailabilityDto> findAll(Filter filter) {
        Pageable pageable = PageRequest.of(filter.getPageNumber(), filter.getPageSize(),
                Sort.by(Sort.Direction.valueOf(filter.getSortType()),
                        UtilClass.getSortField(UserDocument.class, filter.getSortBy())));
        return sellerAvailabilityRepository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public SellerAvailabilityDto getById(String id) {
        SellerAvailabilityDocument availability = findById(id);
        return mapper.toDto(availability);
    }

    @Override
    public SellerAvailabilityDto updateById(String id, SellerAvailabilityDto availabilityDto) {
        SellerAvailabilityDocument availability = findById(id);
        SellerAvailabilityDocument updated = sellerAvailabilityRepository.save(mapper
                .updateAvailabilityFromDto(availabilityDto,availability));
        return mapper.toDto(updated);
    }

    @Override
    public String deleteById(String id) {
        sellerAvailabilityRepository.deleteById(id);
        return DELETED_SUCCESSFULLY.replace(DOCUMENT,SELLER_AVAILABILITY);
    }

    private SellerAvailabilityDocument findById(String id) {
        return sellerAvailabilityRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(SELLER_AVAILABILITY,ID, id));
    }
}
