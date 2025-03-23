package command;

import java.time.LocalDateTime;
import java.util.Scanner;
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

  public EditEventCommand(Scanner parts, ICalendarModel model, String currentCalendar) {
    this.model = model;
    this.calendarName = currentCalendar;

    if (!parts.hasNext()) {
      throw new IllegalArgumentException("Missing 'property'.");
    }
    this.property = parts.next();

    if (!parts.hasNext()) {
      throw new IllegalArgumentException("Missing event name.");
    }
    this.eventName = parts.next();

    if (parts.hasNext("from")) {
      parts.next(); // consume "from"
      if (!parts.hasNext()) {
        throw new IllegalArgumentException("Missing 'from' datetime.");
      }
      this.fromDateTime = LocalDateTime.parse(parts.next());
    }

    if (parts.hasNext("to")) {
      parts.next();
      if (!parts.hasNext()) {
        throw new IllegalArgumentException("Missing 'to' datetime.");
      }
      this.toDateTime = LocalDateTime.parse(parts.next());
    }

    if (parts.hasNext("with")) {
      parts.next();
      if (!parts.hasNext()) {
        throw new IllegalArgumentException("Missing new value after 'with'.");
      }
      this.newValue = parts.next();
    } else {
      // fallback: assume last token is new value (e.g., edit events <property> <eventName> <newValue>)
      if (!parts.hasNext()) {
        throw new IllegalArgumentException("Missing new property value.");
      }
      this.newValue = parts.next();
    }

    this.editAll = this.fromDateTime == null || this.toDateTime == null;
  }

  @Override
  public String execute() {
    boolean success = model.editEvents(calendarName, property, eventName,
            fromDateTime, toDateTime, newValue, editAll);
    return success ? "Event(s) edited successfully." : "Error editing event(s).";
  }
}