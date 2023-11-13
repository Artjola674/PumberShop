package com.ikubinfo.plumbershop.optaplanner.repo;

import com.ikubinfo.plumbershop.optaplanner.enums.SellerAvailabilityState;
import com.ikubinfo.plumbershop.optaplanner.model.SellerAvailabilityDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SellerAvailabilityRepositoryTest {

    @Autowired
    private SellerAvailabilityRepository underTest;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void canFindByStartDateTimeGreaterThanEqualAndEndDateTimeLessThanEqual() {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusHours(10);
        SellerAvailabilityDocument sellerAvailability1 = createAvailability(startDate, endDate, SellerAvailabilityState.DESIRED);
        SellerAvailabilityDocument sellerAvailability2 = createAvailability(startDate.plusHours(5), endDate.plusHours(2), SellerAvailabilityState.UNAVAILABLE);
        SellerAvailabilityDocument sellerAvailability3 = createAvailability(startDate.plusHours(5), endDate.minusHours(2), SellerAvailabilityState.DESIRED);
        SellerAvailabilityDocument sellerAvailability4 = createAvailability(startDate.minusHours(5), endDate.plusHours(2), SellerAvailabilityState.UNDESIRED);
        SellerAvailabilityDocument sellerAvailability5 = createAvailability(startDate.minusHours(5), endDate.minusHours(2), SellerAvailabilityState.UNAVAILABLE);

        List<SellerAvailabilityDocument> availabilityList = Arrays.asList(sellerAvailability1, sellerAvailability2, sellerAvailability3,sellerAvailability4,sellerAvailability5);

        underTest.saveAll(availabilityList);

        List<SellerAvailabilityDocument> result = underTest.findByStartDateTimeGreaterThanEqualAndEndDateTimeLessThanEqual(startDate, endDate);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getSellerAvailabilityState()).isEqualTo(SellerAvailabilityState.DESIRED);

    }

    @Test
    void canNotFindByStartDateTimeGreaterThanEqualAndEndDateTimeLessThanEqual() {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusHours(10);
        SellerAvailabilityDocument sellerAvailability1 = createAvailability(startDate, endDate, SellerAvailabilityState.DESIRED);
        SellerAvailabilityDocument sellerAvailability2 = createAvailability(startDate.plusHours(5), endDate.plusHours(2), SellerAvailabilityState.UNAVAILABLE);
        SellerAvailabilityDocument sellerAvailability3 = createAvailability(startDate.plusHours(5), endDate.minusHours(2), SellerAvailabilityState.DESIRED);
        SellerAvailabilityDocument sellerAvailability4 = createAvailability(startDate.minusHours(5), endDate.plusHours(2), SellerAvailabilityState.UNDESIRED);
        SellerAvailabilityDocument sellerAvailability5 = createAvailability(startDate.minusHours(5), endDate.minusHours(2), SellerAvailabilityState.UNAVAILABLE);

        List<SellerAvailabilityDocument> availabilityList = Arrays.asList(sellerAvailability1, sellerAvailability2, sellerAvailability3,sellerAvailability4,sellerAvailability5);

        underTest.saveAll(availabilityList);

        List<SellerAvailabilityDocument> result = underTest.findByStartDateTimeGreaterThanEqualAndEndDateTimeLessThanEqual(startDate.plusHours(11), endDate.minusHours(11));

        assertThat(result.isEmpty()).isTrue();

    }

    private SellerAvailabilityDocument createAvailability(LocalDateTime startDate, LocalDateTime endDate, SellerAvailabilityState state) {
        return SellerAvailabilityDocument.builder()
                .startDateTime(startDate)
                .endDateTime(endDate)
                .sellerAvailabilityState(state)
                .build();
    }
}