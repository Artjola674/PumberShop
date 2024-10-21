package com.ikubinfo.plumbershop.email.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {

    private String documentPath;
    private String filename;
    private List<String> emails;
}
