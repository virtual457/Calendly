package command;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import model.CalendarEvent;
import model.ICalendarModel;

/**
 * Command to export events from a calendar to a file.
 */
public class ExportEventsCommand implements ICommand {
  private final ICalendarModel model;
  private final String calendarName;
  private final String fileName;

  public ExportEventsCommand(Scanner parts, ICalendarModel model, String currentCalendar) {
    this.model = model;
    this.calendarName = currentCalendar;

    if (!parts.hasNext()) {
      throw new IllegalArgumentException("Missing filename for export.");
    }
    this.fileName = parts.next();
  }

  @Override
  public String execute() {
    if (calendarName == null) {
      return "Error: No calendar selected.";
    }
    LocalDateTime start = LocalDateTime.of(2000, 1, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2100, 12, 31, 23, 59);
    List<CalendarEvent> events = model.getEventsInRange(calendarName, start, end);

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
      for (CalendarEvent event : events) {
        writer.write(event.getEventName() + ", " + event.getStartDateTime() + " to " + event.getEndDateTime());
        writer.newLine();
      }
      return "Events exported successfully to " + fileName;
    } catch (IOException e) {
      return "Error exporting events: " + e.getMessage();
    }
  }
}
