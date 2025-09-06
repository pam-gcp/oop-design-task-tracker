package com.canva.tasktracker.patterns.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/** Wrapper che filtra un iteratore in base a un Predicate. */
public final class FilteredIterator<T> implements Iterator<T> {
    private final Iterator<T> delegate;
    private final Predicate<T> predicate;
    private T next;

    public FilteredIterator(Iterator<T> delegate, Predicate<T> predicate) {
        this.delegate = delegate;
        this.predicate = predicate;
        advance();
    }

    private void advance() {
        next = null;
        while (delegate.hasNext()) {
            T candidate = delegate.next();
            if (predicate.test(candidate)) {
                next = candidate;
                break;
            }
        }
    }

    @Override public boolean hasNext() { return next != null; }

    @Override public T next() {
        if (next == null) throw new NoSuchElementException();
        T out = next;
        advance();
        return out;
    }
}