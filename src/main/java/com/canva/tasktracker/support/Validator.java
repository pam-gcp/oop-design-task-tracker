package com.canva.tasktracker.support;

import com.canva.tasktracker.domain.Priority;
import com.canva.tasktracker.domain.TaskType;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public final class Validator {
    private Validator() {}

    public static String title(String raw) {
        if (raw == null) throw new AppException(AppErrorCode.VALIDATION, "Titolo mancante");
        String t = raw.strip();
        if (t.isEmpty()) throw new AppException(AppErrorCode.VALIDATION, "Titolo vuoto");
        // blocca caratteri di controllo
        if (t.chars().anyMatch(ch -> Character.isISOControl(ch) && ch != '\n' && ch != '\r' && ch != '\t')) {
            throw new AppException(AppErrorCode.VALIDATION, "Titolo contiene caratteri non validi");
        }
        return t;
    }

    public static UUID uuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (Exception e) {
            throw new AppException(AppErrorCode.VALIDATION, "UUID non valido: " + raw);
        }
    }

    public static LocalDate dateISO(String raw) {
        try {
            return LocalDate.parse(raw); // YYYY-MM-DD
        } catch (DateTimeParseException e) {
            throw new AppException(AppErrorCode.VALIDATION, "Data non valida (usa YYYY-MM-DD): " + raw);
        }
    }

    public static Priority priority(String raw) {
        try {
            return Priority.valueOf(raw.toUpperCase());
        } catch (Exception e) {
            throw new AppException(AppErrorCode.VALIDATION, "Priorità non valida (LOW|MEDIUM|HIGH|CRITICAL): " + raw);
        }
    }

    public static TaskType taskType(String raw) {
        try {
            return TaskType.valueOf(raw.toUpperCase());
        } catch (Exception e) {
            throw new AppException(AppErrorCode.VALIDATION, "Tipo task non valido (DESIGN|REVIEW|PUBLISH): " + raw);
        }
    }
    public static void requireArgs(String[] args, int min, String usage) {
    if (args == null || args.length < min) {
        throw new AppException(AppErrorCode.VALIDATION, usage);
    }
}
}
