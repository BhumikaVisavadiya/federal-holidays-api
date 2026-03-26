package com.holidays.api.service;

import com.holidays.api.model.Country;
import com.holidays.api.util.CsvParser;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class CsvParserTest {

    private final CsvParser csvParser = new CsvParser();

    @Test
    void parse_returnsValidRequests_forWellFormedCsv() throws IOException {
        String csv = "name,date,country,description\nCanada Day,2026-07-01,CANADA,National holiday\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csv.getBytes());

        CsvParser.ParseResult result = csvParser.parse(file);

        assertThat(result.valid()).hasSize(1);
        assertThat(result.errors()).isEmpty();
        assertThat(result.valid().get(0).getName()).isEqualTo("Canada Day");
        assertThat(result.valid().get(0).getCountry()).isEqualTo(Country.CANADA);
    }

    @Test
    void parse_returnsError_forInvalidHeaders() throws IOException {
        String csv = "title,when,where\nCanada Day,2026-07-01,CANADA\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csv.getBytes());

        CsvParser.ParseResult result = csvParser.parse(file);

        assertThat(result.valid()).isEmpty();
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0)).containsIgnoringCase("Invalid CSV format");
    }

    @Test
    void parse_returnsError_forInvalidDate() throws IOException {
        String csv = "name,date,country,description\nCanada Day,07-01-2026,CANADA,\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csv.getBytes());

        CsvParser.ParseResult result = csvParser.parse(file);

        assertThat(result.valid()).isEmpty();
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0)).containsIgnoringCase("date");
    }

    @Test
    void parse_returnsError_forInvalidCountry() throws IOException {
        String csv = "name,date,country,description\nSome Holiday,2026-01-01,FRANCE,\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csv.getBytes());

        CsvParser.ParseResult result = csvParser.parse(file);

        assertThat(result.valid()).isEmpty();
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0)).containsIgnoringCase("country");
    }

    @Test
    void parse_returnsError_forBlankName() throws IOException {
        String csv = "name,date,country,description\n ,2026-07-01,CANADA,\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csv.getBytes());

        CsvParser.ParseResult result = csvParser.parse(file);

        assertThat(result.valid()).isEmpty();
        assertThat(result.errors()).hasSize(1);
    }

    @Test
    void parse_handlesMultipleRowsMixedValidity() throws IOException {
        String csv = "name,date,country,description\n" +
                "Canada Day,2026-07-01,CANADA,\n" +
                "Bad Row,not-a-date,USA,\n" +
                "Independence Day,2026-07-04,USA,\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csv.getBytes());

        CsvParser.ParseResult result = csvParser.parse(file);

        assertThat(result.valid()).hasSize(2);
        assertThat(result.errors()).hasSize(1);
    }

    @Test
    void parse_handlesUsaCountry() throws IOException {
        String csv = "name,date,country,description\nIndependence Day,2026-07-04,USA,\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csv.getBytes());

        CsvParser.ParseResult result = csvParser.parse(file);

        assertThat(result.valid()).hasSize(1);
        assertThat(result.valid().get(0).getCountry()).isEqualTo(Country.USA);
    }

    @Test
    void parse_handlesEmptyBody() throws IOException {
        String csv = "name,date,country,description\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csv.getBytes());

        CsvParser.ParseResult result = csvParser.parse(file);

        assertThat(result.valid()).isEmpty();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void parse_returnsError_forInsufficientColumns() throws IOException {
        String csv = "name,date,country,description\nCanada Day\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csv.getBytes());

        CsvParser.ParseResult result = csvParser.parse(file);

        assertThat(result.errors()).hasSize(1);
    }
}
