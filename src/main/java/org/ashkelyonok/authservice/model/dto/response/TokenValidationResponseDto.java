package org.ashkelyonok.authservice.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Response status of token validation")
public class TokenValidationResponseDto {

    @Schema(description = "Is the token valid?",
            example = "true")
    private boolean valid;

    @Schema(description = "User ID extracted from token",
            example = "105")
    private Long userId;

    @Schema(description = "User role",
            example = "ROLE_USER")
    private String role;

    @Schema(description = "Error details if invalid",
            example = "Token expired")
    private String errorMessage;
}
