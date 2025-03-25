package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CalendarModel stores multiple calendars in a list.
 * Each operation requires a calendar name to identify the target calendar.
 */
public class CalendarModel implements ICalendarModel {
  private List<Calendar> calendars;

  public CalendarModel() {
    this.calendars = new ArrayList<>();
  }

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

    // Create a new Calendar using a builder
    Calendar newCalendar = Calendar.builder()
            .setCalendarName(calName)
            .setTimezone(timezone)
            .build();
    calendars.add(newCalendar);
    return true;
  }

  /**
   * Adds an event to the calendar identified by calendarName.
   *
   * @param calendarName the name of the target calendar
   * @param eventDTO     the event data transfer object containing event details
   * @return true if the event is successfully added
   * @throws IllegalArgumentException if the calendar is not found or event validation fails
   * @throws IllegalStateException    if a conflict is detected in the target calendar
   */

  @Override
  public boolean addEvent(String calendarName, ICalendarEventDTO eventDTO) {
    // Look up the target calendar by name.
    Calendar targetCalendar = getCalendarByName(calendarName);
    if (targetCalendar == null) {
      throw new IllegalArgumentException("Calendar not found: " + calendarName);
    }

    // Validate basic event fields.
    validateBasicEvent(eventDTO);

    if (eventDTO.isRecurring()) {
      validateRecurringEvent(eventDTO);
      List<CalendarEvent> occurrences = generateRecurringOccurrences(eventDTO);
      // Check each occurrence for conflicts in the target calendar.
      for (CalendarEvent occ : occurrences) {
        if (doesEventConflict(targetCalendar.getEvents(), convertToDTO(occ))) {
          throw new IllegalStateException("Conflict detected on " + occ.getStartDateTime() + ", event not created");
        }
      }
      targetCalendar.addEvents(occurrences);
      return true;
    } else {
      // Validate that non-recurring events do not include recurrence info.
      validateNonRecurringEvent(eventDTO);
      if (doesEventConflict(targetCalendar.getEvents(), eventDTO)) {
        throw new IllegalStateException("Conflict detected, event not created");
      }
      CalendarEvent event = createSingleEvent(eventDTO);
      targetCalendar.addEvent(event);
      return true;
    }
  }

  @Override
  public boolean editEvents(String calendarName, String property, String eventName,
                            LocalDateTime fromDateTime,
                            String newValue, boolean editAll) {
    // Find the target calendar by name.
    Calendar targetCalendar = getCalendarByName(calendarName);
    if (targetCalendar == null) {
      throw new IllegalArgumentException("Calendar not found: " + calendarName);
    }

    // For properties other than "name", require a non-empty value.
    if (newValue == null || (newValue.trim().isEmpty() && !property.equalsIgnoreCase("name"))) {
      throw new IllegalArgumentException("Missing value for property update.");
    }

    boolean found = false;
    // Iterate through the events in the target calendar.
    for (CalendarEvent event : targetCalendar.getEvents()) {
      // Check if the event matches the criteria.
      if (event.getEventName().equals(eventName)
              && (event.getStartDateTime().isAfter(fromDateTime)
              || event.getStartDateTime().equals(fromDateTime))) {

        // Save original values for rollback.
        LocalDateTime originalStart = event.getStartDateTime();
        LocalDateTime originalEnd = event.getEndDateTime();

        // Update the specified property.
        switch (property.toLowerCase()) {
          case "name":
            event.setEventName(newValue);
            found = true;
            break;
          case "start":
            // Parse new value and update only the time portion, preserving the original date.
            LocalDateTime parsedNewStart = LocalDateTime.parse(newValue);
            LocalDateTime newStart = LocalDateTime.of(event.getStartDateTime().toLocalDate(),
                    parsedNewStart.toLocalTime());
            if (!event.getEndDateTime().isAfter(newStart)) {
              throw new IllegalArgumentException("New start must be before current end time.");
            }
            event.setStartDateTime(newStart);
            found = true;
            break;
          case "end":
            // Parse new value and update only the time portion, preserving the original date.
            LocalDateTime parsedNewEnd = LocalDateTime.parse(newValue);
            LocalDateTime newEnd = LocalDateTime.of(event.getEndDateTime().toLocalDate(),
                    parsedNewEnd.toLocalTime());
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
          case "isprivate":
            // If newValue is not exactly "true" or "false" (ignoring case), throw an exception.
            if (!newValue.equalsIgnoreCase("true") && !newValue.equalsIgnoreCase("false")) {
              throw new IllegalArgumentException("For input string: \"" + newValue + "\"");
            }
            event.setPublic(!Boolean.parseBoolean(newValue));
            found = true;
            break;
          default:
            throw new IllegalArgumentException("Unsupported property for edit: " + property);
        }

        // After the change, check for conflicts.
        if (checkConflictForEvent(event, targetCalendar.getEvents())) {
          // Rollback to original values.
          event.setStartDateTime(originalStart);
          event.setEndDateTime(originalEnd);
          throw new IllegalStateException("Conflict detected after editing " + property);
        }

        // If not editing all matching events, return immediately.
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

  @Override
  public boolean editEvent(String calendarName, String property, String eventName, LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue) {
    Calendar targetCalendar = getCalendarByName(calendarName);
    boolean editAll = true;
    if (targetCalendar == null) {
      throw new IllegalArgumentException("Calendar not found: " + calendarName);
    }

    boolean found = false;
    // Iterate through the events in the target calendar.
    for (CalendarEvent event : targetCalendar.getEvents()) {
      // Check if the event matches the criteria.
      if (event.getEventName().equals(eventName)
              && (event.getStartDateTime().isEqual(fromDateTime) && event.getEndDateTime().equals(toDateTime))) {

        // Save original values for rollback.
        LocalDateTime originalStart = event.getStartDateTime();
        LocalDateTime originalEnd = event.getEndDateTime();

        // Update the specified property.
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

        // After the change, check for conflicts.
        if (checkConflictForEvent(event, targetCalendar.getEvents())) {
          // Rollback to original values.
          event.setStartDateTime(originalStart);
          event.setEndDateTime(originalEnd);
          throw new IllegalStateException("Conflict detected after editing " + property);
        }

        // If not editing all matching events, return immediately.
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
        // If a date is provided, check if there are any events on that date.
        // If at least one event exists, the calendar is busy (not available).
        for (CalendarEvent event : cal.getEvents()) {
          if (event.getStartDateTime().toLocalDate().equals(date)) {
            return false; // busy, not available
          }
        }
        // No events on the given date, so the calendar is available (free).
        return true;
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
        return true;
      }
    }
    return false;
  }

  @Override
  public List<ICalendarEventDTO> getEventsInSpecificDateTime(String calendarName, LocalDateTime dateTime) {
    // Look up the target calendar by its name.
    Calendar targetCalendar = getCalendarByName(calendarName);
    if (targetCalendar == null) {
      throw new IllegalArgumentException("Calendar not found: " + calendarName);
    }

    if (dateTime == null) {
      throw new IllegalArgumentException("date time cannot be null");
    }

    List<CalendarEvent> rangeEvents = new ArrayList<>();
    for (CalendarEvent event : targetCalendar.getEvents()) {
      if ((event.getStartDateTime().isBefore(dateTime) || event.getStartDateTime().equals(dateTime)) &&
              (event.getEndDateTime().isAfter(dateTime) || event.getEndDateTime().equals(dateTime))) {
        rangeEvents.add(event);
      }
    }
    return rangeEvents.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  @Override
  public List<ICalendarEventDTO> getEventsInRange(String calendarName, LocalDateTime fromDateTime, LocalDateTime toDateTime) {
    // Look up the target calendar by its name.
    Calendar targetCalendar = getCalendarByName(calendarName);
    if (targetCalendar == null) {
      throw new IllegalArgumentException("Calendar not found: " + calendarName);
    }

    if (fromDateTime == null || toDateTime == null) {
      throw new IllegalArgumentException("Both start and end date-times must be provided.");
    }
    if (toDateTime.isBefore(fromDateTime)) {
      throw new IllegalArgumentException("The end date-time must not be before the start date-time.");
    }

    List<CalendarEvent> rangeEvents = new ArrayList<>();
    // Iterate over the events in the target calendar.
    for (CalendarEvent event : targetCalendar.getEvents()) {
      // Check if the event's start time falls within the specified range (inclusive).
      if ((!event.getStartDateTime().isBefore(fromDateTime) &&
              !event.getStartDateTime().isAfter(toDateTime)) || (event.getEndDateTime().isAfter(fromDateTime) && event.getEndDateTime().isBefore(toDateTime))) {
        rangeEvents.add(event);
      }
    }
    return rangeEvents.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  @Override
  public boolean copyEvents(String sourceCalendarName, LocalDateTime sourceStart, LocalDateTime sourceEnd,
                            String targetCalendarName, LocalDateTime targetStart) {

    //TODO: move these validations to another controller
    // Find the source calendar.
    Calendar sourceCal = getCalendarByName(sourceCalendarName);
    if (sourceCal == null) {
      throw new IllegalArgumentException("Source calendar not found: " + sourceCalendarName);
    }
    // Find the target calendar.
    Calendar targetCal = getCalendarByName(targetCalendarName);
    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar not found: " + targetCalendarName);
    }

    // Validate the date-time parameters.
    if (sourceStart == null || sourceEnd == null) {
      throw new IllegalArgumentException("Source start and source end times must be provided.");
    }
    if (sourceEnd.isBefore(sourceStart)) {
      throw new IllegalArgumentException("Source end time must not be before source start time.");
    }

    // Collect all events from the source calendar that start within the source interval.
    List<CalendarEvent> eventsToCopy = new ArrayList<>();
    for (CalendarEvent event : sourceCal.getEvents()) {
      if (!event.getStartDateTime().isBefore(sourceStart) &&
              !event.getStartDateTime().isAfter(sourceEnd)) {
        eventsToCopy.add(event);
      }
    }
    //TODO: Throw exception
    // If no events found in the interval, return false.
    if (eventsToCopy.isEmpty()) {
      return false;
    }

    // Sort events by start time.
    eventsToCopy.sort((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()));

    // Get the ZoneIds for the source and target calendars.
    java.time.ZoneId sourceZone = java.time.ZoneId.of(sourceCal.getTimezone());
    java.time.ZoneId targetZone = java.time.ZoneId.of(targetCal.getTimezone());

    // For each event to be copied, convert its start and end times from the source to the target timezone.
    for (CalendarEvent event : eventsToCopy) {


      LocalDateTime newStart = convertTimeToTargetDate(event.getStartDateTime(),sourceCal.getTimezone(), targetStart.toLocalDate(), targetCal.getTimezone());
      java.time.Duration duration = java.time.Duration.between(event.getStartDateTime(), event.getEndDateTime());

      LocalDateTime newEnd = newStart.plus(duration);
      // Create a new event with the converted times.
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

      // Check for conflicts in the target calendar.
      if (doesEventConflict(targetCal.getEvents(), convertToDTO(newEvent))) {
        throw new IllegalStateException("Conflict detected when copying event: " + event.getEventName());
      }

      // Add the new event to the target calendar.
      targetCal.addEvent(newEvent);
    }

    return true;
  }

  @Override
  public boolean copyEvent(String sourceCalendarName, LocalDateTime eventDateTime, String eventName,
                           String targetCalendarName, LocalDateTime targetStart) {
    // Validate input parameters.
    if (sourceCalendarName == null || eventDateTime == null || eventName == null ||
            targetCalendarName == null || targetStart == null) {
      throw new IllegalArgumentException("All parameters must be provided and non-null.");
    }

    // Look up source calendar.
    Calendar sourceCal = getCalendarByName(sourceCalendarName);
    if (sourceCal == null) {
      throw new IllegalArgumentException("Source calendar not found: " + sourceCalendarName);
    }

    // Look up target calendar.
    Calendar targetCal = getCalendarByName(targetCalendarName);
    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar not found: " + targetCalendarName);
    }

    // Find the specific event in the source calendar with the given name and start time.
    CalendarEvent eventToCopy = null;
    for (CalendarEvent event : sourceCal.getEvents()) {
      if (event.getEventName().equalsIgnoreCase(eventName) &&
              event.getStartDateTime().equals(eventDateTime)) {
        eventToCopy = event;
        break;
      }
    }
    if (eventToCopy == null) {
      throw new IllegalStateException("Event with name '" + eventName + "' on " + eventDateTime + " not found in calendar " + sourceCalendarName);
    }

    // Compute the duration of the event.
    java.time.Duration duration = java.time.Duration.between(eventToCopy.getStartDateTime(), eventToCopy.getEndDateTime());

    // New event's start time is targetStart, and end time is targetStart plus duration.
    LocalDateTime newStart = targetStart;
    LocalDateTime newEnd = newStart.plus(duration);

    // Create a new event with the same details but adjusted times.
    CalendarEvent newEvent = new CalendarEvent(
            eventToCopy.getEventName(),
            newStart,
            newEnd,
            eventToCopy.getEventDescription(),
            eventToCopy.getEventLocation(),
            eventToCopy.isPublic(),
            eventToCopy.isRecurring(),
            eventToCopy.getRecurrenceDays(),
            eventToCopy.isAutoDecline()
    );

    // Check for conflicts in the target calendar.
    if (doesEventConflict(targetCal.getEvents(), convertToDTO(newEvent))) {
      throw new IllegalStateException("Conflict detected when copying event: " + eventToCopy.getEventName());
    }

    // Add the new event to the target calendar.
    targetCal.addEvent(newEvent);
    return true;
  }

  @Override
  public boolean isCalendarPresent(String calName) {
    return calendars.stream()
            .anyMatch(cal -> calName != null && calName.equals(cal.getCalendarName()));
  }

  @Override
  public boolean editCalendar(String calendarName, String property, String newValue) {
    // Look up the target calendar by name.
    Calendar targetCalendar = getCalendarByName(calendarName);
    if (targetCalendar == null) {
      throw new IllegalArgumentException("Calendar not found: " + calendarName);
    }

    switch (property.toLowerCase()) {
      case "name":
        // Check if another calendar already has the new name.
        for (Calendar cal : calendars) {
          if (cal.getCalendarName().equalsIgnoreCase(newValue)) {
            throw new IllegalArgumentException("Calendar with name '" + newValue + "' already exists.");
          }
        }
        targetCalendar.setCalendarName(newValue);
        return true;
      case "timezone":
        // Validate the new timezone.
        try {
          ZoneId.of(newValue);
        } catch (Exception e) {
          throw new IllegalArgumentException("Invalid timezone: " + newValue, e);
        }
        targetCalendar.setTimezone(newValue);
        return true;
      default:
        throw new IllegalArgumentException("Unsupported property for calendar edit: " + property);
    }
  }

  // Helper method to retrieve a calendar by its name.
  private Calendar getCalendarByName(String calName) {
    for (Calendar cal : calendars) {
      if (cal.getCalendarName().equalsIgnoreCase(calName)) {
        return cal;
      }
    }
    return null;
  }

  // Validates required basic event fields.
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

  // Validates properties specific to recurring events.
  private void validateRecurringEvent(ICalendarEventDTO eventDTO) {
    // Ensure the event's start and end occur on the same day.
    if (!eventDTO.getStartDateTime().toLocalDate().equals(eventDTO.getEndDateTime().toLocalDate())) {
      throw new IllegalArgumentException("Recurring events must have start and end on the same day.");
    }

    // Retrieve recurrence count and recurrence end date.
    Integer recCount = eventDTO.getRecurrenceCount();
    LocalDateTime recEndDate = eventDTO.getRecurrenceEndDate();

    // Either recurrence count or recurrence end date must be defined, but not both.
    if (recCount == null && recEndDate == null) {
      throw new IllegalArgumentException("Either recurrence count or recurrence end date must be defined for a recurring event.");
    }
    if (recCount != null && recEndDate != null) {
      throw new IllegalArgumentException("Cannot define both recurrence count and recurrence end date for a recurring event.");
    }
    // If recurrence count is provided, it must be greater than 0.
    if (recCount != null && recCount <= 0) {
      throw new IllegalArgumentException("Recurrence count must be greater than 0.");
    }

    // Recurrence days must be provided.
    if (eventDTO.getRecurrenceDays() == null) {
      throw new IllegalArgumentException("Recurrence days must be provided for recurring events.");
    }
  }

  // Helper method for non-recurring event validation.
  private void validateNonRecurringEvent(ICalendarEventDTO eventDTO) {
    Integer recCount = eventDTO.getRecurrenceCount();
    if ((recCount != null && recCount > 0) || eventDTO.getRecurrenceEndDate() != null) {
      throw new IllegalArgumentException("Non-recurring event should not have recurrence count or recurrence end date.");
    }
  }


  // Generates recurring event occurrences based on the eventDTO.
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

    // Loop until termination condition (by count or recurrence end date) is met.
    while (true) {
      if (eventDTO.getRecurrenceCount() != null && eventDTO.getRecurrenceCount() > 0) {
        if (count >= eventDTO.getRecurrenceCount()) break;
      } else if (recurrenceEnd != null) {
        if (currentDate.isAfter(recurrenceEnd)) break;
      }
      if (eventDTO.getRecurrenceDays().contains(currentDate.getDayOfWeek())) {
        LocalDateTime occurrenceStart = LocalDateTime.of(currentDate, startTime);
        LocalDateTime occurrenceEnd = LocalDateTime.of(currentDate, endTime);
        CalendarEventDTO occurrenceDTO = CalendarEventDTO.builder()
                .setEventName(eventDTO.getEventName())
                .setStartDateTime(occurrenceStart)
                .setEndDateTime(occurrenceEnd)
                .setAutoDecline(true)
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

  // Creates a single, non-recurring event.
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

  // Converts a CalendarEvent to a DTO for conflict checking.
  private ICalendarEventDTO convertToDTO(CalendarEvent event) {
    return CalendarEventDTO.builder()
            .setEventName(event.getEventName())
            .setStartDateTime(event.getStartDateTime())
            .setEndDateTime(event.getEndDateTime())
            .setEventLocation(event.getEventLocation())
            .setEventDescription(event.getEventDescription())
            .setPrivate(!event.isPublic())
            .setAutoDecline(true)
            .build();
  }

  // Checks if a new event conflicts with any existing events in the target list.
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

  // Returns the next date matching one of the recurrence days.
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

  private boolean checkConflictForEvent(CalendarEvent updatedEvent, List<CalendarEvent> events) {
    for (CalendarEvent other : events) {
      if (other != updatedEvent) {
        // If the updated event overlaps with any other event, return true.
        if (updatedEvent.getStartDateTime().isBefore(other.getEndDateTime()) &&
                updatedEvent.getEndDateTime().isAfter(other.getStartDateTime())) {
          return true;
        }
      }
    }
    return false;
  }

  public static LocalDateTime convertTimeToTargetDate(
          LocalDateTime sourceDateTime,
          String sourceZone,
          LocalDate targetDate,
          String targetZone
  ) {
    ZonedDateTime sourceZoned = sourceDateTime.atZone(ZoneId.of(sourceZone));
    ZonedDateTime targetZoned = sourceZoned.withZoneSameInstant(ZoneId.of(targetZone));
    LocalTime targetTime = targetZoned.toLocalTime();
    int dayShift = targetZoned.toLocalDate().compareTo(sourceZoned.toLocalDate());
    LocalDate adjustedDate = targetDate.plusDays(dayShift);
    return LocalDateTime.of(adjustedDate, targetTime);
  }
}