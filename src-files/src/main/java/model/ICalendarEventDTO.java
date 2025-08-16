package model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a Data Transfer Object (DTO) for a calendar event.
 * <p>
 * This interface defines the core properties and behaviors that
 * any event-related DTO must support, such as event names, start
 * and end times, and flags indicating whether the event is private,
 * recurring, or set to auto-decline conflicts.
 * </p>
 */

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

