package controller.command;

import java.time.LocalDateTime;
import java.util.List;
import model.ICalendarEventDTO;
import model.ICalendarModel;

/**
 * Command to check the user status (busy/available) on a given date and time.
 */
public class ShowStatusCommand implements ICommand {
  private final ICalendarModel model;
  private final String calendarName;
  private final LocalDateTime dateTime;

  public ShowStatusCommand(List<String> parts, ICalendarModel model, String currentCalendar) {
    this.model = model;
    this.calendarName = currentCalendar;

    if (parts.size() != 2) {
      throw new IllegalArgumentException("Invalid syntax. Expected: show status on <datetime>");
    }

    if (!parts.get(0).equals("on")) {
      throw new IllegalArgumentException("Missing 'on' keyword.");
    }

    try {
      this.dateTime = LocalDateTime.parse(parts.get(1));
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid date and time format. Expected: yyyy-MM-ddTHH:mm");
    }
  }

  @Override
  public String execute() {
    List<ICalendarEventDTO> events = model.getEventsInSpecificDateTime(calendarName, dateTime);
    return events.isEmpty() ? "Available" : "Busy";
  }
}