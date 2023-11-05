package com.ikubinfo.plumbershop.optaplanner.dto;

import com.ikubinfo.plumbershop.common.dto.BaseDto;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShiftDto extends BaseDto {
    private UserDto seller;
    private String department;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}
