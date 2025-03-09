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
  public boolean addEvent(CalendarEventDTO eventDTO) {
    //If event is recurring then generate occurances 
    if (eventDTO.isRecurring()) {
      List<CalendarEvent> occurrences = new ArrayList<>();
      LocalDate startDate = eventDTO.getStartDateTime().toLocalDate();
      LocalTime startTime = eventDTO.getStartDateTime().toLocalTime();
      LocalTime endTime = eventDTO.getEndDateTime().toLocalTime();

    // Recurrence can be defined either by a fixed count...
      if (eventDTO.getRecurrenceCount() > 0) {
        int count = 0;
        LocalDate currentDate = startDate;
        while (count < eventDTO.getRecurrenceCount()) {
          if (eventDTO.getRecurrenceDays().contains(currentDate.getDayOfWeek())) {
            LocalDateTime occurrenceStart = LocalDateTime.of(currentDate, startTime);
            LocalDateTime occurrenceEnd = LocalDateTime.of(currentDate, endTime);
            if (eventDTO.isAutoDecline() && doesEventConflictForInterval(occurrenceStart, occurrenceEnd)) {
              System.out.println("Conflict detected on " + occurrenceStart + ", event not created");
              return false;
            }
            occurrences.add(new CalendarEvent(
                    eventDTO.getEventName(),
                    occurrenceStart,
                    occurrenceEnd,
                    null,           // description
                    null,           // location
                    false,          // isPublic (default)
                    true,           // isRecurring
                    eventDTO.getRecurrenceDays(),
                    eventDTO.isAutoDecline()  // autoDecline flag
            ));
            count++;
          }
          currentDate = currentDate.plusDays(1);
        }
      } 
      // ...or until a specific end date.
      else if (eventDTO.getRecurrenceEndDate() != null) {
        LocalDate endRecurrence = eventDTO.getRecurrenceEndDate().toLocalDate();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endRecurrence)) {
          if (eventDTO.getRecurrenceDays().contains(currentDate.getDayOfWeek())) {
            LocalDateTime occurrenceStart = LocalDateTime.of(currentDate, startTime);
            LocalDateTime occurrenceEnd = LocalDateTime.of(currentDate, endTime);
            if (eventDTO.isAutoDecline() && doesEventConflictForInterval(occurrenceStart, occurrenceEnd)) {
              System.out.println("Conflict detected on " + occurrenceStart + ", event not created");
              return false;
            }
            occurrences.add(new CalendarEvent(
                    eventDTO.getEventName(),
                    occurrenceStart,
                    occurrenceEnd,
                    null,
                    null,
                    false,
                    true,
                    eventDTO.getRecurrenceDays(),
                    eventDTO.isAutoDecline()
            ));
          }
          currentDate = currentDate.plusDays(1);
        }
      }
      events.addAll(occurrences);
      System.out.println("Recurring event created: " + eventDTO.getEventName());
      return true;
    } else {
      // Single event: check conflict if auto-decline is set.
      if (eventDTO.isAutoDecline() && doesEventConflict(eventDTO)) {
        System.out.println("Conflict detected, event not created");
        return false;
      }
      CalendarEvent event = new CalendarEvent(
              eventDTO.getEventName(),
              eventDTO.getStartDateTime(),
              eventDTO.getEndDateTime(),
              null,
              null,
              false,
              false,
              null,
              eventDTO.isAutoDecline()
      );
      events.add(event);
      System.out.println("Event created: " + eventDTO.getEventName());
      return true;
    }
  }

  @Override
  public boolean editEvent(String property, String eventName, LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue) {
    // Locate the specific event by name and exact time interval.
    for (CalendarEvent event : events) {
      if (event.getEventName().equals(eventName) &&
          event.getStartDateTime().equals(fromDateTime) &&
          event.getEndDateTime().equals(toDateTime)) {
        switch(property.toLowerCase()) {
          case "name":
            event.setEventName(newValue);
            return true;
          case "start":
            LocalDateTime newStart = LocalDateTime.parse(newValue);
            event.setStartDateTime(newStart);
            return true;
          case "end":
            LocalDateTime newEnd = LocalDateTime.parse(newValue);
            event.setEndDateTime(newEnd);
            return true;
          default:
            System.out.println("Unsupported property for edit.");
            return false;
        }
      }
    }
    System.out.println("Event not found for editing.");
    return false;
  }

  @Override
  public boolean editEvents(String property, String eventName, LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue) {
    //Todo write logic to find and edit events
    return false;
  }

  @Override
  public String printEvents(LocalDate fromDate, LocalDate toDate) {
    //Todo to wrrite logic to create print events
    return "";
  }

  @Override
  public String exportEvents(String filename) {
    //TODo write logic to create a file and export all events into it and return the file path
    return "";
  }

  @Override
  public String showStatus(LocalDateTime dateTime) {
    //Todo write logic to show status on that datetime free or not
    return "";
  }

  private Boolean doesEventConflict(CalendarEventDTO eventDTO){
    //TODO check existing events with the given DTO
    return false;
  }

  private Boolean checkStatus(LocalDateTime dateTime){
    //Todo check if there are events for that datetime
    return false;
  }
}
