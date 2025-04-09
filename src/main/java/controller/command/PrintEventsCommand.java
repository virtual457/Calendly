package controller.command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import model.ICalendarEventDTO;
import model.ICalendarModel;

/**
 * Command to print events from a calendar.
 */
public class PrintEventsCommand implements ICommand {
  private final ICalendarModel model;
  private final String calendarName;
  private final LocalDateTime fromDateTime;
  private final LocalDateTime toDateTime;

  /**
   * Constructs a {@code PrintEventsCommand}
   * with the given input parts, model, and selected calendar.
   */
  public PrintEventsCommand(List<String> parts, ICalendarModel model, String currentCalendar) {
    this.model = Objects.requireNonNull(model,"Model cannot be null");
    this.calendarName = currentCalendar;

    CommandParser.requireMinArgs(parts, 1, "Print Command too short need more details");

    // Handle different print formats
    if (parts.get(0).equals("on")) {
      CommandParser.requireExactArgs(parts, 2, "Invalid format. Expected: print events on <date>");
      LocalDate date = CommandParser.parseDate(parts, 1, "Invalid date format");
      this.fromDateTime = date.atStartOfDay();
      this.toDateTime = date.atTime(23, 59, 59);
    }
    else if (parts.get(0).equals("from")) {
      CommandParser.requireExactArgs(parts, 4,
            "Invalid format. Expected: print events from <datetime> to <datetime>");
      CommandParser.requireKeyword(parts, 2, "to",
            "Expected 'to' after start datetime in print events command");
      this.fromDateTime = CommandParser.parseDateTime(parts, 1, "Invalid start datetime format");
      this.toDateTime = CommandParser.parseDateTime(parts, 3, "Invalid end datetime format");

      if (toDateTime.isBefore(fromDateTime)) {
        throw new IllegalArgumentException("End datetime must be after start datetime");
      }
    }
    else {
      throw new IllegalArgumentException(
            "Expected 'on' or 'from' at start of print events command.");
    }
  }

  @Override
  public String execute() {
    try {
      List<ICalendarEventDTO> events = model.getEventsInRange(
            calendarName, fromDateTime, toDateTime);

      if (events.isEmpty()) {
        return "No events found.";
      }
      return buildStringForPrintEvents(events);
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "An unexpected error occurred: " + e.getMessage();
    }
  }

  private String buildStringForPrintEvents(List<ICalendarEventDTO> events) {
    StringBuilder sb = new StringBuilder();
    for (ICalendarEventDTO event : events) {
      sb.append("- ")
            .append(event.getEventName())
            .append(" [")
            .append(event.getStartDateTime())
            .append(" to ")
            .append(event.getEndDateTime())
            .append("]");
      if (event.getEventLocation() != null && !event.getEventLocation().isEmpty()) {
        sb.append(" at ").append(event.getEventLocation());
      }
      sb.append(System.lineSeparator());
    }
    return sb.toString().trim();
  }
}