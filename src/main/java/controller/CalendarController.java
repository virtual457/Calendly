package controller;

import java.time.*;
import java.util.*;

import model.CalendarEventDTO;
import model.ICalendarModel;

class CalendarController implements ICalendarController {
  private final ICalendarModel model;

  public CalendarController(ICalendarModel model) {
    this.model = model;
  }

  public String processCommand(String command) {
    Scanner parts = new Scanner(command);
    try {
      if (!parts.hasNext()) return "Error: Empty command.";
      String action = parts.next();

      switch (action) {
        case "create":
          return handleCreateCommand(parts);
        case "edit":
          return handleEditCommand(parts);
        case "print":
          return handlePrintCommand(parts);
        case "export":
          return handleExportCommand(parts);
        case "show":
          return handleShowStatusCommand(parts);
      }

      return "Error: Unsupported command.";
    } catch (Exception e) {
      return "Error processing command: " + e.getMessage();
    } finally {
      parts.close();
    }
  }

  private String handleEditCommand(Scanner parts) {
    if (!parts.hasNext()) {
      return "Error: Missing event type (event/events).";
    }
    String eventType = parts.next();
    if (!parts.hasNext()) {
      return "Error: Missing property.";
    }
    String property = parts.next();
    if (!parts.hasNext()) {
      return "Error: Missing event name.";
    }
    String eventName = parts.next();

    if (eventType.equals("event")) {
      if (!parts.hasNext("from")) {
        return "Error: Missing 'from' keyword.";
      }
      parts.next();
      if (!parts.hasNext()) {
        return "Error: Missing start date and time.";
      }
      LocalDateTime startDateTime = LocalDateTime.parse(parts.next());
      if (!parts.hasNext("to")) {
        return "Error: Missing 'to' keyword.";
      }
      parts.next();
      if (!parts.hasNext()) {
        return "Error: Missing end date and time.";
      }
      LocalDateTime endDateTime = LocalDateTime.parse(parts.next());
      if (!parts.hasNext("with")) {
        return "Error: Missing 'with' keyword.";
      }
      parts.next();
      if (!parts.hasNext()) {
        return "Error: Missing new property value.";
      }
      String newValue = parts.next();
      model.editEvent(property, eventName, startDateTime, endDateTime, newValue);
      return "Event edited successfully.";
    }
    else if (eventType.equals("events")) {
      if (parts.hasNext("from")) {
        parts.next();
        if (!parts.hasNext()) {
          return "Error: Missing start date and time.";
        }
        LocalDateTime startDateTime = LocalDateTime.parse(parts.next());
        if (!parts.hasNext("with")) {
          return "Error: Missing 'with' keyword.";
        }
        parts.next();
        if (!parts.hasNext()) {
          return "Error: Missing new property value.";
        }
        String newValue = parts.next();
        model.editEvents(property, eventName, startDateTime, newValue);
      } else {
        if (!parts.hasNext()) {
          return "Error: Missing new property value.";
        }
        String newValue = parts.next();
        model.editEvents(property, eventName, null, newValue);
      }
      return "Events edited successfully.";
    } else {
      return "Error: Invalid event type. Expected 'event' or 'events'.";
    }
  }

  private String handlePrintCommand(Scanner parts) {
    if (!parts.hasNext("events")) {
      return "Error: Expected 'events' after 'print'.";
    }
    parts.next();
    if (parts.hasNext("on")) {
      parts.next();
      if (!parts.hasNext()) {
        return "Error: Missing date.";
      }
      LocalDate date = LocalDate.parse(parts.next());
      return model.printEventsOnSpecificDate(date);
    } else if (parts.hasNext("from")) {
      parts.next();
      if (!parts.hasNext()) {
        return "Error: Missing start date and time.";
      }
      LocalDateTime startDateTime = LocalDateTime.parse(parts.next());
      if (!parts.hasNext("to")) {
        return "Error: Missing 'to' keyword.";
      }
      parts.next();
      if (!parts.hasNext()) {
        return "Error: Missing end date and time.";
      }
      LocalDateTime endDateTime = LocalDateTime.parse(parts.next());
      return model.printEventsInSpecificRange(startDateTime, endDateTime);
    } else {
      return "Error: Invalid print command format.";
    }
  }

  private String handleExportCommand(Scanner parts) {
    if (!parts.hasNext("cal")) {
      return "Error: Expected 'cal' after 'export'.";
    }
    parts.next();
    if (!parts.hasNext()) {
      return "Error: Missing filename.";
    }
    return model.exportEvents(parts.next());
  }

  private String handleShowStatusCommand(Scanner parts) {
    if (!parts.hasNext("status")) {
      return "Error: Expected 'status' after 'show'.";
    }
    parts.next();
    if (!parts.hasNext("on")) {
      return "Error: Expected 'on' keyword.";
    }
    parts.next();
    if (!parts.hasNext()) {
      return "Error: Missing date and time.";
    }
    return model.showStatus(LocalDateTime.parse(parts.next()));
  }

  private String handleCreateCommand(Scanner parts) {
    if (!parts.hasNext("event")) {
      return "Error: Expected 'event' after 'create'.";
    }
    parts.next();
    boolean autoDecline = false;
    if (parts.hasNext("--autoDecline")) {
      parts.next();
      autoDecline = true;
    }
    if (!parts.hasNext()) {
      return "Error: Missing event name.";
    }
    String eventName = parts.next();

    if (!parts.hasNext("from") && !parts.hasNext("on")) {
      return "Error: Missing 'from' or 'on' keyword.";
    }

    LocalDateTime startDateTime;
    LocalDateTime endDateTime;
    boolean isRecurring = false;
    List<DayOfWeek> recurrenceDays = new ArrayList<>();
    int recurrenceCount = 0;
    LocalDateTime recurrenceEndDate = null;
    String location = "";
    String description = "";
    boolean isPrivate = false;
    boolean locationSet = false;
    boolean descriptionSet = false;
    boolean privateSet = false;

    if (parts.hasNext("from")) {
      parts.next();
      if (!parts.hasNext()) {
        return "Error: Missing start date and time.";
      }
      startDateTime = LocalDateTime.parse(parts.next());
      if (!parts.hasNext("to")) {
        return "Error: Missing 'to' keyword and end time.";
      }
      parts.next();
      if (!parts.hasNext()) {
        return "Error: Missing end date and time.";
      }
      endDateTime = LocalDateTime.parse(parts.next());
    } else {
      parts.next();
      if (!parts.hasNext()) {
        return "Error: Missing date for all-day event.";
      }
      LocalDate date = LocalDate.parse(parts.next());
      startDateTime = date.atStartOfDay();
      endDateTime = startDateTime.plusDays(1).minusSeconds(1);
    }

    if (parts.hasNext("repeats")) {
      parts.next();
      String weekdays = parts.next();
      for (char day : weekdays.toCharArray()) {
        switch (day) {
          case 'M': recurrenceDays.add(DayOfWeek.MONDAY); break;
          case 'T': recurrenceDays.add(DayOfWeek.TUESDAY); break;
          case 'W': recurrenceDays.add(DayOfWeek.WEDNESDAY); break;
          case 'R': recurrenceDays.add(DayOfWeek.THURSDAY); break;
          case 'F': recurrenceDays.add(DayOfWeek.FRIDAY); break;
          case 'S': recurrenceDays.add(DayOfWeek.SATURDAY); break;
          case 'U': recurrenceDays.add(DayOfWeek.SUNDAY); break;
          default: return "Error: Invalid recurrence day.";
        }
      }
      isRecurring = true;
      if (parts.hasNext("for")) {
        parts.next();
        recurrenceCount = Integer.parseInt(parts.next());
      } else if (parts.hasNext("until")) {
        parts.next();
        recurrenceEndDate = LocalDateTime.parse(parts.next());
      }
    }

    while (parts.hasNext()) {
      String option = parts.next();
      if (option.equals("-location")) {
        if (locationSet) return "Error: Location specified multiple times.";
        if (!parts.hasNext()) return "Error: Missing location value.";
        location = parts.next();
        locationSet = true;
      } else if (option.equals("-description")) {
        if (descriptionSet) return "Error: Description specified multiple times.";
        if (!parts.hasNext()) return "Error: Missing description value.";
        description = parts.next();
        descriptionSet = true;
      } else if (option.equals("-private")) {
        if (privateSet) return "Error: Private flag specified multiple times.";
        isPrivate = true;
        privateSet = true;
      } else {
        return "Error: Unknown option: " + option;
      }
    }

    CalendarEventDTO event = new CalendarEventDTO(eventName, startDateTime, endDateTime,
            isRecurring, recurrenceDays, recurrenceCount, recurrenceEndDate, autoDecline,description,location,isPrivate);
    model.addEvent(event);
    return "Event created successfully.";
  }
}
