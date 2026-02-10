package org.ashkelyonok.authservice.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Response DTO containing authentication tokens")
public class AuthResponseDto {

    @Schema(description = "JWT Access Token",
            example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "Refresh Token",
            example = "d87a6d87-6f8a-4c8d...")
    private String refreshToken;

    @Schema(description = "Token Type",
            example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";
}
