package controller.command;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import model.ICalendarEventDTO;
import model.ICalendarModel;

/**
 * Command to check the user status (busy/available) on a given date and time.
 */
public class ShowStatusCommand implements ICommand {
  private final ICalendarModel model;
  private final String calendarName;
  private final LocalDateTime dateTime;

  /**
   * Constructs a {@code ShowStatusCommand}
   * with the specified arguments, model, and current calendar.
   */
  public ShowStatusCommand(List<String> parts, ICalendarModel model, String currentCalendar) {
    this.model = Objects.requireNonNull(model,"Model cannot be null");
    this.calendarName = currentCalendar;

    CommandParser.requireExactArgs(parts, 2, "Invalid syntax. Expected: show status on <datetime>");

    CommandParser.requireKeyword(parts, 0, "on", "Missing 'on' keyword");

    this.dateTime = CommandParser.parseDateTime(parts, 1, "Invalid date and time format. Expected: yyyy-MM-ddTHH:mm");
  }


  @Override
  public String execute() {
    try {
      List<ICalendarEventDTO> events = model.getEventsInSpecificDateTime(calendarName, dateTime);
      return events.isEmpty() ? "Available" : "Busy";
    } catch (IllegalArgumentException e) {
      // Specific handling for validation errors
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      // Generic error handling
      return "An unexpected error occurred: " + e.getMessage();
    }
  }
}