package command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;
import model.ICalendarModel;

/**
 * Command to copy an event from one calendar to another.
 */
public class CopyEventCommand implements ICommand {
  private final ICalendarModel model;
  private final String sourceCalendar;
  private final String eventName;
  private final LocalDateTime sourceDateTime;
  private final String targetCalendar;
  private final LocalDate targetDateTime;

  public CopyEventCommand(Scanner parts, ICalendarModel model, String currentCalendar) {
    this.model = model;
    this.sourceCalendar = currentCalendar;

    if (!parts.hasNext("event")) {
      throw new IllegalArgumentException("Expected 'event' after 'copy'.");
    }
    parts.next();

    if (!parts.hasNext()) {
      throw new IllegalArgumentException("Missing event name.");
    }
    this.eventName = parts.next();

    if (!parts.hasNext("on")) {
      throw new IllegalArgumentException("Missing 'on' keyword.");
    }
    parts.next();

    if (!parts.hasNext()) {
      throw new IllegalArgumentException("Missing source date and time.");
    }
    this.sourceDateTime = LocalDateTime.parse(parts.next());

    if (!parts.hasNext("--target")) {
      throw new IllegalArgumentException("Missing --target flag.");
    }
    parts.next();

    if (!parts.hasNext()) {
      throw new IllegalArgumentException("Missing target calendar name.");
    }
    this.targetCalendar = parts.next();

    if (!parts.hasNext("to")) {
      throw new IllegalArgumentException("Missing 'to' keyword.");
    }
    parts.next();

    if (!parts.hasNext()) {
      throw new IllegalArgumentException("Missing target date and time.");
    }
    this.targetDateTime = LocalDate.parse(parts.next());
  }

  @Override
  public String execute() {
    boolean success = model.copyEvents(sourceCalendar, sourceDateTime, sourceDateTime, targetCalendar, targetDateTime);
    return success ? "Event copied successfully." : "Error copying event.";
  }
}
