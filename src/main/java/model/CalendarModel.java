package model;

import java.time.ZoneId;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class CalendarModel implements ICalendarModel {
  private List<Calendar> calendars;
  private Calendar currentCalendar;

  public CalendarModel() {
    this.calendars = new ArrayList<>();
    this.currentCalendar = null;
  }

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param calName  the unique name for the new calendar
   * @param timezone the IANA timezone string (e.g., "America/New_York")
   * @return true if the calendar is successfully created
   * @throws IllegalArgumentException if the timezone is invalid or the name is already in use
   */
  @Override
  public boolean createCalendar(String calName, String timezone) {
    // Validate the timezone.
    ZoneId zone;
    try {
      zone = ZoneId.of(timezone);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezone, e);
    }

    // Check if a calendar with the given name already exists.
    for (Calendar cal : calendars) {
      if (cal.getCalendarName().equalsIgnoreCase(calName)) {
        throw new IllegalArgumentException("Calendar with name '" + calName + "' already exists.");
      }
    }

    // Create a new Calendar and add it to the list.
    Calendar newCalendar = new Calendar(calName, timezone);
    calendars.add(newCalendar);

    return true;
  }

  @Override
  public boolean addEvent(ICalendarEventDTO eventDTO) {
    // Validate basic event fields.
    validateBasicEvent(eventDTO);

    // Ensure that a calendar is currently selected.
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar selected. Use 'use calendar --name <calendarName>' to set the current calendar.");
    }

    if (eventDTO.isRecurring()) {
      // Validate recurring-specific properties.
      validateRecurringEvent(eventDTO);
      // Generate all occurrences for the recurring event.
      List<CalendarEvent> occurrences = generateRecurringOccurrences(eventDTO);

      // Check each occurrence for conflicts in the current calendar.
      for (CalendarEvent occ : occurrences) {
        if (doesEventConflict(currentCalendar.getEvents(), convertToDTO(occ))) {
          throw new IllegalStateException("Conflict detected on " + occ.getStartDateTime() + ", event not created");
        }
      }

      // Add all occurrences to the current calendar.
      currentCalendar.addEvents(occurrences);
      return true;
    } else {
      // For non-recurring events, ensure no recurrence info is provided.
      if (eventDTO.getRecurrenceCount() > 0 || eventDTO.getRecurrenceEndDate() != null) {
        throw new IllegalArgumentException("Non-recurring event should not have recurrence count or recurrence end date.");
      }
      // Check conflict for a single event.
      if (doesEventConflict(currentCalendar.getEvents(), eventDTO)) {
        throw new IllegalStateException("Conflict detected, event not created");
      }
      // Create a single event and add it to the current calendar.
      CalendarEvent event = createSingleEvent(eventDTO);
      currentCalendar.addEvent(event);
      return true;
    }
  }

// --- Helper methods ---

  private void validateBasicEvent(ICalendarEventDTO eventDTO) {
    if (eventDTO.getEventName() == null || eventDTO.getEventName().trim().isEmpty()) {
      throw new IllegalArgumentException("Event name is required.");
    }
    if (eventDTO.getStartDateTime() == null) {
      throw new IllegalArgumentException("Start date and time are required.");
    }
    if (eventDTO.getEndDateTime() == null) {
      throw new IllegalArgumentException("End date and time are required.");
    }
    if (!eventDTO.getEndDateTime().isAfter(eventDTO.getStartDateTime())) {
      throw new IllegalArgumentException("End date and time must be after start date and time.");
    }
  }

  private void validateRecurringEvent(ICalendarEventDTO eventDTO) {
    if (!eventDTO.getStartDateTime().toLocalDate().equals(eventDTO.getEndDateTime().toLocalDate())) {
      throw new IllegalArgumentException("Recurring events must have start and end on the same day.");
    }
    if (eventDTO.getRecurrenceCount() > 0 && eventDTO.getRecurrenceEndDate() != null) {
      throw new IllegalArgumentException("Cannot define both recurrence count and recurrence end date for a recurring event.");
    }
    if (eventDTO.getRecurrenceCount() <= 0 && eventDTO.getRecurrenceEndDate() == null) {
      throw new IllegalArgumentException("Either recurrence count or recurrence end date must be defined for a recurring event.");
    }
    if (eventDTO.getRecurrenceDays() == null) {
      throw new IllegalArgumentException("Recurrence days must be provided for recurring events.");
    }
  }

  private List<CalendarEvent> generateRecurringOccurrences(ICalendarEventDTO eventDTO) {
    List<CalendarEvent> occurrences = new ArrayList<>();
    LocalDate startDate = eventDTO.getStartDateTime().toLocalDate();
    LocalTime startTime = eventDTO.getStartDateTime().toLocalTime();
    LocalTime endTime = eventDTO.getEndDateTime().toLocalTime();
    int count = 0;
    LocalDate currentDate = startDate;
    LocalDate recurrenceEnd = (eventDTO.getRecurrenceEndDate() != null)
            ? eventDTO.getRecurrenceEndDate().toLocalDate()
            : null;

    while (true) {
      if (eventDTO.getRecurrenceCount() > 0) {
        if (count >= eventDTO.getRecurrenceCount()) break;
      } else if (recurrenceEnd != null) {
        if (currentDate.isAfter(recurrenceEnd)) break;
      }

      if (eventDTO.getRecurrenceDays().contains(currentDate.getDayOfWeek())) {
        LocalDateTime occurrenceStart = LocalDateTime.of(currentDate, startTime);
        LocalDateTime occurrenceEnd = LocalDateTime.of(currentDate, endTime);
        // Build a temporary DTO for conflict checking.
        CalendarEventDTO occurrenceDTO = CalendarEventDTO.builder()
                .setEventName(eventDTO.getEventName())
                .setStartDateTime(occurrenceStart)
                .setEndDateTime(occurrenceEnd)
                .setAutoDecline(true)  // Conflicts are always declined.
                .build();
        Boolean isPrivate = eventDTO.isPrivate();
        Boolean isPublic = (isPrivate == null) ? true : !isPrivate;
        occurrences.add(new CalendarEvent(
                eventDTO.getEventName(),
                occurrenceStart,
                occurrenceEnd,
                eventDTO.getEventDescription(),
                eventDTO.getEventLocation(),
                Boolean.TRUE.equals(isPublic),
                true,
                eventDTO.getRecurrenceDays(),
                true
        ));
        count++;
      }
      currentDate = getNextRecurrenceDate(currentDate, eventDTO.getRecurrenceDays());
    }
    return occurrences;
  }

  private CalendarEvent createSingleEvent(ICalendarEventDTO eventDTO) {
    Boolean isPrivate = eventDTO.isPrivate();
    Boolean isPublic = (isPrivate == null) ? true : !isPrivate;
    return new CalendarEvent(
            eventDTO.getEventName(),
            eventDTO.getStartDateTime(),
            eventDTO.getEndDateTime(),
            eventDTO.getEventDescription(),
            eventDTO.getEventLocation(),
            Boolean.TRUE.equals(isPublic),
            false,
            null,
            true
    );
  }

  private ICalendarEventDTO convertToDTO(CalendarEvent event) {
    return CalendarEventDTO.builder()
            .setEventName(event.getEventName())
            .setStartDateTime(event.getStartDateTime())
            .setEndDateTime(event.getEndDateTime())
            .setAutoDecline(true)
            .build();
  }

  private boolean doesEventConflict(List<CalendarEvent> eventList, ICalendarEventDTO newEventDTO) {
    LocalDateTime newStart = newEventDTO.getStartDateTime();
    LocalDateTime newEnd = newEventDTO.getEndDateTime();
    for (CalendarEvent event : eventList) {
      if (newStart.isBefore(event.getEndDateTime()) && newEnd.isAfter(event.getStartDateTime())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean editEvents(String property, String eventName,
                           LocalDateTime fromDateTime, LocalDateTime toDateTime,
                           String newValue, boolean editAll) {
    // Ensure that a calendar is currently selected.
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar selected. Use 'use calendar --name <calendarName>' to set the current calendar.");
    }

    boolean found = false;
    // Iterate through the events in the current calendar.
    for (CalendarEvent event : currentCalendar.getEvents()) {
      // Identify events that match the given criteria.
      if (event.getEventName().equalsIgnoreCase(eventName)
              && event.getStartDateTime().equals(fromDateTime)
              && event.getEndDateTime().equals(toDateTime)) {
        // Update the property based on the provided flag.
        switch (property.toLowerCase()) {
          case "name":
            event.setEventName(newValue);
            found = true;
            break;
          case "start":
            LocalDateTime newStart = LocalDateTime.parse(newValue);
            if (!event.getEndDateTime().isAfter(newStart)) {
              throw new IllegalArgumentException("New start must be before current end time.");
            }
            event.setStartDateTime(newStart);
            found = true;
            break;
          case "end":
            LocalDateTime newEnd = LocalDateTime.parse(newValue);
            if (!newEnd.isAfter(event.getStartDateTime())) {
              throw new IllegalArgumentException("New end must be after current start time.");
            }
            event.setEndDateTime(newEnd);
            found = true;
            break;
          case "description":
            event.setEventDescription(newValue);
            found = true;
            break;
          case "location":
            event.setEventLocation(newValue);
            found = true;
            break;
          case "ispublic":
            event.setPublic(Boolean.parseBoolean(newValue));
            found = true;
            break;
          default:
            throw new IllegalArgumentException("Unsupported property for edit: " + property);
        }
        // If the flag indicates only the first occurrence should be updated, exit here.
        if (!editAll) {
          return true;
        }
      }
    }
    if (!found) {
      throw new IllegalStateException("No matching event found for editing: " + eventName);
    }
    return found;
  }

  public boolean isCalendarAvailable(String calName, LocalDate date) {
    // Iterate through the stored calendars.
    for (Calendar cal : calendars) {
      if (cal.getCalendarName().equalsIgnoreCase(calName)) {
        // If no specific date is provided, return true since the calendar exists.
        if (date == null) {
          return true;
        }
        // If a date is provided, check if at least one event occurs on that date.
        for (CalendarEvent event : cal.getEvents()) {
          if (event.getStartDateTime().toLocalDate().equals(date)) {
            return true;
          }
        }
        // Calendar exists but no events on the given date.
        return false;
      }
    }
    // No calendar with the given name exists.
    return false;
  }

  public boolean deleteCalendar(String calName) {
    for (Iterator<Calendar> iterator = calendars.iterator(); iterator.hasNext(); ) {
      Calendar cal = iterator.next();
      if (cal.getCalendarName().equalsIgnoreCase(calName)) {
        iterator.remove();
        // If the deleted calendar is the currently selected one, clear the selection.
        if (currentCalendar != null && currentCalendar.getCalendarName().equalsIgnoreCase(calName)) {
          currentCalendar = null;
        }
        return true;
      }
    }
    return false;
  }

  public List<CalendarEvent> getEventsInRange(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar selected. Please select a calendar first.");
    }
    if (fromDateTime == null || toDateTime == null) {
      throw new IllegalArgumentException("Both start and end date-times must be provided.");
    }
    if (toDateTime.isBefore(fromDateTime)) {
      throw new IllegalArgumentException("The end date-time must not be before the start date-time.");
    }

    List<CalendarEvent> rangeEvents = new ArrayList<>();
    for (CalendarEvent event : currentCalendar.getEvents()) {
      // Check if the event's start time falls between fromDateTime and toDateTime (inclusive).
      if (!event.getStartDateTime().isBefore(fromDateTime) &&
              !event.getStartDateTime().isAfter(toDateTime)) {
        rangeEvents.add(event);
      }
    }
    return rangeEvents;
  }

  public boolean copyEvents(String sourceCalendarName,
                            LocalDateTime sourceStart, LocalDateTime sourceEnd,
                            String targetCalendarName,
                            LocalDateTime targetStart) {
    // Find the source and target calendars in the stored list.
    Calendar sourceCal = null;
    Calendar targetCal = null;
    for (Calendar cal : calendars) {
      if (cal.getCalendarName().equalsIgnoreCase(sourceCalendarName)) {
        sourceCal = cal;
      }
      if (cal.getCalendarName().equalsIgnoreCase(targetCalendarName)) {
        targetCal = cal;
      }
    }
    if (sourceCal == null) {
      throw new IllegalArgumentException("Source calendar not found: " + sourceCalendarName);
    }
    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar not found: " + targetCalendarName);
    }

    // Collect all events in the source calendar that occur within the specified interval.
    List<CalendarEvent> eventsToCopy = new ArrayList<>();
    for (CalendarEvent event : sourceCal.getEvents()) {
      // Check if event's start time falls within the source interval (inclusive).
      if (!event.getStartDateTime().isBefore(sourceStart) &&
              !event.getStartDateTime().isAfter(sourceEnd)) {
        eventsToCopy.add(event);
      }
    }

    // For each event, calculate the time offset and create a new event for the target calendar.
    for (CalendarEvent event : eventsToCopy) {
      // Calculate the offset from the source start.
      java.time.Duration offset = java.time.Duration.between(sourceStart, event.getStartDateTime());
      LocalDateTime newStart = targetStart.plus(offset);
      // Keep the event duration the same.
      java.time.Duration eventDuration = java.time.Duration.between(event.getStartDateTime(), event.getEndDateTime());
      LocalDateTime newEnd = newStart.plus(eventDuration);

      // Create a new event with the same properties but adjusted times.
      CalendarEvent newEvent = new CalendarEvent(
              event.getEventName(),
              newStart,
              newEnd,
              event.getEventDescription(),
              event.getEventLocation(),
              event.isPublic(),
              event.isRecurring(),
              event.getRecurrenceDays(),
              event.isAutoDecline()
      );

      // Conflict checking in the target calendar.
      if (doesEventConflict(targetCal.getEvents(), convertToDTO(newEvent))) {
        throw new IllegalStateException("Conflict detected when copying event: " + event.getEventName());
      }

      // Add the new event to the target calendar.
      targetCal.addEvent(newEvent);
    }

    return true;
  }


  private LocalDate getNextRecurrenceDate(LocalDate currentDate, List<DayOfWeek> recurrenceDays) {
    if (recurrenceDays.isEmpty()) {
      return currentDate.plusDays(1);
    }
    LocalDate nextDate = currentDate.plusDays(1);
    while (!recurrenceDays.contains(nextDate.getDayOfWeek())) {
      nextDate = nextDate.plusDays(1);
    }
    return nextDate;
  }
}
