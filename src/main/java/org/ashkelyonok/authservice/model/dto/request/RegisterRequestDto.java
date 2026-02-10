package org.ashkelyonok.authservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.ashkelyonok.authservice.model.enums.Role;

import java.time.LocalDate;

@Data
@Schema(description = "Request DTO containing full data for new user registration")
public class RegisterRequestDto {

    @Schema(description = "User first name",
            example = "John",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @Schema(description = "User surname",
            example = "Doe",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Surname is required")
    @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
    private String surname;

    @Schema(description = "Unique username",
            example = "john_doe_99",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @Schema(description = "User email",
            example = "user@gmail.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Schema(description = "User password",
            example = "StrongP@ss123!",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 255, message = "Password must be between 6 and 255 characters")
    private String password;

    @Schema(description = "User role",
            example = "ROLE_USER",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Role is required")
    private Role role;
}
