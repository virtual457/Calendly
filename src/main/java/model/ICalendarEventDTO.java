package model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

public interface ICalendarEventDTO {
  String getEventName();
  LocalDateTime getStartDateTime();
  LocalDateTime getEndDateTime();
  Boolean isRecurring();
  List<DayOfWeek> getRecurrenceDays();
  Integer getRecurrenceCount();
  LocalDateTime getRecurrenceEndDate();
  Boolean isAutoDecline();
  String getEventDescription();
  String getEventLocation();
  Boolean isPrivate();

  static ICalendarEventDTOBuilder<?> builder() {
    return new CalendarEventDTO.CalendarEventDTOBuilder();
  }
}

