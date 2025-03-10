package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class CalendarModel implements ICalendarModel {
  private List<CalendarEvent> events;

  public CalendarModel() {
    this.events = new ArrayList<>();
  }


  @Override
  public boolean addEvent(CalendarEventDTO event) {
    return false;
  }

  @Override
  public boolean editEvent(String property, String eventName, LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue) {
    return false;
  }


  @Override
  public boolean editEvents(String property, String eventName, LocalDateTime fromDateTime, String newValue) {
    return false;
  }

  @Override
  public String printEventsOnSpecificDate(LocalDate date) {
    return "";
  }

  @Override
  public String printEventsInSpecificRange(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
    return "";
  }

  @Override
  public String exportEvents(String filename) {
    return "";
  }

  @Override
  public String showStatus(LocalDateTime dateTime) {
    return "";
  }
}
