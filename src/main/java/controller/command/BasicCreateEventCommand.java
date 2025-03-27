package controller.command;

import java.util.List;

import model.ICalendarEventDTO;
import model.ICalendarModel;

/**
 * Represents a basic implementation of the {@code CreateEventCommand} used to handle
 * the creation of a calendar event in a straightforward, non-interactive manner.
 * <p>
 * This command constructs an event using the provided arguments and delegates the creation
 * logic to the underlying model.
 * </p>
 * Implements the {@code ICommand} interface for execution through the calendar controller.
 */

public class BasicCreateEventCommand extends CreateEventCommand implements ICommand {
  public BasicCreateEventCommand(List<String> fromArgs, ICalendarModel model, String calendarName) {
    super(fromArgs, model, calendarName);
  }

  @Override
  public String execute() {

    ICalendarEventDTO event = ICalendarEventDTO.builder()
          .setEventName(this.eventName)
          .setStartDateTime(startDateTime)
          .setEndDateTime(endDateTime)
          .setRecurring(isRecurring)
          .setRecurrenceDays(recurrenceDays)
          .setRecurrenceCount(recurrenceCount)
          .setRecurrenceEndDate(recurrenceEndDate)
          .setAutoDecline(autoDecline)
          .setEventDescription(description)
          .setEventLocation(location)
          .setPrivate(isPrivate)
          .build();

    boolean success = model.addEvent(calendarName, event);
    return success ? "Event created successfully." : "Error: Event creation failed.";

  }
}
