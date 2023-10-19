package com.ikubinfo.plumbershop.user.controller;

import com.ikubinfo.plumbershop.security.CurrentUser;
import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import com.ikubinfo.plumbershop.user.dto.UserRequest;
import com.ikubinfo.plumbershop.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;


    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserDto> saveUser(@Valid @RequestBody UserDto userDto,
                                            @CurrentUser CustomUserDetails loggedUser){
        return ResponseEntity.ok(userService.saveUser(userDto, loggedUser));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get all users")
    public ResponseEntity<Page<UserDto>> getAllUsers(@Valid @RequestBody UserRequest userRequest){
        return ResponseEntity.ok(userService.getAllUsers(userRequest));
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get a user by ID")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id,
                                               @CurrentUser CustomUserDetails loggedUser){
        return ResponseEntity.ok(userService.getById(id,loggedUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Update a user by ID")
    public ResponseEntity<UserDto> updateUserById(@PathVariable String id,
                                                  @Valid @RequestBody UserDto userDto,
                                                  @CurrentUser CustomUserDetails loggedUser){
        return ResponseEntity.ok(userService.updateById(id, userDto, loggedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete a user by ID")
    public ResponseEntity<String> deleteUserById(@PathVariable String id){
        return ResponseEntity.ok(userService.deleteById(id));
    }
}
