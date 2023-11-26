package com.ikubinfo.plumbershop.optaplanner.service;

import com.ikubinfo.plumbershop.common.dto.PageParams;
import com.ikubinfo.plumbershop.optaplanner.dto.ScheduleDto;
import org.springframework.data.domain.Page;

public interface ScheduleService {


    ScheduleDto solve(int numberOfDays);

    Page<ScheduleDto> findAll(PageParams pageParams);

    ScheduleDto getById(String id);

    String deleteById(String id);
}
