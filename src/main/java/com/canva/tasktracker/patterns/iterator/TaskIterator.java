package com.canva.tasktracker.patterns.iterator;

import com.canva.tasktracker.domain.Task;

import java.util.*;

/**
 * Iterator DFS (Depth-First Search) per scorrere una gerarchia di Task (Composite).
 * Può partire da un singolo root o da una foresta di root.
 */
public final class TaskIterator implements Iterator<Task>, Iterable<Task> {
    private final Deque<Iterator<Task>> stack = new ArrayDeque<>();
    private Task next;

    /** Crea l'iteratore a partire da un singolo nodo root (incluso). */
    public TaskIterator(Task root) {
        this.next = Objects.requireNonNull(root, "root");
        // per il root, prepariamo lo stack con i suoi figli
        stack.push(root.children().iterator());
    }

    /** Crea l'iteratore a partire da una lista di root (inclusi). */
    public TaskIterator(List<? extends Task> roots) {
        Objects.requireNonNull(roots, "roots");
        // Per semplicità, creiamo un nodo fittizio i cui children = roots
        Task fakeRoot = new Task() {
            @Override public UUID id() { return UUID.fromString("00000000-0000-0000-0000-000000000000"); }
            @Override public String title() { return "__ROOT__"; }
            @Override public com.canva.tasktracker.domain.Priority priority() { return com.canva.tasktracker.domain.Priority.MEDIUM; }
            @Override public Optional<java.time.LocalDate> dueDate() { return Optional.empty(); }
            @Override public boolean done() { return false; }
            @Override public List<Task> children() { return (List<Task>) roots; }
        };
        this.next = fakeRoot; // partirà restituendo i veri root
        stack.push(fakeRoot.children().iterator());
    }

    @Override public boolean hasNext() { return next != null; }

    @Override public Task next() {
        if (next == null) throw new NoSuchElementException();
        Task current = next;
        next = advance();
        return current;
    }

    private Task advance() {
        while (!stack.isEmpty()) {
            Iterator<Task> it = stack.peek();
            if (it.hasNext()) {
                Task t = it.next();
                // prima di restituire t al prossimo next(), mettiamo i suoi figli sullo stack
                stack.push(t.children().iterator());
                return t;
            } else {
                stack.pop();
            }
        }
        return null;
    }

    @Override public Iterator<Task> iterator() { return this; }
}
