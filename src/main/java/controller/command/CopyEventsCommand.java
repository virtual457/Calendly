package controller.command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import model.ICalendarModel;

/**
 * Command to copy events from a calendar.
 */
public class CopyEventsCommand implements ICommand {
  private final ICalendarModel model;
  private final String sourceCalendar;

  // Parsed values
  private LocalDate fromDate;
  private LocalDate toDate;
  private String targetCalendar;
  private LocalDate targetStartDate;

  public CopyEventsCommand(List<String> parts, ICalendarModel model, String currentCalendar) {
    this.model = model;
    this.sourceCalendar = currentCalendar;

    if (parts.isEmpty()) {
      throw new IllegalArgumentException("Expected 'copy events on <date> --target <calendar> to " +
          "<date>' or 'copy events between <date> and <date> --target <calendar> to <date>'");
    }

    int index = 0;

    String type = safeGet(parts, index);
    if (type.equals("on")) {
      index++;
      fromDate = parseDate(safeGet(parts, index++), "Invalid source date.");

      if (!safeGet(parts, index++).equals("--target")) {
        throw new IllegalArgumentException("Expected '--target' after source date.");
      }

      targetCalendar = safeGet(parts, index++);

      if (!safeGet(parts, index++).equals("to")) {
        throw new IllegalArgumentException("Expected 'to' after target calendar.");
      }

      targetStartDate = parseDate(safeGet(parts, index++), "Invalid target date.");
      toDate = fromDate;

    } else if (type.equals("between")) {
      index++;
      fromDate = parseDate(safeGet(parts, index++), "Invalid start date.");

      if (!safeGet(parts, index++).equals("and")) {
        throw new IllegalArgumentException("Expected 'and' after start date.");
      }

      toDate = parseDate(safeGet(parts, index++), "Invalid end date.");

      if (!safeGet(parts, index++).equals("--target")) {
        throw new IllegalArgumentException("Expected '--target' after end date.");
      }

      targetCalendar = safeGet(parts, index++);

      if (!safeGet(parts, index++).equals("to")) {
        throw new IllegalArgumentException("Expected 'to' after target calendar.");
      }

      targetStartDate = parseDate(safeGet(parts, index++), "Invalid target start date.");
    } else {
      throw new IllegalArgumentException("Expected 'on' or 'between' after 'events'.");
    }
  }

  private String safeGet(List<String> parts, int index) {
    if (index >= parts.size()) {
      throw new IllegalArgumentException("Incomplete command. Expected more arguments.");
    }
    return parts.get(index);
  }

  private LocalDate parseDate(String value, String errorMessage) {
    try {
      return LocalDate.parse(value);
    } catch (Exception e) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  @Override
  public String execute() {
    LocalDateTime rangeStart = fromDate.atStartOfDay();
    LocalDateTime rangeEnd = toDate.atTime(LocalTime.MAX);

    boolean success = model.copyEvents(sourceCalendar, rangeStart, rangeEnd, targetCalendar,
        targetStartDate);
    return success ? "Events copied successfully." : "Error copying events.";
  }
}
