package com.ikubinfo.plumbershop.optaplanner.model;

import com.ikubinfo.plumbershop.common.model.BaseDocument;
import com.ikubinfo.plumbershop.optaplanner.enums.SellerAvailabilityState;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerAvailabilityDocument extends BaseDocument {
    private UserDocument seller;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private SellerAvailabilityState sellerAvailabilityState;
}
