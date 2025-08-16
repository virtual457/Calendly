package controller.command;

import model.ICalendarModel;

import java.util.List;
import java.util.Objects;

/**
 * Command to edit a calendar's name or timezone.
 */
public class EditCalendarCommand implements ICommand {
  private final ICalendarModel model;
  private String calendarName;
  private final String property;
  private final String newValue;

  /**
   * Constructs an {@code EditCalendarCommand}
   * that allows editing properties of a calendar.
   */
  public EditCalendarCommand(List<String> args, ICalendarModel model, String calendarName) {
    this.model = Objects.requireNonNull(model,"Model cannot be null");

    // Validate minimum arguments
    CommandParser.requireMinArgs(args, 5, "Insufficient arguments for edit calendar command");

    // Check --name flag
    CommandParser.requireKeyword(args, 0, "--name", "Expected '--name' keyword");

    // Get calendar name
    this.calendarName = CommandParser.getRequiredArg(args, 1, "Missing calendar name");
    CommandParser.requireNonEmpty(this.calendarName, "Calendar name cannot be empty");

    // Check --property flag
    CommandParser.requireKeyword(args, 2, "--property", "Expected '--property' keyword");

    // Get property name
    this.property = CommandParser.getRequiredArg(args, 3, "Missing property name");

    // Validate property type
    if (!property.equals("name") && !property.equals("timezone")) {
      throw new IllegalArgumentException("Invalid property. Only 'name' or 'timezone' allowed.");
    }

    // Get new value
    this.newValue = CommandParser.getRequiredArg(args, 4, "Missing new property value");
    CommandParser.requireNonEmpty(newValue, "New property value cannot be empty");
  }

  @Override
  public String execute() {
    try {
      boolean updated = model.editCalendar(calendarName, property, newValue);
      if (!updated) {
        return "Failed to update calendar. Please ensure the calendar exists and the value is valid.";
      }
      return "Calendar updated successfully.";
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "An unexpected error occurred: " + e.getMessage();
    }
  }
}