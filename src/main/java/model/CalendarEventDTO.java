package model;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

public class CalendarEventDTO {
  private String eventName;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private boolean isRecurring;
  private List<DayOfWeek> recurrenceDays;
  private int recurrenceCount;
  private LocalDateTime recurrenceEndDate;
  private boolean autoDecline;
  private String eventDescription;
  private String eventLocation;
  private boolean isPrivate;

  public CalendarEventDTO(String eventName, LocalDateTime startDateTime, LocalDateTime endDateTime,
                          boolean isRecurring, List<DayOfWeek> recurrenceDays, int recurrenceCount,
                          LocalDateTime recurrenceEndDate, boolean autoDecline, String eventDescription, String eventLocation, boolean isPrivate) {
    this.eventName = eventName;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.isRecurring = isRecurring;
    this.recurrenceDays = recurrenceDays;
    this.recurrenceCount = recurrenceCount;
    this.recurrenceEndDate = recurrenceEndDate;
    this.autoDecline = autoDecline;
    this.eventDescription = eventDescription;
    this.eventLocation = eventLocation;
    this.isPrivate = isPrivate;
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

  public List<DayOfWeek> getRecurrenceDays() {
    return recurrenceDays;
  }

  public int getRecurrenceCount() {
    return recurrenceCount;
  }

  public LocalDateTime getRecurrenceEndDate() {
    return recurrenceEndDate;
  }

  public boolean isAutoDecline() {
    return autoDecline;
  }

  public String getEventDescription() {
    return eventDescription;
  }
  public String getEventLocation() {
    return eventLocation;
  }
  public boolean isPrivate() {
    return isPrivate;
  }
}