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
class Calendar {
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

  public void setTimezone(String newTimezone) {
    String oldTimezone = this.timezone;
    this.timezone = newTimezone;

    ZoneId oldZone = ZoneId.of(oldTimezone);
    ZoneId newZone = ZoneId.of(newTimezone);

    for (CalendarEvent event : events) {
      // Convert start time
      ZonedDateTime oldStartZoned = event.getStartDateTime().atZone(oldZone);
      ZonedDateTime newStartZoned = oldStartZoned.withZoneSameInstant(newZone);
      event.setStartDateTime(newStartZoned.toLocalDateTime());

      // Convert end time
      ZonedDateTime oldEndZoned = event.getEndDateTime().atZone(oldZone);
      ZonedDateTime newEndZoned = oldEndZoned.withZoneSameInstant(newZone);
      event.setEndDateTime(newEndZoned.toLocalDateTime());

    }
  }

  public List<CalendarEvent> getEventsCopy() {
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

  /**
   * Returns a new Builder instance for constructing a Calendar.
   *
   * @return a Builder for Calendar.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder class for constructing Calendar instances.
   */
  public static class Builder {
    private String calendarName;
    private String timezone;

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
      return new Calendar(calendarName, timezone);
    }
  }
}