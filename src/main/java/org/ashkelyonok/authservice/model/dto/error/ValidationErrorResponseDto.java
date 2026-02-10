package org.ashkelyonok.authservice.model.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Validation Failure Response")
public class ValidationErrorResponseDto extends ErrorResponseDto {

    @Schema(description = "Map of invalid fields and their error messages")
    private Map<String, String> validationErrors;
}
