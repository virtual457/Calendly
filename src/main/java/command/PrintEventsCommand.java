package command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;
import model.ICalendarModel;

/**
 * Command to print events from a calendar.
 */
public class PrintEventsCommand implements ICommand {
  private final ICalendarModel model;
  private final String calendarName;
  private final LocalDateTime fromDateTime;
  private final LocalDateTime toDateTime;
  private final boolean isRange;

  public PrintEventsCommand(Scanner parts, ICalendarModel model, String currentCalendar) {
    this.model = model;
    this.calendarName = currentCalendar;

    if (parts.hasNext("on")) {
      parts.next(); // Consume "on"
      if (!parts.hasNext()) {
        throw new IllegalArgumentException("Missing date.");
      }
      LocalDate date = LocalDate.parse(parts.next());
      this.fromDateTime = date.atStartOfDay();
      this.toDateTime = date.atTime(23, 59, 59);
      this.isRange = false;
    } else if (parts.hasNext("from")) {
      parts.next(); // Consume "from"
      if (!parts.hasNext()) {
        throw new IllegalArgumentException("Missing start date and time.");
      }
      this.fromDateTime = LocalDateTime.parse(parts.next());
      if (!parts.hasNext("to")) {
        throw new IllegalArgumentException("Missing 'to' keyword.");
      }
      parts.next(); // Consume "to"
      if (!parts.hasNext()) {
        throw new IllegalArgumentException("Missing end date and time.");
      }
      this.toDateTime = LocalDateTime.parse(parts.next());
      this.isRange = true;
    } else {
      throw new IllegalArgumentException("Invalid print command format.");
    }
  }

  @Override
  public String execute() {
    if (isRange) {
      return model.getEventsInRange(calendarName, fromDateTime, toDateTime).toString();
    } else {
      return model.getEventsInRange(calendarName, fromDateTime, toDateTime).toString();
    }
  }
}
