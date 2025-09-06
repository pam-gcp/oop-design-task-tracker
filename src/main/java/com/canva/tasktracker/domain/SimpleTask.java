package com.canva.tasktracker.domain;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Foglia del Composite: un task "semplice".
 */
public final class SimpleTask implements Task {
    private final UUID id;
    private final String title;
    private final Priority priority;
    private final Optional<LocalDate> dueDate;
    private boolean done;

    /** Costruttore per nuovi task */
    public SimpleTask(String title, Priority priority, LocalDate dueDate) {
        this(UUID.randomUUID(), title, priority, dueDate, false);
    }

    /** Costruttore completo (per ricarica da file) */
    public SimpleTask(UUID id, String title, Priority priority, LocalDate dueDate, boolean done) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.dueDate = Optional.ofNullable(dueDate);
        this.done = done;
    }

    @Override public UUID id() { return id; }
    @Override public String title() { return title; }
    @Override public Priority priority() { return priority; }
    @Override public Optional<LocalDate> dueDate() { return dueDate; }
    @Override public boolean done() { return done; }

    public void markDone() { this.done = true; }
}
