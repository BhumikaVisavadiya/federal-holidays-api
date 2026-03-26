package com.holidays.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of a holiday file upload")
public class UploadResult {

    @Schema(description = "Number of holidays successfully imported")
    private int successCount;

    @Schema(description = "Number of records that failed to import")
    private int failureCount;

    @Schema(description = "List of successfully created holidays")
    private List<HolidayResponse> created;

    @Schema(description = "List of error messages for failed rows")
    private List<String> errors;
}
