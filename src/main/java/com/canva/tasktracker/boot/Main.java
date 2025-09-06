package com.canva.tasktracker.boot;

import com.canva.tasktracker.domain.*;
import com.canva.tasktracker.patterns.factory.TaskFactory;
import com.canva.tasktracker.patterns.iterator.TaskIterator;
import com.canva.tasktracker.support.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * CLI con Composite + Iterator + Factory + Exception Shielding + Logging
 */
public class Main {
    private static final List<Task> ROOTS = new ArrayList<>();
    private static final Map<UUID, Task> INDEX = new HashMap<>();
    private static final Path DATA_DIR  = Path.of(System.getProperty("user.home"), ".dtt");
    private static final Path DATA_FILE = DATA_DIR.resolve("tasks.tsv");
    private static final Logger LOG = LoggerUtil.get();

    public static void main(String[] args) {
        try {
            loadFromFile();
        } catch (IOException e) {
            LOG.warning("Errore caricamento: " + e.getMessage());
            System.out.println("Attenzione: errore I/O caricando i dati.");
        }

        if (args.length == 0) {
            printHelp();
            return;
        }

        try {
            switch (args[0]) {
                case "add-group" -> {
                    Validator.requireArgs(args, 2, "Uso: add-group \"TitoloProgetto\"");
                    String title = Validator.title(joinArgs(args, 1));
                    TaskGroup g = new TaskGroup(title);
                    ROOTS.add(g);
                    INDEX.put(g.id(), g);
                    System.out.println("Creato gruppo: " + g.title() + " | id=" + g.id());
                    saveToFile();
                }
                case "add" -> {
                    // add <PARENT_ID or ROOT> <TYPE> <PRIORITY> <YYYY-MM-DD> "TitoloTask"
                    Validator.requireArgs(args, 6,
                            "Uso: add <PARENT_ID or ROOT> <TYPE> <PRIORITY> <YYYY-MM-DD> \"TitoloTask\"");
                    String parentArg = args[1];
                    TaskType type = Validator.taskType(args[2]);
                    Priority pr = Validator.priority(args[3]);
                    LocalDate due = Validator.dateISO(args[4]);
                    String title = Validator.title(joinArgs(args, 5));

                    Task t = TaskFactory.create(type, title, pr, due);

                    if (parentArg.equalsIgnoreCase("ROOT")) {
                        ROOTS.add(t);
                    } else {
                        UUID pid = Validator.uuid(parentArg);
                        Task parent = INDEX.get(pid);
                        if (parent == null) throw new AppException(AppErrorCode.VALIDATION, "Parent non trovato: " + pid);
                        parent.add(t);
                    }
                    INDEX.put(t.id(), t);
                    System.out.println("Creato task: " + t.title() + " | id=" + t.id());
                    saveToFile();
                }
                case "list-tree" -> printTree();
                case "list-flat" -> printFlat();
                case "sort-flat" -> printSortedSimpleTasks();
                case "debug-paths" -> debugPaths();
                default -> {
                    System.out.println("Comando sconosciuto.");
                    printHelp();
                }
            }
        } catch (AppException ae) {
            LOG.info(ae.code() + " - " + ae.getMessage());
            System.out.println("Errore: " + ae.getMessage());
        } catch (Exception ex) {
            LOG.severe("INTERNAL - " + ex);
            System.out.println("Errore interno. Controlla i log in " + DATA_DIR);
        }
    }

    /* =================== HELP =================== */

    private static void printHelp() {
        System.out.println("Design Task Tracker ▶ Composite + Factory + Iterator + TSV");
        System.out.println("Comandi:");
        System.out.println("  add-group \"TitoloProgetto\"");
        System.out.println("  add <PARENT_ID or ROOT> <TYPE> <PRIORITY> <YYYY-MM-DD> \"TitoloTask\"");
        System.out.println("      TYPE: DESIGN | REVIEW | PUBLISH");
        System.out.println("  list-tree");
        System.out.println("  list-flat");
        System.out.println("  sort-flat");
        System.out.println("  debug-paths");
        System.out.println();
        System.out.println("Esempi:");
        System.out.println("  add-group \"Brand Refresh\"");
        System.out.println("  add ROOT DESIGN HIGH 2025-09-20 \"Task root\"");
        System.out.println("  add <UUID_GRUPPO> REVIEW MEDIUM 2025-09-10 \"Crea poster\"");
    }

    /* =================== STAMPE =================== */

    private static void printTree() {
        if (ROOTS.isEmpty()) {
            System.out.println("(Nessun elemento)");
            return;
        }
        for (Task root : ROOTS) printNode(root, 0);
    }

    private static void printNode(Task t, int depth) {
        String indent = "  ".repeat(depth);
        String type = (t instanceof TaskGroup) ? "[G]" : "[T]";
        String due  = t.dueDate().map(LocalDate::toString).orElse("-");
        System.out.println(indent + type + " " + t.title()
                + " | id=" + t.id()
                + " | prio=" + t.priority()
                + " | due=" + due);
        for (Task child : t.children()) printNode(child, depth + 1);
    }

    private static void printFlat() {
        if (ROOTS.isEmpty()) {
            System.out.println("(Nessun elemento)");
            return;
        }
        TaskIterator it = new TaskIterator(ROOTS);
        while (it.hasNext()) {
            Task task = it.next();
            if (task.title().equals("__ROOT__")) continue;
            String type = (task instanceof TaskGroup) ? "[G]" : "[T]";
            String due  = task.dueDate().map(LocalDate::toString).orElse("-");
            System.out.println(type + " " + task.title()
                    + " | id=" + task.id()
                    + " | prio=" + task.priority()
                    + " | due=" + due);
        }
    }

    private static void printSortedSimpleTasks() {
        TaskIterator it = new TaskIterator(ROOTS);
        List<SimpleTask> all = new ArrayList<>();
        while (it.hasNext()) {
            Task task = it.next();
            if (task instanceof SimpleTask st) all.add(st);
        }
        if (all.isEmpty()) {
            System.out.println("(Nessun task)");
            return;
        }
        all.stream()
           .sorted(Comparator
                   .comparing((SimpleTask t) -> t.priority().getWeight()).reversed()
                   .thenComparing(t -> t.dueDate().orElse(LocalDate.MAX)))
           .forEach(t -> System.out.println("- " + t.title()
                   + " | id=" + t.id()
                   + " | prio=" + t.priority()
                   + " | due=" + t.dueDate().map(LocalDate::toString).orElse("-")));
    }

    /* =================== DIAGNOSTICA =================== */

    private static void debugPaths() {
        System.out.println("HOME       : " + System.getProperty("user.home"));
        System.out.println("DATA_DIR   : " + DATA_DIR.toAbsolutePath());
        System.out.println("DATA_FILE  : " + DATA_FILE.toAbsolutePath());
        System.out.println("LOG_FILE   : " + DATA_DIR.resolve("app.log").toAbsolutePath());
        System.out.println("DIR EXISTS : " + Files.exists(DATA_DIR));
        System.out.println("FILE EXISTS: " + Files.exists(DATA_FILE));
    }

    /* ============ PERSISTENZA TSV (semplice + migrazione) ============ */

    private record Row(String type, UUID id, UUID parentId, String title, Priority pr, LocalDate due, boolean done) {}

    private static void loadFromFile() throws IOException {
        ROOTS.clear();
        INDEX.clear();
        if (Files.notExists(DATA_DIR)) Files.createDirectories(DATA_DIR);
        if (Files.notExists(DATA_FILE)) return;

        List<Row> rows = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(DATA_FILE)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\t", -1);
                if (p.length >= 7) {
                    String type = p[0];
                    UUID id = UUID.fromString(p[1]);
                    UUID parent = p[2].isBlank() ? null : UUID.fromString(p[2]);
                    String title = p[3];
                    Priority pr = Priority.valueOf(p[4]);
                    LocalDate due = p[5].isBlank() ? null : LocalDate.parse(p[5]);
                    boolean done = Boolean.parseBoolean(p[6]);
                    rows.add(new Row(type, id, parent, title, pr, due, done));
                }
            }
        }

        Map<UUID, Task> tmp = new HashMap<>();
        for (Row r : rows) {
            Task node = r.type.equals("G")
                    ? new TaskGroup(r.id, r.title)
                    : new SimpleTask(r.id, r.title, r.pr, r.due, r.done);
            tmp.put(r.id, node);
        }

        for (Row r : rows) {
            Task node = tmp.get(r.id);
            if (r.parentId == null) ROOTS.add(node);
            else {
                Task parent = tmp.get(r.parentId);
                if (parent != null) parent.add(node);
                else ROOTS.add(node);
            }
        }
        INDEX.putAll(tmp);
    }

    private static void saveToFile() {
        try {
            if (Files.notExists(DATA_DIR)) Files.createDirectories(DATA_DIR);
            Path tmp = DATA_FILE.resolveSibling("tasks.tmp");
            try (BufferedWriter bw = Files.newBufferedWriter(tmp)) {
                for (Task root : ROOTS) writeNodeRecursive(root, null, bw);
            }
            try {
                Files.move(tmp, DATA_FILE,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception atomicFail) {
                Files.move(tmp, DATA_FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new AppException(AppErrorCode.IO, "Problema di I/O durante il salvataggio", e);
        }
    }

    private static void writeNodeRecursive(Task node, Task parent, BufferedWriter bw) throws IOException {
        String type = (node instanceof TaskGroup) ? "G" : "S";
        String id = node.id().toString();
        String parentId = (parent == null) ? "" : parent.id().toString();
        String title = node.title().replace("\t", " ");
        String pr = node.priority().name();
        String due = node.dueDate().map(LocalDate::toString).orElse("");
        String done = Boolean.toString(node.done());

        bw.write(String.join("\t", type, id, parentId, title, pr, due, done));
        bw.newLine();

        for (Task child : node.children()) {
            writeNodeRecursive(child, node, bw);
        }
    }

    /* =================== UTILITY =================== */

    private static String joinArgs(String[] args, int startIndex) {
        String joined = Arrays.stream(args).skip(startIndex).collect(Collectors.joining(" "));
        return joined.replaceAll("^[\"“”]+|[\"“”]+$", "").trim();
    }
}
