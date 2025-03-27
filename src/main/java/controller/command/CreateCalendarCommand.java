package controller.command;

import java.util.List;
import model.ICalendarModel;

/**
 * Command to create a new calendar.
 */
public class CreateCalendarCommand implements ICommand {
  private final ICalendarModel model;
  private final String calendarName;
  private final String timezone;

  public CreateCalendarCommand(List<String> args, ICalendarModel model, String currentCalendar) {
    this.model = model;

    if (args.size() < 4) {
      throw new IllegalArgumentException("Usage: create calendar --name <name> --timezone <timezone>");
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
      throw new IllegalArgumentException("Unrecognized extra arguments: " + String.join(" ", args.subList(4, args.size())));
    }
  }

  @Override
  public String execute() {
    return model.createCalendar(calendarName, timezone)
            ? "Calendar created successfully."
            : "Error: Calendar creation failed.";
  }
}
