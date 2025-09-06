package com.canva.tasktracker.domain;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Composito del Composite: un "progetto" o "gruppo" che contiene altri Task.
 */
public final class TaskGroup implements Task {
    private final UUID id;
    private final String title;
    private final List<Task> children = new ArrayList<>();

    public TaskGroup(String title) {
        this(UUID.randomUUID(), title);
    }

    /** Costruttore completo (per ricarica da file) */
    public TaskGroup(UUID id, String title) {
        this.id = id;
        this.title = title;
    }

    @Override public UUID id() { return id; }
    @Override public String title() { return title; }

    // Per un gruppo usiamo una priorità "di default" (non incide sul sort dei figli)
    @Override public Priority priority() { return Priority.MEDIUM; }

    // Per i gruppi la scadenza è opzionale e di solito non applicabile
    @Override public Optional<LocalDate> dueDate() { return Optional.empty(); }

    // Un gruppo è "done" se tutti i figli sono done (o se non ha figli, consideriamolo non fatto)
    @Override public boolean done() {
        return !children.isEmpty() && children.stream().allMatch(Task::done);
    }

    @Override public void add(Task child) { children.add(child); }
    @Override public void remove(Task child) { children.remove(child); }
    @Override public List<Task> children() { return Collections.unmodifiableList(children); }

    /** Ritorna tutti i discendenti in profondità (utile per sort/flat list) */
    public List<Task> flatten() {
        List<Task> out = new ArrayList<>();
        Deque<Task> stack = new ArrayDeque<>();
        stack.push(this);
        while (!stack.isEmpty()) {
            Task t = stack.pop();
            out.add(t);
            // push dei figli in ordine inverso per visita "naturale"
            List<Task> cs = new ArrayList<>(t.children());
            Collections.reverse(cs);
            cs.forEach(stack::push);
        }
        return out;
    }

    @Override public String toString() {
        String kids = children.stream().map(Task::title).collect(Collectors.joining(", "));
        return "TaskGroup(" + title + ") -> [" + kids + "]";
    }
}
