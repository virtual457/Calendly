package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CalendarModel stores multiple calendars in a list.
 * Each operation requires a calendar name to identify the target calendar.
 */
class CalendarModel implements ICalendarModel {
  private List<ICalendar> calendars;

  public CalendarModel() {
    this.calendars = new ArrayList<>();
  }

  @Override
  public boolean createCalendar(String calName, String timezone) {

    try {
      ZoneId.of(timezone);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezone, e);
    }


    for (ICalendar cal : calendars) {
      if (cal.getCalendarName().equalsIgnoreCase(calName)) {
        throw new IllegalArgumentException("Calendar with name '" + calName + "' " +
            "already exists.");
      }
    }


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
   * @throws IllegalArgumentException if the calendar is not found or event validation
   *                                  fails
   * @throws IllegalStateException    if a conflict is detected in the target calendar
   */

  @Override
  public boolean addEvent(String calendarName, ICalendarEventDTO eventDTO) {
    // Validate calendar existence
    ICalendar targetCalendar = getCalendarByName(calendarName);

    // Validate event data
    validateEvent(eventDTO);

    try {
      // Handle adding the event based on recurrence status
      if (Boolean.TRUE.equals(eventDTO.isRecurring())) {
        addRecurringEvent(targetCalendar, eventDTO);
      } else {
        addSingleEvent(targetCalendar, eventDTO);
      }

      return true;
    } catch (IllegalStateException e) {
      // Pass through validation/conflict exceptions
      throw e;
    } catch (Exception e) {
      // Log and handle other unexpected errors
      System.err.println("Error adding event: " + e.getMessage());
      return false;
    }
  }

  /**
   * Adds a recurring event to the calendar, handling all occurrences.
   *
   * @param calendar the calendar to add the event to
   * @param eventDTO the recurring event data
   * @throws IllegalStateException if there are conflicts
   */
  private void addRecurringEvent(ICalendar calendar, ICalendarEventDTO eventDTO) {
    List<CalendarEvent> occurrences = generateRecurringOccurrences(eventDTO);

    // Check for conflicts if auto-decline is enabled
    if (eventDTO.isAutoDecline()) {
      for (CalendarEvent occurrence : occurrences) {
        for (ICalendarEvent existingEvent : calendar.getEvents()) {
          if (existingEvent.doesEventConflict(occurrence)) {
            throw new IllegalStateException("Conflict detected on " +
                  occurrence.getStartDateTime() + ", event not created");
          }
        }
      }
    }

    // Add all occurrences
    calendar.addEvents(occurrences);
  }

  /**
   * Adds a single, non-recurring event to the calendar.
   *
   * @param calendar the calendar to add the event to
   * @param eventDTO the event data
   * @throws IllegalStateException if there are conflicts
   */
  private void addSingleEvent(ICalendar calendar, ICalendarEventDTO eventDTO) {
    // Check for conflicts if auto-decline is enabled
    if (eventDTO.isAutoDecline() && doesEventConflict(calendar.getEvents(), eventDTO)) {
      throw new IllegalStateException("Conflict detected, event not created");
    }

    // Create and add the event
    CalendarEvent event = createSingleEvent(eventDTO);
    calendar.addEvent(event);
  }

  @Override
  public boolean editEvents(String calendarName, String property, String eventName,
                            LocalDateTime fromDateTime,
                            String newValue, boolean editAll) {

    ICalendar targetCalendar = getCalendarByName(calendarName);



    if (newValue == null || (newValue.trim().isEmpty() && (!property.equalsIgnoreCase(
        "description") || property.equalsIgnoreCase("location")))) {
      throw new IllegalArgumentException("Missing value for property update.");
    }

    List<ICalendarEvent> originalEvents = targetCalendar.getEventsCopy();

    boolean found = false;

    try {
      for (ICalendarEvent event : targetCalendar.getEvents()) {

        if (event.getEventName().equals(eventName)
            && (event.getStartDateTime().isAfter(fromDateTime)
            || event.getStartDateTime().equals(fromDateTime))) {

          LocalDateTime originalStart = event.getStartDateTime();
          LocalDateTime originalEnd = event.getEndDateTime();


          switch (property.toLowerCase()) {
            case "name":
              event.setEventName(newValue);
              found = true;
              break;
            case "start":

              LocalDateTime parsedNewStart = LocalDateTime.parse(newValue);
              LocalDateTime newStart =
                  LocalDateTime.of(event.getStartDateTime().toLocalDate(),
                      parsedNewStart.toLocalTime());
              if (!event.getEndDateTime().isAfter(newStart)) {
                throw new IllegalArgumentException("New start must be before current end " +
                    "time.");
              }
              event.setStartDateTime(newStart);
              found = true;
              break;
            case "end":

              LocalDateTime parsedNewEnd = LocalDateTime.parse(newValue);
              LocalDateTime newEnd = LocalDateTime.of(event.getEndDateTime().toLocalDate(),
                  parsedNewEnd.toLocalTime());
              if (!newEnd.isAfter(event.getStartDateTime())) {
                throw new IllegalArgumentException("New end must be after current start " +
                    "time.");
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

              if (!newValue.equalsIgnoreCase("true") && !newValue.equalsIgnoreCase("false"
              )) {
                throw new IllegalArgumentException("For input string: \"" + newValue +
                    "\"");
              }
              event.setPublic(!Boolean.parseBoolean(newValue));
              found = true;
              break;
            default:
              throw new IllegalArgumentException("Unsupported property for edit: " + property);
          }


          if (checkConflictForEvent(event, targetCalendar.getEvents())) {
            throw new IllegalStateException("Conflict detected after editing " + property);
          }

          if (!editAll) {
            return true;
          }
        }
      }
    } catch (Exception e) {
      targetCalendar.setEvents(originalEvents);
      throw e;
    }
    if (!found) {
      throw new IllegalStateException("No matching event found for editing: " + eventName);
    }
    return found;
  }

  @Override
  public boolean editEvent(String calendarName, String property, String eventName,
                           LocalDateTime fromDateTime, LocalDateTime toDateTime,
                           String newValue) {
    ICalendar targetCalendar = getCalendarByName(calendarName);
    boolean editAll = true;
    boolean found = false;

    for (ICalendarEvent event : targetCalendar.getEvents()) {

      if (event.getEventName().equals(eventName)
          && (event.getStartDateTime().isEqual(fromDateTime)
          && event.getEndDateTime().equals(toDateTime))) {

        LocalDateTime originalStart = event.getStartDateTime();
        LocalDateTime originalEnd = event.getEndDateTime();

        found = true;

        updateSpecifiedProperty(event, property, newValue);


        if (checkConflictForEvent(event, targetCalendar.getEvents())) {

          event.setStartDateTime(originalStart);
          event.setEndDateTime(originalEnd);
          throw new IllegalStateException("Conflict detected after editing " + property);
        }

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

  private void updateSpecifiedProperty(ICalendarEvent event, String property,
                                       String newValue) {
    switch (property.toLowerCase()) {
      case "name":
        event.setEventName(newValue);

        break;
      case "start":
        LocalDateTime newStart = LocalDateTime.parse(newValue);
        if (!event.getEndDateTime().isAfter(newStart)) {
          throw new IllegalArgumentException("New start must be before current end " +
              "time.");
        }
        event.setStartDateTime(newStart);

        break;
      case "end":
        LocalDateTime newEnd = LocalDateTime.parse(newValue);
        if (!newEnd.isAfter(event.getStartDateTime())) {
          throw new IllegalArgumentException("New end must be after current start " +
              "time.");
        }
        event.setEndDateTime(newEnd);

        break;
      case "description":
        event.setEventDescription(newValue);

        break;
      case "location":
        event.setEventLocation(newValue);

        break;
      case "isprivate":
        if (!newValue.equalsIgnoreCase("true") && !newValue.equalsIgnoreCase("false"
        )) {
          throw new IllegalArgumentException("For input string: \"" + newValue +
              "\"");
        }
        event.setPublic(!Boolean.parseBoolean(newValue));

        break;
      default:
        throw new IllegalArgumentException("Unsupported property for edit: " + property);
    }
  }

  public boolean isCalendarAvailable(String calName, LocalDate date) {

    for (ICalendar cal : calendars) {
      if (cal.getCalendarName().equalsIgnoreCase(calName)) {
        // If no specific date is provided, return true since the calendar exists.
        if (date == null) {
          return true;
        }

        for (ICalendarEvent event : cal.getEvents()) {
          if (event.getStartDateTime().toLocalDate().equals(date)) {
            return false;
          }
        }
        // No events on the given date, so the calendar is available.
        return true;
      }
    }
    // No calendar with the given name exists.
    return false;
  }

  @Override
  public List<String> getCalendarNames() {
    return calendars.stream().map(ICalendar::getCalendarName).collect(Collectors.toList());
  }

  public boolean deleteCalendar(String calName) {
    for (Iterator<ICalendar> iterator = calendars.iterator(); iterator.hasNext(); ) {
      ICalendar cal = iterator.next();
      if (cal.getCalendarName().equalsIgnoreCase(calName)) {
        iterator.remove();
        return true;
      }
    }
    return false;
  }

  @Override
  public List<ICalendarEventDTO> getEventsInSpecificDateTime(String calendarName,
                                                             LocalDateTime dateTime) {
    // Find the target calendar by its name.
    ICalendar targetCalendar = getCalendarByName(calendarName);
    if (targetCalendar == null) {
      throw new IllegalArgumentException("Calendar not found: " + calendarName);
    }

    if (dateTime == null) {
      throw new IllegalArgumentException("date time cannot be null");
    }

    List<ICalendarEvent> rangeEvents = new ArrayList<>();
    for (ICalendarEvent event : targetCalendar.getEvents()) {
      if ((event.getStartDateTime().isBefore(dateTime) ||
          event.getStartDateTime().equals(dateTime)) &&
          (event.getEndDateTime().isAfter(dateTime) || event.getEndDateTime().equals(dateTime))) {
        rangeEvents.add(event);
      }
    }
    return rangeEvents.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  @Override
  public List<ICalendarEventDTO> getEventsInRange(String calendarName,
                                                  LocalDateTime fromDateTime,
                                                  LocalDateTime toDateTime) {
    // find the target calendar by its name.
    ICalendar targetCalendar = getCalendarByName(calendarName);
    if (targetCalendar == null) {
      throw new IllegalArgumentException("Calendar not found: " + calendarName);
    }

    if (fromDateTime == null || toDateTime == null) {
      throw new IllegalArgumentException("Both start and end date-times must be " +
          "provided.");
    }
    if (toDateTime.isBefore(fromDateTime)) {
      throw new IllegalArgumentException("The end date-time must not be before the " +
          "start date-time.");
    }

    List<ICalendarEvent> rangeEvents = new ArrayList<>();
    // Iterates over the events in the target calendar.
    for (ICalendarEvent event : targetCalendar.getEvents()) {

      if ((!event.getStartDateTime().isBefore(fromDateTime) &&
          !event.getStartDateTime().isAfter(toDateTime)) ||
          (event.getEndDateTime().isAfter(fromDateTime) &&
              event.getEndDateTime().isBefore(toDateTime))) {
        rangeEvents.add(event);
      }
    }
    return rangeEvents.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  @Override
  public boolean copyEvents(String sourceCalendarName, LocalDateTime sourceStart,
                            LocalDateTime sourceEnd,
                            String targetCalendarName, LocalDate targetStart) {


    ICalendar sourceCal = getCalendarByName(sourceCalendarName);
    if (sourceCal == null) {
      throw new IllegalArgumentException("Source calendar not found: " + sourceCalendarName);
    }

    ICalendar targetCal = getCalendarByName(targetCalendarName);
    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar not found: " + targetCalendarName);
    }

    if (sourceStart == null || sourceEnd == null) {
      throw new IllegalArgumentException("Source start and source end times must be " +
          "provided.");
    }
    if (sourceEnd.isBefore(sourceStart)) {
      throw new IllegalArgumentException("Source end time must not be before source " +
          "start time.");
    }

    List<ICalendarEvent> eventsToCopy = new ArrayList<>();
    for (ICalendarEvent event : sourceCal.getEvents()) {
      if (!event.getStartDateTime().isBefore(sourceStart) &&
          !event.getStartDateTime().isAfter(sourceEnd)) {
        eventsToCopy.add(event);
      }
    }

    if (eventsToCopy.isEmpty()) {
      throw new IllegalArgumentException("Events to be copied are empty");
    }


    eventsToCopy.sort((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()));


    java.time.ZoneId sourceZone = java.time.ZoneId.of(sourceCal.getTimezone());
    java.time.ZoneId targetZone = java.time.ZoneId.of(targetCal.getTimezone());

    List<CalendarEvent> eventsToBeCopied = new ArrayList<>();
    for (ICalendarEvent event : eventsToCopy) {

      LocalDate calculatedTargetStart =
          targetStart.plusDays(ChronoUnit.DAYS.between(sourceStart,
              event.getStartDateTime()));
      LocalDateTime newStart = convertTimeToTargetDate(event.getStartDateTime(),
          sourceCal.getTimezone(), calculatedTargetStart, targetCal.getTimezone());
      java.time.Duration duration = java.time.Duration.between(event.getStartDateTime()
          , event.getEndDateTime());

      LocalDateTime newEnd = newStart.plus(duration);
      CalendarEvent newEvent = CalendarEvent.builder()
          .setEventName(event.getEventName())
          .setStartDateTime(newStart)
          .setEndDateTime(newEnd)
          .setEventDescription(event.getEventDescription())
          .setEventLocation(event.getEventLocation())
          .setPublic(event.isPublic())
          .build();


      if (doesEventConflict(targetCal.getEvents(), convertToDTO(newEvent))) {
        throw new IllegalStateException("Conflict detected when copying event: " +
            event.getEventName());
      }

      eventsToBeCopied.add(newEvent);
    }

    eventsToBeCopied.forEach(targetCal::addEvent);

    return true;
  }

  @Override
  public boolean copyEvent(String sourceCalendarName, LocalDateTime eventDateTime,
                           String eventName,
                           String targetCalendarName, LocalDateTime targetStart) {

    if (sourceCalendarName == null || eventDateTime == null || eventName == null ||
        targetCalendarName == null || targetStart == null) {
      throw new IllegalArgumentException("All parameters must be provided and non-null.");
    }


    ICalendar sourceCal = getCalendarByName(sourceCalendarName);
    if (sourceCal == null) {
      throw new IllegalArgumentException("Source calendar not found: " + sourceCalendarName);
    }


    ICalendar targetCal = getCalendarByName(targetCalendarName);
    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar not found: " + targetCalendarName);
    }


    ICalendarEvent eventToCopy = null;
    for (ICalendarEvent event : sourceCal.getEvents()) {
      if (event.getEventName().equalsIgnoreCase(eventName) &&
          event.getStartDateTime().equals(eventDateTime)) {
        eventToCopy = event;
        break;
      }
    }
    if (eventToCopy == null) {
      throw new IllegalStateException("Event with name '" +
          eventName + "' on " + eventDateTime + " not found in calendar " + sourceCalendarName);
    }

    java.time.Duration duration =
        java.time.Duration.between(eventToCopy.getStartDateTime(),
            eventToCopy.getEndDateTime());


    LocalDateTime newStart = targetStart;
    LocalDateTime newEnd = newStart.plus(duration);


    CalendarEvent newEvent = CalendarEvent.builder()
        .setEventName(eventToCopy.getEventName())
        .setStartDateTime(newStart)
        .setEndDateTime(newEnd)
        .setEventDescription(eventToCopy.getEventDescription())
        .setEventLocation(eventToCopy.getEventLocation())
        .setPublic(eventToCopy.isPublic())
        .build();


    if (doesEventConflict(targetCal.getEvents(), convertToDTO(newEvent))) {
      throw new IllegalStateException("Conflict detected when copying event: " +
          eventToCopy.getEventName());
    }


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

    ICalendar targetCalendar = getCalendarByName(calendarName);
    if (targetCalendar == null) {
      throw new IllegalArgumentException("Calendar not found: " + calendarName);
    }

    switch (property.toLowerCase()) {
      case "name":

        for (ICalendar cal : calendars) {
          if (cal.getCalendarName().equalsIgnoreCase(newValue)) {
            throw new IllegalArgumentException("Calendar with name '" + newValue + "' " +
                "already exists.");
          }
        }
        targetCalendar.setCalendarName(newValue);
        return true;
      case "timezone":

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


  @Override
  public boolean addEvents(String calendarName, List<ICalendarEventDTO> events) {
    // First validate all events
    ICalendar targetCalendar = getCalendarByName(calendarName);
    List<String> errors = new ArrayList<>();

    // Create a list to hold all new occurrences to check for conflicts among them
    List<CalendarEvent> allNewOccurrences = new ArrayList<>();
    // Keep track of which events have autoDecline enabled
    Map<CalendarEvent, Boolean> autoDeclineMap = new HashMap<>();

    // First validation phase - basic properties + prepare occurrences
    for (ICalendarEventDTO eventDTO : events) {
      try {
        // Basic validation
        validateBasicEvent(eventDTO);

        boolean autoDecline = (eventDTO.isAutoDecline() != null) && eventDTO.isAutoDecline();

        if (Boolean.TRUE.equals(eventDTO.isRecurring())) {
          validateRecurringEvent(eventDTO);
          List<CalendarEvent> occurrences = generateRecurringOccurrences(eventDTO);
          for (CalendarEvent occurrence : occurrences) {
            allNewOccurrences.add(occurrence);
            autoDeclineMap.put(occurrence, autoDecline);
          }
        } else {
          validateNonRecurringEvent(eventDTO);
          CalendarEvent event = createSingleEvent(eventDTO);
          allNewOccurrences.add(event);
          autoDeclineMap.put(event, autoDecline);
        }
      } catch (Exception e) {
        errors.add("Event " + eventDTO.getEventName() + ": " + e.getMessage());
      }
    }

// If there are validation errors, stop here
    if (!errors.isEmpty()) {
      throw new IllegalStateException("Cannot add all events: " + String.join("; ", errors));
    }

// Second phase - check for conflicts (both with existing events and among new events)
    List<ICalendarEvent> existingEvents = targetCalendar.getEvents();
    for (int i = 0; i < allNewOccurrences.size(); i++) {
      CalendarEvent event = allNewOccurrences.get(i);

      // Only check conflicts if this event has autoDecline enabled
      if (autoDeclineMap.get(event)) {
        // Check against existing events
        for (ICalendarEvent existing : existingEvents) {
          if (existing.doesEventConflict(event)) {
            errors.add("Event " + event.getEventName() + " conflicts with existing event " + existing.getEventName());
            break;
          }
        }

        // Check against other new events with autoDecline enabled
        for (int j = 0; j < allNewOccurrences.size(); j++) {
          if (i == j) continue; // Skip comparing with itself

          CalendarEvent otherNew = allNewOccurrences.get(j);
          // Only check if the other event also has autoDecline
          if (autoDeclineMap.get(otherNew) && event.doesEventConflict(otherNew)) {
            errors.add("New event " + event.getEventName() + " conflicts with another new event " + otherNew.getEventName());
            break;
          }
        }
      }
    }

// If any conflicts were found, don't add any events
    if (!errors.isEmpty()) {
      throw new IllegalStateException("Cannot add all events: " + String.join("; ", errors));
    }

// If all checks pass, add all the events
    for (ICalendarEventDTO eventDTO : events) {
      if (Boolean.TRUE.equals(eventDTO.isRecurring())) {
        List<CalendarEvent> occurrences = generateRecurringOccurrences(eventDTO);
        targetCalendar.addEvents(occurrences);
      } else {
        CalendarEvent event = createSingleEvent(eventDTO);
        targetCalendar.addEvent(event);
      }
    }

    return true;
  }

  private ICalendar getCalendarByName(String calName) {
    for (ICalendar cal : calendars) {
      if (cal.getCalendarName().equalsIgnoreCase(calName)) {
        return cal;
      }
    }
    throw new IllegalArgumentException("Calendar not found: " + calName);
  }


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
      throw new IllegalArgumentException("End date and time must be after start date " +
          "and time.");
    }
  }


  private void validateRecurringEvent(ICalendarEventDTO eventDTO) {

    if (!eventDTO.getStartDateTime().toLocalDate().
        equals(eventDTO.getEndDateTime().toLocalDate())) {
      throw new IllegalArgumentException("Recurring events must have start and end on " +
          "the same day.");
    }


    Integer recCount = getRecCount(eventDTO);

    if (recCount != null && recCount <= 0) {
      throw new IllegalArgumentException("Recurrence count must be greater than 0.");
    }

    // Recurrence days must be provided.
    if (eventDTO.getRecurrenceDays() == null) {
      throw new IllegalArgumentException("Recurrence days must be provided for " +
          "recurring events.");
    }
  }

  private static Integer getRecCount(ICalendarEventDTO eventDTO) {
    Integer recCount = eventDTO.getRecurrenceCount();
    LocalDateTime recEndDate = eventDTO.getRecurrenceEndDate();

    if (recCount == null && recEndDate == null) {
      throw new IllegalArgumentException("Either recurrence count or recurrence end " +
          "date must be defined for a recurring event.");
    }
    if (recCount != null && recEndDate != null) {
      throw new IllegalArgumentException("Cannot define both recurrence count and " +
          "recurrence end date for a recurring event.");
    }
    return recCount;
  }

  private void validateNonRecurringEvent(ICalendarEventDTO eventDTO) {
    Integer recCount = eventDTO.getRecurrenceCount();
    if ((recCount != null && recCount > 0) || eventDTO.getRecurrenceEndDate() != null) {
      throw new IllegalArgumentException("Non-recurring event should not have " +
          "recurrence count or recurrence end date.");
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
    LocalDate recurrenceEnd = getRecurrenceEndDate(eventDTO);

    while (!isRecurrenceComplete(eventDTO, currentDate, count, recurrenceEnd)) {
      if (isRecurrenceDay(eventDTO, currentDate)) {
        CalendarEvent occurrence = createOccurrence(eventDTO, currentDate, startTime, endTime);
        occurrences.add(occurrence);
        count++;
      }
      currentDate = getNextRecurrenceDate(currentDate, eventDTO.getRecurrenceDays());
    }
    return occurrences;
  }

  private LocalDate getRecurrenceEndDate(ICalendarEventDTO eventDTO) {
    return eventDTO.getRecurrenceEndDate() != null
          ? eventDTO.getRecurrenceEndDate().toLocalDate()
          : null;
  }

  private boolean isRecurrenceComplete(ICalendarEventDTO eventDTO, LocalDate currentDate,
                                       int count, LocalDate recurrenceEnd) {
    // Check count-based termination
    if (eventDTO.getRecurrenceCount() != null && eventDTO.getRecurrenceCount() > 0) {
      if (count >= eventDTO.getRecurrenceCount()) {
        return true;
      }
    }
    // Check date-based termination
    else if (recurrenceEnd != null) {
      if (currentDate.isAfter(recurrenceEnd)) {
        return true;
      }
    }
    return false;
  }

  private boolean isRecurrenceDay(ICalendarEventDTO eventDTO, LocalDate date) {
    return eventDTO.getRecurrenceDays().contains(date.getDayOfWeek());
  }

  private CalendarEvent createOccurrence(ICalendarEventDTO eventDTO, LocalDate date,
                                         LocalTime startTime, LocalTime endTime) {
    LocalDateTime occurrenceStart = LocalDateTime.of(date, startTime);
    LocalDateTime occurrenceEnd = LocalDateTime.of(date, endTime);

    Boolean isPrivate = eventDTO.isPrivate();
    Boolean isPublic = isPrivate == null || !isPrivate;

    return CalendarEvent.builder()
          .setEventName(eventDTO.getEventName())
          .setStartDateTime(occurrenceStart)
          .setEndDateTime(occurrenceEnd)
          .setEventDescription(eventDTO.getEventDescription())
          .setEventLocation(eventDTO.getEventLocation())
          .setPublic(Boolean.TRUE.equals(isPublic))
          .build();
  }

  // Creates a single, non-recurring event.
  private CalendarEvent createSingleEvent(ICalendarEventDTO eventDTO) {
    Boolean isPrivate = eventDTO.isPrivate();
    Boolean isPublic = isPrivate == null || !isPrivate;
    return CalendarEvent.builder()
        .setEventName(eventDTO.getEventName())
        .setStartDateTime(eventDTO.getStartDateTime())
        .setEndDateTime(eventDTO.getEndDateTime())
        .setEventDescription(eventDTO.getEventDescription())
        .setEventLocation(eventDTO.getEventLocation())
        .setPublic(Boolean.TRUE.equals(isPublic))
        .build();
  }

  private ICalendarEventDTO convertToDTO(ICalendarEvent event) {
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


  private boolean doesEventConflict(List<ICalendarEvent> eventList,
                                    ICalendarEventDTO newEventDTO) {
    CalendarEvent firstEvent =
        CalendarEvent.builder().setStartDateTime(newEventDTO.getStartDateTime()).
            setEndDateTime(newEventDTO.getEndDateTime()).build();
    for (ICalendarEvent event : eventList) {
      if (event.doesEventConflict(firstEvent)) {
        return true;
      }
    }
    return false;
  }

  private LocalDate getNextRecurrenceDate(LocalDate currentDate,
                                          List<DayOfWeek> recurrenceDays) {
    if (recurrenceDays.isEmpty()) {
      return currentDate.plusDays(1);
    }
    LocalDate nextDate = currentDate.plusDays(1);
    while (!recurrenceDays.contains(nextDate.getDayOfWeek())) {
      nextDate = nextDate.plusDays(1);
    }
    return nextDate;
  }

  private boolean checkConflictForEvent(ICalendarEvent updatedEvent,
                                        List<ICalendarEvent> events) {
    for (ICalendarEvent other : events) {
      if (other != updatedEvent) {

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


  /**
   * Comprehensive validation for calendar events.
   * Validates both recurring and non-recurring events in a centralized method.
   *
   * @param eventDTO the event data to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validateEvent(ICalendarEventDTO eventDTO) {
    // 1. Basic validation that applies to all events
    if (eventDTO.getStartDateTime() == null) {
      throw new IllegalArgumentException("Start date and time are required.");
    }
    if (eventDTO.getEndDateTime() == null) {
      throw new IllegalArgumentException("End date and time are required.");
    }
    if (!eventDTO.getEndDateTime().isAfter(eventDTO.getStartDateTime())) {
      throw new IllegalArgumentException("End date and time must be after start date and time.");
    }

    // 2. Specific validation based on recurring status
    if (Boolean.TRUE.equals(eventDTO.isRecurring())) {
      validateRecurringEventRules(eventDTO);
    } else {
      validateNonRecurringEventRules(eventDTO);
    }
  }

  /**
   * Validates rules specific to recurring events.
   *
   * @param eventDTO the recurring event to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validateRecurringEventRules(ICalendarEventDTO eventDTO) {
    // Verify start and end are on the same day
    if (!eventDTO.getStartDateTime().toLocalDate().equals(eventDTO.getEndDateTime().toLocalDate())) {
      throw new IllegalArgumentException("Recurring events must have start and end on the same day.");
    }

    // Verify recurrence termination is specified
    Integer recCount = eventDTO.getRecurrenceCount();
    LocalDateTime recEndDate = eventDTO.getRecurrenceEndDate();

    if (recCount == null && recEndDate == null) {
      throw new IllegalArgumentException("Either recurrence count or recurrence end date must be defined for a recurring event.");
    }
    if (recCount != null && recEndDate != null) {
      throw new IllegalArgumentException("Cannot define both recurrence count and recurrence end date for a recurring event.");
    }
    if (recCount != null && recCount <= 0) {
      throw new IllegalArgumentException("Recurrence count must be greater than 0.");
    }

    // Verify recurrence days are specified
    if (eventDTO.getRecurrenceDays() == null || eventDTO.getRecurrenceDays().isEmpty()) {
      throw new IllegalArgumentException("Recurrence days must be provided for recurring events.");
    }

    // Verify recurrence end date is after start date
    if (recEndDate != null && !recEndDate.isAfter(eventDTO.getStartDateTime())) {
      throw new IllegalArgumentException("Recurrence end date must be after the event start date.");
    }
  }

  /**
   * Validates rules specific to non-recurring events.
   *
   * @param eventDTO the non-recurring event to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validateNonRecurringEventRules(ICalendarEventDTO eventDTO) {
    // Non-recurring events should not have recurrence parameters
    if (eventDTO.getRecurrenceCount() != null || eventDTO.getRecurrenceEndDate() != null ||
          (eventDTO.getRecurrenceDays() != null && !eventDTO.getRecurrenceDays().isEmpty())) {
      throw new IllegalArgumentException("Non-recurring event should not have recurrence parameters.");
    }
  }
}