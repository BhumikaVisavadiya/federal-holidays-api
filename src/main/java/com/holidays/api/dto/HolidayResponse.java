package com.holidays.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.holidays.api.model.Country;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Federal holiday response")
public class HolidayResponse {

    @Schema(description = "Unique identifier", example = "1")
    private Long id;

    @Schema(description = "Name of the holiday", example = "Canada Day")
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Date of the holiday", example = "2026-07-01")
    private LocalDate date;

    @Schema(description = "Country for the holiday", example = "CANADA")
    private Country country;

    @Schema(description = "Optional description")
    private String description;
}
