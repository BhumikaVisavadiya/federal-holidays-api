package com.holidays.api.repository;

import com.holidays.api.model.Country;
import com.holidays.api.model.FederalHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<FederalHoliday, Long> {

    List<FederalHoliday> findByCountry(Country country);

    List<FederalHoliday> findByCountryAndDateBetween(Country country, LocalDate from, LocalDate to);

    Optional<FederalHoliday> findByDateAndCountry(LocalDate date, Country country);

    boolean existsByDateAndCountry(LocalDate date, Country country);
}
