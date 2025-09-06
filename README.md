# Design Task Tracker (mini-Jira per Canva)

Applicazione Java SE semplice per gestire progetti (gruppi) e task con priorità e scadenze.  
Dimostra **Object-Oriented Design** + **pattern** + **core tecnologie Java** richieste dall’esame.

---

## ✨ Funzionalità
- Progetti (gruppi) e task (sottotask) con priorità/scadenze
- CLI con comandi: `add-group`, `add`, `list-tree`, `list-flat`, `sort-flat`, `debug-paths`
- Persistenza locale in `%USERPROFILE%\.dtt\tasks.tsv`
- Log applicativo in `%USERPROFILE%\.dtt\app.log`

---

## 📦 Tech & Pattern usati (con giustificazione)
- **Composite**: `Task` (componente), `SimpleTask` (foglia), `TaskGroup` (composito) — per modellare gerarchie progetto→task.
- **Iterator**: `TaskIterator` — scorre la gerarchia senza duplicare logica di visita.
- **Factory**: `TaskFactory` — centralizza la creazione per tipo (`DESIGN/REVIEW/PUBLISH`) e normalizza i titoli.
- **Exception Shielding**: `AppException` + `AppErrorCode` (+ messaggi user-friendly nel `Main`, niente stacktrace in console).
- **Logging**: `java.util.logging` su file `%USERPROFILE%\.dtt\app.log`.
- **Collections**: `List`, `Map`, `Deque`, `Comparator` per gestire nodi e ordinamenti.
- **Generics**: uso attraverso collezioni tipizzate e iteratori.
- **Java I/O**: lettura/scrittura TSV con `java.nio.file.*`.
- **JUnit**: test minimi su `Validator`, `Factory`, `Iterator`.

---

## 🧰 Requisiti & mappatura (rubric)
| Requisito                        | Dove implementato |
|----------------------------------|-------------------|
| Factory                          | `TaskFactory`, `Main:add` |
| Composite                        | `Task`, `SimpleTask`, `TaskGroup` |
| Iterator                         | `TaskIterator`, comandi `list-flat`, `sort-flat` |
| Exception Shielding              | `AppException`, `AppErrorCode`, `Validator`, `Main` |
| Collections Framework            | uso diffuso (`List`, `Map`, `Deque`, `Comparator`) |
| Generics                         | collezioni tipizzate + iteratore |
| Java I/O                         | `Main.loadFromFile/saveToFile` |
| Logging                          | `LoggerUtil` → `%USERPROFILE%\.dtt\app.log` |
| JUnit Testing                    | `ValidatorTest`, `FactoryTest`, `IteratorTest` |
| Input sanitization               | `Validator` (title/uuid/date/enum) |
| No hardcoded secrets             | non presenti |
| Controlled exception propagation | `AppException` + catch differenziati |

---

## ▶️ Esecuzione (Windows)
Compila e crea il jar:
```bat
mvn clean package

Mostra help:
java -jar target\design-task-tracker-0.1.0-SNAPSHOT.jar

Esempio rapido:
java -jar target\design-task-tracker-0.1.0-SNAPSHOT.jar add-group "Brand Refresh"
REM prendi l'UUID stampato e sostituiscilo a <GID>
java -jar target\design-task-tracker-0.1.0-SNAPSHOT.jar add ROOT DESIGN HIGH 2025-09-20 "Task root"
java -jar target\design-task-tracker-0.1.0-SNAPSHOT.jar add <GID> REVIEW MEDIUM 2025-09-10 "Crea poster"
java -jar target\design-task-tracker-0.1.0-SNAPSHOT.jar list-tree
java -jar target\design-task-tracker-0.1.0-SNAPSHOT.jar sort-flat


Test
Lanciare i test JUnit:
mvn test

UML
classDiagram
    direction TB

    class Task {
      <<interface>>
      +UUID id()
      +String title()
      +Priority priority()
      +Optional<LocalDate> dueDate()
      +boolean done()
      +void add(Task)*
      +void remove(Task)*
      +List<Task> children()*
    }

    class SimpleTask {
      -UUID id
      -String title
      -Priority priority
      -Optional<LocalDate> dueDate
      -boolean done
    }

    class TaskGroup {
      -UUID id
      -String title
      -List<Task> children
    }

    class TaskIterator
    class TaskFactory
    class Validator
    class AppException
    class AppErrorCode
    class LoggerUtil

    Task <|.. SimpleTask
    Task <|.. TaskGroup
    TaskIterator --> Task : iterate
    TaskFactory --> Task : create()
    Validator --> AppException
    AppException --> AppErrorCode

