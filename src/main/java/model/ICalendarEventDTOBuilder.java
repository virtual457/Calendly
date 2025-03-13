package model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

public interface ICalendarEventDTOBuilder<T extends ICalendarEventDTO> {
  ICalendarEventDTOBuilder<T> setEventName(String eventName);
  ICalendarEventDTOBuilder<T> setStartDateTime(LocalDateTime startDateTime);
  ICalendarEventDTOBuilder<T> setEndDateTime(LocalDateTime endDateTime);
  ICalendarEventDTOBuilder<T> setRecurring(Boolean isRecurring);
  ICalendarEventDTOBuilder<T> setRecurrenceCount(Integer recurrenceCount);
  ICalendarEventDTOBuilder<T> setRecurrenceDays(List<DayOfWeek> recurrenceDays);
  ICalendarEventDTOBuilder<T> setRecurrenceEndDate(LocalDateTime recurrenceEndDate);
  ICalendarEventDTOBuilder<T> setAutoDecline(Boolean autoDecline);
  ICalendarEventDTOBuilder<T> setEventDescription(String eventDescription);
  ICalendarEventDTOBuilder<T> setEventLocation(String eventLocation);
  ICalendarEventDTOBuilder<T> setPrivate(Boolean isPrivate);
  T build();
}
