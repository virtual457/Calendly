package model;

import java.time.LocalDateTime;

/**
 * Represents an event in a calendar system.
 * <p>
 * This interface defines the contract for retrieving information about a calendar event,
 * such as its name, start and end time, recurrence, privacy, and location details.
 * </p>
 */

public interface ICalendarEvent extends IReadOnlyCalendarEvent {
  void setEventName(String name);
  void setStartDateTime(LocalDateTime start);
  void setEndDateTime(LocalDateTime end);
  void setEventDescription(String desc);
  void setEventLocation(String location);
  void setPublic(boolean isPublic);

  static ICalendarEventBuilder<?> builder() {
    return new CalendarEvent.Builder();
  }
}
