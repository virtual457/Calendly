package model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

public class CalendarEvent {
  private String eventName;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private String description;
  private String location;
  private boolean isPublic;
  private boolean isRecurring;
  private List<DayOfWeek> recurrenceDays;
  private boolean autoDecline;

  public CalendarEvent(String eventName, LocalDateTime startDateTime, LocalDateTime endDateTime,
                       String description, String location, boolean isPublic, boolean isRecurring,
                       List<DayOfWeek> recurrenceDays, boolean autoDecline) {
    this.eventName = eventName;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.description = description;
    this.location = location;
    this.isPublic = isPublic;
    this.isRecurring = isRecurring;
    this.recurrenceDays = recurrenceDays;
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

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public boolean isRecurring() {
    return isRecurring;
  }

  public List<DayOfWeek> getRecurrenceDays() {
    return recurrenceDays;
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

  public void setDescription(String description) {
    this.description = description;
  }

  public void setLocation(String location) {
    this.location = location;
  }
}
