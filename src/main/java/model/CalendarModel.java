package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class CalendarModel implements ICalendarModel {
  private List<CalendarEvent> events;

  public CalendarModel() {
    this.events = new ArrayList<>();
  }

  @Override
  public boolean addEvent(CalendarEventDTO event, boolean autoDecline) {
    for (CalendarEvent existingEvent: events) {
      if (event.getStartDateTime().isBefore(existingEvent.getEndDateTime()) &&
              event.getEndDateTime().isAfter(existingEvent.getStartDateTime())) {
        if (autoDecline) {
          System.out.println("Event conflicts with an existing event and was declined.");
          return false;
        }
      }
    }
    events.add(null);
    return true;
  }

  @Override
  public List<CalendarEvent> getEventsOnDate(LocalDate date) {
    List<CalendarEvent> result = new ArrayList<>();
    for (CalendarEvent event : events) {
      if (event.getStartDateTime().toLocalDate().equals(date)) {
        result.add(event);
      }
    }
    return result;
  }
}