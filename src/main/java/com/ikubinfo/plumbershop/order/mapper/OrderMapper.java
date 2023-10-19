package com.ikubinfo.plumbershop.order.mapper;

import com.ikubinfo.plumbershop.order.dto.OrderDto;
import com.ikubinfo.plumbershop.order.model.OrderDocument;
import org.mapstruct.Mapper;

@Mapper
public interface OrderMapper {

    OrderDocument toOrderDocument (OrderDto dto);

    OrderDto toOrderDto(OrderDocument document);
}
