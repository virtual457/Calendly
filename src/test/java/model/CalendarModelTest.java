package model;

import static org.junit.Assert.*;
        import static org.mockito.Mockito.*;

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
    // Clean up exported files if needed.
  }

  // --- Single Event Tests using real DTOs ---

  @Test
  public void testAddSingleEventValid() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Meeting")
            .setStartDateTime(LocalDateTime.of(2025, 3, 13, 14, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 13, 15, 0))
            .setEventDescription("Team meeting")
            .setEventLocation("Conference Room")
            .setPrivate(false)
            .setAutoDecline(false)
            .build();
    boolean result = calendarModel.addEvent(eventDTO);
    assertTrue(result);
    String output = calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 13));
    assertEquals("- Meeting: 2025-03-13T14:00 to 2025-03-13T15:00 at Conference Room\n", output);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddSingleEventInvalidTime() {
    // End time is before start time.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Invalid Meeting")
            .setStartDateTime(LocalDateTime.of(2025, 3, 13, 15, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 13, 14, 0))
            .build();
    calendarModel.addEvent(eventDTO);
  }

  @Test(expected = IllegalStateException.class)
  public void testAddSingleEventConflict() {
    // Create an event with autoDecline enabled, then add an overlapping event.
    CalendarEventDTO eventDTO1 = CalendarEventDTO.builder()
            .setEventName("Conflict Meeting")
            .setStartDateTime(LocalDateTime.of(2025, 3, 13, 14, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 13, 15, 0))
            .setAutoDecline(true)
            .build();
    calendarModel.addEvent(eventDTO1);

    CalendarEventDTO eventDTO2 = CalendarEventDTO.builder()
            .setEventName("Conflict Meeting")
            .setStartDateTime(LocalDateTime.of(2025, 3, 13, 14, 30))
            .setEndDateTime(LocalDateTime.of(2025, 3, 13, 15, 30))
            .setAutoDecline(true)
            .build();
    calendarModel.addEvent(eventDTO2); // Expect exception.
  }

  // --- Recurring Event Tests using real DTOs ---

  @Test
  public void testAddRecurringEventFixedCountValid() {
    // Recurring event: every Wednesday for 3 occurrences.
    LocalDateTime start = LocalDateTime.of(2025, 3, 12, 10, 0); // Assume 2025-03-12 is Wednesday.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Weekly Standup")
            .setStartDateTime(start)
            .setEndDateTime(LocalDateTime.of(2025, 3, 12, 11, 0))
            .setRecurring(true)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.WEDNESDAY))
            .setRecurrenceCount(3)
            .setAutoDecline(false)
            .build();
    boolean result = calendarModel.addEvent(eventDTO);
    assertTrue(result);
    int occurrenceCount = 0;
    // Check over 28 days to count occurrences.
    for (int i = 0; i < 28; i++) {
      LocalDate date = LocalDate.of(2025, 3, 12).plusDays(i);
      String output = calendarModel.printEventsOnSpecificDate(date);
      if (output.contains("Weekly Standup")) {
        occurrenceCount++;
      }
    }
    assertEquals(3, occurrenceCount);
  }

  @Test
  public void testAddRecurringEventUntilValid() {
    // Recurring event: every Friday until 2025-04-04.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Friday Review")
            .setStartDateTime(LocalDateTime.of(2025, 3, 14, 9, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 14, 10, 0))
            .setRecurring(true)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.FRIDAY))
            .setRecurrenceEndDate(LocalDateTime.of(2025, 4, 4, 0, 0))
            .setAutoDecline(false)
            .build();
    boolean result = calendarModel.addEvent(eventDTO);
    assertTrue(result);
    int count = 0;
    LocalDate date = LocalDate.of(2025, 3, 14);
    LocalDate end = LocalDate.of(2025, 4, 4);
    while (!date.isAfter(end)) {
      if (date.getDayOfWeek() == DayOfWeek.FRIDAY) {
        String output = calendarModel.printEventsOnSpecificDate(date);
        if (output.contains("Friday Review")) {
          count++;
        }
      }
      date = date.plusDays(1);
    }
    assertEquals(4, count);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddRecurringEventDifferentDayError() {
    // Recurring event with start and end on different days.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Invalid Recurring")
            .setStartDateTime(LocalDateTime.of(2025, 3, 12, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 13, 11, 0))
            .setRecurring(true)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.WEDNESDAY))
            .setRecurrenceCount(3)
            .build();
    calendarModel.addEvent(eventDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddRecurringEventBothCountAndEndDateError() {
    // Both recurrence count and recurrence end date provided.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Invalid Recurrence")
            .setStartDateTime(LocalDateTime.of(2025, 3, 12, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 12, 11, 0))
            .setRecurring(true)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.WEDNESDAY))
            .setRecurrenceCount(3)
            .setRecurrenceEndDate(LocalDateTime.of(2025, 4, 12, 0, 0))
            .build();
    calendarModel.addEvent(eventDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddRecurringEventNoRecurrenceDefinedError() {
    // Neither recurrence count nor recurrence end date provided.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("No Recurrence Defined")
            .setStartDateTime(LocalDateTime.of(2025, 3, 12, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 12, 11, 0))
            .setRecurring(true)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.WEDNESDAY))
            .build();
    calendarModel.addEvent(eventDTO);
  }

  // --- Mock-based Tests using Mockito ---

  @Test
  public void testAddSingleEventUsingMock() {
    CalendarEventDTO mockDTO = mock(CalendarEventDTO.class);
    LocalDateTime start = LocalDateTime.of(2025, 3, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 15, 11, 0);

    when(mockDTO.getStartDateTime()).thenReturn(start);
    when(mockDTO.getEndDateTime()).thenReturn(end);
    when(mockDTO.isRecurring()).thenReturn(false);
    when(mockDTO.isAutoDecline()).thenReturn(false);
    when(mockDTO.getEventName()).thenReturn("Mock Event");
    when(mockDTO.getEventDescription()).thenReturn("Test Description");
    when(mockDTO.getEventLocation()).thenReturn("Test Location");
    when(mockDTO.isPrivate()).thenReturn(false);


    boolean result = calendarModel.addEvent(mockDTO);
    assertTrue(result);
    String output = calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 15));
    assertTrue(output.contains("Mock Event"));
    assertTrue(output.contains("Test Location"));
    assertTrue(output.contains("Test Description"));

    verify(mockDTO, atLeastOnce()).getStartDateTime();
    verify(mockDTO, atLeastOnce()).getEndDateTime();
    verify(mockDTO, atLeastOnce()).getEventName();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddSingleEventInvalidTimeWithMock() {
    CalendarEventDTO mockDTO = mock(CalendarEventDTO.class);
    LocalDateTime start = LocalDateTime.of(2025, 3, 15, 12, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 15, 11, 0);

    when(mockDTO.getStartDateTime()).thenReturn(start);
    when(mockDTO.getEndDateTime()).thenReturn(end);
    when(mockDTO.isRecurring()).thenReturn(false);

    calendarModel.addEvent(mockDTO);
  }

  @Test
  public void testAddRecurringEventFixedCountUsingMock() {
    CalendarEventDTO mockDTO = mock(CalendarEventDTO.class);
    LocalDateTime start = LocalDateTime.of(2025, 3, 12, 9, 0); // Wednesday
    LocalDateTime end = LocalDateTime.of(2025, 3, 12, 10, 0);

    when(mockDTO.getStartDateTime()).thenReturn(start);
    when(mockDTO.getEndDateTime()).thenReturn(end);
    when(mockDTO.isRecurring()).thenReturn(true);
    when(mockDTO.getRecurrenceDays()).thenReturn(Arrays.asList(DayOfWeek.WEDNESDAY));
    when(mockDTO.getRecurrenceCount()).thenReturn(3);
    when(mockDTO.getRecurrenceEndDate()).thenReturn(null);
    when(mockDTO.isAutoDecline()).thenReturn(false);
    when(mockDTO.getEventName()).thenReturn("Mock Recurring");
    when(mockDTO.getEventDescription()).thenReturn("Recurring Description");
    when(mockDTO.getEventLocation()).thenReturn("Recurring Location");
    when(mockDTO.isPrivate()).thenReturn(true);

    boolean result = calendarModel.addEvent(mockDTO);
    assertTrue(result);

    int occurrenceCount = 0;
    LocalDate current = start.toLocalDate();
    for (int i = 0; i < 28; i++) {
      String output = calendarModel.printEventsOnSpecificDate(current);
      if (output.contains("Mock Recurring")) {
        occurrenceCount++;
      }
      current = current.plusDays(1);
    }
    assertEquals(3, occurrenceCount);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddRecurringEventBothCountAndEndDateProvidedWithMock() {
    CalendarEventDTO mockDTO = mock(CalendarEventDTO.class);
    LocalDateTime start = LocalDateTime.of(2025, 3, 12, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 12, 10, 0);

    when(mockDTO.getStartDateTime()).thenReturn(start);
    when(mockDTO.getEndDateTime()).thenReturn(end);
    when(mockDTO.isRecurring()).thenReturn(true);
    when(mockDTO.getRecurrenceDays()).thenReturn(Arrays.asList(DayOfWeek.WEDNESDAY));
    when(mockDTO.getRecurrenceCount()).thenReturn(3);
    when(mockDTO.getRecurrenceEndDate()).thenReturn(LocalDateTime.of(2025, 4, 12, 0, 0));

    calendarModel.addEvent(mockDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddRecurringEventNoRecurrenceDefinitionWithMock() {
    CalendarEventDTO mockDTO = mock(CalendarEventDTO.class);
    LocalDateTime start = LocalDateTime.of(2025, 3, 12, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 12, 10, 0);

    when(mockDTO.getStartDateTime()).thenReturn(start);
    when(mockDTO.getEndDateTime()).thenReturn(end);
    when(mockDTO.isRecurring()).thenReturn(true);
    when(mockDTO.getRecurrenceDays()).thenReturn(Arrays.asList(DayOfWeek.WEDNESDAY));
    when(mockDTO.getRecurrenceCount()).thenReturn(0);
    when(mockDTO.getRecurrenceEndDate()).thenReturn(null);

    calendarModel.addEvent(mockDTO);
  }

  // --- Tests for Printing, Exporting, and Status (real DTOs) ---

  @Test
  public void testPrintEventsOnSpecificDate() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Print Test")
            .setStartDateTime(LocalDateTime.of(2025, 3, 22, 9, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 22, 10, 0))
            .build();
    calendarModel.addEvent(eventDTO);
    String output = calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 22));
    assertTrue(output.contains("Print Test"));
  }

  @Test
  public void testPrintEventsInSpecificRange() {
    CalendarEventDTO eventDTO1 = CalendarEventDTO.builder()
            .setEventName("Event 1")
            .setStartDateTime(LocalDateTime.of(2025, 3, 16, 9, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 16, 10, 0))
            .build();
    CalendarEventDTO eventDTO2 = CalendarEventDTO.builder()
            .setEventName("Event 2")
            .setStartDateTime(LocalDateTime.of(2025, 3, 16, 11, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 16, 12, 0))
            .build();
    calendarModel.addEvent(eventDTO1);
    calendarModel.addEvent(eventDTO2);
    String output = calendarModel.printEventsInSpecificRange(
            LocalDateTime.of(2025, 3, 16, 8, 0),
            LocalDateTime.of(2025, 3, 16, 13, 0)
    );
    assertTrue(output.contains("Event 1"));
    assertTrue(output.contains("Event 2"));
  }

  @Test
  public void testExportEvents() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Export Test")
            .setStartDateTime(LocalDateTime.of(2025, 3, 23, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 23, 11, 0))
            .setEventDescription("Export Desc")
            .setEventLocation("Export Loc")
            .setPrivate(true)
            .build();
    calendarModel.addEvent(eventDTO);
    String filePath = calendarModel.exportEvents("test_export.csv");
    assertNotNull(filePath);
    File file = new File(filePath);
    assertTrue(file.exists());
    file.delete(); // Clean up
  }

  @Test
  public void testShowStatusBusy() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Status Test")
            .setStartDateTime(LocalDateTime.of(2025, 3, 24, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 24, 11, 0))
            .build();
    calendarModel.addEvent(eventDTO);
    String status = calendarModel.showStatus(LocalDateTime.of(2025, 3, 24, 10, 30));
    assertEquals("Busy", status);
  }

  @Test
  public void testShowStatusAvailable() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Status Test")
            .setStartDateTime(LocalDateTime.of(2025, 3, 24, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 24, 11, 0))
            .build();
    calendarModel.addEvent(eventDTO);
    String status = calendarModel.showStatus(LocalDateTime.of(2025, 3, 24, 12, 0));
    assertEquals("Available", status);
  }
}
