package model;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class CalendarEventDTO {
  private final String eventName;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final boolean isRecurring;
  private final List<DayOfWeek> recurrenceDays;
  private final int recurrenceCount;
  private final LocalDateTime recurrenceEndDate;
  private final boolean autoDecline;

  private CalendarEventDTO(CalendarEventDTOBuilder builder) {
    this.eventName = builder.eventName;
    this.startDateTime = builder.startDateTime;
    this.endDateTime = builder.endDateTime;
    this.isRecurring = builder.isRecurring;
    this.recurrenceDays = builder.recurrenceDays;
    this.recurrenceCount = builder.recurrenceCount;
    this.recurrenceEndDate = builder.recurrenceEndDate;
    this.autoDecline = builder.autoDecline;
  }

  public static class CalendarEventDTOBuilder {
    private String eventName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private boolean isRecurring = false;
    private List<DayOfWeek> recurrenceDays = new ArrayList<>();
    private int recurrenceCount = 0;
    private LocalDateTime recurrenceEndDate = null;
    private boolean autoDecline = false;

    public CalendarEventDTOBuilder eventName(String eventName) {
      this.eventName = eventName;
      return this;
    }

    public CalendarEventDTOBuilder startDateTime(LocalDateTime startDateTime) {
      this.startDateTime = startDateTime;
      return this;
    }

    public CalendarEventDTOBuilder endDateTime(LocalDateTime endDateTime) {
      this.endDateTime = endDateTime;
      return this;
    }

    public CalendarEventDTOBuilder recurring(boolean isRecurring, List<DayOfWeek> recurrenceDays) {
      this.isRecurring = isRecurring;
      this.recurrenceDays = recurrenceDays;
      return this;
    }

    public CalendarEventDTOBuilder recurrenceCount(int recurrenceCount) {
      this.recurrenceCount = recurrenceCount;
      return this;
    }

    public CalendarEventDTOBuilder recurrenceEndDate(LocalDateTime recurrenceEndDate) {
      this.recurrenceEndDate = recurrenceEndDate;
      return this;
    }

    public CalendarEventDTOBuilder autoDecline(boolean autoDecline) {
      this.autoDecline = autoDecline;
      return this;
    }

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
}
