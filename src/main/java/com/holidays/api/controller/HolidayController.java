package com.holidays.api.controller;

import com.holidays.api.dto.HolidayRequest;
import com.holidays.api.dto.HolidayResponse;
import com.holidays.api.dto.UploadResult;
import com.holidays.api.model.Country;
import com.holidays.api.service.HolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
@Tag(name = "Federal Holidays", description = "APIs for managing federal holidays for Canada and USA")
public class HolidayController {

    private final HolidayService holidayService;

    @GetMapping
    @Operation(summary = "List all holidays", description = "Returns all federal holidays, optionally filtered by country and/or year")
    @ApiResponse(responseCode = "200", description = "List of holidays")
    public ResponseEntity<List<HolidayResponse>> listHolidays(
            @Parameter(description = "Filter by country (CANADA or USA)")
            @RequestParam(required = false) Country country,
            @Parameter(description = "Filter by year (e.g. 2024)")
            @RequestParam(required = false) Integer year) {

        List<HolidayResponse> holidays;

        if (country != null && year != null) {
            holidays = holidayService.getHolidaysByCountryAndYear(country, year);
        } else if (country != null) {
            holidays = holidayService.getHolidaysByCountry(country);
        } else {
            holidays = holidayService.getAllHolidays();
        }

        return ResponseEntity.ok(holidays);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get holiday by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Holiday found"),
            @ApiResponse(responseCode = "404", description = "Holiday not found", content = @Content)
    })
    public ResponseEntity<HolidayResponse> getHoliday(
            @Parameter(description = "Holiday ID") @PathVariable Long id) {
        return ResponseEntity.ok(holidayService.getHolidayById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new holiday")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Holiday created"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "409", description = "Duplicate holiday for same date and country", content = @Content)
    })
    public ResponseEntity<HolidayResponse> createHoliday(
            @Valid @RequestBody HolidayRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(holidayService.createHoliday(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing holiday")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Holiday updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "404", description = "Holiday not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Duplicate holiday for same date and country", content = @Content)
    })
    public ResponseEntity<HolidayResponse> updateHoliday(
            @Parameter(description = "Holiday ID") @PathVariable Long id,
            @Valid @RequestBody HolidayRequest request) {
        return ResponseEntity.ok(holidayService.updateHoliday(id, request));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload holidays from a CSV file",
            description = "Accepts a CSV file with columns: name, date (yyyy-MM-dd), country (CANADA or USA), description"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File processed",
                    content = @Content(schema = @Schema(implementation = UploadResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file format", content = @Content)
    })
    public ResponseEntity<UploadResult> uploadHolidays(
            @Parameter(description = "CSV file containing holidays")
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(holidayService.uploadHolidays(file));
    }
}
