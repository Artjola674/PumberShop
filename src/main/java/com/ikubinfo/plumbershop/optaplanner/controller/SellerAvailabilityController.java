package com.ikubinfo.plumbershop.optaplanner.controller;

import com.ikubinfo.plumbershop.common.dto.Filter;
import com.ikubinfo.plumbershop.optaplanner.dto.SellerAvailabilityDto;
import com.ikubinfo.plumbershop.optaplanner.service.SellerAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
public class SellerAvailabilityController {

    private final SellerAvailabilityService sellerAvailabilityService;


    @PostMapping()
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "create list of shifts")
    public ResponseEntity<SellerAvailabilityDto> create(@Valid @RequestBody SellerAvailabilityDto dto) {

        return ResponseEntity.ok(sellerAvailabilityService.save(dto));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get all availabilities")
    public ResponseEntity<Page<SellerAvailabilityDto>> getAllAvailabilities(@Valid @RequestBody Filter filter){
        return ResponseEntity.ok(sellerAvailabilityService.findAll(filter));
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get a availability by ID")
    public ResponseEntity<SellerAvailabilityDto> getAvailabilityById(@PathVariable String id){
        return ResponseEntity.ok(sellerAvailabilityService.getById(id));
    }

    @PutMapping("/id/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update a availability by ID")
    public ResponseEntity<SellerAvailabilityDto> updateAvailabilityById(@PathVariable String id,
                                                  @Valid @RequestBody SellerAvailabilityDto availabilityDto){
        return ResponseEntity.ok(sellerAvailabilityService.updateById(id, availabilityDto));
    }

    @DeleteMapping("/id/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete a availability by ID")
    public ResponseEntity<String> deleteAvailabilityById(@PathVariable String id){
        return ResponseEntity.ok(sellerAvailabilityService.deleteById(id));
    }

}
