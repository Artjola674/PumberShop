package com.ikubinfo.plumbershop.email.dto;

import com.ikubinfo.plumbershop.order.model.OrderDocument;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmationRequest {

    private OrderDocument orderDocument;
}
