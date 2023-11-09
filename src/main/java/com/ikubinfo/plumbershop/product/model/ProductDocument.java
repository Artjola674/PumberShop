package com.ikubinfo.plumbershop.product.model;

import com.ikubinfo.plumbershop.common.model.BaseDocument;
import lombok.Data;

import java.util.Map;

@Data
public class ProductDocument extends BaseDocument {

    private String name;
    private String description;
    private double buyingPrice;
    private double sellingPrice;
    private Integer count;
    private Map<String,String> attributes;
    private String code;



}
