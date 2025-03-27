package model;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents an individual calendar with a unique name, associated timezone,
 * and its own list of events.
 */
class Calendar implements ICalendar {
  private String calendarName;
  private String timezone;
  private List<ICalendarEvent> events;

  private Calendar(Builder builder) {
    this.calendarName = builder.calendarName;
    this.timezone = builder.timezone;
    this.events = builder.events;
  }

  public static Builder builder() {
    return new Calendar.Builder();
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

  public void setTimezone(String newTimezone) {
    List<ICalendarEvent> updatedEvents = new ArrayList<>();

    ZoneId oldZone = ZoneId.of(this.timezone);
    ZoneId newZone = ZoneId.of(newTimezone);


    for (ICalendarEvent event : events) {
      ZonedDateTime oldStartZoned = event.getStartDateTime().atZone(oldZone);
      ZonedDateTime newStartZoned = oldStartZoned.withZoneSameInstant(newZone);

      ZonedDateTime oldEndZoned = event.getEndDateTime().atZone(oldZone);
      ZonedDateTime newEndZoned = oldEndZoned.withZoneSameInstant(newZone);

      ICalendarEvent updated = ICalendarEvent.builder()
          .setEventName(event.getEventName())
          .setStartDateTime(newStartZoned.toLocalDateTime())
          .setEndDateTime(newEndZoned.toLocalDateTime())
          .setEventDescription(event.getEventDescription())
          .setEventLocation(event.getEventLocation())
          .setPublic(event.isPublic())
          .build();

      updatedEvents.add(updated);
    }

    this.events = updatedEvents;
    this.timezone = newTimezone;
  }

  public List<ICalendarEvent> getEventsCopy() {
    return this.events.stream()
        .map(event -> CalendarEvent.builder()
            .setEventName(event.getEventName())
            .setStartDateTime(event.getStartDateTime())
            .setEndDateTime(event.getEndDateTime())
            .setEventDescription(event.getEventDescription())
            .setEventLocation(event.getEventLocation())
            .setPublic(event.isPublic())
            .build())
        .collect(Collectors.toList());
  }


  public List<ICalendarEvent> getEvents() {
    return events;
  }

  public void setEvents(List<ICalendarEvent> events) {
    this.events = events;
  }

  public void addEvents(List<CalendarEvent> events) {
    this.events.addAll(events);
  }

  public void addEvent(CalendarEvent event) {
    this.events.add(event);
  }

  /**
   * Builder class for constructing Calendar instances.
   */
  public static class Builder implements ICalendarBuilder<Calendar> {
    private String calendarName;
    private String timezone;
    private List<ICalendarEvent> events = new ArrayList<>();

    /**
     * Sets the calendar name.
     *
     * @param calendarName the unique name of the calendar.
     * @return the Builder instance.
     */
    public Builder setCalendarName(String calendarName) {
      this.calendarName = calendarName;
      return this;
    }

    public Builder setEvents(List<ICalendarEvent> events) {
      this.events = events;
      return this;
    }

    /**
     * Sets the timezone.
     *
     * @param timezone the IANA timezone string.
     * @return the Builder instance.
     */
    public Builder setTimezone(String timezone) {
      this.timezone = timezone;
      return this;
    }

    /**
     * Builds and returns a new Calendar instance.
     *
     * @return a new Calendar object.
     */
    public Calendar build() {
      return new Calendar(this);
    }
  }
}