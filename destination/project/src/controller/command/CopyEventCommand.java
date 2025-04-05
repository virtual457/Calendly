package controller.command;

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

  /**
   * Constructs a {@code CopyEventCommand} to copy a specific event from the current calendar
   * to a target calendar at a specified time.
   *
   * @param args             the parsed command
   *                        arguments including event name,
   *                         source time, target calendar, and new time
   * @param model            the calendar model used to perform the event copy operation
   * @param currentCalendar  the name of the currently active calendar (source calendar)
   * @throws IllegalArgumentException if the provided arguments are invalid or incomplete
   */

  public CopyEventCommand(List<String> args, ICalendarModel model, String currentCalendar) {
    this.model = model;
    this.sourceCalendar = currentCalendar;

    int index = 0;

    try {

      if (index == args.size()) {
        throw new IllegalArgumentException("Missing event name.");
      }
      this.eventName = args.get(index++);
      if (eventName.isEmpty()) {
        throw new IllegalArgumentException("Event name cannot be empty.");
      }


      if (index == args.size()) {
        throw new IllegalArgumentException("Missing 'on' " +
            "keyword.");
      }
      String onKeyword = args.get(index++);
      if (!onKeyword.equals("on")) {
        throw new IllegalArgumentException("Expected 'on' after event name.");
      }


      if (index == args.size()) {
        throw new IllegalArgumentException("Missing source " +
            "datetime.");
      }
      try {
        this.sourceDateTime = LocalDateTime.parse(args.get(index++));
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid source date and time format.");
      }


      if (index == args.size()) {
        throw new IllegalArgumentException("Missing '--target' " +
            "keyword.");
      }
      String targetKeyword = args.get(index++);
      if (!targetKeyword.equals("--target")) {
        throw new IllegalArgumentException("Expected '--target' after source date and time.");
      }


      if (index == args.size()) {
        throw new IllegalArgumentException("Missing target " +
            "calendar name.");
      }
      this.targetCalendar = args.get(index++);
      if (targetCalendar.isEmpty()) {
        throw new IllegalArgumentException("Target calendar name cannot be empty.");
      }


      if (index == args.size()) {
        throw new IllegalArgumentException("Missing 'to' " +
            "keyword.");
      }
      String toKeyword = args.get(index++);
      if (!toKeyword.equals("to")) {
        throw new IllegalArgumentException("Expected 'to' after target calendar name.");
      }


      if (index == args.size()) {
        throw new IllegalArgumentException("Missing target " +
            "datetime.");
      }
      try {
        this.targetDateTime = LocalDateTime.parse(args.get(index));
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid target date and time format.");
      }

    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Incomplete command. Please check the syntax.");
    }
  }

  @Override
  public String execute() {
    boolean success = model.copyEvent(sourceCalendar, sourceDateTime, eventName, targetCalendar,
        targetDateTime);
    return success ? "Event copied successfully." : "Error copying event.";
  }
}
