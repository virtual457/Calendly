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
import java.time.format.DateTimeFormatter;

public class CalendarModel implements ICalendarModel {
  private List<CalendarEvent> events;

  public CalendarModel() {
    this.events = new ArrayList<>();
  }

  @Override
  public boolean addEvent(CalendarEventDTO eventDTO) {
    // Check that the event's end time is after its start time.
    if (!eventDTO.getEndDateTime().isAfter(eventDTO.getStartDateTime())) {
      throw new IllegalArgumentException("End date and time must be after start date and time.");
    }

    if (eventDTO.isRecurring()) {
      // For recurring events, start and end must be on the same day.
      if (!eventDTO.getStartDateTime().toLocalDate().equals(eventDTO.getEndDateTime().toLocalDate())) {
        throw new IllegalArgumentException("Recurring events must have start and end on the same day.");
      }

      // Both recurrence count and recurrence end date cannot be provided together.
      if (eventDTO.getRecurrenceCount() > 0 && eventDTO.getRecurrenceEndDate() != null) {
        throw new IllegalArgumentException("Cannot define both recurrence count and recurrence end date for a recurring event.");
      }

      // At least one must be provided.
      if (eventDTO.getRecurrenceCount() <= 0 && eventDTO.getRecurrenceEndDate() == null) {
        throw new IllegalArgumentException("Either recurrence count or recurrence end date must be defined for a recurring event.");
      }

      List<CalendarEvent> occurrences = new ArrayList<>();
      LocalDate startDate = eventDTO.getStartDateTime().toLocalDate();
      LocalTime startTime = eventDTO.getStartDateTime().toLocalTime();
      LocalTime endTime   = eventDTO.getEndDateTime().toLocalTime();

      int count = 0;
      LocalDate currentDate = startDate;
      LocalDate recurrenceEnd = (eventDTO.getRecurrenceEndDate() != null)
              ? eventDTO.getRecurrenceEndDate().toLocalDate()
              : null;

      // Single loop handling both termination conditions.
      while (true) {
        if (eventDTO.getRecurrenceCount() > 0) { // termination by fixed count
          if (count >= eventDTO.getRecurrenceCount()) {
            break;
          }
        } else if (recurrenceEnd != null) { // termination by end date
          if (currentDate.isAfter(recurrenceEnd)) {
            break;
          }
        }

        if (eventDTO.getRecurrenceDays().contains(currentDate.getDayOfWeek())) {
          LocalDateTime occurrenceStart = LocalDateTime.of(currentDate, startTime);
          LocalDateTime occurrenceEnd   = LocalDateTime.of(currentDate, endTime);
          // Create a temporary DTO for conflict checking.
          CalendarEventDTO occurrenceDTO = new CalendarEventDTO(eventDTO.getEventName()
                  ,eventDTO.getStartDateTime(), eventDTO.getEndDateTime(),false,
                  eventDTO.getRecurrenceDays(),eventDTO.getRecurrenceCount(),
                  eventDTO.getRecurrenceEndDate(), eventDTO.isAutoDecline(),
                  eventDTO.getEventDescription(),eventDTO.getEventLocation(),eventDTO.isPrivate());
          if (eventDTO.isAutoDecline() && doesEventConflict(occurrenceDTO)) {
            throw new IllegalStateException("Conflict detected on " + occurrenceStart +
                    ", event not created");
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
        currentDate = getNextRecurrenceDate(eventDTO.getStartDateTime().toLocalDate(), eventDTO.getRecurrenceDays());
      }

      events.addAll(occurrences);
      return true;
    } else {
      // Single event: check conflict if auto-decline is set.
      if (eventDTO.isAutoDecline() && doesEventConflict(eventDTO)) {
        throw new IllegalStateException("Conflict detected, event not created");
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
            throw new IllegalArgumentException("Unsupported property for edit: " + property);
        }
      }
    }
    throw new IllegalStateException("Event not found for editing: " + eventName);
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
            throw new IllegalArgumentException("Unsupported property for edit: " + property);
        }
      }
    }
    if (!found) {
      throw new IllegalStateException("No matching events found for editing: " + eventName);
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
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
      writer.println("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private");

      for (CalendarEvent event : events) {
        String subject = event.getEventName();
        String startDate = event.getStartDateTime().format(dateFormatter);
        String startTime = event.getStartDateTime().format(timeFormatter);
        String endDate = event.getEndDateTime().format(dateFormatter);
        String endTime = event.getEndDateTime().format(timeFormatter);

        // Determine if this is an all-day event.
        boolean isAllDay = event.getStartDateTime().equals(event.getStartDateTime().toLocalDate().atStartOfDay())
                && event.getEndDateTime().equals(event.getStartDateTime().toLocalDate().atTime(23, 59, 59));
        String allDay = isAllDay ? "True" : "False";

        String description = (event.getDescription() == null) ? "" : event.getDescription();
        String location = (event.getLocation() == null) ? "" : event.getLocation();

        // Assume events are public by default.
        String isPrivate = "False";

        writer.println(String.format("\"%s\",%s,%s,%s,%s,%s,\"%s\",\"%s\",%s",
                subject, startDate, startTime, endDate, endTime, allDay, description, location, isPrivate));
      }

      writer.flush();
      File file = new File(filename);
      return file.getAbsolutePath();
    } catch (IOException e) {
      throw new RuntimeException("Error exporting events: " + e.getMessage());
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

  private LocalDate getNextRecurrenceDate(LocalDate startDate, List<DayOfWeek> recurrenceDays) {
    if (recurrenceDays.isEmpty()) {
      return startDate.plusDays(1);
    }

    LocalDate nextDate = startDate.plusDays(1);
    while (!recurrenceDays.contains(nextDate.getDayOfWeek())) {
      nextDate = nextDate.plusDays(1);
    }
    return nextDate;
  }
}
