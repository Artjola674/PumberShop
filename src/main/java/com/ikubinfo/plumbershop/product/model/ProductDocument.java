package com.ikubinfo.plumbershop.product.model;

import com.ikubinfo.plumbershop.common.model.BaseDocument;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document
//@QueryEntity
public class ProductDocument extends BaseDocument {

    private String name;
    private String description;
    private Long buyingPrice;
    private Long sellingPrice;
    private Integer count;
    private Map<String,String> attributes;
    private String code;



}
