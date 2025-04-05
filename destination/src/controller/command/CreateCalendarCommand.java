package controller.command;

import java.util.List;

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
   *
   * @param args             the list of arguments used to specify the calendar name and timezone
   * @param model            the calendar model responsible for handling calendar creation logic
   * @param currentCalendar  the name of the currently active calendar (if any)
   */

  public CreateCalendarCommand(List<String> args, ICalendarModel model, String currentCalendar) {
    this.model = model;
    this.calendarName = currentCalendar;

    if (args.size() < 4) {
      throw new IllegalArgumentException("Usage: create calendar --name <name> --timezone " +
          "<timezone>");
    }

    if (!args.get(0).equals("--name")) {
      throw new IllegalArgumentException("Expected --name flag.");
    }

    this.calendarName = args.get(1);

    if (!args.get(2).equals("--timezone")) {
      throw new IllegalArgumentException("Expected --timezone flag.");
    }

    this.timezone = args.get(3);

    if (args.size() > 4) {
      throw new IllegalArgumentException("Unrecognized extra arguments: " + String.join(" ",
          args.subList(4, args.size())));
    }
  }

  @Override
  public String execute() {
    return model.createCalendar(calendarName, timezone)
        ? "Calendar created successfully."
        : "Error: Calendar creation failed.";
  }
}
