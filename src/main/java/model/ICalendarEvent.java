package model;

import java.time.LocalDateTime;

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
