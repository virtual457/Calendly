package controller.command;

import java.util.List;

import model.ICalendarEventDTO;
import model.ICalendarModel;

public class BasicCreateEventCommand extends CreateEventCommand implements ICommand {
  public BasicCreateEventCommand(List<String> fromArgs, ICalendarModel model, String calendarName) {
    super(fromArgs, model, calendarName);
  }

  @Override
  public String execute() {
    try {
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
    } catch (IllegalArgumentException | IllegalStateException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "Unexpected error: " + e.getMessage();
    }
  }
}
