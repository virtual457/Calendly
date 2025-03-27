package controller.command;

import model.ICalendarEventDTO;
import model.ICalendarModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Command to export events from a calendar to a CSV file in Google Calendar format.
 */
public class ExportEventsCommand implements ICommand {
  private final ICalendarModel model;
  private final String calendarName;
  private final String fileName;

  /**
   * Constructs an {@code ExportEventsCommand}
   * using the provided arguments, model, and active calendar.
   *
   * @param args            the list of command-line arguments
   * @param model           the calendar model to interact with
   * @param currentCalendar the name of the currently selected calendar
   */

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
    LocalDateTime start = LocalDateTime.MIN;
    LocalDateTime end = LocalDateTime.MAX;
    List<ICalendarEventDTO> events = model.getEventsInRange(calendarName, start, end);
    return exportToCSV(events);
  }

  private String escapeCSV(String value) {
    return "\"" + value.replace("\"", "\"\"") + "\"";
  }

  /**
   * private helper to create thhe export file with the events requested.
   *
   * @param events the list of events to be included in the exported file
   * @return returns a string specifing the status of the export.
   */
  private String exportToCSV(List<ICalendarEventDTO> events) {
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
        writer.write("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private");
        writer.newLine();

        for (ICalendarEventDTO event : events) {
          boolean isAllDay = event.getStartDateTime().toLocalTime().equals(LocalTime.MIDNIGHT)
              && event.getEndDateTime().toLocalTime().equals(LocalTime.of(23, 59, 59));

          String subject = escapeCSV(event.getEventName());
          String startDate = event.getStartDateTime().format(dateFormatter);
          String endDate = event.getEndDateTime().format(dateFormatter);
          String startTime = event.getStartDateTime().format(timeFormatter);
          String endTime = event.getEndDateTime().format(timeFormatter);
          String allDay = isAllDay ? "True" : "False";

          String description = event.getEventDescription() != null ?
              escapeCSV(event.getEventDescription()) : "";
          String location = event.getEventLocation() != null ?
              escapeCSV(event.getEventLocation()) : "";
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
}