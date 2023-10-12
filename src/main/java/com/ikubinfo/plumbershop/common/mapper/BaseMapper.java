package com.ikubinfo.plumbershop.common.mapper;

import com.ikubinfo.plumbershop.common.dto.BaseDto;
import com.ikubinfo.plumbershop.common.model.BaseDocument;
import org.mapstruct.Mapper;

@Mapper
public interface BaseMapper {

    BaseDto toDto(BaseDocument document);

    BaseDocument toDocument(BaseDto dto);

}
