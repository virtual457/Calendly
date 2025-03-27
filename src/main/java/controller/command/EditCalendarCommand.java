package controller.command;

import model.ICalendarModel;

import java.util.List;

/**
 * Command to edit a calendar's name or timezone.
 */
public class EditCalendarCommand implements ICommand {
  private final ICalendarModel model;
  private String calendarName;
  private final String property;
  private final String newValue;

  /**
   * Constructs an {@code EditCalendarCommand}
   * that allows editing properties of a calendar.
   *
   * @param args          the list of command-line arguments including calendar name,
   *                     property, and new value
   * @param model         the calendar model instance used to perform operations
   * @param calendarName  the name of the calendar to edit
   */

  public EditCalendarCommand(List<String> args, ICalendarModel model, String calendarName) {
    this.model = model;
    this.calendarName = calendarName;

    int index = 0;

    try {
      // '--name'
      if (index >= args.size()) {
        throw new IllegalArgumentException("Missing '--name' keyword.");
      }
      String nameFlag = args.get(index++);
      if (!nameFlag.equals("--name")) {
        throw new IllegalArgumentException("Expected '--name' keyword.");
      }

      // <name-of-calendar>
      if (index >= args.size()) {
        throw new IllegalArgumentException("Missing calendar name.");
      }
      this.calendarName = args.get(index++);
      if (this.calendarName.isEmpty()) {
        throw new IllegalArgumentException("Calendar name cannot be empty.");
      }

      // '--property'
      if (index >= args.size()) {
        throw new IllegalArgumentException("Missing '--property' keyword.");
      }
      String propFlag = args.get(index++);
      if (!propFlag.equals("--property")) {
        throw new IllegalArgumentException("Expected '--property' keyword.");
      }

      // <property-name>
      if (index >= args.size()) {
        throw new IllegalArgumentException("Missing property name.");
      }
      this.property = args.get(index++);
      if (!property.equals("name") && !property.equals("timezone")) {
        throw new IllegalArgumentException("Invalid property. Only 'name' or 'timezone' allowed.");
      }

      // <new-property-value>
      if (index >= args.size()) {
        throw new IllegalArgumentException("Missing new property value.");
      }
      this.newValue = args.get(index);
      if (newValue.isEmpty()) {
        throw new IllegalArgumentException("New property value cannot be empty.");
      }

    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Incomplete command. Please check the syntax.");
    }
  }

  @Override
  public String execute() {
    boolean updated = model.editCalendar(calendarName, property, newValue);
    if (!updated) {
      return "Failed to update calendar. Please ensure the calendar exists and the value is valid.";
    }
    return "Calendar updated successfully.";
  }
}
