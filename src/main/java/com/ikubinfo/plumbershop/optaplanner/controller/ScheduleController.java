package com.ikubinfo.plumbershop.optaplanner.controller;

import com.ikubinfo.plumbershop.common.dto.Filter;
import com.ikubinfo.plumbershop.optaplanner.dto.ScheduleDto;
import com.ikubinfo.plumbershop.optaplanner.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get all")
    public ResponseEntity<Page<ScheduleDto>> getAll(@Valid @RequestBody Filter filter){
        return ResponseEntity.ok(sellerScheduleService.getAll(filter));
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get a schedule by ID")
    public ResponseEntity<ScheduleDto> getScheduleById(@PathVariable String id){
        return ResponseEntity.ok(sellerScheduleService.getById(id));
    }

    @DeleteMapping("/id/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete a schedule by ID")
    public ResponseEntity<String> deleteScheduleById(@PathVariable String id){
        return ResponseEntity.ok(sellerScheduleService.deleteById(id));
    }
}
