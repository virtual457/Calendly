package command;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import model.ICalendarEventDTO;
import model.ICalendarModel;

/**
 * Command to check the user's status (busy/available) on a given date and time.
 */
public class ShowStatusCommand implements ICommand {
  private final ICalendarModel model;
  private final String calendarName;
  private final LocalDateTime dateTime;

  public ShowStatusCommand(Scanner parts, ICalendarModel model, String currentCalendar) {
    this.model = model;
    this.calendarName = currentCalendar;

    if (!parts.hasNext("on")) {
      throw new IllegalArgumentException("Missing 'on' keyword.");
    }
    parts.next();

    if (!parts.hasNext()) {
      throw new IllegalArgumentException("Missing date and time.");
    }
    this.dateTime = LocalDateTime.parse(parts.next());
  }

  @Override
  public String execute() {
    List<ICalendarEventDTO> events = model.getEventsInRange(calendarName, dateTime, dateTime);
    return events.isEmpty() ? "Available" : "Busy";
  }
}
