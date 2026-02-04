package org.ashkelyonok.authservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request DTO for validating a token")
public class TokenValidationRequestDto {

    @Schema(description = "The JWT Token to validate",
            example = "eyJhbGci...",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Token is required")
    private String token;
}
