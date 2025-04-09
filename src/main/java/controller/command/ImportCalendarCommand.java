package controller.command;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import model.ICalendarEventDTO;
import model.ICalendarModel;

public class ImportCalendarCommand implements ICommand {
  private String calendarName;
  private String filePath;
  private ICalendarModel model;
  private String currentCalendar;

  public ImportCalendarCommand(List<String> args, ICalendarModel model, String currentCalendar) {
    this.model = model;
    this.currentCalendar = currentCalendar;

    if (args.size() < 4) {
      throw new IllegalArgumentException("Usage: import cal --calendar <calendarName> --file <filePath>");
    }

    // Parse arguments
    calendarName = currentCalendar; // Default to current calendar
    for (int i = 0; i < args.size(); i++) {
      if (args.get(i).equals("--calendar") && i + 1 < args.size()) {
        calendarName = args.get(i + 1);
        i++;
      } else if (args.get(i).equals("--file") && i + 1 < args.size()) {
        filePath = args.get(i + 1);
        i++;
      }
    }

    if (filePath == null) {
      throw new IllegalArgumentException("Missing required parameter: file. Usage: import cal --calendar <calendarName> --file <filePath>");
    }

    // Verify calendar exists
    if (!model.isCalendarPresent(calendarName)) {
      throw new IllegalArgumentException("Calendar '" + calendarName + "' not found.");
    }
  }

  @Override
  public String execute() {
    try {
      List<ICalendarEventDTO> eventsToImport = importFromCSV();
      int successCount = 0;

      // Add all events to the calendar
      for (ICalendarEventDTO event : eventsToImport) {
        try {
          model.addEvent(calendarName, event);
          successCount++;
        } catch (Exception e) {
          // Log error but continue with other events
          System.err.println("Error importing event: " + event.getEventName() + " - " + e.getMessage());
        }
      }

      return "Successfully imported " + successCount + " out of " +
          eventsToImport.size() + " events to calendar '" + calendarName + "'";
    } catch (Exception e) {
      return "Error importing calendar: " + e.getMessage();
    }
  }

  private List<ICalendarEventDTO> importFromCSV() throws Exception {
    List<ICalendarEventDTO> events = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;
      boolean isHeader = true;
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

      while ((line = reader.readLine()) != null) {
        if (isHeader) {
          isHeader = false;
          continue; // Skip header row
        }

        String[] fields = parseCSVLine(line);
        if (fields.length < 8) {
          continue; // Skip malformed lines
        }

        try {
          // Parse event fields
          String eventName = fields[0]; // Subject
          String startDateStr = fields[1]; // Start Date
          String startTimeStr = fields[2]; // Start Time
          String endDateStr = fields[3]; // End Date
          String endTimeStr = fields[4]; // End Time
          boolean isAllDay = fields.length > 5 && "TRUE".equalsIgnoreCase(fields[5].trim());
          String description = fields.length > 6 ? fields[6] : ""; // Description
          String location = fields.length > 7 ? fields[7] : ""; // Location
          boolean isPrivate = fields.length > 8 && "TRUE".equalsIgnoreCase(fields[8].trim());

          // Parse dates and times
          LocalDateTime startDateTime;
          LocalDateTime endDateTime;

          if (isAllDay) {
            // For all-day events, set to start of day and end of day
            LocalDate startDate = LocalDate.parse(startDateStr, dateFormatter);
            LocalDate endDate = LocalDate.parse(endDateStr, dateFormatter);
            startDateTime = startDate.atStartOfDay();
            endDateTime = endDate.atTime(23, 59, 59);
          } else {
            // For regular events, parse both date and time
            LocalDate startDate = LocalDate.parse(startDateStr, dateFormatter);
            LocalTime startTime = LocalTime.parse(startTimeStr, timeFormatter);
            LocalDate endDate = LocalDate.parse(endDateStr, dateFormatter);
            LocalTime endTime = LocalTime.parse(endTimeStr, timeFormatter);

            startDateTime = LocalDateTime.of(startDate, startTime);
            endDateTime = LocalDateTime.of(endDate, endTime);
          }

          // Create event DTO
          ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
              .setEventName(eventName)
              .setStartDateTime(startDateTime)
              .setEndDateTime(endDateTime)
              .setEventDescription(description)
              .setEventLocation(location)
              .setPrivate(isPrivate)
              .setAutoDecline(true)
              .build();

          events.add(eventDTO);
        } catch (Exception e) {
          // Log parsing error but continue with other events
          System.err.println("Error parsing event: " + String.join(",", fields) + " - " + e.getMessage());
        }
      }
    }

    return events;
  }

  // Helper method to properly parse CSV lines with quotes
  private String[] parseCSVLine(String line) {
    // Simple CSV parsing - for production code, use a proper CSV library
    return line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
  }
}