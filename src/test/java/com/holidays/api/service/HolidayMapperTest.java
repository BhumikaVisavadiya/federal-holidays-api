package com.holidays.api.service;

import com.holidays.api.dto.HolidayRequest;
import com.holidays.api.dto.HolidayResponse;
import com.holidays.api.model.Country;
import com.holidays.api.model.FederalHoliday;
import com.holidays.api.util.HolidayMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class HolidayMapperTest {

    private final HolidayMapper mapper = new HolidayMapper();

    @Test
    void toEntity_mapsAllFields() {
        HolidayRequest request = HolidayRequest.builder()
                .name("Canada Day")
                .date(LocalDate.of(2026, 7, 1))
                .country(Country.CANADA)
                .description("National holiday")
                .build();

        FederalHoliday entity = mapper.toEntity(request);

        assertThat(entity.getName()).isEqualTo("Canada Day");
        assertThat(entity.getDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(entity.getCountry()).isEqualTo(Country.CANADA);
        assertThat(entity.getDescription()).isEqualTo("National holiday");
        assertThat(entity.getId()).isNull();
    }

    @Test
    void toResponse_mapsAllFields() {
        FederalHoliday entity = FederalHoliday.builder()
                .id(1L)
                .name("Independence Day")
                .date(LocalDate.of(2026, 7, 4))
                .country(Country.USA)
                .description("USA independence")
                .build();

        HolidayResponse response = mapper.toResponse(entity);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Independence Day");
        assertThat(response.getDate()).isEqualTo(LocalDate.of(2026, 7, 4));
        assertThat(response.getCountry()).isEqualTo(Country.USA);
        assertThat(response.getDescription()).isEqualTo("USA independence");
    }

    @Test
    void updateEntity_updatesAllFields() {
        FederalHoliday entity = FederalHoliday.builder()
                .id(1L)
                .name("Old Name")
                .date(LocalDate.of(2026, 1, 1))
                .country(Country.CANADA)
                .description("Old description")
                .build();

        HolidayRequest request = HolidayRequest.builder()
                .name("New Name")
                .date(LocalDate.of(2026, 12, 25))
                .country(Country.USA)
                .description("New description")
                .build();

        mapper.updateEntity(entity, request);

        assertThat(entity.getName()).isEqualTo("New Name");
        assertThat(entity.getDate()).isEqualTo(LocalDate.of(2026, 12, 25));
        assertThat(entity.getCountry()).isEqualTo(Country.USA);
        assertThat(entity.getDescription()).isEqualTo("New description");
        assertThat(entity.getId()).isEqualTo(1L);
    }
}
