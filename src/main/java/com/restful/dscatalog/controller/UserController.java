package com.restful.dscatalog.controller;

import com.restful.dscatalog.dto.user.UserDTO;
import com.restful.dscatalog.dto.user.UserInsertDTO;
import com.restful.dscatalog.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.created;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        Page<UserDTO> users = userService.findAllPaged(pageable);
        return ok(users);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable Long id) {
        UserDTO user = userService.findById(id);
        return ok(user);
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(
            @RequestBody @Valid UserInsertDTO userInsertDTO,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        UserDTO createdUser = userService.insert(userInsertDTO);
        URI uri = uriComponentsBuilder.path("/api/v1/users/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();
        return created(uri).body(createdUser);
    }
}
