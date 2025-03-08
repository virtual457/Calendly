package model;


import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

class CalendarEvent {
  private String eventName;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private boolean isAutoDecline;
  private String seriesId;

  public CalendarEvent(String eventName, LocalDateTime startDateTime, LocalDateTime endDateTime,
                       String description, String location, boolean isPublic, boolean isRecurring,
                       List<DayOfWeek> recurrenceDays) {
    this.eventName = eventName;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;

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
}