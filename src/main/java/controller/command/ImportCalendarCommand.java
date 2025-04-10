package controller.command;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import model.ICalendarEventDTO;
import model.ICalendarModel;

public class ImportCalendarCommand implements ICommand {
  private final String calendarName;
  private final String filePath;
  private final ICalendarModel model;
  private final String timezone;

  /**
   * Constructs an {@code ImportCalendarCommand} to import events from a file.
   */
  public ImportCalendarCommand(List<String> args, ICalendarModel model, String currentCalendar) {
    this.model = Objects.requireNonNull(model,"Model cannot be null");
    this.calendarName = currentCalendar;

    CommandParser.requireMinArgs(args, 1, "Missing filename for import");
    this.filePath = CommandParser.getRequiredArg(args, 0, "Missing import filename");

    // Check for required timezone parameter
    if (args.size() > 1 && args.get(1).equals("--timezone") && args.size() > 2) {
      this.timezone = args.get(2);

      // Validate the timezone
      try {
        ZoneId.of(this.timezone);
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid timezone: " + this.timezone);
      }
    } else {
      // Timezone is mandatory for imports
      throw new IllegalArgumentException("Timezone must be specified for import using --timezone parameter");
    }
  }

  @Override
  public String execute() {
    try {
      List<ICalendarEventDTO> eventsToImport = importFromCSV();

      if (eventsToImport.isEmpty()) {
        return "No events found to import.";
      }

      // Use the addEvents method that handles validation internally
      model.addEvents(calendarName, eventsToImport, timezone);

      return "Successfully imported " + eventsToImport.size() +
            " events to calendar '" + calendarName + "' with timezone '" + timezone + "'";

    } catch (IllegalStateException e) {
      return "Error importing calendar: " + e.getMessage();
    } catch (Exception e) {
      return "Error importing calendar: " + e.getMessage();
    }
  }

  private List<ICalendarEventDTO> importFromCSV() throws Exception {
    List<ICalendarEventDTO> events = new ArrayList<>();
    List<String> validationErrors = new ArrayList<>();
    int lineNumber = 1; // Start counting from header line

    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;
      boolean isHeader = true;
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

      while ((line = reader.readLine()) != null) {
        if (isHeader) {
          isHeader = false;
          lineNumber++;
          if(!line.trim().equals("Subject,Start Date,Start Time,End Date,End Time,All Day " +
                "Event,Description,Location,Private")){
            throw new Exception("Invalid Header line: " + line);
          }
          continue; // Skip header row
        }

        try {
          String[] fields = parseCSVLine(line);
          if (fields.length != 9) {
            validationErrors.add("Line " + lineNumber + ": Expected 9 fields, but found " + fields.length);
            lineNumber++;
            continue;
          }

          // Parse event fields
          String eventName = removeOuterQuotes(fields[0]); // Subject
          String startDateStr = fields[1].trim(); // Start Date
          String startTimeStr = fields[2].trim(); // Start Time
          String endDateStr = fields[3].trim(); // End Date
          String endTimeStr = fields[4].trim(); // End Time
          String allDayStr = fields[5].trim(); // All Day Event
          String description = removeOuterQuotes(fields[6]);
          String location = removeOuterQuotes(fields[7]);
          String isPrivateStr = fields[8].trim();

          // Validate required fields
          if (eventName == null || eventName.trim().isEmpty()) {
            validationErrors.add("Line " + lineNumber + ": Event name is mandatory");
            lineNumber++;
            continue;
          }

          if (startDateStr.isEmpty()) {
            validationErrors.add("Line " + lineNumber + ": Start date is mandatory");
            lineNumber++;
            continue;
          }

          if (endDateStr.isEmpty()) {
            validationErrors.add("Line " + lineNumber + ": End date is mandatory");
            lineNumber++;
            continue;
          }

          // Check for valid boolean fields
          boolean isAllDay = false;
          if (!allDayStr.isEmpty()) {
            if (!isValidBoolean(allDayStr)) {
              validationErrors.add("Line " + lineNumber + ": All Day Event must be TRUE or FALSE");
              lineNumber++;
              continue;
            }
            isAllDay = Boolean.parseBoolean(allDayStr.toLowerCase());
          }

          boolean isPrivate = false;
          if (!isPrivateStr.isEmpty()) {
            if (!isValidBoolean(isPrivateStr)) {
              validationErrors.add("Line " + lineNumber + ": Private must be TRUE or FALSE");
              lineNumber++;
              continue;
            }
            isPrivate = Boolean.parseBoolean(isPrivateStr.toLowerCase());
          }

          // Parse dates and times
          LocalDateTime startDateTime;
          LocalDateTime endDateTime;

          try {
            if (isAllDay) {
              // For all-day events, set to start of day and end of day
              if (startDateStr.isEmpty()) {
                validationErrors.add("Line " + lineNumber + ": Start date is required for all-day events");
                lineNumber++;
                continue;
              }

              LocalDate startDate = LocalDate.parse(startDateStr, dateFormatter);
              LocalDate endDate = LocalDate.parse(endDateStr, dateFormatter);
              startDateTime = startDate.atStartOfDay();
              endDateTime = endDate.atTime(23, 59, 59);
            } else {
              // For regular events, parse both date and time
              if (startTimeStr.isEmpty()) {
                validationErrors.add("Line " + lineNumber + ": Start time is mandatory for non-all-day events");
                lineNumber++;
                continue;
              }

              if (endTimeStr.isEmpty()) {
                validationErrors.add("Line " + lineNumber + ": End time is mandatory for non-all-day events");
                lineNumber++;
                continue;
              }

              LocalDate startDate = LocalDate.parse(startDateStr, dateFormatter);
              LocalTime startTime = LocalTime.parse(startTimeStr, timeFormatter);
              LocalDate endDate = LocalDate.parse(endDateStr, dateFormatter);
              LocalTime endTime = LocalTime.parse(endTimeStr, timeFormatter);

              startDateTime = LocalDateTime.of(startDate, startTime);
              endDateTime = LocalDateTime.of(endDate, endTime);
            }

            // Validate end time is after start time
            if (endDateTime.isBefore(startDateTime) || endDateTime.equals(startDateTime)) {
              validationErrors.add("Line " + lineNumber + ": End date/time must be after start date/time");
              lineNumber++;
              continue;
            }
          } catch (DateTimeParseException e) {
            validationErrors.add("Line " + lineNumber + ": Invalid date/time format - " + e.getMessage());
            lineNumber++;
            continue;
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
          validationErrors.add("Line " + lineNumber + ": " + e.getMessage());
        }

        lineNumber++;
      }
    }

    // If there are validation errors, throw an exception with all errors
    if (!validationErrors.isEmpty()) {
      throw new IllegalStateException("CSV validation errors:\n" + String.join("\n", validationErrors));
    }

    return events;
  }

  /**
   * Checks if a string represents a valid boolean value (true/false).
   * Case-insensitive.
   *
   * @param value The string to check
   * @return true if the string is a valid boolean representation
   */
  private boolean isValidBoolean(String value) {
    return value.equalsIgnoreCase("true") ||
          value.equalsIgnoreCase("false");
  }

  private String[] parseCSVLine(String line) {
    return line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
  }

  /**
   * Removes starting and ending quotes from a string if present.
   *
   * @param value The string to process
   * @return String with outer quotes removed if present
   */
  private String removeOuterQuotes(String value) {
    if (value == null) {
      return null;
    }

    value = value.trim();

    if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
      return value.substring(1, value.length() - 1);
    }

    return value;
  }
}