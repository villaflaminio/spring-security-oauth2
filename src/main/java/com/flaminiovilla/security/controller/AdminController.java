package com.flaminiovilla.security.controller;

import com.flaminiovilla.security.model.dto.AlterUserDto;
import com.flaminiovilla.security.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.flaminiovilla.security.model.User;

import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

        @GetMapping("/testGrants")
        public String hello() {
            return "Hello World!";
        }

    @Operation(summary = "filter", description = "Filtra le palestre")
    @PostMapping("/users/filter")
    public ResponseEntity<Page<User>> filter(
            @RequestBody(required = false) User probe,
            @RequestParam(required = false, defaultValue = "0", name = "page") Integer page,
            @RequestParam(required = false, defaultValue = "10", name = "size") Integer size,
            @RequestParam(required = false, name = "sortField") String sortField,
            @RequestParam(required = false, name = "sortDirection") String sortDirection) {
        return userService.filter(probe, page, size, sortField, sortDirection);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @PutMapping("/user/{id}")
    public ResponseEntity update(@PathVariable("id") Long id, @RequestBody AlterUserDto user) {
        return userService.updateUser(id, user);
    }

}

