package org.ashkelyonok.authservice.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Response from UserService for FeignClient in AuthService")
public class UserResponseDto {

    @Schema(description = "Unique user identifier",
            example = "1")
    private Long id;

    @Schema(description = "User's email",
            example = "example@gmail.com")
    private String email;
}
