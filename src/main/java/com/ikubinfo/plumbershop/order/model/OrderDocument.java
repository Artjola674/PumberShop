package com.ikubinfo.plumbershop.order.model;

import com.ikubinfo.plumbershop.common.model.BaseDocument;
import com.ikubinfo.plumbershop.order.dto.Bill;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class OrderDocument extends BaseDocument {

    private List<OrderItemDocument> orderItems;
    private UserDocument customer;
    private double totalPrice;
    private double earnings;
    private LocalDate date;
    private Bill bill;

}
