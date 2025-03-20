package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an individual calendar with a unique name, associated timezone,
 * and its own list of events.
 */
public class Calendar {
  private String calendarName;
  private String timezone;
  private List<CalendarEvent> events;

  /**
   * Constructs a new Calendar with the specified name and timezone.
   *
   * @param calendarName the unique name of the calendar
   * @param timezone     the IANA timezone string
   */
  public Calendar(String calendarName, String timezone) {
    this.calendarName = calendarName;
    this.timezone = timezone;
    this.events = new ArrayList<>();
  }

  // Getters and setters

  public String getCalendarName() {
    return calendarName;
  }

  public void setCalendarName(String calendarName) {
    this.calendarName = calendarName;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public List<CalendarEvent> getEvents() {
    return events;
  }

  public void setEvents(List<CalendarEvent> events) {
    this.events = events;
  }

  public void addEvents(List<CalendarEvent> events) {
    this.events.addAll(events);
  }

  public void addEvent(CalendarEvent event) {
    this.events.add(event);
  }
}


