package model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A builder interface for creating objects that implement {@link ICalendarEventDTO}.
 * <p>
 * Implementations of this interface define the builder pattern for configuring
 * calendar event DTO objects, such as setting the event name, times, and other
 * properties, and then constructing the final {@code ICalendarEventDTO} instance.
 * Each setter method typically returns this builder instance to enable method chaining.
 * </p>
 *
 * @param <T> the specific type of {@code ICalendarEventDTO} this builder produces
 */

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
