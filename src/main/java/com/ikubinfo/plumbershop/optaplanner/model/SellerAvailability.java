package com.ikubinfo.plumbershop.optaplanner.model;

import com.ikubinfo.plumbershop.optaplanner.enums.SellerAvailabilityState;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Document
@Data
public class SellerAvailability {
    @Id
    private String id;

    private UserDocument seller;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private SellerAvailabilityState sellerAvailabilityState;
}
