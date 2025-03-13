package model;

import static org.junit.Assert.*;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import model.CalendarEventDTO;
import model.CalendarModel;

public class CalendarModelTest {

  private CalendarModel calendarModel;

  @Before
  public void setUp() {
    calendarModel = new CalendarModel();
  }

  @After
  public void tearDown() {
    // Delete any export file if it exists
    File f = new File("test_export.csv");
    if (f.exists()) {
      f.delete();
    }
  }

  // ====================================================
  // AddEvent Tests – Valid Cases
  // ====================================================

  @Test
  public void testAddValidSingleEvent_MinimalOptions() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Single Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 13, 14, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 13, 15, 0))
            .build();
    assertTrue(calendarModel.addEvent(eventDTO));
    String expected = "- Single Event: 2025-03-13T14:00 to 2025-03-13T15:00\n";
    assertEquals(expected, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 13)));
  }

  @Test
  public void testAddValidSingleEvent_WithOptions() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Meeting")
            .setStartDateTime(LocalDateTime.of(2025, 4, 10, 9, 30))
            .setEndDateTime(LocalDateTime.of(2025, 4, 10, 10, 30))
            .setEventDescription("Discuss Q2 targets")
            .setEventLocation("Room 101")
            .setPrivate(false)
            .setAutoDecline(false)
            .build();
    assertTrue(calendarModel.addEvent(eventDTO));
    String expected = "- Meeting: 2025-04-10T09:30 to 2025-04-10T10:30 at Room 101\n";
    assertEquals(expected, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 4, 10)));
  }

  @Test
  public void testAddValidAllDayEvent() {
    // All-day event: start at midnight and end at 23:59:59
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Holiday")
            .setStartDateTime(LocalDateTime.of(2025, 5, 1, 0, 0))
            .setEndDateTime(LocalDateTime.of(2025, 5, 1, 23, 59, 59))
            .build();
    assertTrue(calendarModel.addEvent(eventDTO));
    String expected = "- Holiday: 2025-05-01T00:00 to 2025-05-01T23:59:59\n";
    assertEquals(expected, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 5, 1)));
  }

  @Test
  public void testAddValidRecurringEvent_FixedCount() {
    // Recurring event on Monday for 2 occurrences.
    // Assume 2025-03-10 is Monday.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Weekly Meeting")
            .setStartDateTime(LocalDateTime.of(2025, 3, 10, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 10, 11, 0))
            .setRecurring(true)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.MONDAY))
            .setRecurrenceCount(2)
            .setAutoDecline(false)
            .build();
    assertTrue(calendarModel.addEvent(eventDTO));
    String expectedDay1 = "- Weekly Meeting: 2025-03-10T10:00 to 2025-03-10T11:00\n";
    String expectedDay2 = "- Weekly Meeting: 2025-03-17T10:00 to 2025-03-17T11:00\n";
    assertEquals(expectedDay1, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 10)));
    assertEquals(expectedDay2, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 17)));
  }

  @Test
  public void testAddValidRecurringEvent_UntilEndDate() {
    // Recurring event on Friday until 2025-03-28.
    // Assume 2025-03-14 is Friday.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Friday Review")
            .setStartDateTime(LocalDateTime.of(2025, 3, 14, 9, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 14, 10, 0))
            .setRecurring(true)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.FRIDAY))
            .setRecurrenceEndDate(LocalDateTime.of(2025, 3, 28, 0, 0))
            .setAutoDecline(false)
            .build();
    assertTrue(calendarModel.addEvent(eventDTO));
    String expected1 = "- Friday Review: 2025-03-14T09:00 to 2025-03-14T10:00\n";
    String expected2 = "- Friday Review: 2025-03-21T09:00 to 2025-03-21T10:00\n";
    String expected3 = "- Friday Review: 2025-03-28T09:00 to 2025-03-28T10:00\n";
    assertEquals(expected1, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 14)));
    assertEquals(expected2, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 21)));
    assertEquals(expected3, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 28)));
  }

  @Test
  public void testAddValidRecurringAllDayEvent_FixedCount() {
    // Recurring all-day event on Tuesday for 2 occurrences.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Recurring All Day")
            .setStartDateTime(LocalDateTime.of(2025, 6, 3, 0, 0))  // Tuesday
            .setEndDateTime(LocalDateTime.of(2025, 6, 3, 23, 59, 59))
            .setRecurring(true)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.TUESDAY))
            .setRecurrenceCount(2)
            .setAutoDecline(false)
            .build();
    assertTrue(calendarModel.addEvent(eventDTO));
    String expected1 = "- Recurring All Day: 2025-06-03T00:00 to 2025-06-03T23:59:59\n";
    String expected2 = "- Recurring All Day: 2025-06-10T00:00 to 2025-06-10T23:59:59\n";
    assertEquals(expected1, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 6, 3)));
    assertEquals(expected2, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 6, 10)));
  }

  // ====================================================
  // AddEvent Tests – Error Cases
  // ====================================================

  @Test(expected = IllegalArgumentException.class)
  public void testAddEventWithoutEventName() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            // No event name provided.
            .setStartDateTime(LocalDateTime.of(2025, 3, 13, 14, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 13, 15, 0))
            .build();
    calendarModel.addEvent(eventDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddEventWithoutStartDateTime() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("No Start")
            // Missing start date/time.
            .setEndDateTime(LocalDateTime.of(2025, 3, 13, 15, 0))
            .build();
    calendarModel.addEvent(eventDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddEventWithoutEndDateTime() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("No End")
            .setStartDateTime(LocalDateTime.of(2025, 3, 13, 14, 0))
            // Missing end date/time.
            .build();
    calendarModel.addEvent(eventDTO);
  }

  @Test(expected = NullPointerException.class)
  public void testAddRecurringEventWithoutRecurrenceDays() {
    // When isRecurring is true, recurrenceDays should not be null.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("No Recurrence Days")
            .setStartDateTime(LocalDateTime.of(2025, 3, 12, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 12, 11, 0))
            .setRecurring(true)
            // Not setting recurrenceDays (remains null)
            .setRecurrenceCount(3)
            .build();
    calendarModel.addEvent(eventDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddRecurringEventWithZeroRecurrenceCount() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Zero Recurrence")
            .setStartDateTime(LocalDateTime.of(2025, 3, 12, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 12, 11, 0))
            .setRecurring(true)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.WEDNESDAY))
            .setRecurrenceCount(0)
            .build();
    calendarModel.addEvent(eventDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddRecurringEventWithDifferentDays() {
    // Recurring event with start and end on different days.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Different Day Recurrence")
            .setStartDateTime(LocalDateTime.of(2025, 3, 12, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 13, 10, 0))
            .setRecurring(true)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.WEDNESDAY))
            .setRecurrenceCount(3)
            .build();
    calendarModel.addEvent(eventDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddEventWithRecurrenceDataWhenNotRecurring_CountProvided() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Non-Recurring with Count")
            .setStartDateTime(LocalDateTime.of(2025, 3, 13, 14, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 13, 15, 0))
            .setRecurring(false)
            .setRecurrenceCount(3) // Should not be provided when not recurring.
            .build();
    calendarModel.addEvent(eventDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddEventWithRecurrenceDataWhenNotRecurring_EndDateProvided() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Non-Recurring with End Date")
            .setStartDateTime(LocalDateTime.of(2025, 3, 13, 14, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 13, 15, 0))
            .setRecurring(false)
            .setRecurrenceEndDate(LocalDateTime.of(2025, 3, 14, 0, 0))
            .build();
    calendarModel.addEvent(eventDTO);
  }

  // ====================================================
  // Edit Event Tests
  // ====================================================

  @Test
  public void testEditEventNameValid() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Original Name")
            .setStartDateTime(LocalDateTime.of(2025, 3, 20, 14, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 20, 15, 0))
            .build();
    calendarModel.addEvent(eventDTO);
    boolean result = calendarModel.editEvent("name", "Original Name",
            LocalDateTime.of(2025, 3, 20, 14, 0),
            LocalDateTime.of(2025, 3, 20, 15, 0),
            "Updated Name");
    assertTrue(result);
    String expected = "- Updated Name: 2025-03-20T14:00 to 2025-03-20T15:00\n";
    assertEquals(expected, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 20)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventUnsupportedProperty() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Test Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 20, 14, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 20, 15, 0))
            .build();
    calendarModel.addEvent(eventDTO);
    calendarModel.editEvent("description", "Test Event",
            LocalDateTime.of(2025, 3, 20, 14, 0),
            LocalDateTime.of(2025, 3, 20, 15, 0),
            "New Description");
  }

  @Test(expected = IllegalStateException.class)
  public void testEditEventNotFound() {
    calendarModel.editEvent("name", "Nonexistent",
            LocalDateTime.of(2025, 3, 20, 14, 0),
            LocalDateTime.of(2025, 3, 20, 15, 0),
            "New Name");
  }

  @Test
  public void testEditEventsNameValid() {
    // Add two events with the same name and start time.
    CalendarEventDTO eventDTO1 = CalendarEventDTO.builder()
            .setEventName("Group Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 21, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 21, 11, 0))
            .build();
    CalendarEventDTO eventDTO2 = CalendarEventDTO.builder()
            .setEventName("Group Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 21, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 21, 11, 0))
            .build();
    calendarModel.addEvent(eventDTO1);
    calendarModel.addEvent(eventDTO2);
    boolean result = calendarModel.editEvents("name", "Group Event",
            LocalDateTime.of(2025, 3, 21, 10, 0),
            "Group Event Updated");
    assertTrue(result);
    String expected = "- Group Event Updated: 2025-03-21T10:00 to 2025-03-21T11:00\n" +
            "- Group Event Updated: 2025-03-21T10:00 to 2025-03-21T11:00\n";
    assertEquals(expected, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 21)));
  }

  @Test(expected = IllegalStateException.class)
  public void testEditEventsNotFound() {
    calendarModel.editEvents("name", "Nonexistent", null, "New Name");
  }

  // ====================================================
  // Print Events Tests
  // ====================================================

  @Test
  public void testPrintEventsOnSpecificDateValid() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Print Test")
            .setStartDateTime(LocalDateTime.of(2025, 3, 22, 9, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 22, 10, 0))
            .build();
    calendarModel.addEvent(eventDTO);
    String expected = "- Print Test: 2025-03-22T09:00 to 2025-03-22T10:00\n";
    assertEquals(expected, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 22)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPrintEventsOnSpecificDate_NullDate() {
    calendarModel.printEventsOnSpecificDate(null);
  }

  @Test
  public void testPrintEventsInSpecificRangeValid() {
    CalendarEventDTO eventDTO1 = CalendarEventDTO.builder()
            .setEventName("Range Event 1")
            .setStartDateTime(LocalDateTime.of(2025, 3, 16, 9, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 16, 10, 0))
            .build();
    CalendarEventDTO eventDTO2 = CalendarEventDTO.builder()
            .setEventName("Range Event 2")
            .setStartDateTime(LocalDateTime.of(2025, 3, 16, 11, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 16, 12, 0))
            .build();
    calendarModel.addEvent(eventDTO1);
    calendarModel.addEvent(eventDTO2);
    String expected = "- Range Event 1: 2025-03-16T09:00 to 2025-03-16T10:00\n" +
            "- Range Event 2: 2025-03-16T11:00 to 2025-03-16T12:00\n";
    assertEquals(expected, calendarModel.printEventsInSpecificRange(
            LocalDateTime.of(2025, 3, 16, 8, 0),
            LocalDateTime.of(2025, 3, 16, 13, 0)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPrintEventsInSpecificRange_NullParameters() {
    calendarModel.printEventsInSpecificRange(null, LocalDateTime.now());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPrintEventsInSpecificRange_StartAfterEnd() {
    calendarModel.printEventsInSpecificRange(
            LocalDateTime.of(2025, 3, 16, 15, 0),
            LocalDateTime.of(2025, 3, 16, 14, 0));
  }

  // ====================================================
  // Export Events Tests
  // ====================================================


  // ====================================================
  // Show Status Tests
  // ====================================================

  @Test
  public void testShowStatusBusy() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Status Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 24, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 24, 11, 0))
            .build();
    calendarModel.addEvent(eventDTO);
    assertEquals("Busy", calendarModel.showStatus(LocalDateTime.of(2025, 3, 24, 10, 30)));
  }

  @Test
  public void testShowStatusAvailable() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Status Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 24, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 24, 11, 0))
            .build();
    calendarModel.addEvent(eventDTO);
    assertEquals("Available", calendarModel.showStatus(LocalDateTime.of(2025, 3, 24, 12, 0)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testShowStatus_NullDateTime() {
    calendarModel.showStatus(null);
  }
}
