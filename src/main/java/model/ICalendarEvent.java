package model;

import java.time.LocalDateTime;

/**
 * Represents a calendar event within the domain model.
 * Implementations must provide details such as timing, name, visibility,
 * and logic to detect scheduling conflicts with other events.
 */
public interface ICalendarEvent {
  String getEventName();
  LocalDateTime getStartDateTime();
  LocalDateTime getEndDateTime();
  String getEventDescription();
  String getEventLocation();
  boolean isPublic();

  boolean doesEventConflict(ICalendarEvent other);

  static ICalendarEventBuilder<?> builder() {
    return new CalendarEvent.Builder();
  }
}
