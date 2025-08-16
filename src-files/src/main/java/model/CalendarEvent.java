package model;

import java.time.LocalDateTime;


class CalendarEvent implements ICalendarEvent {
  private String eventName;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private String eventDescription;
  private String eventLocation;
  private boolean isPublic;

  private CalendarEvent(Builder builder) {
    this.eventName = builder.eventName;
    this.startDateTime = builder.startDateTime;
    this.endDateTime = builder.endDateTime;
    this.eventDescription = builder.eventDescription;
    this.eventLocation = builder.eventLocation;
    this.isPublic = builder.isPublic;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder implements ICalendarEventBuilder<CalendarEvent> {
    private String eventName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String eventDescription;
    private String eventLocation;
    private boolean isPublic;


    public Builder setEventName(String eventName) {
      this.eventName = eventName;
      return this;
    }

    public Builder setStartDateTime(LocalDateTime startDateTime) {
      this.startDateTime = startDateTime;
      return this;
    }

    public Builder setEndDateTime(LocalDateTime endDateTime) {
      this.endDateTime = endDateTime;
      return this;
    }

    public Builder setEventDescription(String eventDescription) {
      this.eventDescription = eventDescription;
      return this;
    }

    public Builder setEventLocation(String eventLocation) {
      this.eventLocation = eventLocation;
      return this;
    }

    public Builder setPublic(boolean isPublic) {
      this.isPublic = isPublic;
      return this;
    }


    public CalendarEvent build() {
      return new CalendarEvent(this);
    }
  }

  public boolean doesEventConflict(ICalendarEvent event) {
    return this.getStartDateTime().isBefore(event.getEndDateTime()) &&
        this.getEndDateTime().isAfter(event.getStartDateTime());
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

  public String getEventDescription() {
    return eventDescription;
  }

  public String getEventLocation() {
    return eventLocation;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public void setStartDateTime(LocalDateTime startDateTime) {
    this.startDateTime = startDateTime;
  }

  public void setEndDateTime(LocalDateTime endDateTime) {
    this.endDateTime = endDateTime;
  }

  public void setEventDescription(String eventDescription) {
    this.eventDescription = eventDescription;
  }

  public void setEventLocation(String eventLocation) {
    this.eventLocation = eventLocation;
  }

  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }
}
