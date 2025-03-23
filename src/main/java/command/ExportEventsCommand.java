package command;

import model.CalendarEvent;
import model.ICalendarModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Command to export events from a calendar to a CSV file in Google Calendar format.
 */
public class ExportEventsCommand implements ICommand {
  private final ICalendarModel model;
  private final String calendarName;
  private final String fileName;

  public ExportEventsCommand(List<String> args, ICalendarModel model, String currentCalendar) {
    this.model = model;
    this.calendarName = currentCalendar;

    if (args.isEmpty()) {
      throw new IllegalArgumentException("Missing filename for export.");
    }
    this.fileName = args.get(0);
    if (args.size() > 1) {
      throw new IllegalArgumentException("Too many arguments for export command.");
    }
  }

  @Override
  public String execute() {
    if (calendarName == null) {
      return "Error: No calendar selected.";
    }
    LocalDateTime start = LocalDateTime.of(2000, 1, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2100, 12, 31, 23, 59);
    List<CalendarEvent> events = model.getEventsInRange(calendarName, start, end);

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

    try {
      File file = new File(fileName);
      if (file.exists() && !file.delete()) {
        return "Error: Unable to delete existing file.";
      }
      if (!file.createNewFile()) {
        return "Error: Unable to create file.";
      }

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        // Write Google Calendar CSV headers
        writer.write("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private");
        writer.newLine();

        for (CalendarEvent event : events) {
          boolean isAllDay = event.getStartDateTime().toLocalTime().equals(java.time.LocalTime.MIDNIGHT)
                  && event.getEndDateTime().toLocalTime().equals(java.time.LocalTime.of(23, 59, 59));

          String subject = escapeCSV(event.getEventName());
          String startDate = event.getStartDateTime().format(dateFormatter);
          String endDate = event.getEndDateTime().format(dateFormatter);
          String startTime = isAllDay ? "" : event.getStartDateTime().format(timeFormatter);
          String endTime = isAllDay ? "" : event.getEndDateTime().format(timeFormatter);
          String allDay = isAllDay ? "True" : "False";

          String description = event.getDescription() != null ? escapeCSV(event.getDescription()) : "";
          String location = event.getLocation() != null ? escapeCSV(event.getLocation()) : "";
          String isPrivate = event.isPrivate() ? "True" : "False";

          writer.write(String.join(",",
                  subject, startDate, startTime, endDate, endTime,
                  allDay, description, location, isPrivate));
          writer.newLine();
        }
      }
      return "Events exported successfully to " + fileName;
    } catch (IOException e) {
      return "Error exporting events: " + e.getMessage();
    }
  }

  private String escapeCSV(String value) {
    return "\"" + value.replace("\"", "\"\"") + "\"";
  }
}
