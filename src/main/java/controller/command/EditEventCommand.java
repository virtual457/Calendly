package controller.command;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import model.ICalendarModel;

/**
 * Command to edit event(s) in various formats.
 */
public class EditEventCommand implements ICommand {
  private final ICalendarModel model;
  private final String calendarName;
  private final String property;
  private final String eventName;
  private LocalDateTime fromDateTime;
  private LocalDateTime toDateTime;
  private final String newValue;

  /**
   * Constructs an {@code EditEventCommand} to edit a specific event in the given calendar.
   *
   * @param parts           the list of command arguments
   * @param model           the calendar model to interact with
   * @param currentCalendar the name of the currently selected calendar
   */
  public EditEventCommand(List<String> parts, ICalendarModel model, String currentCalendar) {
    this.model = Objects.requireNonNull(model,"Model cannot be null");
    this.calendarName = currentCalendar;

    // Validate we have enough arguments
    CommandParser.requireMinArgs(parts, 8, "Insufficient arguments for edit event " +
          "command. Expected:edit event property eventName from startDateTime to " +
          "endDateTime " +
          "with newValue");

    // Parse property and event name
    this.property = CommandParser.getRequiredArg(parts, 0, "Missing property name");
    this.eventName = CommandParser.getRequiredArg(parts, 1, "Missing event name");

    // Check keywords and parse date times
    CommandParser.requireKeyword(parts, 2, "from", "Expected 'from' keyword at position 3");
    this.fromDateTime = CommandParser.parseDateTime(parts, 3, "Invalid from date/time format");

    CommandParser.requireKeyword(parts, 4, "to", "Expected 'to' keyword at position 5");
    this.toDateTime = CommandParser.parseDateTime(parts, 5, "Invalid to date/time format");

    CommandParser.requireKeyword(parts, 6, "with", "Expected 'with' keyword at position 7");
    this.newValue = CommandParser.getRequiredArg(parts, 7, "Missing new value");
  }

  @Override
  public String execute() {
    try {
      boolean success = model.editEvent(calendarName, property, eventName,
            fromDateTime, toDateTime, newValue);
      return success ? "Event(s) edited successfully." : "Error editing event(s).";
    } catch (IllegalArgumentException e) {
      // Specific handling for validation errors
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      // Generic error handling
      return "An unexpected error occurred: " + e.getMessage();
    }
  }
}