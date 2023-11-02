package com.ikubinfo.plumbershop.optaplanner.controller;

import com.ikubinfo.plumbershop.optaplanner.dto.ScheduleDto;
import com.ikubinfo.plumbershop.optaplanner.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService sellerScheduleService;

    @PostMapping("/solve")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "schedule shifts")
    public ResponseEntity<ScheduleDto> solve(@RequestBody Integer numberOfDays) {

        return ResponseEntity.ok(sellerScheduleService.solve(numberOfDays));
    }
}
