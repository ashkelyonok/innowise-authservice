package org.ashkelyonok.authservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request DTO for refreshing an access token")
public class RefreshTokenRequestDto {

    @Schema(description = "Refresh token string",
            example = "d87a6d87-6f8a-4c8d...",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
