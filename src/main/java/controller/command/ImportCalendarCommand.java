package controller.command;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import model.ICalendarEventDTO;
import model.ICalendarModel;

public class ImportCalendarCommand implements ICommand {
  private final String calendarName;
  private final String filePath;
  private final ICalendarModel model;

  /**
   * Constructs an {@code ImportCalendarCommand} to import events from a file.
   */
  public ImportCalendarCommand(List<String> args, ICalendarModel model, String currentCalendar) {
    this.model = Objects.requireNonNull(model,"Model cannot be null");
    this.calendarName = currentCalendar;

    CommandParser.requireMinArgs(args, 1, "Missing filename for import");

    this.filePath = CommandParser.getRequiredArg(args, 0, "Missing import filename");

    // Ensure no extra arguments
    if (args.size() > 1) {
      throw new IllegalArgumentException("Too many arguments for import command");
    }
  }

  @Override
  public String execute() {
    try {
      List<ICalendarEventDTO> eventsToImport = importFromCSV();

      if (eventsToImport.isEmpty()) {
        return "No events found to import.";
      }

      // Use the new addEvents method that handles validation internally
      model.addEvents(calendarName, eventsToImport);

      return "Successfully imported " + eventsToImport.size() +
            " events to calendar '" + calendarName + "'";

    } catch (IllegalStateException e) {
      return "Error importing calendar: " + e.getMessage();
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

        // Parse event fields
        String eventName = fields[0].replaceAll("^\"|\"$", "");
        String startDateStr = fields[1]; // Start Date
        String startTimeStr = fields[2]; // Start Time
        String endDateStr = fields[3]; // End Date
        String endTimeStr = fields[4]; // End Time
        boolean isAllDay = fields.length > 5 && "TRUE".equalsIgnoreCase(fields[5].trim());
        String description = fields.length > 6 ? fields[6].replaceAll("^\"|\"$", "") : ""; // Description
        String location = fields.length > 7 ? fields[7].replaceAll("^\"|\"$", "") : ""; // Location
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