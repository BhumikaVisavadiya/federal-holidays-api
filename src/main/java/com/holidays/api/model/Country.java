package com.holidays.api.model;

public enum Country {
    CANADA("CA"),
    USA("US");

    private final String code;

    Country(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Country fromCode(String code) {
        for (Country country : values()) {
            if (country.code.equalsIgnoreCase(code)) {
                return country;
            }
        }
        throw new IllegalArgumentException("Unsupported country code: " + code);
    }
}
