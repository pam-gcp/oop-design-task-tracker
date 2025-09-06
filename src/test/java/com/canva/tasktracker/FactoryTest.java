package com.canva.tasktracker;

import com.canva.tasktracker.domain.*;
import com.canva.tasktracker.patterns.factory.TaskFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FactoryTest {

    @Test
    void factory_prepends_type_in_title() {
        Task t = TaskFactory.create(TaskType.DESIGN, "Crea poster", Priority.HIGH, LocalDate.now());
        assertTrue(t.title().startsWith("[DESIGN] "), "Il titolo deve avere il prefisso del tipo");
    }

    @Test
    void factory_generates_id_and_priority_not_null() {
        Task t = TaskFactory.create(TaskType.REVIEW, "Revisione", null, LocalDate.now());
        assertNotNull(t.id());
        assertNotNull(t.priority());
    }
}
