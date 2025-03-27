package model;

import java.time.LocalDateTime;

/**
 * A builder interface for constructing {@link ICalendarEvent} implementations.
 * Enables fluent construction of calendar events with core scheduling metadata.
 *
 * @param <T> the concrete type of event to build
 */
public interface ICalendarEventBuilder<T extends ICalendarEvent> {
  ICalendarEventBuilder<T> setEventName(String eventName);
  ICalendarEventBuilder<T> setStartDateTime(LocalDateTime startDateTime);
  ICalendarEventBuilder<T> setEndDateTime(LocalDateTime endDateTime);
  ICalendarEventBuilder<T> setEventDescription(String eventDescription);
  ICalendarEventBuilder<T> setEventLocation(String eventLocation);
  ICalendarEventBuilder<T> setPublic(boolean isPublic);

  T build();
}
