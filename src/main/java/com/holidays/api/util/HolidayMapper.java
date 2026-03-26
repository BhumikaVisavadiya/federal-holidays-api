package com.holidays.api.util;

import com.holidays.api.dto.HolidayRequest;
import com.holidays.api.dto.HolidayResponse;
import com.holidays.api.model.FederalHoliday;
import org.springframework.stereotype.Component;

@Component
public class HolidayMapper {

    public FederalHoliday toEntity(HolidayRequest request) {
        return FederalHoliday.builder()
                .name(request.getName())
                .date(request.getDate())
                .country(request.getCountry())
                .description(request.getDescription())
                .build();
    }

    public HolidayResponse toResponse(FederalHoliday holiday) {
        return HolidayResponse.builder()
                .id(holiday.getId())
                .name(holiday.getName())
                .date(holiday.getDate())
                .country(holiday.getCountry())
                .description(holiday.getDescription())
                .build();
    }

    public void updateEntity(FederalHoliday holiday, HolidayRequest request) {
        holiday.setName(request.getName());
        holiday.setDate(request.getDate());
        holiday.setCountry(request.getCountry());
        holiday.setDescription(request.getDescription());
    }
}
