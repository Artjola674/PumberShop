package com.ikubinfo.plumbershop.order.model;

import com.ikubinfo.plumbershop.product.model.ProductDocument;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Data
public class OrderItemDocument {

    @DBRef
    private ProductDocument product;
    private Integer amount;

}
