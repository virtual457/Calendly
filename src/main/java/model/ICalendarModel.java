package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ICalendarModel {
  static ICalendarModel createInstance(String type) {
    if (type.equalsIgnoreCase("listBased")) {
      return new CalendarModel();
    } else {
      throw new IllegalArgumentException("Invalid CalendarModel type.");
    }
  }

  boolean addEvent(CalendarEventDTO event);
  boolean editEvent(String property, String eventName, LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue);
  boolean editEvents(String property, String eventName, LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue);
  String printEvents(LocalDate fromDate, LocalDate toDate);
  String exportEvents(String filename);
  String showStatus(LocalDateTime dateTime);

}
