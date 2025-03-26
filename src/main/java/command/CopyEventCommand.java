package command;

import model.ICalendarModel;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Command to copy a specific event from one calendar to another.
 */
public class CopyEventCommand implements ICommand {
  private final ICalendarModel model;
  private final String sourceCalendar;
  private final String eventName;
  private final LocalDateTime sourceDateTime;
  private final String targetCalendar;
  private final LocalDateTime targetDateTime;

  public CopyEventCommand(List<String> args, ICalendarModel model, String currentCalendar) {
    this.model = model;
    this.sourceCalendar = currentCalendar;

    if (args.size() < 7) {
      throw new IllegalArgumentException("Incomplete command. Expected: copy event <eventName> on <dateTime> --target <calendar> to <dateTime>");
    }

    if (!args.get(0).equals("event")) {
      throw new IllegalArgumentException("Expected 'event' after 'copy'.");
    }

    this.eventName = args.get(1);
    if (eventName.isEmpty()) {
      throw new IllegalArgumentException("Missing event name.");
    }

    if (!args.get(2).equals("on")) {
      throw new IllegalArgumentException("Expected 'on' after event name.");
    }

    try {
      this.sourceDateTime = LocalDateTime.parse(args.get(3));
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid source date and time format.");
    }

    if (!args.get(4).equals("--target")) {
      throw new IllegalArgumentException("Expected '--target' after source date and time.");
    }

    this.targetCalendar = args.get(5);
    if (targetCalendar.isEmpty()) {
      throw new IllegalArgumentException("Missing target calendar name.");
    }

    if (!args.get(6).equals("to")) {
      throw new IllegalArgumentException("Expected 'to' after target calendar name.");
    }

    if (args.size() < 8) {
      throw new IllegalArgumentException("Missing target date and time.");
    }

    try {
      this.targetDateTime = LocalDateTime.parse(args.get(7));
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid target date and time format.");
    }
  }

  @Override
  public String execute() {
    boolean success = model.copyEvent(sourceCalendar, sourceDateTime, eventName, targetCalendar, targetDateTime);
    return success ? "Event copied successfully." : "Error copying event.";
  }
}
