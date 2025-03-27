package model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class CalendarEventDTOTest {
  private CalendarEventDTO eventDTO;
  private final String eventName = "Meeting";
  private final LocalDateTime startDateTime = LocalDateTime.of(2024, 3,
          15, 10, 0);
  private final LocalDateTime endDateTime = LocalDateTime.of(2024, 3,
          15, 11, 0);
  private final String description = "Team sync-up";
  private final String location = "Conference Room";
  private final Boolean isRecurring = Boolean.TRUE;
  private final List<DayOfWeek> recurrenceDays = Arrays.asList(DayOfWeek.MONDAY,
          DayOfWeek.WEDNESDAY);
  private final Integer recurrenceCount = Integer.valueOf(2);
  private final LocalDateTime recurrenceEndDate = LocalDateTime.of(2024,
          4, 1, 0, 0);
  private final Boolean autoDecline = Boolean.FALSE;
  private final Boolean isPrivate = Boolean.FALSE;

  @Before
  public void setUp() {
    eventDTO = CalendarEventDTO.builder()
            .setEventName(eventName)
            .setStartDateTime(startDateTime)
            .setEndDateTime(endDateTime)
            .setEventDescription(description)
            .setEventLocation(location)
            .setRecurring(isRecurring)
            .setRecurrenceDays(recurrenceDays)
            .setRecurrenceCount(recurrenceCount)
            .setRecurrenceEndDate(recurrenceEndDate)
            .setAutoDecline(autoDecline)
            .setPrivate(isPrivate)
            .build();
  }

  @Test
  public void testGetters() {
    assertEquals(eventName, eventDTO.getEventName());
    assertEquals(startDateTime, eventDTO.getStartDateTime());
    assertEquals(endDateTime, eventDTO.getEndDateTime());
    assertEquals(description, eventDTO.getEventDescription());
    assertEquals(location, eventDTO.getEventLocation());
    assertEquals(isRecurring, eventDTO.isRecurring());
    assertEquals(recurrenceDays, eventDTO.getRecurrenceDays());
    assertEquals(recurrenceCount, eventDTO.getRecurrenceCount());
    assertEquals(recurrenceEndDate, eventDTO.getRecurrenceEndDate());
    assertEquals(autoDecline, eventDTO.isAutoDecline());
    assertEquals(isPrivate, eventDTO.isPrivate());
  }
}
