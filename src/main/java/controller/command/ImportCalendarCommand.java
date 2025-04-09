package controller.command;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import model.ICalendarEventDTO;
import model.ICalendarModel;

public class ImportCalendarCommand implements ICommand {
  private final String calendarName;
  private final String filePath;
  private final ICalendarModel model;

  private static final List<String> REQUIRED_HEADER_FIELDS = Arrays.asList(
        "Subject", "Start Date", "Start Time", "End Date", "End Time", "All Day Event");

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
      String headerLine = reader.readLine();

      // Validate the header line
      if (headerLine == null) {
        throw new IllegalArgumentException("CSV file is empty");
      }

      validateHeader(headerLine);

      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

      String line;
      while ((line = reader.readLine()) != null) {
        if (line.trim().isEmpty()) {
          continue; // Skip empty lines
        }

        try {
          String[] fields = parseCSVLine(line);
          if (fields.length < 8) {
            throw new IllegalArgumentException("Line does not have enough fields: " + line);
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

          try {
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
          } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date/time format in line: " + line + ", Error: " + e.getMessage());
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
          throw new IllegalArgumentException("Error parsing line: " + line + ", Error: " + e.getMessage());
        }
      }
    }

    return events;
  }

  /**
   * Validates that the CSV header has all the required fields.
   *
   * @param headerLine The header line to validate
   * @throws IllegalArgumentException if required fields are missing
   */
  private void validateHeader(String headerLine) throws IllegalArgumentException {
    if (headerLine == null || headerLine.trim().isEmpty()) {
      throw new IllegalArgumentException("CSV header is empty");
    }

    // Parse the header line into field names
    String[] headerFields = parseCSVLine(headerLine);
    Set<String> headerFieldSet = new HashSet<>();
    for (String field : headerFields) {
      headerFieldSet.add(field.trim());
    }

    // Check for required fields
    List<String> missingFields = new ArrayList<>();
    for (String requiredField : REQUIRED_HEADER_FIELDS) {
      if (!headerFieldSet.contains(requiredField)) {
        missingFields.add(requiredField);
      }
    }

    if (!missingFields.isEmpty()) {
      throw new IllegalArgumentException("CSV file is missing required header fields: " +
            String.join(", ", missingFields));
    }
  }

  // Helper method to properly parse CSV lines with quotes
  private String[] parseCSVLine(String line) {
    // Simple CSV parsing - for production code, use a proper CSV library
    return line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
  }
}