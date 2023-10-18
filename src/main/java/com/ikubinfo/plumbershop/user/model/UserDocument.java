package com.ikubinfo.plumbershop.user.model;

import com.ikubinfo.plumbershop.common.model.BaseDocument;
import com.ikubinfo.plumbershop.user.dto.Address;
import com.ikubinfo.plumbershop.user.enums.Department;
import com.ikubinfo.plumbershop.user.enums.Role;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class UserDocument extends BaseDocument {
    private String firstName;
    private String lastName;
    @Indexed(unique = true)
    private String email;
    private Role role;
    private String password;
    private Department department;
    private String phone;
    private Address address;
}
