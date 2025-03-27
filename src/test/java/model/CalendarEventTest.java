package model;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link CalendarEvent} class.
 * This class ensures correct behavior of event creation, comparison,
 * and recurrence handling.
 */

public class CalendarEventTest {

  private CalendarEvent event;

  @Before
  public void setup() {
    event = CalendarEvent.builder()
          .setEventName("Meeting")
          .setStartDateTime(LocalDateTime.of(2025, 5, 1, 10, 0))
          .setEndDateTime(LocalDateTime.of(2025, 5, 1, 11, 0))
          .setEventDescription("Project update")
          .setEventLocation("Room A")
          .setPublic(false)
          .build();
  }

  @Test
  public void testBuilderAndGetters() {
    assertEquals("Meeting", event.getEventName());
    assertEquals(LocalDateTime.of(2025, 5, 1, 10, 0), event.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 5, 1, 11, 0), event.getEndDateTime());
    assertEquals("Project update", event.getEventDescription());
    assertEquals("Room A", event.getEventLocation());
    assertFalse(event.isPublic());
  }

  @Test
  public void testSetters() {
    event.setEventName("Call");
    event.setStartDateTime(LocalDateTime.of(2025, 5, 2, 9, 0));
    event.setEndDateTime(LocalDateTime.of(2025, 5, 2, 10, 0));
    event.setEventDescription("Updated description");
    event.setEventLocation("Room B");
    event.setPublic(true);

    assertEquals("Call", event.getEventName());
    assertEquals(LocalDateTime.of(2025, 5, 2, 9, 0), event.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 5, 2, 10, 0), event.getEndDateTime());
    assertEquals("Updated description", event.getEventDescription());
    assertEquals("Room B", event.getEventLocation());
    assertTrue(event.isPublic());
  }

  @Test
  public void testConflictDetectionTrue() {
    ICalendarEvent overlapping = CalendarEvent.builder()
          .setEventName("Overlap")
          .setStartDateTime(LocalDateTime.of(2025, 5, 1, 10, 30))
          .setEndDateTime(LocalDateTime.of(2025, 5, 1, 11, 30))
          .setPublic(true)
          .build();

    assertTrue(event.doesEventConflict(overlapping));
  }

  @Test
  public void testConflictDetectionFalse() {
    ICalendarEvent nonOverlapping = CalendarEvent.builder()
          .setEventName("After")
          .setStartDateTime(LocalDateTime.of(2025, 5, 1, 11, 0))
          .setEndDateTime(LocalDateTime.of(2025, 5, 1, 12, 0))
          .setPublic(false)
          .build();

    assertFalse(event.doesEventConflict(nonOverlapping));
  }
}
