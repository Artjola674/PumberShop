package com.ikubinfo.plumbershop.email;

import com.ikubinfo.plumbershop.email.dto.MessageRequest;
import com.ikubinfo.plumbershop.email.dto.OrderConfirmationRequest;
import com.ikubinfo.plumbershop.order.model.OrderDocument;

public class EmailHealper {

    public static MessageRequest createOrderConfirmationEmailRequest(OrderDocument orderDocument) {
        OrderConfirmationRequest orderConfirmationRequest = new OrderConfirmationRequest(orderDocument);

        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setMessageType(MessageType.ORDER_CONFIRMATION);
        messageRequest.setOrderConfirmationRequest(orderConfirmationRequest);
        return messageRequest;
    }
}
