package com.holidays.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.holidays.api.model.Country;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating or updating a federal holiday")
public class HolidayRequest {

    @NotBlank(message = "Holiday name is required")
    @Schema(description = "Name of the holiday", example = "Canada Day")
    private String name;

    @NotNull(message = "Holiday date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Date of the holiday (yyyy-MM-dd)", example = "2026-07-01")
    private LocalDate date;

    @NotNull(message = "Country is required")
    @Schema(description = "Country for the holiday", example = "CANADA")
    private Country country;

    @Schema(description = "Optional description of the holiday", example = "Celebrates the anniversary of Canadian Confederation")
    private String description;
}
