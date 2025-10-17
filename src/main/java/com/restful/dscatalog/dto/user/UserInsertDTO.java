package com.restful.dscatalog.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

public record UserInsertDTO(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 60, message = "First name must be between 2 and 60 characters")
        @Pattern(regexp = "^[\\p{L} .'-]{2,60}$", message = "First name contains invalid characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 60, message = "Last name must be between 2 and 60 characters")
        @Pattern(regexp = "^[\\p{L} .'-]{2,60}$", message = "Last name contains invalid characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 120, message = "Email must be at most 120 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 60, message = "Password must be between 6 and 60 characters")
        @JsonProperty(access = WRITE_ONLY)
        String password
) {
}

