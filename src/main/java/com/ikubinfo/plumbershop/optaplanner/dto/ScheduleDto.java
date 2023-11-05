package com.ikubinfo.plumbershop.optaplanner.dto;

import com.ikubinfo.plumbershop.common.dto.BaseDto;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import lombok.Data;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.List;

@Data
public class ScheduleDto extends BaseDto {
    List<SellerAvailabilityDto> availabilityList;

    List<UserDto> employeeList;

    List<ShiftDto> shiftList;

    HardSoftScore score;

}
