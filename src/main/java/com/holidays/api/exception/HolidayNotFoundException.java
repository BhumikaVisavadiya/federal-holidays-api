package com.holidays.api.exception;

public class HolidayNotFoundException extends RuntimeException {
    public HolidayNotFoundException(Long id) {
        super("Holiday not found with id: " + id);
    }
}
