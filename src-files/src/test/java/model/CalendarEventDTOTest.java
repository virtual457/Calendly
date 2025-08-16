package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test suite for the {@link CalendarEventDTO} class.
 * Tests the builder pattern implementation with validation.
 */
public class CalendarEventDTOTest {

  // Common test values
  private final String validName = "Meeting";
  private final LocalDateTime validStart = LocalDateTime.of(2024, 3, 15, 10, 0);
  private final LocalDateTime validEnd = LocalDateTime.of(2024, 3, 15, 11, 0);
  private final String validDescription = "Team sync-up";
  private final String validLocation = "Conference Room";
  private final List<DayOfWeek> validRecurrenceDays = Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);

  // Test successful creation of a basic event (non-recurring)
  @Test
  public void testBasicEventCreation() {
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
          .setEventName(validName)
          .setStartDateTime(validStart)
          .setEndDateTime(validEnd)
          .setEventDescription(validDescription)
          .setEventLocation(validLocation)
          .setPrivate(false)
          .setAutoDecline(true)
          .build();

    assertEquals(validName, eventDTO.getEventName());
    assertEquals(validStart, eventDTO.getStartDateTime());
    assertEquals(validEnd, eventDTO.getEndDateTime());
    assertEquals(validDescription, eventDTO.getEventDescription());
    assertEquals(validLocation, eventDTO.getEventLocation());
    assertEquals(Boolean.FALSE, eventDTO.isPrivate());
    assertEquals(Boolean.TRUE, eventDTO.isAutoDecline());
    assertEquals(Boolean.FALSE, eventDTO.isRecurring());
  }

  // Test successful creation of a recurring event with recurrence count
  @Test
  public void testRecurringEventWithCountCreation() {
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
          .setEventName(validName)
          .setStartDateTime(validStart)
          .setEndDateTime(validEnd)
          .setRecurring(true)
          .setRecurrenceDays(validRecurrenceDays)
          .setRecurrenceCount(5)
          .build();

    assertEquals(validName, eventDTO.getEventName());
    assertEquals(validStart, eventDTO.getStartDateTime());
    assertEquals(validEnd, eventDTO.getEndDateTime());
    assertEquals(Boolean.TRUE, eventDTO.isRecurring());
    assertEquals(validRecurrenceDays, eventDTO.getRecurrenceDays());
    assertEquals(Integer.valueOf(5), eventDTO.getRecurrenceCount());
  }

  // Test successful creation of a recurring event with end date
  @Test
  public void testRecurringEventWithEndDateCreation() {
    LocalDateTime recurrenceEnd = LocalDateTime.of(2024, 4, 15, 0, 0);

    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
          .setEventName(validName)
          .setStartDateTime(validStart)
          .setEndDateTime(validEnd)
          .setRecurring(true)
          .setRecurrenceDays(validRecurrenceDays)
          .setRecurrenceEndDate(recurrenceEnd)
          .build();

    assertEquals(validName, eventDTO.getEventName());
    assertEquals(validStart, eventDTO.getStartDateTime());
    assertEquals(validEnd, eventDTO.getEndDateTime());
    assertEquals(Boolean.TRUE, eventDTO.isRecurring());
    assertEquals(validRecurrenceDays, eventDTO.getRecurrenceDays());
    assertEquals(recurrenceEnd, eventDTO.getRecurrenceEndDate());
  }

  // Test validation failure: Empty event name
  @Test
  public void testEmptyEventNameValidation() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CalendarEventDTO.builder()
            .setEventName("")
            .setStartDateTime(validStart)
            .setEndDateTime(validEnd)
            .build();
    });

    assertEquals("Event name cannot be empty", exception.getMessage());
  }

  // Test validation failure: Null event name
  @Test
  public void testNullEventNameValidation() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CalendarEventDTO.builder()
            .setEventName(null)
            .setStartDateTime(validStart)
            .setEndDateTime(validEnd)
            .build();
    });

    assertEquals("Event name cannot be empty", exception.getMessage());
  }

  // Test validation failure: Missing start date/time
  @Test
  public void testMissingStartDateTimeValidation() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CalendarEventDTO.builder()
            .setEventName(validName)
            .setEndDateTime(validEnd)
            .build();
    });

    assertEquals("Start date/time cannot be null", exception.getMessage());
  }

  // Test validation failure: Missing end date/time
  @Test
  public void testMissingEndDateTimeValidation() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CalendarEventDTO.builder()
            .setEventName(validName)
            .setStartDateTime(validStart)
            .build();
    });

    assertEquals("End date/time cannot be null", exception.getMessage());
  }

  // Test validation failure: End date/time before start date/time
  @Test
  public void testEndBeforeStartValidation() {
    LocalDateTime invalidEnd = LocalDateTime.of(2024, 3, 15, 9, 0); // Before start time

    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CalendarEventDTO.builder()
            .setEventName(validName)
            .setStartDateTime(validStart)
            .setEndDateTime(invalidEnd)
            .build();
    });

    assertEquals("End date/time must be after start date/time", exception.getMessage());
  }

  // Test validation failure: End date/time equal to start date/time
  @Test
  public void testEndEqualToStartValidation() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CalendarEventDTO.builder()
            .setEventName(validName)
            .setStartDateTime(validStart)
            .setEndDateTime(validStart) // Same as start time
            .build();
    });

    assertEquals("End date/time must be after start date/time", exception.getMessage());
  }

  // Test validation failure: Recurring event without recurrence days
  @Test
  public void testRecurringWithoutDaysValidation() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CalendarEventDTO.builder()
            .setEventName(validName)
            .setStartDateTime(validStart)
            .setEndDateTime(validEnd)
            .setRecurring(true)
            .setRecurrenceCount(5)
            .build(); // Missing recurrence days
    });

    assertEquals("Recurring events must specify recurrence days", exception.getMessage());
  }

  // Test validation failure: Recurring event with empty recurrence days
  @Test
  public void testRecurringWithEmptyDaysValidation() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CalendarEventDTO.builder()
            .setEventName(validName)
            .setStartDateTime(validStart)
            .setEndDateTime(validEnd)
            .setRecurring(true)
            .setRecurrenceDays(Collections.emptyList())
            .setRecurrenceCount(5)
            .build();
    });

    assertEquals("Recurring events must specify recurrence days", exception.getMessage());
  }

  // Test validation failure: Recurring event with both count and end date
  @Test
  public void testRecurringWithBothCountAndEndDateValidation() {
    LocalDateTime recurrenceEnd = LocalDateTime.of(2024, 4, 15, 0, 0);

    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CalendarEventDTO.builder()
            .setEventName(validName)
            .setStartDateTime(validStart)
            .setEndDateTime(validEnd)
            .setRecurring(true)
            .setRecurrenceDays(validRecurrenceDays)
            .setRecurrenceCount(5)
            .setRecurrenceEndDate(recurrenceEnd) // Both count and end date
            .build();
    });

    assertEquals("Cannot specify both recurrence count and end date", exception.getMessage());
  }

  // Test validation failure: Recurring event without count or end date
  @Test
  public void testRecurringWithoutCountOrEndDateValidation() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CalendarEventDTO.builder()
            .setEventName(validName)
            .setStartDateTime(validStart)
            .setEndDateTime(validEnd)
            .setRecurring(true)
            .setRecurrenceDays(validRecurrenceDays)
            .build(); // Missing both count and end date
    });

    assertEquals("Must specify either recurrence count or end date", exception.getMessage());
  }

  // Test validation failure: Recurring event with negative count
  @Test
  public void testRecurringWithNegativeCountValidation() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CalendarEventDTO.builder()
            .setEventName(validName)
            .setStartDateTime(validStart)
            .setEndDateTime(validEnd)
            .setRecurring(true)
            .setRecurrenceDays(validRecurrenceDays)
            .setRecurrenceCount(-1) // Negative count
            .build();
    });

    assertEquals("Recurrence count must be positive", exception.getMessage());
  }

  // Test validation failure: Recurring event with zero count
  @Test
  public void testRecurringWithZeroCountValidation() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CalendarEventDTO.builder()
            .setEventName(validName)
            .setStartDateTime(validStart)
            .setEndDateTime(validEnd)
            .setRecurring(true)
            .setRecurrenceDays(validRecurrenceDays)
            .setRecurrenceCount(0) // Zero count
            .build();
    });

    assertEquals("Recurrence count must be positive", exception.getMessage());
  }

  // Test validation failure: Recurring event with end date before start date
  @Test
  public void testRecurringWithEndDateBeforeStartValidation() {
    LocalDateTime invalidRecurrenceEnd = LocalDateTime.of(2024, 2, 15, 0, 0); // Before start date

    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CalendarEventDTO.builder()
            .setEventName(validName)
            .setStartDateTime(validStart)
            .setEndDateTime(validEnd)
            .setRecurring(true)
            .setRecurrenceDays(validRecurrenceDays)
            .setRecurrenceEndDate(invalidRecurrenceEnd)
            .build();
    });

    assertEquals("Recurrence end date must be after start date/time", exception.getMessage());
  }

  // Test validation failure: Non-recurring event with recurrence days
  @Test
  public void testNonRecurringWithRecurrenceDaysValidation() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CalendarEventDTO.builder()
            .setEventName(validName)
            .setStartDateTime(validStart)
            .setEndDateTime(validEnd)
            .setRecurring(false)
            .setRecurrenceDays(validRecurrenceDays) // Recurrence days for non-recurring event
            .build();
    });

    assertEquals("Non-recurring events should not have recurrence parameters", exception.getMessage());
  }

  // Test validation failure: Non-recurring event with recurrence count
  @Test
  public void testNonRecurringWithRecurrenceCountValidation() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CalendarEventDTO.builder()
            .setEventName(validName)
            .setStartDateTime(validStart)
            .setEndDateTime(validEnd)
            .setRecurring(false)
            .setRecurrenceCount(5) // Recurrence count for non-recurring event
            .build();
    });

    assertEquals("Non-recurring events should not have recurrence parameters", exception.getMessage());
  }

  // Test validation failure: Non-recurring event with recurrence end date
  @Test
  public void testNonRecurringWithRecurrenceEndDateValidation() {
    LocalDateTime recurrenceEnd = LocalDateTime.of(2024, 4, 15, 0, 0);

    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CalendarEventDTO.builder()
            .setEventName(validName)
            .setStartDateTime(validStart)
            .setEndDateTime(validEnd)
            .setRecurring(false)
            .setRecurrenceEndDate(recurrenceEnd) // Recurrence end date for non-recurring event
            .build();
    });

    assertEquals("Non-recurring events should not have recurrence parameters", exception.getMessage());
  }
}