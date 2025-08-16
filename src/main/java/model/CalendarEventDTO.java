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
      validateEventData();
      return new CalendarEventDTO(this);


    }

    /**
     * Validates all event data before constructing the object.
     *
     * @throws IllegalStateException if any validation fails
     */
    private void validateEventData() {
      // Required fields validation
      if (eventName == null || eventName.trim().isEmpty()) {
        throw new IllegalStateException("Event name cannot be empty");
      }

      if (startDateTime == null) {
        throw new IllegalStateException("Start date/time cannot be null");
      }

      if (endDateTime == null) {
        throw new IllegalStateException("End date/time cannot be null");
      }

      // Logical validation
      if (endDateTime.isBefore(startDateTime) || endDateTime.equals(startDateTime)) {
        throw new IllegalStateException("End date/time must be after start date/time");
      }

      // Recurring event validation
      if (Boolean.TRUE.equals(isRecurring)) {
        // Check if recurrence days are specified
        if (recurrenceDays == null || recurrenceDays.isEmpty()) {
          throw new IllegalStateException("Recurring events must specify recurrence days");
        }

        // Either recurrence count or end date must be specified, but not both
        if (recurrenceCount != null && recurrenceEndDate != null) {
          throw new IllegalStateException("Cannot specify both recurrence count and end date");
        }

        if (recurrenceCount == null && recurrenceEndDate == null) {
          throw new IllegalStateException("Must specify either recurrence count or end date");
        }

        if (recurrenceCount != null && recurrenceCount <= 0) {
          throw new IllegalStateException("Recurrence count must be positive");
        }

        if (recurrenceEndDate != null && recurrenceEndDate.isBefore(startDateTime)) {
          throw new IllegalStateException("Recurrence end date must be after start date/time");
        }
      } else {
        // Non-recurring events should not have recurrence parameters
        if (recurrenceCount != null || recurrenceEndDate != null ||
              (recurrenceDays != null && !recurrenceDays.isEmpty())) {
          throw new IllegalStateException("Non-recurring events should not have recurrence parameters");
        }
      }
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
