package com.ikubinfo.plumbershop.optaplanner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

import static com.ikubinfo.plumbershop.common.constants.Constants.DATETIME_PATTERN;
import static com.ikubinfo.plumbershop.common.constants.Constants.INPUT_NOT_NULL;

@Data
public class SellerAvailabilityDto {

    private String id;

    @NotNull(message = INPUT_NOT_NULL)
    private UserDto seller;
    @NotNull(message = INPUT_NOT_NULL)
    @JsonFormat(pattern = DATETIME_PATTERN)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startDateTime;
    @NotNull(message = INPUT_NOT_NULL)
    @JsonFormat(pattern = DATETIME_PATTERN)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endDateTime;
    @NotNull(message = INPUT_NOT_NULL)
    @Pattern(regexp = "(^UNAVAILABLE$|^UNDESIRED$|^DESIRED$)",
            message = "Availability state must be 'DESIRED', 'UNDESIRED' or 'UNAVAILABLE' ")
    private String sellerAvailabilityState;
}
