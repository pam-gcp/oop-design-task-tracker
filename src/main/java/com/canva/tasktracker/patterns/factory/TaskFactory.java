package com.canva.tasktracker.patterns.factory;

import com.canva.tasktracker.domain.Priority;
import com.canva.tasktracker.domain.SimpleTask;
import com.canva.tasktracker.domain.Task;
import com.canva.tasktracker.domain.TaskType;

import java.time.LocalDate;

/**
 * Factory molto semplice che istanzia Task in base al tipo richiesto.
 * Per ora ritorna sempre SimpleTask, ma centralizza le regole di creazione:
 * - default di priorità per tipo
 * - eventuale normalizzazione del titolo
 * In futuro si potrebbero introdurre sottoclassi diverse per tipo.
 */
public final class TaskFactory {

    private TaskFactory() { /* utility class */ }

    public static Task create(TaskType type, String title, Priority priority, LocalDate dueDate) {
        // Se la priorità non è specificata, applica un default ragionevole per il tipo
        Priority effective = (priority != null) ? priority : switch (type) {
            case DESIGN -> Priority.HIGH;
            case REVIEW -> Priority.MEDIUM;
            case PUBLISH -> Priority.MEDIUM;
        };

        String normalizedTitle = normalizeTitle(type, title);
        return new SimpleTask(normalizedTitle, effective, dueDate);
    }

    private static String normalizeTitle(TaskType type, String title) {
        if (title == null || title.isBlank()) return "[" + type + "] Untitled";
        // Aggiunge un prefisso leggero per rendere evidente il tipo a colpo d'occhio
        return "[" + type + "] " + title.trim();
    }
}
