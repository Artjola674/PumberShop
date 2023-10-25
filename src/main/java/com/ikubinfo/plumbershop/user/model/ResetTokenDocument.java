package com.ikubinfo.plumbershop.user.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
@Data
@Builder
public class ResetTokenDocument {

    @Id
    private String id;
    private UserDocument user;
    private String token;
    private Date expirationDate;
}
