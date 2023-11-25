package com.ikubinfo.plumbershop.optaplanner.service;

import com.ikubinfo.plumbershop.common.dto.Filter;
import com.ikubinfo.plumbershop.optaplanner.dto.ScheduleDto;
import org.springframework.data.domain.Page;

public interface ScheduleService {


    ScheduleDto solve(int numberOfDays);

    Page<ScheduleDto> findAll(Filter filter);

    ScheduleDto getById(String id);

    String deleteById(String id);
}
