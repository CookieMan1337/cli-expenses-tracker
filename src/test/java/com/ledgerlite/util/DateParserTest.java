package com.ledgerlite.util;

import com.ledgerlite.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateParserTest {

    @Test
    void shouldParseIsoDate() {
        LocalDate date = DateParser.parse("2026-02-13");

        assertEquals(2026, date.getYear());
        assertEquals(2, date.getMonthValue());
        assertEquals(13, date.getDayOfMonth());
    }

    @Test
    void shouldParseRussianFormat() {
        LocalDate date = DateParser.parse("13.02.2026");

        assertEquals(2026, date.getYear());
        assertEquals(2, date.getMonthValue());
        assertEquals(13, date.getDayOfMonth());
    }

    @Test
    void shouldParseSlashFormat() {
        LocalDate date = DateParser.parse("13/02/2026");

        assertEquals(2026, date.getYear());
        assertEquals(2, date.getMonthValue());
        assertEquals(13, date.getDayOfMonth());
    }

    @Test
    void shouldThrowOnInvalidFormat() {
        assertThrows(ValidationException.class,
                () -> DateParser.parse("invalid-date"));
    }

    @Test
    void shouldThrowOnBlankInput() {
        assertThrows(ValidationException.class,
                () -> DateParser.parse(""));
    }

    @Test
    void shouldParseOrDefaultWithEmptyInput() {
        LocalDate defaultDate = LocalDate.of(2026, 1, 1);
        LocalDate result = DateParser.parseOrDefault("", defaultDate);

        assertEquals(defaultDate, result);
    }

    @Test
    void shouldParseOrDefaultWithNullInput() {
        LocalDate defaultDate = LocalDate.of(2026, 1, 1);
        LocalDate result = DateParser.parseOrDefault(null, defaultDate);

        assertEquals(defaultDate, result);
    }

    @Test
    void shouldReturnToday() {
        LocalDate today = DateParser.today();

        assertEquals(LocalDate.now(), today);
    }
}