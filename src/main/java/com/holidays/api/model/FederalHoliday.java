package com.holidays.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
    name = "federal_holidays",
    uniqueConstraints = @UniqueConstraint(columnNames = {"holiday_date", "country"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FederalHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Country country;

    @Column
    private String description;
}
