package command;

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
  private final boolean editAll;

  public EditEventCommand(List<String> parts, ICalendarModel model, String currentCalendar) {
    this.model = model;
    this.calendarName = currentCalendar;

    if (parts.size() < 3) {
      throw new IllegalArgumentException("Insufficient arguments for edit event command.");
    }

    this.property = parts.get(0);
    this.eventName = parts.get(1);

    int index = 2;
    String fromValue = null;
    String toValue = null;
    String withValue = null;

    while (index < parts.size()) {
      String token = parts.get(index);

      if (token.equals("from")) {
        index++;
        if (index >= parts.size()) {
          throw new IllegalArgumentException("Missing 'from' datetime.");
        }
        fromValue = parts.get(index++);
      } else if (token.equals("to")) {
        index++;
        if (index >= parts.size()) {
          throw new IllegalArgumentException("Missing 'to' datetime.");
        }
        toValue = parts.get(index++);
      } else if (token.equals("with")) {
        index++;
        if (index >= parts.size()) {
          throw new IllegalArgumentException("Missing value after 'with'.");
        }
        withValue = parts.get(index++);
      } else {
        if (index == parts.size() - 1) {
          withValue = parts.get(index);
          index++;
        } else {
          throw new IllegalArgumentException("Unexpected token: '" + token + "'");
        }
      }
    }

    this.fromDateTime = fromValue != null ? LocalDateTime.parse(fromValue) : null;
    this.toDateTime = toValue != null ? LocalDateTime.parse(toValue) : null;
    this.newValue = withValue;

    if (this.newValue == null) {
      throw new IllegalArgumentException("Missing new property value.");
    }

    this.editAll = this.fromDateTime == null || this.toDateTime == null;
  }

  @Override
  public String execute() {
    boolean success = model.editEvent(calendarName, property, eventName,
            fromDateTime,toDateTime, newValue);
    return success ? "Event(s) edited successfully." : "Error editing event(s).";
  }
}
