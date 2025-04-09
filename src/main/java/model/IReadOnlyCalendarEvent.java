package model;

import java.time.LocalDateTime;

public interface IReadOnlyCalendarEvent {
  String getEventName();
  LocalDateTime getStartDateTime();
  LocalDateTime getEndDateTime();
  String getEventDescription();
  String getEventLocation();
  boolean isPublic();
  boolean doesEventConflict(ICalendarEvent other);
}