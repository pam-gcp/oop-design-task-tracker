package com.canva.tasktracker;

import com.canva.tasktracker.domain.*;
import com.canva.tasktracker.patterns.iterator.TaskIterator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class IteratorTest {

    @Test
    void dfs_visits_group_and_children() {
        TaskGroup g = new TaskGroup("Progetto X");
        g.add(new SimpleTask("Logo", Priority.HIGH, LocalDate.now()));
        g.add(new SimpleTask("Poster", Priority.MEDIUM, LocalDate.now().plusDays(1)));

        TaskIterator it = new TaskIterator(List.of(g));

        boolean seenGroup = false, seenLogo = false, seenPoster = false;
        while (it.hasNext()) {
            Task t = it.next();
            if (t instanceof TaskGroup && t.title().equals("Progetto X")) seenGroup = true;
            if (t instanceof SimpleTask st && st.title().contains("Logo"))   seenLogo = true;
            if (t instanceof SimpleTask st && st.title().contains("Poster")) seenPoster = true;
        }
        assertTrue(seenGroup && seenLogo && seenPoster, "Iterator deve visitare root e figli");
    }
}
