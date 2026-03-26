package com.holidays.api.service;

import com.holidays.api.dto.HolidayRequest;
import com.holidays.api.dto.HolidayResponse;
import com.holidays.api.dto.UploadResult;
import com.holidays.api.exception.DuplicateHolidayException;
import com.holidays.api.exception.HolidayNotFoundException;
import com.holidays.api.model.Country;
import com.holidays.api.model.FederalHoliday;
import com.holidays.api.repository.HolidayRepository;
import com.holidays.api.util.CsvParser;
import com.holidays.api.util.HolidayMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HolidayServiceTest {

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private HolidayMapper holidayMapper;

    @Mock
    private CsvParser csvParser;

    @InjectMocks
    private HolidayService holidayService;

    private FederalHoliday sampleHoliday;
    private HolidayRequest sampleRequest;
    private HolidayResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleHoliday = FederalHoliday.builder()
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

        sampleResponse = HolidayResponse.builder()
                .id(1L)
                .name("Canada Day")
                .date(LocalDate.of(2026, 7, 1))
                .country(Country.CANADA)
                .description("National holiday")
                .build();
    }

    @Test
    void getAllHolidays_returnsAllHolidays() {
        when(holidayRepository.findAll()).thenReturn(List.of(sampleHoliday));
        when(holidayMapper.toResponse(sampleHoliday)).thenReturn(sampleResponse);

        List<HolidayResponse> result = holidayService.getAllHolidays();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Canada Day");
    }

    @Test
    void getHolidaysByCountry_returnsFilteredHolidays() {
        when(holidayRepository.findByCountry(Country.CANADA)).thenReturn(List.of(sampleHoliday));
        when(holidayMapper.toResponse(sampleHoliday)).thenReturn(sampleResponse);

        List<HolidayResponse> result = holidayService.getHolidaysByCountry(Country.CANADA);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCountry()).isEqualTo(Country.CANADA);
    }

    @Test
    void getHolidaysByCountryAndYear_returnsFilteredHolidays() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);
        when(holidayRepository.findByCountryAndDateBetween(Country.CANADA, from, to))
                .thenReturn(List.of(sampleHoliday));
        when(holidayMapper.toResponse(sampleHoliday)).thenReturn(sampleResponse);

        List<HolidayResponse> result = holidayService.getHolidaysByCountryAndYear(Country.CANADA, 2026);

        assertThat(result).hasSize(1);
    }

    @Test
    void getHolidayById_returnsHoliday_whenFound() {
        when(holidayRepository.findById(1L)).thenReturn(Optional.of(sampleHoliday));
        when(holidayMapper.toResponse(sampleHoliday)).thenReturn(sampleResponse);

        HolidayResponse result = holidayService.getHolidayById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getHolidayById_throwsNotFoundException_whenNotFound() {
        when(holidayRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> holidayService.getHolidayById(99L))
                .isInstanceOf(HolidayNotFoundException.class);
    }

    @Test
    void createHoliday_savesAndReturnsHoliday() {
        when(holidayRepository.existsByDateAndCountry(sampleRequest.getDate(), sampleRequest.getCountry()))
                .thenReturn(false);
        when(holidayMapper.toEntity(sampleRequest)).thenReturn(sampleHoliday);
        when(holidayRepository.save(sampleHoliday)).thenReturn(sampleHoliday);
        when(holidayMapper.toResponse(sampleHoliday)).thenReturn(sampleResponse);

        HolidayResponse result = holidayService.createHoliday(sampleRequest);

        assertThat(result.getName()).isEqualTo("Canada Day");
        verify(holidayRepository).save(sampleHoliday);
    }

    @Test
    void createHoliday_throwsDuplicateException_whenAlreadyExists() {
        when(holidayRepository.existsByDateAndCountry(sampleRequest.getDate(), sampleRequest.getCountry()))
                .thenReturn(true);

        assertThatThrownBy(() -> holidayService.createHoliday(sampleRequest))
                .isInstanceOf(DuplicateHolidayException.class);

        verify(holidayRepository, never()).save(any());
    }

    @Test
    void updateHoliday_updatesAndReturnsHoliday() {
        when(holidayRepository.findById(1L)).thenReturn(Optional.of(sampleHoliday));
        when(holidayRepository.save(sampleHoliday)).thenReturn(sampleHoliday);
        when(holidayMapper.toResponse(sampleHoliday)).thenReturn(sampleResponse);

        HolidayResponse result = holidayService.updateHoliday(1L, sampleRequest);

        assertThat(result).isNotNull();
        verify(holidayMapper).updateEntity(sampleHoliday, sampleRequest);
    }

    @Test
    void updateHoliday_throwsNotFoundException_whenNotFound() {
        when(holidayRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> holidayService.updateHoliday(99L, sampleRequest))
                .isInstanceOf(HolidayNotFoundException.class);
    }

    @Test
    void updateHoliday_throwsDuplicateException_whenDateConflicts() {
        FederalHoliday existing = FederalHoliday.builder()
                .id(1L)
                .name("Old Holiday")
                .date(LocalDate.of(2026, 1, 1))
                .country(Country.CANADA)
                .build();

        HolidayRequest updatedRequest = HolidayRequest.builder()
                .name("Canada Day")
                .date(LocalDate.of(2026, 7, 1))
                .country(Country.CANADA)
                .build();

        when(holidayRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(holidayRepository.existsByDateAndCountry(updatedRequest.getDate(), updatedRequest.getCountry()))
                .thenReturn(true);

        assertThatThrownBy(() -> holidayService.updateHoliday(1L, updatedRequest))
                .isInstanceOf(DuplicateHolidayException.class);
    }

    @Test
    void uploadHolidays_processesValidCsvFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "holidays.csv", "text/csv", "name,date,country,description\nCanada Day,2026-07-01,CANADA,".getBytes()
        );

        CsvParser.ParseResult parseResult = new CsvParser.ParseResult(List.of(sampleRequest), List.of());
        when(csvParser.parse(file)).thenReturn(parseResult);
        when(holidayRepository.existsByDateAndCountry(any(), any())).thenReturn(false);
        when(holidayMapper.toEntity(sampleRequest)).thenReturn(sampleHoliday);
        when(holidayRepository.save(sampleHoliday)).thenReturn(sampleHoliday);
        when(holidayMapper.toResponse(sampleHoliday)).thenReturn(sampleResponse);

        UploadResult result = holidayService.uploadHolidays(file);

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(0);
    }

    @Test
    void uploadHolidays_rejectsNonCsvFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "holidays.txt", "text/plain", "data".getBytes()
        );

        assertThatThrownBy(() -> holidayService.uploadHolidays(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CSV");
    }

    @Test
    void uploadHolidays_rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "holidays.csv", "text/csv", new byte[0]
        );

        assertThatThrownBy(() -> holidayService.uploadHolidays(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void uploadHolidays_skipsDuplicatesAndRecordsErrors() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "holidays.csv", "text/csv", "data".getBytes()
        );

        CsvParser.ParseResult parseResult = new CsvParser.ParseResult(List.of(sampleRequest), List.of());
        when(csvParser.parse(file)).thenReturn(parseResult);
        when(holidayRepository.existsByDateAndCountry(any(), any())).thenReturn(true);

        UploadResult result = holidayService.uploadHolidays(file);

        assertThat(result.getSuccessCount()).isEqualTo(0);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getErrors().get(0)).contains("duplicate");
    }
}
