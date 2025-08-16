package model;

import java.time.LocalDateTime;

/**
 * Represents an event in a calendar system.
 * <p>
 * This interface defines the contract for retrieving information about a calendar event,
 * such as its name, start and end time, recurrence, privacy, and location details.
 * </p>
 */

public interface ICalendarEvent {
  String getEventName();

  void setEventName(String name);

  LocalDateTime getStartDateTime();

  void setStartDateTime(LocalDateTime start);

  LocalDateTime getEndDateTime();

  void setEndDateTime(LocalDateTime end);

  String getEventDescription();

  void setEventDescription(String desc);

  String getEventLocation();

  void setEventLocation(String location);

  boolean isPublic();

  void setPublic(boolean isPublic);

  boolean doesEventConflict(ICalendarEvent other);

  static ICalendarEventBuilder<?> builder() {
    return new CalendarEvent.Builder();
  }
}
