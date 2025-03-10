package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class CalendarModel implements ICalendarModel {
  private List<CalendarEvent> events;

  public CalendarModel() {
    this.events = new ArrayList<>();
  }

  @Override
  public boolean addEvent(CalendarEventDTO eventDTO) {
    // Check that the event's end time is after its start time.
    if (!eventDTO.getEndDateTime().isAfter(eventDTO.getStartDateTime())) {
      System.out.println("Error: End date and time must be after start date and time.");
      return false;
    }

    // If the event is recurring, then additional conditions apply.
    if (eventDTO.isRecurring()) {
      // For recurring events, start and end must be on the same day.
      if (!eventDTO.getStartDateTime().toLocalDate().equals(eventDTO.getEndDateTime().toLocalDate())) {
        System.out.println("Error: Recurring events must have start and end on the same day.");
        return false;
      }

      List<CalendarEvent> occurrences = new ArrayList<>();
      LocalDate startDate = eventDTO.getStartDateTime().toLocalDate();
      LocalTime startTime = eventDTO.getStartDateTime().toLocalTime();
      LocalTime endTime   = eventDTO.getEndDateTime().toLocalTime();

      // Recurrence defined by a fixed count...
      if (eventDTO.getRecurrenceCount() > 0) {
        int count = 0;
        LocalDate currentDate = startDate;
        while (count < eventDTO.getRecurrenceCount()) {
          if (eventDTO.getRecurrenceDays().contains(currentDate.getDayOfWeek())) {
            LocalDateTime occurrenceStart = LocalDateTime.of(currentDate, startTime);
            LocalDateTime occurrenceEnd   = LocalDateTime.of(currentDate, endTime);
            // Create a temporary DTO for conflict checking.
            CalendarEventDTO occurrenceDTO = new CalendarEventDTO.CalendarEventDTOBuilder()
                    .eventName(eventDTO.getEventName())
                    .startDateTime(occurrenceStart)
                    .endDateTime(occurrenceEnd)
                    .autoDecline(eventDTO.isAutoDecline())
                    .build();
            if (eventDTO.isAutoDecline() && doesEventConflict(occurrenceDTO)) {
              System.out.println("Conflict detected on " + occurrenceStart + ", event not created");
              return false;
            }
            occurrences.add(new CalendarEvent(
                    eventDTO.getEventName(),
                    occurrenceStart,
                    occurrenceEnd,
                    null,     // description
                    null,     // location
                    false,    // isPublic (default)
                    true,     // isRecurring
                    eventDTO.getRecurrenceDays(),
                    eventDTO.isAutoDecline()
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
            LocalDateTime occurrenceEnd   = LocalDateTime.of(currentDate, endTime);
            CalendarEventDTO occurrenceDTO = new CalendarEventDTO.CalendarEventDTOBuilder()
                    .eventName(eventDTO.getEventName())
                    .startDateTime(occurrenceStart)
                    .endDateTime(occurrenceEnd)
                    .autoDecline(eventDTO.isAutoDecline())
                    .build();
            if (eventDTO.isAutoDecline() && doesEventConflict(occurrenceDTO)) {
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
  public boolean editEvents(String property, String eventName, LocalDateTime fromDateTime, String newValue) {
    boolean found = false;
    // Edit all events with the given event name.
    // If fromDateTime is provided (non-null), only update events whose start equals fromDateTime.
    for (CalendarEvent event : events) {
      if (event.getEventName().equals(eventName) &&
              (fromDateTime == null || event.getStartDateTime().equals(fromDateTime))) {
        switch(property.toLowerCase()) {
          case "name":
            event.setEventName(newValue);
            found = true;
            break;
          case "start":
            LocalDateTime newStart = LocalDateTime.parse(newValue);
            event.setStartDateTime(newStart);
            found = true;
            break;
          case "end":
            LocalDateTime newEnd = LocalDateTime.parse(newValue);
            event.setEndDateTime(newEnd);
            found = true;
            break;
          default:
            System.out.println("Unsupported property for edit.");
            return false;
        }
      }
    }
    if (!found) {
      System.out.println("No matching events found for editing.");
    }
    return found;
  }

  @Override
  public String printEventsOnSpecificDate(LocalDate date) {
    StringBuilder sb = new StringBuilder();
    for (CalendarEvent event : events) {
      if (event.getStartDateTime().toLocalDate().equals(date)) {
        sb.append("- ").append(event.getEventName()).append(": ")
                .append(event.getStartDateTime()).append(" to ")
                .append(event.getEndDateTime());
        if (event.getLocation() != null) {
          sb.append(" at ").append(event.getLocation());
        }
        sb.append("\n");
      }
    }
    return sb.toString();
  }

  @Override
  public String printEventsInSpecificRange(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
    StringBuilder sb = new StringBuilder();
    for (CalendarEvent event : events) {
      if ((event.getStartDateTime().equals(fromDateTime) || event.getStartDateTime().isAfter(fromDateTime)) &&
              (event.getStartDateTime().equals(toDateTime) || event.getStartDateTime().isBefore(toDateTime))) {
        sb.append("- ").append(event.getEventName()).append(": ")
                .append(event.getStartDateTime()).append(" to ")
                .append(event.getEndDateTime());
        if (event.getLocation() != null) {
          sb.append(" at ").append(event.getLocation());
        }
        sb.append("\n");
      }
    }
    return sb.toString();
  }

  @Override
  public String exportEvents(String filename) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
      writer.println("Event Name,Start DateTime,End DateTime,Recurring,Recurrence Days,AutoDecline");
      for (CalendarEvent event : events) {
        String recurrence = event.isRecurring() ? "Yes" : "No";
        String days = "";
        if (event.isRecurring() && event.getRecurrenceDays() != null) {
          days = event.getRecurrenceDays().toString();
        }
        writer.println(String.format("%s,%s,%s,%s,%s,%s",
                event.getEventName(),
                event.getStartDateTime(),
                event.getEndDateTime(),
                recurrence,
                days,
                event.isAutoDecline() ? "Yes" : "No"));
      }
      writer.flush();
      File file = new File(filename);
      return file.getAbsolutePath();
    } catch (IOException e) {
      System.out.println("Error exporting events: " + e.getMessage());
      return "";
    }
  }

  @Override
  public String showStatus(LocalDateTime dateTime) {
    if (checkStatus(dateTime)) {
      return "Busy";
    }
    return "Available";
  }

  // Helper method for conflict checking using a CalendarEventDTO.
  private Boolean doesEventConflict(CalendarEventDTO eventDTO) {
    LocalDateTime newStart = eventDTO.getStartDateTime();
    LocalDateTime newEnd = eventDTO.getEndDateTime();
    for (CalendarEvent event : events) {
      if (newStart.isBefore(event.getEndDateTime()) && newEnd.isAfter(event.getStartDateTime())) {
        return true;
      }
    }
    return false;
  }

  // Helper method to check if there is any event at the given time.
  private Boolean checkStatus(LocalDateTime dateTime) {
    for (CalendarEvent event : events) {
      if (!dateTime.isBefore(event.getStartDateTime()) && !dateTime.isAfter(event.getEndDateTime())) {
        return true;
      }
    }
    return false;
  }
}
