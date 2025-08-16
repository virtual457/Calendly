package controller.command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import model.ICalendarModel;

/**
 * Command to copy events from a calendar.
 */
public class CopyEventsCommand implements ICommand {
  private final ICalendarModel model;
  private final String sourceCalendar;
  private LocalDate fromDate;
  private LocalDate toDate;
  private String targetCalendar;
  private LocalDate targetStartDate;

  /**
   * Constructs a {@code CopyEventsCommand}
   * for copying events from a source calendar to a target calendar.
   */
  public CopyEventsCommand(List<String> parts, ICalendarModel model, String currentCalendar) {
    this.model = Objects.requireNonNull(model,"Model cannot be null");
    this.sourceCalendar = currentCalendar;

    CommandParser.requireMinArgs(parts, 1, "Expected 'copy events on <date> --target <calendar> to " +
          "<date>' or 'copy events between <date> and <date> --target <calendar> to <date>'");

    String type = CommandParser.getRequiredArg(parts, 0, "Missing copy type ('on' or 'between')");

    if (type.equals("on")) {
      // Format: copy events on <date> --target <calendar> to <date>
      CommandParser.requireMinArgs(parts, 5, "Insufficient arguments for copy events on date");

      // Parse source date
      fromDate = CommandParser.parseDate(parts, 1, "Invalid source date");

      // Check --target keyword
      CommandParser.requireKeyword(parts, 2, "--target", "Expected '--target' after source date");

      // Get target calendar
      targetCalendar = CommandParser.getRequiredArg(parts, 3, "Missing target calendar name");

      // Check to keyword
      CommandParser.requireKeyword(parts, 4, "to", "Expected 'to' after target calendar");

      // Parse target date
      targetStartDate = CommandParser.parseDate(parts, 5, "Invalid target date");

      // For 'on' mode, to date is same as from date
      toDate = fromDate;
    }
    else if (type.equals("between")) {
      // Format: copy events between <date> and <date> --target <calendar> to <date>
      CommandParser.requireMinArgs(parts, 7, "Insufficient arguments for copy events between dates");

      // Parse start date
      fromDate = CommandParser.parseDate(parts, 1, "Invalid start date");

      // Check 'and' keyword
      CommandParser.requireKeyword(parts, 2, "and", "Expected 'and' after start date");

      // Parse end date
      toDate = CommandParser.parseDate(parts, 3, "Invalid end date");

      // Check --target keyword
      CommandParser.requireKeyword(parts, 4, "--target", "Expected '--target' after end date");

      // Get target calendar
      targetCalendar = CommandParser.getRequiredArg(parts, 5, "Missing target calendar name");

      // Check to keyword
      CommandParser.requireKeyword(parts, 6, "to", "Expected 'to' after target calendar");

      // Parse target start date
      targetStartDate = CommandParser.parseDate(parts, 7, "Invalid target start date");
    }
    else {
      throw new IllegalArgumentException("Expected 'on' or 'between' after 'events'.");
    }
  }

  @Override
  public String execute() {
    try {
      LocalDateTime rangeStart = fromDate.atStartOfDay();
      LocalDateTime rangeEnd = toDate.atTime(LocalTime.MAX);

      boolean success = model.copyEvents(sourceCalendar, rangeStart, rangeEnd, targetCalendar,
            targetStartDate);
      return success ? "Events copied successfully." : "Error copying events.";
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "An unexpected error occurred: " + e.getMessage();
    }
  }
}