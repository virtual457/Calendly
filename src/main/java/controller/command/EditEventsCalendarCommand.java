package controller.command;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import model.ICalendarModel;

/**
 * Command to edit properties of calendar events.
 */
public class EditEventsCalendarCommand implements ICommand {
  private final ICalendarModel model;
  private final String calendarName;
  private final String property;
  private final String eventName;
  private final String newValue;
  private final LocalDateTime fromDateTime;
  private final boolean hasFromDateTime;

  /**
   * Constructs an {@code EditEventsCalendarCommand} to edit multiple events in a calendar.
   */
  public EditEventsCalendarCommand(List<String> parts, ICalendarModel model, String currentCalendar) {
    this.model = Objects.requireNonNull(model,"Model cannot be null");
    this.calendarName = currentCalendar;

    // Validate minimum arguments
    CommandParser.requireMinArgs(parts, 2, "Insufficient arguments for edit events command");

    // Get property and event name
    this.property = CommandParser.getRequiredArg(parts, 0, "Missing property name");
    this.eventName = CommandParser.getRequiredArg(parts, 1, "Missing event name");

    int index = 2;
    if (index < parts.size() && parts.get(index).equals("from")) {
      index++;

      this.fromDateTime = CommandParser.parseDateTime(parts, index++, "Invalid datetime format after 'from'");


      CommandParser.requireKeyword(parts, index++, "with", "Missing 'with' keyword after datetime");

      this.newValue = CommandParser.getRequiredArg(parts, index, "Missing new property value after 'with'");

      this.hasFromDateTime = true;
    }
    else {
      // Format without from clause: edit events property eventName newValue
      this.newValue = CommandParser.getRequiredArg(parts, index, "Missing new property value");
      this.fromDateTime = null;
      this.hasFromDateTime = false;
    }
  }

  @Override
  public String execute() {
    try {
      boolean success;
      if (hasFromDateTime) {
        success = model.editEvents(calendarName, property, eventName, fromDateTime, newValue, true);
      } else {
        success = model.editEvents(calendarName, property, eventName, LocalDateTime.MIN, newValue, true);
      }
      return success ? "Events updated successfully." : "No matching events found to update.";
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "An unexpected error occurred: " + e.getMessage();
    }
  }
}