package com.holidays.api.util;

import com.holidays.api.dto.HolidayRequest;
import com.holidays.api.model.Country;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvParser {

    private static final String[] EXPECTED_HEADERS = {"name", "date", "country", "description"};

    public record ParseResult(List<HolidayRequest> valid, List<String> errors) {}

    public ParseResult parse(MultipartFile file) throws IOException {
        List<HolidayRequest> validRequests = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = reader.readNext();
            if (headers == null || !hasRequiredHeaders(headers)) {
                errors.add("Invalid CSV format. Required columns: name, date, country, description");
                return new ParseResult(validRequests, errors);
            }

            String[] row;
            int lineNumber = 2;
            while ((row = reader.readNext()) != null) {
                try {
                    validRequests.add(parseRow(row, lineNumber));
                } catch (IllegalArgumentException e) {
                    errors.add("Row " + lineNumber + ": " + e.getMessage());
                }
                lineNumber++;
            }
        } catch (CsvValidationException e) {
            errors.add("CSV parsing error: " + e.getMessage());
        }

        return new ParseResult(validRequests, errors);
    }

    private boolean hasRequiredHeaders(String[] headers) {
        for (int i = 0; i < EXPECTED_HEADERS.length - 1; i++) {
            if (i >= headers.length || !EXPECTED_HEADERS[i].equalsIgnoreCase(headers[i].trim())) {
                return false;
            }
        }
        return true;
    }

    private HolidayRequest parseRow(String[] row, int lineNumber) {
        if (row.length < 3) {
            throw new IllegalArgumentException("Insufficient columns. Expected at least: name, date, country");
        }

        String name = row[0].trim();
        String dateStr = row[1].trim();
        String countryStr = row[2].trim();
        String description = row.length > 3 ? row[3].trim() : null;

        if (name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format '" + dateStr + "'. Use yyyy-MM-dd");
        }

        Country country;
        try {
            country = Country.valueOf(countryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid country '" + countryStr + "'. Allowed: CANADA, USA");
        }

        return HolidayRequest.builder()
                .name(name)
                .date(date)
                .country(country)
                .description(description)
                .build();
    }
}
