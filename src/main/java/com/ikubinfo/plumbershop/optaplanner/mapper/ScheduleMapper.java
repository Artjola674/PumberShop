package com.ikubinfo.plumbershop.optaplanner.mapper;

import com.ikubinfo.plumbershop.optaplanner.dto.ScheduleDto;
import com.ikubinfo.plumbershop.optaplanner.model.ScheduleDocument;
import org.mapstruct.Mapper;

@Mapper
public interface ScheduleMapper {

    ScheduleDocument toDocument (ScheduleDto dto);

    ScheduleDto toDto (ScheduleDocument document);
}
