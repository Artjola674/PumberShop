package com.ikubinfo.plumbershop.auth.model;

import com.ikubinfo.plumbershop.user.model.UserDocument;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document
@Builder
public class RefreshToken {

    @Id
    private String id;
    @DBRef
    private UserDocument user;
    private String token;
    private Date expirationDate;
}
