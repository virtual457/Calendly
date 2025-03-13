package controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.*;
import java.util.*;

import model.CalendarEventDTO;
import model.ICalendarModel;
import view.IView;

class CalendarController implements ICalendarController {
  private final ICalendarModel model;
  private final IView view;

  public CalendarController(ICalendarModel model, IView view) {
    this.model = model;
    this.view = view;
  }

  private String processCommand(String command) {
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

  @Override
  public void run(String mode, String filePath) {
    if (!mode.equalsIgnoreCase("interactive") && !mode.equalsIgnoreCase("headless")) {
      view.display("Invalid mode. Use --mode interactive OR --mode headless <filePath>");
      return;
    }
    runMode(mode.equalsIgnoreCase("interactive"), filePath);
  }

  private void runMode(boolean isInteractive, String filePath) {
    BufferedReader reader = null;

    try {
      reader = isInteractive
              ? new BufferedReader(new InputStreamReader(System.in))
              : new BufferedReader(new FileReader(filePath));

      view.display("Welcome to the Calendar App!");

      String command;
      while ((command = reader.readLine()) != null) {
        if (command.equalsIgnoreCase("exit")) {
          break;
        }
        String response = processCommand(command);
        view.display(response);
      }
    } catch (IOException e) {
      view.display("Error reading input: " + e.getMessage());
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException e) {
        view.display("Error Closing reading Stream" + e.getMessage());
      }
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
      String newValue = readQuotedValue(parts);
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
        String newValue = readQuotedValue(parts);
        model.editEvents(property, eventName, startDateTime, newValue);
      } else {
        if (!parts.hasNext()) {
          return "Error: Missing new property value.";
        }
        String newValue = readQuotedValue(parts);
        if(parts.hasNext()) {
          return "Error: Invalid Edit events command";
        }
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
      //todo extract to a common method
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
    String eventName = readQuotedValue(parts);

    if (!parts.hasNext("from") && !parts.hasNext("on")) {
      return "Error: Missing 'from' or 'on' keyword. Or EventName is required.";
    }

    LocalDateTime startDateTime;
    LocalDateTime endDateTime;
    Boolean isRecurring = Boolean.FALSE;
    List<DayOfWeek> recurrenceDays = new ArrayList<>();
    Integer recurrenceCount = 0;
    LocalDateTime recurrenceEndDate = null;
    String location = "";
    String description = "";
    Boolean isPrivate = null;
    Boolean locationSet = Boolean.FALSE;
    Boolean descriptionSet = Boolean.FALSE;
    Boolean privateSet = Boolean.FALSE;
    Boolean isAllDayEvent = Boolean.FALSE;

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
      isAllDayEvent = Boolean.TRUE;
    }

    if (parts.hasNext("repeats")) {
      parts.next();
      String weekdays = parts.next();
      for (char day : weekdays.toCharArray()) {
        switch (day) {
          case 'M':
            recurrenceDays.add(DayOfWeek.MONDAY);
            break;
          case 'T':
            recurrenceDays.add(DayOfWeek.TUESDAY);
            break;
          case 'W':
            recurrenceDays.add(DayOfWeek.WEDNESDAY);
            break;
          case 'R':
            recurrenceDays.add(DayOfWeek.THURSDAY);
            break;
          case 'F':
            recurrenceDays.add(DayOfWeek.FRIDAY);
            break;
          case 'S':
            recurrenceDays.add(DayOfWeek.SATURDAY);
            break;
          case 'U':
            recurrenceDays.add(DayOfWeek.SUNDAY);
            break;
          default:
            return "Error: Invalid recurrence day.";
        }
      }

      isRecurring = true;
      if (parts.hasNext("for")) {
        parts.next();
        recurrenceCount = Integer.parseInt(parts.next());
        parts.next();
      }
      else if (parts.hasNext("until")) {
        parts.next();
        if (isAllDayEvent) {
          recurrenceEndDate = LocalDate.parse(parts.next()).atTime(23, 59, 59);
        } else {
          recurrenceEndDate = LocalDateTime.parse(parts.next());
        }
      }
      else {
        return "Error: command missing until or from.";
      }
    }

    while (parts.hasNext()) {
      String option = parts.next();
      switch (option) {
        case "-location":
          if (locationSet) return "Error: Location specified multiple times.";
          if (!parts.hasNext()) return "Error: Missing location value.";
          location = readQuotedValue(parts);
          locationSet = Boolean.TRUE;
          break;
        case "-description":
          if (descriptionSet) return "Error: Description specified multiple times.";
          if (!parts.hasNext()) return "Error: Missing description value.";
          description = readQuotedValue(parts);
          descriptionSet = Boolean.TRUE;
          break;
        case "-private":
          if (privateSet) return "Error: Private flag specified multiple times.";
          isPrivate = Boolean.TRUE;
          privateSet = Boolean.TRUE;
          break;
        default:
          return "Error: Unknown option: " + option;
      }
    }

    CalendarEventDTO event = CalendarEventDTO.builder()
            .setAutoDecline(autoDecline)
            .setEventDescription(description)
            .setEventName(eventName)
            .setStartDateTime(startDateTime)
            .setEndDateTime(endDateTime)
            .setEventLocation(location)
            .setPrivate(isPrivate)
            .setRecurrenceCount(recurrenceCount)
            .setRecurrenceEndDate(recurrenceEndDate)
            .setRecurring(isRecurring)
            .setRecurrenceDays(recurrenceDays)
            .build();

    model.addEvent(event);
    return "Event created successfully.";
  }

  private String readQuotedValue(Scanner parts) {
    if (!parts.hasNext()) {
      return "";
    }
    StringBuilder value = new StringBuilder();
    String token = parts.next();

    // Check if the value starts with a quote
    if (token.startsWith("'") || token.startsWith("\"")) {
      char quoteType = token.charAt(0); // Get the type of quote (single or double)
      value.append(token.substring(1)); // Append without starting quote

      while (parts.hasNext()) {
        token = parts.next();
        // If we find a closing quote, remove it and break
        if (token.endsWith(String.valueOf(quoteType))) {
          value.append(" ").append(token, 0, token.length() - 1);
          break;
        } else {
          value.append(" ").append(token);
        }
      }
    } else {
      value.append(token);
    }
    return value.toString();
  }
}
