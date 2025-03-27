package model;

import java.util.List;

/**
 * Represents a calendar that can contain events.
 * <p>
 * This interface defines the core operations for managing a calendar,
 * including retrieving events, checking calendar details, and interacting
 * with its metadata such as name and timezone.
 * </p>
 */

public interface ICalendar {
  String getCalendarName();

  void setCalendarName(String calendarName);

  String getTimezone();

  void setTimezone(String newTimezone);

  List<ICalendarEvent> getEventsCopy();

  List<ICalendarEvent> getEvents();

  void setEvents(List<ICalendarEvent> events);

  void addEvents(List<CalendarEvent> events);

  void addEvent(CalendarEvent event);

  static ICalendarBuilder<?> builder() {
    return new Calendar.Builder();
  }

}
