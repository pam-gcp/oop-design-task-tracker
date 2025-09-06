package com.canva.tasktracker;

import com.canva.tasktracker.support.*;
import com.canva.tasktracker.domain.Priority;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ValidatorTest {

    @Test
    void title_ok_and_strips() {
        String t = Validator.title("  Crea poster  ");
        assertEquals("Crea poster", t);
    }

    @Test
    void title_empty_throws_validation() {
        AppException ex = assertThrows(AppException.class, () -> Validator.title("   "));
        assertEquals(AppErrorCode.VALIDATION, ex.code());
    }

    @Test
    void dateISO_ok() {
        LocalDate d = Validator.dateISO("2025-09-10");
        assertEquals(2025, d.getYear());
    }

    @Test
    void dateISO_bad_format() {
        AppException ex = assertThrows(AppException.class, () -> Validator.dateISO("10/09/2025"));
        assertEquals(AppErrorCode.VALIDATION, ex.code());
    }

    @Test
    void priority_parse_ok() {
        Priority p = Validator.priority("high");
        assertEquals(Priority.HIGH, p);
    }
}
