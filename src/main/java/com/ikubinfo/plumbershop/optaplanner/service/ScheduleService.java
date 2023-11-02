package com.ikubinfo.plumbershop.optaplanner.service;

import com.ikubinfo.plumbershop.optaplanner.dto.ScheduleDto;

public interface ScheduleService {


    ScheduleDto solve(int numberOfDays);
}
