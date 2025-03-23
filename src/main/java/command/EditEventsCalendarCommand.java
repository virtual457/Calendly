package command;

import java.util.Scanner;
import model.ICalendarModel;

/**
 * Command to edit an existing calendar property (name or timezone).
 */
public class EditEventsCalendarCommand implements ICommand {
  private final ICalendarModel model;
  private final String calendarName;
  private final String property;
  private final String newValue;

  public EditEventsCalendarCommand(Scanner parts, ICalendarModel model) {
    this.model = model;

    if (!parts.hasNext("--name")) {
      throw new IllegalArgumentException("Missing --name flag.");
    }
    parts.next(); // Consume --name

    if (!parts.hasNext()) {
      throw new IllegalArgumentException("Missing calendar name.");
    }
    this.calendarName = parts.next();

    if (!parts.hasNext("--property")) {
      throw new IllegalArgumentException("Missing --property flag.");
    }
    parts.next(); // Consume --property

    if (!parts.hasNext()) {
      throw new IllegalArgumentException("Missing property name.");
    }
    this.property = parts.next();

    if (!parts.hasNext()) {
      throw new IllegalArgumentException("Missing new property value.");
    }
    this.newValue = parts.next();
  }

  @Override
  public String execute() {
    //boolean success = model.editCalendar(calendarName, property, newValue);
    //return success ? "Calendar updated successfully." : "Error updating calendar.";
    return "Blast";
  }
}
