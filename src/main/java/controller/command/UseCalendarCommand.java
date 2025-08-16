package controller.command;

import java.util.List;
import java.util.Objects;

import model.ICalendarModel;

/**
 * Command to switch to a specific calendar.
 */
public class UseCalendarCommand implements ICommand {
  private String calendarName;
  private final ICalendarModel model;

  /**
   * Constructs a {@code UseCalendarCommand} with the given arguments, model, and current calendar.
   */
  public UseCalendarCommand(List<String> args, ICalendarModel model, String currentCalendar) {
    this.calendarName = currentCalendar;
    this.model = Objects.requireNonNull(model, "Model is null for use calendar command.");

    CommandParser.requireMinArgs(args, 2, "Expected usage: use calendar --name <calendarName>");
    CommandParser.requireKeyword(args, 0, "--name", "Expected --name flag");

    this.calendarName = CommandParser.getRequiredArg(args, 1, "Missing calendar name");

    if (args.size() > 2) {
      throw new IllegalArgumentException("Unrecognized extra arguments: " +
            String.join(" ", args.subList(2, args.size())));
    }
  }

  @Override
  public String execute() {
    try {
      if (this.model.isCalendarPresent(calendarName)) {
        return "Using calendar: " + calendarName;
      }
      return "Error: calendar not found";
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "An unexpected error occurred: " + e.getMessage();
    }
  }

  public String getCalendarName() {
    return calendarName;
  }
}