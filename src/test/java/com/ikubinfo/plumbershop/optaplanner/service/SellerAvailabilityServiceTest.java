package com.ikubinfo.plumbershop.optaplanner.service;

import com.ikubinfo.plumbershop.common.dto.Filter;
import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;
import com.ikubinfo.plumbershop.optaplanner.dto.SellerAvailabilityDto;
import com.ikubinfo.plumbershop.optaplanner.enums.SellerAvailabilityState;
import com.ikubinfo.plumbershop.optaplanner.mapper.SellerAvailabilityMapper;
import com.ikubinfo.plumbershop.optaplanner.model.SellerAvailabilityDocument;
import com.ikubinfo.plumbershop.optaplanner.repo.SellerAvailabilityRepository;
import com.ikubinfo.plumbershop.optaplanner.service.impl.SellerAvailabilityServiceImpl;
import com.ikubinfo.plumbershop.product.dto.ProductDto;
import com.ikubinfo.plumbershop.product.dto.ProductRequest;
import com.ikubinfo.plumbershop.product.model.ProductDocument;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ikubinfo.plumbershop.common.constants.Constants.*;
import static com.ikubinfo.plumbershop.optaplanner.constants.Constants.SELLER_AVAILABILITY;
import static com.ikubinfo.plumbershop.product.constants.ProductConstants.PRODUCT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class SellerAvailabilityServiceTest {

    @Autowired
    private SellerAvailabilityService underTest;

    @Mock
    private SellerAvailabilityRepository sellerAvailabilityRepository;
    private SellerAvailabilityMapper mapper = Mappers.getMapper(SellerAvailabilityMapper.class);

    @BeforeEach
    void setUp() {
        underTest = new SellerAvailabilityServiceImpl(sellerAvailabilityRepository);
    }

    @Test
    void save_success() {
        SellerAvailabilityDto availabilityDto = createAvailabilityDto();
        SellerAvailabilityDocument availabilityDocument = mapper.toDocument(availabilityDto);

        given(sellerAvailabilityRepository.save(availabilityDocument)).willReturn(availabilityDocument);

        SellerAvailabilityDto result = underTest.save(availabilityDto);

        assertThat(result).isEqualTo(availabilityDto);
    }

    @Test
    void findAllByDates_success() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(2);
        underTest.findAllByDates(start, end);
        verify(sellerAvailabilityRepository,times(1))
                .findByStartDateTimeGreaterThanEqualAndEndDateTimeLessThanEqual(start, end);
    }

    @Test
    void getAllAvailabilities_success() {
        SellerAvailabilityDocument availabilityDocument = createAvailabilityDocument();
        Filter filter = new Filter();
        Page<SellerAvailabilityDocument> mockedPage = new PageImpl<>(List.of(availabilityDocument));

        given(sellerAvailabilityRepository.findAll(any(Pageable.class)))
                .willReturn(mockedPage);

        Page<SellerAvailabilityDto> result = underTest.findAll(filter);

        assertThat(result.getContent().get(0).getStartDateTime()).isEqualTo(availabilityDocument.getStartDateTime());
    }

    @Test
    void getById_success() {
        SellerAvailabilityDto availabilityDto = createAvailabilityDto();
        SellerAvailabilityDocument availabilityDocument = mapper.toDocument(availabilityDto);

        given(sellerAvailabilityRepository.findById(availabilityDto.getId())).willReturn(Optional.of(availabilityDocument));

        SellerAvailabilityDto result = underTest.getById(availabilityDto.getId());

        assertThat(result).isEqualTo(availabilityDto);
    }

    @Test
    void getById_throwException_notFound() {
        String id = "1";
        given(sellerAvailabilityRepository.findById(id)).willReturn(Optional.ofNullable(null));

        assertThatThrownBy(() ->underTest.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("%s not found with %s : '%s' ",SELLER_AVAILABILITY,ID, id);
    }

    @Test
    void updateById_success() {
        SellerAvailabilityDto availabilityDto = createAvailabilityDto();

        SellerAvailabilityDocument availabilityDocument = createAvailabilityDocument();
        String availabilityState = availabilityDocument.getSellerAvailabilityState().toString();

        given(sellerAvailabilityRepository.findById(availabilityDto.getId()))
                .willReturn(Optional.of(availabilityDocument));

        SellerAvailabilityDocument updatedDocument = mapper.updateAvailabilityFromDto(availabilityDto,availabilityDocument);

        given(sellerAvailabilityRepository.save(updatedDocument)).willReturn(updatedDocument);
        SellerAvailabilityDto result = underTest.updateById(availabilityDocument.getId(),availabilityDto);

        assertThat(result.getSellerAvailabilityState()).isNotEqualTo(availabilityState);
        assertThat(result.getSellerAvailabilityState()).isEqualTo(availabilityDto.getSellerAvailabilityState());

    }

    @Test
    void updateById_throwException_notFound() {
        SellerAvailabilityDto availabilityDto = createAvailabilityDto();

        given(sellerAvailabilityRepository.findById(availabilityDto.getId()))
                .willReturn(Optional.ofNullable(null));

        assertThatThrownBy(() ->underTest.updateById(availabilityDto.getId(), availabilityDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("%s not found with %s : '%s' ",SELLER_AVAILABILITY,ID, availabilityDto.getId());

        verify(sellerAvailabilityRepository,never()).save(any());

    }


    @Test
    void deleteById_success() {
        String id = "1";
        String result = underTest.deleteById(id);

        verify(sellerAvailabilityRepository).deleteById(id);

        assertThat(result).isEqualTo(DELETED_SUCCESSFULLY.replace(DOCUMENT,SELLER_AVAILABILITY));
    }

    private SellerAvailabilityDocument createAvailabilityDocument() {
        SellerAvailabilityDocument document = new SellerAvailabilityDocument();
        document.setId("1");
        document.setStartDateTime(LocalDateTime.now());
        document.setEndDateTime(LocalDateTime.now());
        document.setSellerAvailabilityState(SellerAvailabilityState.UNDESIRED);

        return document;
    }

    private SellerAvailabilityDto createAvailabilityDto() {
        SellerAvailabilityDto dto = new SellerAvailabilityDto();
        dto.setId("1");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now());
        dto.setSellerAvailabilityState("DESIRED");

        return dto;
    }
}