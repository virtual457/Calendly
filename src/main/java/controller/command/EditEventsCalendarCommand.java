package controller.command;

import java.time.LocalDateTime;
import java.util.List;
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

  public EditEventsCalendarCommand(List<String> parts, ICalendarModel model, String currentCalendar) {
    this.model = model;
    this.calendarName = currentCalendar;



    int index = 0;
    if (index >= parts.size()) {
      throw new IllegalArgumentException("Missing property name.");
    }
    this.property = parts.get(index++);

    if (index >= parts.size()) {
      throw new IllegalArgumentException("Missing event name.");
    }
    this.eventName = parts.get(index++);

    if (index < parts.size() && parts.get(index).equals("from")) {
      index++;
      if (index >= parts.size()) {
        throw new IllegalArgumentException("Missing datetime after 'from'.");
      }
      this.fromDateTime = LocalDateTime.parse(parts.get(index++));

      if (index >= parts.size() || !parts.get(index).equals("with")) {
        throw new IllegalArgumentException("Missing 'with' keyword after datetime.");
      }
      index++;

      if (index >= parts.size()) {
        throw new IllegalArgumentException("Missing new property value after 'with'.");
      }
      this.newValue = parts.get(index);
      this.hasFromDateTime = true;
    } else {
      if (index >= parts.size()) {
        throw new IllegalArgumentException("Missing new property value.");
      }
      this.newValue = parts.get(index);
      this.fromDateTime = null;
      this.hasFromDateTime = false;
    }
  }

  @Override
  public String execute() {
    boolean success;
    if (hasFromDateTime) {
      success = model.editEvents(calendarName, property, eventName, fromDateTime, newValue, true);
    } else {
      success = model.editEvents(calendarName, property, eventName, null, newValue, true);
    }
    return success ? "Events updated successfully." : "No matching events found to update.";
  }
}
