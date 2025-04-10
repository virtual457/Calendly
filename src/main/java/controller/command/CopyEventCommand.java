package controller.command;

import model.ICalendarModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
    this.model = Objects.requireNonNull(model,"Model cannot be null");
    this.sourceCalendar = currentCalendar;

    validateCommandArguments(args);


    int index = 0;
    this.eventName = args.get(index++);


    index++;

    this.sourceDateTime = LocalDateTime.parse(args.get(index++));

    index++;

    this.targetCalendar = args.get(index++);


    index++;

    this.targetDateTime = LocalDateTime.parse(args.get(index));
  }

  /**
   * Validates the command arguments before parsing.
   *
   * @param args the command arguments to validate
   * @throws IllegalArgumentException if any validation fails
   */
  private void validateCommandArguments(List<String> args) {
    if (args.size() < 6) {
      throw new IllegalArgumentException(
            "Insufficient arguments. Expected: event_name on datetime --target calendar_name to datetime");
    }

    // Validate event name
    if (args.get(0).isEmpty()) {
      throw new IllegalArgumentException("Event name cannot be empty.");
    }

    // Validate "on" keyword
    if (!args.get(1).equals("on")) {
      throw new IllegalArgumentException("Expected 'on' after event name.");
    }

    // Validate source datetime format
    try {
      LocalDateTime.parse(args.get(2));
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid source date and time format: " + args.get(2));
    }

    // Validate "--target" keyword
    if (!args.get(3).equals("--target")) {
      throw new IllegalArgumentException("Expected '--target' after source date and time.");
    }

    // Validate target calendar name
    if (args.get(4).isEmpty()) {
      throw new IllegalArgumentException("Target calendar name cannot be empty.");
    }

    // Validate "to" keyword
    if (!args.get(5).equals("to")) {
      throw new IllegalArgumentException("Expected 'to' after target calendar name.");
    }

    // Validate target datetime format
    if (args.size() > 6) {
      try {
        LocalDateTime.parse(args.get(6));
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid target date and time format: " + args.get(6));
      }
    } else {
      throw new IllegalArgumentException("Missing target datetime.");
    }
  }

  @Override
  public String execute() {
    try {
      boolean success = model.copyEvent(sourceCalendar, sourceDateTime, eventName, targetCalendar,
            targetDateTime);
      return success ? "Event copied successfully." : "Error copying event.";
     } catch (IllegalArgumentException | IllegalStateException e) {
    return "Error: " + e.getMessage();
  } catch (Exception e) {

    return "An unexpected error occurred: " + e.getMessage();
  }
  }
}
