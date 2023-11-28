package com.ikubinfo.plumbershop.optaplanner.model;

import com.ikubinfo.plumbershop.common.model.BaseDocument;
import com.ikubinfo.plumbershop.optaplanner.enums.SellerAvailabilityState;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SellerAvailabilityDocument extends BaseDocument {
    private UserDocument seller;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private SellerAvailabilityState sellerAvailabilityState;


}
