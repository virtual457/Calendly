package model;

import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CalendarEventTest {
  private CalendarEvent event;
  private final String eventName = "Meeting";
  private final LocalDateTime startDateTime = LocalDateTime.of(2024, 3,
          15, 10, 0);
  private final LocalDateTime endDateTime = LocalDateTime.of(2024, 3,
          15, 11, 0);
  private final String description = "Team sync-up";
  private final String location = "Conference Room";
  private final boolean isPublic = true;
  private final boolean isRecurring = true;
  private final List<DayOfWeek> recurrenceDays = Arrays.asList(DayOfWeek.MONDAY,
          DayOfWeek.WEDNESDAY);
  private final boolean autoDecline = false;

  @Before
  public void setUp() {
    event = new CalendarEvent(eventName, startDateTime, endDateTime, description,
            location, isPublic, isRecurring, recurrenceDays, autoDecline);
  }

  @Test
  public void testGetters() {
    assertEquals(eventName, event.getEventName());
    assertEquals(startDateTime, event.getStartDateTime());
    assertEquals(endDateTime, event.getEndDateTime());
    assertEquals(description, event.getDescription());
    assertEquals(location, event.getLocation());
    assertEquals(isPublic, event.isPublic());
    assertEquals(isRecurring, event.isRecurring());
    assertEquals(recurrenceDays, event.getRecurrenceDays());
    assertEquals(autoDecline, event.isAutoDecline());
  }

  @Test
  public void testSetters() {
    event.setEventName("Updated Meeting");
    event.setStartDateTime(LocalDateTime.of(2024, 3, 16, 9, 0));
    event.setEndDateTime(LocalDateTime.of(2024, 3, 16, 10, 0));
    event.setDescription("Updated Description");
    event.setLocation("New Conference Room");
    event.setPublic(false);

    assertEquals("Updated Meeting", event.getEventName());
    assertEquals(LocalDateTime.of(2024, 3, 16, 9, 0),
            event.getStartDateTime());
    assertEquals(LocalDateTime.of(2024, 3, 16, 10, 0),
            event.getEndDateTime());
    assertEquals("Updated Description", event.getDescription());
    assertEquals("New Conference Room", event.getLocation());
    assertFalse(event.isPublic());
  }

  @Test
  public void testIsRecurring() {
    assertTrue(event.isRecurring());

    event = new CalendarEvent(eventName, startDateTime, endDateTime, description, location,
            isPublic, false, recurrenceDays, autoDecline);
    assertFalse(event.isRecurring());
  }

  @Test
  public void testIsAutoDecline() {
    assertFalse(event.isAutoDecline());

    event = new CalendarEvent(eventName, startDateTime, endDateTime, description, location,
            isPublic, isRecurring, recurrenceDays, true);
    assertTrue(event.isAutoDecline());
  }
}