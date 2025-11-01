package com.restful.dscatalog.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateDTO(
        @Size(min = 2, max = 60)
        @Pattern(regexp = "^[\\p{L} .'-]{2,60}$")
        String firstName,

        @Size(min = 2, max = 60)
        @Pattern(regexp = "^[\\p{L} .'-]{2,60}$")
        String lastName,

        @Email(message = "Email must be valid")
        @Size(max = 120)
        String email
) {
}
