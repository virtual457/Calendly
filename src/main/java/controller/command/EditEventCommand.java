package controller.command;

import java.time.LocalDateTime;
import java.util.List;

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

  public EditEventCommand(List<String> parts, ICalendarModel model, String currentCalendar) {
    this.model = model;
    this.calendarName = currentCalendar;


    this.property = parts.get(0);
    this.eventName = parts.get(1);

    if (!parts.get(2).equalsIgnoreCase("from")) {
      throw new IllegalArgumentException("Expected 'from' keyword at position 3.");
    }

    String fromValue = parts.get(3);

    if (!parts.get(4).equalsIgnoreCase("to")) {
      throw new IllegalArgumentException("Expected 'to' keyword at position 5.");
    }

    String toValue = parts.get(5);

    if (!parts.get(6).equalsIgnoreCase("with")) {
      throw new IllegalArgumentException("Expected 'with' keyword at position 7.");
    }

    String withValue = parts.get(7);

    this.fromDateTime = LocalDateTime.parse(fromValue);
    this.toDateTime = LocalDateTime.parse(toValue);
    this.newValue = withValue;
  }


  @Override
  public String execute() {
    boolean success = model.editEvent(calendarName, property, eventName,
        fromDateTime, toDateTime, newValue);
    return success ? "Event(s) edited successfully." : "Error editing event(s).";
  }
}
