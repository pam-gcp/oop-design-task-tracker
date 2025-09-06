package com.canva.tasktracker.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Componente del pattern Composite.
 * Sia SimpleTask (foglia) che TaskGroup (composito) implementano questa interfaccia.
 */
public interface Task {
    UUID id();
    String title();
    Priority priority();                    // per i gruppi useremo una prio "di default"
    Optional<LocalDate> dueDate();          // per i gruppi può essere vuota
    boolean done();

    // Operazioni strutturali: per le foglie sono operazioni non supportate
    default void add(Task child) { throw new UnsupportedOperationException("Not a group"); }
    default void remove(Task child) { throw new UnsupportedOperationException("Not a group"); }
    default List<Task> children() { return List.of(); }
}