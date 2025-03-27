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

  public UseCalendarCommand(List<String> args, ICalendarModel model, String currentCalendar) {
    this.calendarName = currentCalendar;
    if (Objects.isNull(model)) {
      throw new IllegalArgumentException("Model is null for use calendar command.");
    }
    this.model = model;

    if (args.size() < 2 || !args.get(0).equals("--name")) {
      throw new IllegalArgumentException("Expected usage: use calendar --name <calendarName>");
    }

    this.calendarName = args.get(1);

    if (args.size() > 2) {
      throw new IllegalArgumentException("Unrecognized extra arguments: " + String.join(" ", args.subList(2, args.size())));
    }
  }

  @Override
  public String execute() {
    if (this.model.isCalendarPresent(calendarName)) {
      return "Using calendar: " + calendarName;
    }
    return "Error: calendar not found";
  }

  public String getCalendarName() {
    return calendarName;
  }
}
