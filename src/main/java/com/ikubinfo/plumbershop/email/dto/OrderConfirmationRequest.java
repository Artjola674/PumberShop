package com.ikubinfo.plumbershop.email.dto;

import com.ikubinfo.plumbershop.user.model.UserDocument;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmationRequest {

    private String fileName;
    private String fileLocation;
    private UserDocument customer;
}
