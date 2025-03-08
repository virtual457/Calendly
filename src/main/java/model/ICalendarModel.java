package model;

import java.time.LocalDate;
import java.util.List;

public interface ICalendarModel {
  static ICalendarModel createInstance(String type) {
    if (type.equalsIgnoreCase("listBased")) {
      return new CalendarModel();
    } else {
      throw new IllegalArgumentException("Invalid CalendarModel type.");
    }
  }

  boolean addEvent(CalendarEventDTO event, boolean autoDecline);

  List<CalendarEvent> getEventsOnDate(LocalDate date);
}
