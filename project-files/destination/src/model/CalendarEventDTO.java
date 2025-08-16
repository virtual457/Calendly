package model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A Data Transfer Object (DTO) that represents a calendar event.
 * <p>
 * This class encapsulates all the details of a calendar event such as
 * name, start and end times, recurrence rules, description, location,
 * privacy settings, and auto-decline behavior.
 * </p>
 */

public class CalendarEventDTO implements ICalendarEventDTO {
  private String eventName;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private Boolean isRecurring;
  private List<DayOfWeek> recurrenceDays;
  private Integer recurrenceCount;
  private LocalDateTime recurrenceEndDate;
  private Boolean autoDecline;
  private String eventDescription;
  private String eventLocation;
  private Boolean isPrivate;

  static CalendarEventDTOBuilder builder() {
    return new CalendarEventDTOBuilder();
  }

  private CalendarEventDTO(CalendarEventDTOBuilder builder) {
    this.eventName = builder.eventName;
    this.startDateTime = builder.startDateTime;
    this.endDateTime = builder.endDateTime;
    this.isRecurring = builder.isRecurring;
    this.recurrenceDays = builder.recurrenceDays;
    this.recurrenceCount = builder.recurrenceCount;
    this.recurrenceEndDate = builder.recurrenceEndDate;
    this.autoDecline = builder.autoDecline;
    this.eventDescription = builder.eventDescription;
    this.eventLocation = builder.eventLocation;
    this.isPrivate = builder.isPrivate;
  }

  static class CalendarEventDTOBuilder implements ICalendarEventDTOBuilder<CalendarEventDTO> {
    private String eventName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Boolean isRecurring = false;
    private List<DayOfWeek> recurrenceDays;
    private Integer recurrenceCount;
    private LocalDateTime recurrenceEndDate;
    private Boolean autoDecline = false;
    private String eventDescription = "";
    private String eventLocation = "";
    private Boolean isPrivate = false;


    public CalendarEventDTOBuilder setEventName(String eventName) {
      this.eventName = eventName;
      return this;
    }

    public CalendarEventDTOBuilder setStartDateTime(LocalDateTime startDateTime) {
      this.startDateTime = startDateTime;
      return this;
    }

    public CalendarEventDTOBuilder setEndDateTime(LocalDateTime endDateTime) {
      this.endDateTime = endDateTime;
      return this;
    }

    public CalendarEventDTOBuilder setRecurring(Boolean isRecurring) {
      this.isRecurring = isRecurring;
      return this;
    }

    public CalendarEventDTOBuilder setRecurrenceCount(Integer recurrenceCount) {
      this.recurrenceCount = recurrenceCount;
      return this;
    }

    public CalendarEventDTOBuilder setRecurrenceDays(List<DayOfWeek> recurrenceDays) {
      this.recurrenceDays = recurrenceDays;
      return this;
    }

    public CalendarEventDTOBuilder setRecurrenceEndDate(LocalDateTime recurrenceEndDate) {
      this.recurrenceEndDate = recurrenceEndDate;
      return this;
    }

    public CalendarEventDTOBuilder setAutoDecline(Boolean autoDecline) {
      this.autoDecline = autoDecline;
      return this;
    }

    public CalendarEventDTOBuilder setEventDescription(String eventDescription) {
      this.eventDescription = eventDescription;
      return this;
    }

    public CalendarEventDTOBuilder setEventLocation(String eventLocation) {
      this.eventLocation = eventLocation;
      return this;
    }

    public CalendarEventDTOBuilder setPrivate(Boolean isPrivate) {
      this.isPrivate = isPrivate;
      return this;
    }

    @Override
    public CalendarEventDTO build() {
      return new CalendarEventDTO(this);
    }
  }

  public String getEventName() {
    return eventName;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  public Boolean isRecurring() {
    return isRecurring;
  }

  public List<DayOfWeek> getRecurrenceDays() {
    return recurrenceDays;
  }

  public Integer getRecurrenceCount() {
    return recurrenceCount;
  }

  public LocalDateTime getRecurrenceEndDate() {
    return recurrenceEndDate;
  }

  public Boolean isAutoDecline() {
    return autoDecline;
  }

  public String getEventDescription() {
    return eventDescription;
  }

  public String getEventLocation() {
    return eventLocation;
  }

  public Boolean isPrivate() {
    return isPrivate;
  }
}
