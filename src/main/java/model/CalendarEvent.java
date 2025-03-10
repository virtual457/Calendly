package model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

class CalendarEvent {
  private String eventName;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private boolean isRecurring;
  private boolean autoDecline;

  public CalendarEvent(String eventName, LocalDateTime startDateTime, LocalDateTime endDateTime,
                       boolean isRecurring, boolean autoDecline) {
    this.eventName = eventName;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.isRecurring = isRecurring;
    this.autoDecline = autoDecline;
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

  public boolean isRecurring() {
    return isRecurring;
  }


  public boolean isAutoDecline() {
    return autoDecline;
  }

  // Setters for editable fields
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public void setStartDateTime(LocalDateTime startDateTime) {
    this.startDateTime = startDateTime;
  }

  public void setEndDateTime(LocalDateTime endDateTime) {
    this.endDateTime = endDateTime;
  }

}
