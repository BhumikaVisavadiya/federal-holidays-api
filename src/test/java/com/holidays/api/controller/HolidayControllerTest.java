package com.holidays.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.holidays.api.dto.HolidayRequest;
import com.holidays.api.dto.HolidayResponse;
import com.holidays.api.dto.UploadResult;
import com.holidays.api.exception.DuplicateHolidayException;
import com.holidays.api.exception.HolidayNotFoundException;
import com.holidays.api.model.Country;
import com.holidays.api.service.HolidayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HolidayController.class)
class HolidayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HolidayService holidayService;

    private HolidayResponse sampleResponse;
    private HolidayRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleResponse = HolidayResponse.builder()
                .id(1L)
                .name("Canada Day")
                .date(LocalDate.of(2026, 7, 1))
                .country(Country.CANADA)
                .description("National holiday")
                .build();

        sampleRequest = HolidayRequest.builder()
                .name("Canada Day")
                .date(LocalDate.of(2026, 7, 1))
                .country(Country.CANADA)
                .description("National holiday")
                .build();
    }

    @Test
    void listHolidays_returnsAllHolidays() throws Exception {
        when(holidayService.getAllHolidays()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/holidays"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Canada Day"))
                .andExpect(jsonPath("$[0].country").value("CANADA"));
    }

    @Test
    void listHolidays_withCountryFilter_returnsFilteredHolidays() throws Exception {
        when(holidayService.getHolidaysByCountry(Country.CANADA)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/holidays").param("country", "CANADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].country").value("CANADA"));
    }

    @Test
    void listHolidays_withCountryAndYearFilter_returnsFilteredHolidays() throws Exception {
        when(holidayService.getHolidaysByCountryAndYear(Country.CANADA, 2026))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/holidays")
                        .param("country", "CANADA")
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Canada Day"));
    }

    @Test
    void getHoliday_returnsHoliday_whenFound() throws Exception {
        when(holidayService.getHolidayById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/holidays/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Canada Day"));
    }

    @Test
    void getHoliday_returns404_whenNotFound() throws Exception {
        when(holidayService.getHolidayById(99L)).thenThrow(new HolidayNotFoundException(99L));

        mockMvc.perform(get("/api/v1/holidays/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createHoliday_returns201_withValidRequest() throws Exception {
        when(holidayService.createHoliday(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/holidays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Canada Day"));
    }

    @Test
    void createHoliday_returns400_withMissingName() throws Exception {
        HolidayRequest invalidRequest = HolidayRequest.builder()
                .date(LocalDate.of(2026, 7, 1))
                .country(Country.CANADA)
                .build();

        mockMvc.perform(post("/api/v1/holidays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void createHoliday_returns400_withMissingDate() throws Exception {
        HolidayRequest invalidRequest = HolidayRequest.builder()
                .name("Canada Day")
                .country(Country.CANADA)
                .build();

        mockMvc.perform(post("/api/v1/holidays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.date").exists());
    }

    @Test
    void createHoliday_returns409_whenDuplicate() throws Exception {
        when(holidayService.createHoliday(any()))
                .thenThrow(new DuplicateHolidayException(LocalDate.of(2026, 7, 1), "CANADA"));

        mockMvc.perform(post("/api/v1/holidays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateHoliday_returns200_withValidRequest() throws Exception {
        when(holidayService.updateHoliday(eq(1L), any())).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/holidays/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Canada Day"));
    }

    @Test
    void updateHoliday_returns404_whenNotFound() throws Exception {
        when(holidayService.updateHoliday(eq(99L), any()))
                .thenThrow(new HolidayNotFoundException(99L));

        mockMvc.perform(put("/api/v1/holidays/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadHolidays_returns200_withValidCsvFile() throws Exception {
        UploadResult uploadResult = UploadResult.builder()
                .successCount(1)
                .failureCount(0)
                .created(List.of(sampleResponse))
                .errors(List.of())
                .build();

        when(holidayService.uploadHolidays(any())).thenReturn(uploadResult);

        MockMultipartFile file = new MockMultipartFile(
                "file", "holidays.csv", "text/csv",
                "name,date,country,description\nCanada Day,2026-07-01,CANADA,".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/holidays/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.failureCount").value(0));
    }

    @Test
    void uploadHolidays_returns400_whenServiceThrowsIllegalArgument() throws Exception {
        when(holidayService.uploadHolidays(any()))
                .thenThrow(new IllegalArgumentException("Only CSV files are supported"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "holidays.txt", "text/plain", "data".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/holidays/upload").file(file))
                .andExpect(status().isBadRequest());
    }
}
