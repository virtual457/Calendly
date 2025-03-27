package model;

import java.util.List;

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
