package com.holidays.api.exception;

import java.time.LocalDate;

public class DuplicateHolidayException extends RuntimeException {
    public DuplicateHolidayException(LocalDate date, String country) {
        super("A holiday already exists on " + date + " for " + country);
    }
}
