package controller.command;

import java.util.List;
import java.util.Objects;

import model.ICalendarModel;

/**
 * Command to create a new calendar.
 */
public class CreateCalendarCommand implements ICommand {
  private final ICalendarModel model;
  private String calendarName;
  private final String timezone;

  /**
   * Constructs a {@code CreateCalendarCommand}
   * that creates a new calendar based on the provided arguments.
   */
  public CreateCalendarCommand(List<String> args, ICalendarModel model, String currentCalendar) {
    this.model = Objects.requireNonNull(model,"Model cannot be null");
    this.calendarName = currentCalendar;

    // Validate minimum required arguments
    CommandParser.requireMinArgs(args, 4, "Usage: create calendar --name <name> --timezone <timezone>");

    // Check for correct flags
    CommandParser.requireKeyword(args, 0, "--name", "Expected --name flag");

    // Get calendar name
    this.calendarName = CommandParser.getRequiredArg(args, 1, "Missing calendar name");

    // Check timezone flag
    CommandParser.requireKeyword(args, 2, "--timezone", "Expected --timezone flag");

    // Get timezone
    this.timezone = CommandParser.getRequiredArg(args, 3, "Missing timezone");

    // Ensure no extra arguments
    if (args.size() > 4) {
      throw new IllegalArgumentException("Unrecognized extra arguments: " +
            String.join(" ", args.subList(4, args.size())));
    }
  }

  @Override
  public String execute() {
    try {
      boolean success = model.createCalendar(calendarName, timezone);
      return success ? "Calendar created successfully." : "Error: Calendar creation failed.";
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "An unexpected error occurred: " + e.getMessage();
    }
  }
}