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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final HolidayMapper holidayMapper;
    private final CsvParser csvParser;

    @Transactional(readOnly = true)
    public List<HolidayResponse> getAllHolidays() {
        return holidayRepository.findAll()
                .stream()
                .map(holidayMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HolidayResponse> getHolidaysByCountry(Country country) {
        return holidayRepository.findByCountry(country)
                .stream()
                .map(holidayMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HolidayResponse> getHolidaysByCountryAndYear(Country country, int year) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        return holidayRepository.findByCountryAndDateBetween(country, from, to)
                .stream()
                .map(holidayMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HolidayResponse getHolidayById(Long id) {
        return holidayMapper.toResponse(findHolidayById(id));
    }

    @Transactional
    public HolidayResponse createHoliday(HolidayRequest request) {
        if (holidayRepository.existsByDateAndCountry(request.getDate(), request.getCountry())) {
            throw new DuplicateHolidayException(request.getDate(), request.getCountry().name());
        }
        FederalHoliday saved = holidayRepository.save(holidayMapper.toEntity(request));
        return holidayMapper.toResponse(saved);
    }

    @Transactional
    public HolidayResponse updateHoliday(Long id, HolidayRequest request) {
        FederalHoliday existing = findHolidayById(id);

        boolean dateOrCountryChanged = !existing.getDate().equals(request.getDate())
                || !existing.getCountry().equals(request.getCountry());

        if (dateOrCountryChanged && holidayRepository.existsByDateAndCountry(request.getDate(), request.getCountry())) {
            throw new DuplicateHolidayException(request.getDate(), request.getCountry().name());
        }

        holidayMapper.updateEntity(existing, request);
        return holidayMapper.toResponse(holidayRepository.save(existing));
    }

    @Transactional
    public UploadResult uploadHolidays(MultipartFile file) throws IOException {
        validateCsvFile(file);

        CsvParser.ParseResult parseResult = csvParser.parse(file);

        List<HolidayResponse> created = new ArrayList<>();
        List<String> errors = new ArrayList<>(parseResult.errors());

        for (HolidayRequest request : parseResult.valid()) {
            try {
                created.add(createHoliday(request));
            } catch (DuplicateHolidayException e) {
                errors.add("Skipped duplicate: " + request.getName() + " on " + request.getDate() + " for " + request.getCountry());
            }
        }

        return UploadResult.builder()
                .successCount(created.size())
                .failureCount(errors.size())
                .created(created)
                .errors(errors)
                .build();
    }

    private void validateCsvFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Only CSV files are supported");
        }
    }

    private FederalHoliday findHolidayById(Long id) {
        return holidayRepository.findById(id)
                .orElseThrow(() -> new HolidayNotFoundException(id));
    }
}
