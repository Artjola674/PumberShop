package com.ikubinfo.plumbershop.common.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class BaseDocument extends Auditable{
    @Id
    private String id;
    private boolean deleted;
}
