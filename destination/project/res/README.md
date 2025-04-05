# Calendar Management Application

This Java-based command-line application supports calendar event creation, editing, querying, and exporting. It is designed with extensibility and modularity in mind and runs in both **interactive** and **headless** modes. The app has evolved from supporting a single calendar (Assignment 4) to managing multiple calendars with timezones, copy operations, and advanced event manipulation (Assignments 5).

---

## A. Design Changes and Justifications

| Change                                                | Justification                                                                                                |
|--------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| Introduced `ModeHandler` abstraction                  | Modularizes execution logic for interactive and headless modes.                                              |
| Added interfaces for `Calendar` and `CalendarEvent`   | Enables extensibility and future support for alternative calendar implementations.                           |
| Created `AbstractController` superclass               | Reduces code duplication by unifying shared logic across `CalendarControllerBasic` and `CalendarController`. |
| Introduced controller separation (basic vs advanced)  | Supports Assignment 4 via `CalendarControllerBasic`, and Assignments 5 via `CalendarController`.             |
| Integrated Command Pattern                            | Enables clean separation of command parsing and execution (Assignment 5 onward).                             |
| Timezone support per calendar                         | Allows regionalized scheduling and accurate event conversion.                                                |
| Default conflict rejection                            | Standardizes event behavior and removes ambiguity. `--autoDecline` is optional in A4 and default in A5+.     |
| Copy operations across calendars and date ranges      | Facilitates efficient reuse of event structures (e.g., semester templates).                                  |
| Calendar context management (`create`, `use`, `edit`) | Introduced in Assignment 5 for organizing and manipulating multiple calendars.                               |

---

##  B. How to Run the Program

### Requirements:

- Java 11 or higher

### Execution Modes:

```bash
java -jar calendar-app.jar --mode interactive
```

Launches interactive mode, where the user can enter commands live.

```bash
java -jar calendar-app.jar --mode headless commands.txt
```

Executes commands from a text file in sequence. Stops on the first invalid command.

---

## C. Feature Overview

### Core Features (Basic Mode):

- Create single or recurring events
- Edit event fields (name, time, location, etc.)
- Print and query events by date or range
- Check availability (`show status`)
- Export calendar to CSV (Google Calendar compatible)

### Advanced Features (Assignments 5):

- Create and switch between multiple calendars
- Set calendar-specific timezones
- Copy individual or multiple events across calendars
- Edit calendar properties (name, timezone)

### Optional Event Parameters:

- `--location <value>`
- `--description <value>`
- `--private`

---

## D. Supported Commands by Assignment

### Assignment 4 (Basic Mode):

- `create event <name> from <start> to <end>`
- `create event <name> on <date>`
- `create event <name> on <date> repeats <days> for <N> times`
- `edit event <property> <name> from <start> to <end> with <value>`
- `edit events <property> <name> from <start> with <value>`
- `edit events <property> <name> <value>`
- `print events on <date>`
- `print events from <start> to <end>`
- `show status on <dateTtime>`
- `export cal <filename>.csv`

### Assignments 5  (Advanced Mode):

- `create calendar --name <name> --timezone <area/location>`
- `edit calendar --name <name> --property <property> <value>`
- `use calendar --name <name>`
- `copy event <name> on <start> --target <calendar> to <targetStart>`
- `copy events on <date> --target <calendar> to <targetDate>`
- `copy events between <start> and <end> --target <calendar> to <targetStart>`

---

## E. Execution Modes Summary

| Mode        | Description                                                                 |
| ----------- | --------------------------------------------------------------------------- |
| Interactive | User types commands directly into the terminal.                             |
| Headless    | Commands are read from a file and executed one after another automatically. |

Both execution modes work in both basic and advanced configurations.

---

## F. Team Contributions

| Team Member       | Contributions                                                                  |
| ----------------- | ------------------------------------------------------------------------------ |
| Chandan Gowda K S | Implemented event copy functionality, export logic, helped with controller design, and test support |
| Saswata Lahari    | Led command pattern integration, recurrence handling, and timezone features, and testing support |

All members contributed to architecture planning, debugging, and refinement throughout the project.

---

The Jars created for calendarBasic and calendarAdvanced are for assignment 4 and 
assignment 4 respectively by changing config in main app.