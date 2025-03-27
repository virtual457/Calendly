package controller.command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
   *
   * @param parts           the command arguments parsed from user input
   * @param model           the calendar model used to perform actions
   * @param currentCalendar the name of the currently selected calendar
   */

  public PrintEventsCommand(List<String> parts, ICalendarModel model, String currentCalendar) {
    this.model = model;
    this.calendarName = currentCalendar;

    if (parts.isEmpty()) {
      throw new IllegalArgumentException("Print Command too short need more details");
    }
    if (parts.get(0).equals("on")) {
      if (parts.size() != 2) {
        throw new IllegalArgumentException("Invalid format. Expected: print events on <date>");
      }
      LocalDate date = LocalDate.parse(parts.get(1));
      this.fromDateTime = date.atStartOfDay();
      this.toDateTime = date.atTime(23, 59, 59);

    } else if (parts.get(0).equals("from")) {
      if (parts.size() != 4 || !parts.get(2).equals("to")) {
        throw new IllegalArgumentException("Invalid format. Expected: print events from " +
            "<datetime> to <datetime>");
      }
      this.fromDateTime = LocalDateTime.parse(parts.get(1));
      this.toDateTime = LocalDateTime.parse(parts.get(3));
    } else {
      throw new IllegalArgumentException("Expected 'on' or 'from' at start of print events " +
          "command.");
    }
  }

  @Override
  public String execute() {
    List<ICalendarEventDTO> events = model.getEventsInRange(calendarName, fromDateTime, toDateTime);
    if (events.isEmpty()) {
      return "No events found.";
    }
    return buildStringForPrintEvents(events);
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