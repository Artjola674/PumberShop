package com.ikubinfo.plumbershop.order.mapper;

import com.ikubinfo.plumbershop.order.dto.OrderItemDto;
import com.ikubinfo.plumbershop.order.model.OrderItemDocument;
import org.mapstruct.Mapper;

@Mapper
public interface OrderItemMapper {

    OrderItemDto toOrderItemDto(OrderItemDocument document);

    OrderItemDocument toOrderItemDocument(OrderItemDto dto);
}
