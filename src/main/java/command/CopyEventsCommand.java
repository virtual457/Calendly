package command;

import model.ICalendarModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Command to copy events from a single day or a date range to another calendar.
 */
public class CopyEventsCommand implements ICommand {
  private final ICalendarModel model;
  private final String sourceCalendar;

  // Parsed values
  private String copyType; // "on" or "between"
  private LocalDate fromDate;
  private LocalDate toDate;
  private String targetCalendar;
  private LocalDate targetStartDate;

  public CopyEventsCommand(List<String> args, ICalendarModel model, String currentCalendar) {
    this.model = model;
    this.sourceCalendar = currentCalendar;

    // Step-by-step parsing and validation
    if (args.isEmpty()) {
      throw new IllegalArgumentException("Missing command arguments after 'copy'.");
    }

    if (!args.get(0).equals("events")) {
      throw new IllegalArgumentException("Expected 'events' after 'copy'.");
    }

    if (args.size() < 2) {
      throw new IllegalArgumentException("Expected 'on' or 'between' after 'events'.");
    }

    this.copyType = args.get(1);
    if (copyType.equals("on")) {
      parseOnFormat(args);
    } else if (copyType.equals("between")) {
      parseBetweenFormat(args);
    } else {
      throw new IllegalArgumentException("Expected 'on' or 'between' after 'events'.");
    }
  }

  private void parseOnFormat(List<String> args) {
    if (args.size() < 7) {
      throw new IllegalArgumentException("Incomplete command. Expected: copy events on <date> --target <calendar> to <date>");
    }

    try {
      this.fromDate = LocalDate.parse(args.get(2));
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid source date format.");
    }

    if (!args.get(3).equals("--target")) {
      throw new IllegalArgumentException("Expected '--target' after source date.");
    }

    this.targetCalendar = args.get(4);
    if (targetCalendar.isEmpty()) {
      throw new IllegalArgumentException("Missing target calendar name.");
    }

    if (!args.get(5).equals("to")) {
      throw new IllegalArgumentException("Expected 'to' after target calendar.");
    }

    try {
      this.targetStartDate = LocalDate.parse(args.get(6));
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid target date format.");
    }

    this.toDate = this.fromDate; // same day
  }

  private void parseBetweenFormat(List<String> args) {
    if (args.size() < 9) {
      throw new IllegalArgumentException("Incomplete command. Expected: copy events between <date> and <date> --target <calendar> to <date>");
    }

    try {
      this.fromDate = LocalDate.parse(args.get(2));
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid start date format.");
    }

    if (!args.get(3).equals("and")) {
      throw new IllegalArgumentException("Expected 'and' after start date.");
    }

    try {
      this.toDate = LocalDate.parse(args.get(4));
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid end date format.");
    }

    if (!args.get(5).equals("--target")) {
      throw new IllegalArgumentException("Expected '--target' after end date.");
    }

    this.targetCalendar = args.get(6);
    if (targetCalendar.isEmpty()) {
      throw new IllegalArgumentException("Missing target calendar name.");
    }

    if (!args.get(7).equals("to")) {
      throw new IllegalArgumentException("Expected 'to' after target calendar.");
    }

    try {
      this.targetStartDate = LocalDate.parse(args.get(8));
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid target start date format.");
    }
  }

  @Override
  public String execute() {
    LocalDateTime rangeStart = fromDate.atStartOfDay();
    LocalDateTime rangeEnd = toDate.atTime(LocalTime.MAX);
    LocalDate targetStart = targetStartDate;

    boolean success = model.copyEvents(sourceCalendar, rangeStart, rangeEnd, targetCalendar, targetStart);
    return success ? "Events copied successfully." : "Error copying events.";
  }
}
