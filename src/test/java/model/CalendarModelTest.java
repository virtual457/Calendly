package model;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


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
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
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
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
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
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
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
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
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
  public void testRecurringEventTerminationByFixedCount() {
    // Create a recurring event on Monday with a recurrence count of 3.
    // Assume 2025-03-10 is a Monday.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Termination Test")
            .setStartDateTime(LocalDateTime.of(2025, 3, 10, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 10, 11, 0))
            .setRecurring(true)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.MONDAY))
            .setRecurrenceCount(3)
            .setAutoDecline(false)
            .build();
    assertTrue(calendarModel.addEvent(eventDTO));

    // Expect events on 2025-03-10, 2025-03-17, and 2025-03-24.
    String expectedMonday1 = "- Termination Test: 2025-03-10T10:00 to 2025-03-10T11:00\n";
    String expectedMonday2 = "- Termination Test: 2025-03-17T10:00 to 2025-03-17T11:00\n";
    String expectedMonday3 = "- Termination Test: 2025-03-24T10:00 to 2025-03-24T11:00\n";

    // Verify the occurrence on each expected Monday.
    assertEquals(expectedMonday1, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 10)));
    assertEquals(expectedMonday2, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 17)));
    assertEquals(expectedMonday3, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 24)));

    // Verify that no event occurs on the following Monday (2025-03-31) since count=3 has been reached.
    assertEquals("", calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 31)));
  }

  @Test
  public void testAddValidRecurringEvent_UntilEndDate() {
    // Recurring event on Friday until 2025-03-28.
    // Assume 2025-03-14 is Friday.
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
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
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
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

  @Test
  public void testExportPrivateEventFlag() {
    // Create a non-recurring event with isPrivate true.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Private Test")
            .setStartDateTime(LocalDateTime.of(2025, 7, 1, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 7, 1, 11, 0))
            .setEventDescription("Test Desc")
            .setEventLocation("Test Loc")
            .setPrivate(true)   // Event is private.
            .setAutoDecline(false)
            .build();

    CalendarModel model = new CalendarModel();
    assertTrue(model.addEvent(eventDTO));

    // Export the event to a CSV file.
    String filePath = model.exportEvents("temp_export.csv");
    File file = new File(filePath);
    assertTrue(file.exists());

    // Read the content of the file.
    StringBuilder content = new StringBuilder();
    try (Scanner scanner = new Scanner(file)) {
      while (scanner.hasNextLine()) {
        content.append(scanner.nextLine()).append("\n");
      }
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    }

    // The exported CSV header is:
    // Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private
    // The event row should end with ,True indicating that the event is private.
    // For example, it might look like:
    // "Private Test",07/01/2025,10:00 AM,07/01/2025,11:00 AM,False,"Test Desc","Test Loc",True
    // We check that the line ends with ",True".
    String[] lines = content.toString().split("\n");
    assertTrue("CSV file should have at least two lines", lines.length >= 2);
    String eventRow = lines[1].trim();
    assertTrue("The event row should end with ',True'", eventRow.endsWith(",True"));

    // Clean up the temporary file.
    file.delete();
  }

  // ====================================================
  // AddEvent Tests – Error Cases
  // ====================================================

  @Test(expected = IllegalArgumentException.class)
  public void testAddEventWithoutEventName() {
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
            // No event name provided.
            .setStartDateTime(LocalDateTime.of(2025, 3, 13, 14, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 13, 15, 0))
            .build();
    calendarModel.addEvent(eventDTO);
  }

  @Test(expected = IllegalStateException.class)
  public void testSingleEventConflictWithAutoDecline() {
    // First event: occupies 14:00 to 15:00 on 2025-03-13.
    CalendarEventDTO eventDTO1 = CalendarEventDTO.builder()
            .setEventName("Conflict Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 13, 14, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 13, 15, 0))
            .setAutoDecline(true)
            .build();
    calendarModel.addEvent(eventDTO1);

    // Second event: overlaps with the first event (14:30 to 15:30).
    CalendarEventDTO eventDTO2 = CalendarEventDTO.builder()
            .setEventName("Conflict Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 13, 14, 30))
            .setEndDateTime(LocalDateTime.of(2025, 3, 13, 15, 30))
            .setAutoDecline(true)
            .build();

    // This call should throw IllegalStateException due to the conflict.
    calendarModel.addEvent(eventDTO2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventStartTimeAfterOrEqualToEndTime() {
    // Create an event with start time 14:00 and end time 15:00.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Test Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 20, 14, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 20, 15, 0))
            .build();
    calendarModel.addEvent(eventDTO);

    // Attempt to edit the event's start time to 15:00 (which is not before the current end time).
    calendarModel.editEvent("start", "Test Event",
            LocalDateTime.of(2025, 3, 20, 14, 0),
            LocalDateTime.of(2025, 3, 20, 15, 0),
            "2025-03-20T15:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddEventWithoutStartDateTime() {
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
            .setEventName("No Start")
            // Missing start date/time.
            .setEndDateTime(LocalDateTime.of(2025, 3, 13, 15, 0))
            .build();
    calendarModel.addEvent(eventDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEndTimeNotAfterStartTime() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Invalid Time Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 13, 15, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 13, 14, 0)) // End before start
            .build();
    calendarModel.addEvent(eventDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBothRecurrenceCountAndEndDateProvided() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Recurring Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 12, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 12, 11, 0))
            .setRecurring(true)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.WEDNESDAY))
            .setRecurrenceCount(5)  // Fixed count provided.
            .setRecurrenceEndDate(LocalDateTime.of(2025, 4, 12, 0, 0))  // End date also provided.
            .build();
    // This call should throw IllegalArgumentException.
    calendarModel.addEvent(eventDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddEventWithoutEndDateTime() {
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
            .setEventName("No End")
            .setStartDateTime(LocalDateTime.of(2025, 3, 13, 14, 0))
            // Missing end date/time.
            .build();
    calendarModel.addEvent(eventDTO);
  }

  @Test(expected = NullPointerException.class)
  public void testAddRecurringEventWithoutRecurrenceDays() {
    // When isRecurring is true, recurrenceDays should not be null.
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
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
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
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
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
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
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
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
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
            .setEventName("Non-Recurring with End Date")
            .setStartDateTime(LocalDateTime.of(2025, 3, 13, 14, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 13, 15, 0))
            .setRecurring(false)
            .setRecurrenceEndDate(LocalDateTime.of(2025, 3, 14, 0, 0))
            .build();
    calendarModel.addEvent(eventDTO);
  }

  @Test(expected = IllegalStateException.class)
  public void testRecurringEventConflictWithAutoDecline() {
    // First, add an existing event that occupies the time slot on a Monday.
    CalendarEventDTO existingEvent = CalendarEventDTO.builder()
            .setEventName("Existing Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 10, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 10, 11, 0))
            .build();
    calendarModel.addEvent(existingEvent);

    // Now create a recurring event that would conflict with the above event.
    // It recurs on Monday, with a fixed recurrence count.
    CalendarEventDTO recurringEvent = CalendarEventDTO.builder()
            .setEventName("Recurring Conflict")
            .setStartDateTime(LocalDateTime.of(2025, 3, 10, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 10, 11, 0))
            .setRecurring(true)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.MONDAY))
            .setRecurrenceCount(2)  // Will generate occurrence on 2025-03-10.
            .setAutoDecline(true)
            .build();

    // This call should throw an IllegalStateException because the recurring event's first occurrence conflicts.
    calendarModel.addEvent(recurringEvent);
  }

  // ====================================================
  // Edit Event Tests
  // ====================================================

  @Test
  public void testEditEventNameValid() {
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
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

  @Test
  public void testEditEventValidStart() {
    // Add event with start=14:00 and end=15:00.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Edit Start Test")
            .setStartDateTime(LocalDateTime.of(2025, 3, 20, 14, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 20, 15, 0))
            .build();
    calendarModel.addEvent(eventDTO);

    // Change start time to 13:30, which is valid (13:30 < 15:00)
    boolean result = calendarModel.editEvent("start", "Edit Start Test",
            LocalDateTime.of(2025, 3, 20, 14, 0),
            LocalDateTime.of(2025, 3, 20, 15, 0),
            "2025-03-20T13:30");
    assertTrue(result);

    // Expect the printed output to reflect the updated start time.
    String expected = "- Edit Start Test: 2025-03-20T13:30 to 2025-03-20T15:00\n";
    assertEquals(expected, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 20)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventInvalidStart() {
    // Add event with start=14:00 and end=15:00.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Invalid Start Test")
            .setStartDateTime(LocalDateTime.of(2025, 3, 20, 14, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 20, 15, 0))
            .build();
    calendarModel.addEvent(eventDTO);

    // Attempt to update start time to 15:00 (which is not strictly before end time).
    calendarModel.editEvent("start", "Invalid Start Test",
            LocalDateTime.of(2025, 3, 20, 14, 0),
            LocalDateTime.of(2025, 3, 20, 15, 0),
            "2025-03-20T15:00");
  }

  @Test
  public void testEditEventValidEnd() {
    // Add event with start=14:00 and end=15:00.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Edit End Test")
            .setStartDateTime(LocalDateTime.of(2025, 3, 20, 14, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 20, 15, 0))
            .build();
    calendarModel.addEvent(eventDTO);

    // Change end time to 15:30, which is valid (15:30 > 14:00).
    boolean result = calendarModel.editEvent("end", "Edit End Test",
            LocalDateTime.of(2025, 3, 20, 14, 0),
            LocalDateTime.of(2025, 3, 20, 15, 0),
            "2025-03-20T15:30");
    assertTrue(result);

    String expected = "- Edit End Test: 2025-03-20T14:00 to 2025-03-20T15:30\n";
    assertEquals(expected, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 20)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventInvalidEnd() {
    // Add event with start=14:00 and end=15:00.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Invalid End Test")
            .setStartDateTime(LocalDateTime.of(2025, 3, 20, 14, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 20, 15, 0))
            .build();
    calendarModel.addEvent(eventDTO);

    // Attempt to update end time to 14:00 (which is not after start time).
    calendarModel.editEvent("end", "Invalid End Test",
            LocalDateTime.of(2025, 3, 20, 14, 0),
            LocalDateTime.of(2025, 3, 20, 15, 0),
            "2025-03-20T14:00");
  }

  @Test
  public void testEditEventDescription() {
    // Add event without description.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Description Test")
            .setStartDateTime(LocalDateTime.of(2025, 3, 21, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 21, 11, 0))
            .build();
    calendarModel.addEvent(eventDTO);

    // Edit the description.
    boolean result = calendarModel.editEvent("description", "Description Test",
            LocalDateTime.of(2025, 3, 21, 10, 0),
            LocalDateTime.of(2025, 3, 21, 11, 0),
            "New Description");
    assertTrue(result);

    // We can't see the description in the printEventsOnSpecificDate method.
    // Instead, we use the exportEvents method and check that the CSV row contains the description.
    String filePath = calendarModel.exportEvents("temp_desc_export.csv");
    File file = new File(filePath);
    StringBuilder content = new StringBuilder();
    try (java.util.Scanner scanner = new java.util.Scanner(file)) {
      while (scanner.hasNextLine()) {
        content.append(scanner.nextLine()).append("\n");
      }
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    }
    // The CSV row for our event should include "New Description" in the Description column.
    // For simplicity, we'll assert that the exported content contains that substring.
    assertTrue(content.toString().contains("New Description"));
    file.delete();
  }

  @Test
  public void testEditEventLocation() {
    // Add event without location.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Location Test")
            .setStartDateTime(LocalDateTime.of(2025, 3, 21, 12, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 21, 13, 0))
            .build();
    calendarModel.addEvent(eventDTO);

    // Edit the location.
    boolean result = calendarModel.editEvent("location", "Location Test",
            LocalDateTime.of(2025, 3, 21, 12, 0),
            LocalDateTime.of(2025, 3, 21, 13, 0),
            "New Location");
    assertTrue(result);

    String expected = "- Location Test: 2025-03-21T12:00 to 2025-03-21T13:00 at New Location\n";
    assertEquals(expected, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 21)));
  }

  @Test
  public void testEditEventIsPublic() {
    // Add event with isPrivate false (i.e. public = true).
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Public Test")
            .setStartDateTime(LocalDateTime.of(2025, 3, 22, 12, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 22, 13, 0))
            .setPrivate(false)
            .build();
    calendarModel.addEvent(eventDTO);

    // Change the public flag by editing "ispublic" property.
    boolean result = calendarModel.editEvent("ispublic", "Public Test",
            LocalDateTime.of(2025, 3, 22, 12, 0),
            LocalDateTime.of(2025, 3, 22, 13, 0),
            "false"); // setting to false means event becomes not public (private).
    assertTrue(result);

    // To verify, we use the exportEvents method. The last column of the CSV row should now be "True" because isPublic is false.
    String filePath = calendarModel.exportEvents("temp_public_export.csv");
    File file = new File(filePath);
    StringBuilder content = new StringBuilder();
    try (java.util.Scanner scanner = new java.util.Scanner(file)) {
      while (scanner.hasNextLine()) {
        content.append(scanner.nextLine()).append("\n");
      }
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    }
    // The CSV row for the event should end with ,True
    String[] lines = content.toString().split("\n");
    assertTrue("CSV file should have at least two lines", lines.length >= 2);
    String eventRow = lines[1].trim();
    assertTrue("The event row should end with ',True'", eventRow.endsWith(",True"));
    file.delete();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventUnsupportedProperty() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Unsupported Prop Test")
            .setStartDateTime(LocalDateTime.of(2025, 3, 23, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 23, 11, 0))
            .build();
    calendarModel.addEvent(eventDTO);
    calendarModel.editEvent("foo", "Unsupported Prop Test",
            LocalDateTime.of(2025, 3, 23, 10, 0),
            LocalDateTime.of(2025, 3, 23, 11, 0),
            "Some Value");
  }

  @Test(expected = IllegalStateException.class)
  public void testEditEventNotFound() {
    calendarModel.editEvent("name", "Nonexistent Event",
            LocalDateTime.of(2025, 3, 23, 10, 0),
            LocalDateTime.of(2025, 3, 23, 11, 0),
            "New Name");
  }

  @Test
  public void testEditEventsName() {
    // Create two events with the same name and identical start times.
    LocalDateTime start = LocalDateTime.of(2025, 3, 21, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 21, 11, 0);

    CalendarEventDTO eventDTO1 = CalendarEventDTO.builder()
            .setEventName("Original")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .build();

    CalendarEventDTO eventDTO2 = CalendarEventDTO.builder()
            .setEventName("Original")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .build();

    // Add both events.
    calendarModel.addEvent(eventDTO1);
    calendarModel.addEvent(eventDTO2);

    // Edit events: change name from "Original" to "Updated" for events with the given start time.
    boolean result = calendarModel.editEvents("name", "Original", start, "Updated");
    assertTrue(result);

    // The printEventsOnSpecificDate method produces a string like:
    // "- Updated: 2025-03-21T10:00 to 2025-03-21T11:00\n"
    // Since there are two events, the expected output is both lines concatenated.
    String expected = "- Updated: 2025-03-21T10:00 to 2025-03-21T11:00\n" +
            "- Updated: 2025-03-21T10:00 to 2025-03-21T11:00\n";
    String actual = calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 21));
    assertEquals(expected, actual);
  }

  @Test
  public void testEditEventsStartValid() {
    // Create an event with start=14:00 and end=15:00.
    LocalDateTime start = LocalDateTime.of(2025, 3, 20, 14, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 20, 15, 0);
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Test Event")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .build();
    calendarModel.addEvent(eventDTO);

    // Update start to 13:30 (which is before current end)
    boolean result = calendarModel.editEvents("start", "Test Event", start, "2025-03-20T13:30");
    assertTrue(result);

    // Expected event now: start 13:30 to 15:00 on 2025-03-20.
    String expected = "- Test Event: 2025-03-20T13:30 to 2025-03-20T15:00\n";
    assertEquals(expected, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 20)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventsStartInvalid() {
    // Create an event with start=14:00 and end=15:00.
    LocalDateTime start = LocalDateTime.of(2025, 3, 20, 14, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 20, 15, 0);
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Test Event")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .build();
    calendarModel.addEvent(eventDTO);

    // Attempt to update start time to 15:00 (which is not before the end time)
    calendarModel.editEvents("start", "Test Event", start, "2025-03-20T15:00");
  }




  @Test(expected = IllegalArgumentException.class)
  public void testEditEventUnsupportedProperty2() {
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
            .setEventName("Test Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 20, 14, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 20, 15, 0))
            .build();
    calendarModel.addEvent(eventDTO);
    calendarModel.editEvent("Blast", "Test Event",
            LocalDateTime.of(2025, 3, 20, 14, 0),
            LocalDateTime.of(2025, 3, 20, 15, 0),
            "New Description");
  }

  @Test
  public void testEditEventSupportedProperty() {
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
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
  public void testEditEventNotFound2() {
    calendarModel.editEvent("name", "Nonexistent",
            LocalDateTime.of(2025, 3, 20, 14, 0),
            LocalDateTime.of(2025, 3, 20, 15, 0),
            "New Name");
  }

  @Test
  public void testEditEventsEndValid() {
    // Create an event with start=14:00 and end=15:00.
    LocalDateTime start = LocalDateTime.of(2025, 3, 20, 14, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 20, 15, 0);
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Test Event")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .build();
    calendarModel.addEvent(eventDTO);

    // Update end time to 15:30 (valid, since 15:30 > 14:00)
    boolean result = calendarModel.editEvents("end", "Test Event", start, "2025-03-20T15:30");
    assertTrue(result);

    // Expected event now: start 14:00 to 15:30.
    String expected = "- Test Event: 2025-03-20T14:00 to 2025-03-20T15:30\n";
    assertEquals(expected, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 20)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventsEndInvalid() {
    // Create an event with start=14:00 and end=15:00.
    LocalDateTime start = LocalDateTime.of(2025, 3, 20, 14, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 20, 15, 0);
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Test Event")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .build();
    calendarModel.addEvent(eventDTO);

    // Attempt to update end time to 14:00 (invalid because 14:00 is not after start time)
    calendarModel.editEvents("end", "Test Event", start, "2025-03-20T14:00");
  }

  @Test
  public void testEditEventsDescription() {
    // Create an event with no description.
    LocalDateTime start = LocalDateTime.of(2025, 3, 21, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 21, 11, 0);
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Desc Event")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .build();
    calendarModel.addEvent(eventDTO);

    // Edit the description.
    boolean result = calendarModel.editEvents("description", "Desc Event", start, "Updated Description");
    assertTrue(result);

    // Use exportEvents to verify description.
    String filePath = calendarModel.exportEvents("temp_export_desc.csv");
    File file = new File(filePath);
    StringBuilder content = new StringBuilder();
    try (Scanner scanner = new Scanner(file)) {
      while (scanner.hasNextLine()) {
        content.append(scanner.nextLine()).append("\n");
      }
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    }
    // Expected CSV row should include Updated Description in the Description column.
    // Construct expected row manually:
    // For simplicity, assume the CSV row for our event is:
    // "Desc Event",<startDate>,<startTime>,<endDate>,<endTime>,False,"Updated Description","",False
    // We can compute these based on known date/time format.
    String expectedStartDate = "03/21/2025";
    String expectedStartTime = "10:00 AM";
    String expectedEndDate = "03/21/2025";
    String expectedEndTime = "11:00 AM";
    String expectedRow = String.format("\"Desc Event\",%s,%s,%s,%s,False,\"Updated Description\",\"\",False",
            expectedStartDate, expectedStartTime, expectedEndDate, expectedEndTime);
    // The file should contain a header and one row.
    String expectedContent = "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
            expectedRow + "\n";
    assertEquals(expectedContent, content.toString());
    file.delete();
  }

  @Test
  public void testEditEventsLocation() {
    // Create an event with no location.
    LocalDateTime start = LocalDateTime.of(2025, 3, 21, 12, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 21, 13, 0);
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Loc Event")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .build();
    calendarModel.addEvent(eventDTO);

    // Edit the location.
    boolean result = calendarModel.editEvents("location", "Loc Event", start, "New Location");
    assertTrue(result);

    String expected = "- Loc Event: 2025-03-21T12:00 to 2025-03-21T13:00 at New Location\n";
    assertEquals(expected, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 21)));
  }

  @Test
  public void testEditEventsIsPublic() {
    // Create an event with isPrivate true (so isPublic false).
    LocalDateTime start = LocalDateTime.of(2025, 3, 22, 12, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 22, 13, 0);
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Public Flag Test")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setPrivate(true)  // means event is private (i.e. isPublic false)
            .build();
    calendarModel.addEvent(eventDTO);

    // Now update the "ispublic" property to "true" using editEvents.
    boolean result = calendarModel.editEvents("ispublic", "Public Flag Test", start, "true");
    assertTrue(result);

    // After update, isPublic becomes true, so in export, the last column (Private) is "False".
    String filePath = calendarModel.exportEvents("temp_export_pub.csv");
    File file = new File(filePath);
    StringBuilder content = new StringBuilder();
    try (Scanner scanner = new Scanner(file)) {
      while (scanner.hasNextLine()) {
        content.append(scanner.nextLine()).append("\n");
      }
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    }
    // Expected CSV row for our event:
    // "Public Flag Test",<startDate>,<startTime>,<endDate>,<endTime>,False,<desc>,<loc>,False
    String expectedStartDate = "03/22/2025";
    String expectedStartTime = "12:00 PM";
    String expectedEndDate = "03/22/2025";
    String expectedEndTime = "01:00 PM";
    String expectedRow = String.format("\"Public Flag Test\",%s,%s,%s,%s,False,\"\",\"\",False",
            expectedStartDate, expectedStartTime, expectedEndDate, expectedEndTime);
    String expectedContent = "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
            expectedRow + "\n";
    assertEquals(expectedContent, content.toString());
    file.delete();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventsUnsupportedProperty() {
    // Create an event.
    LocalDateTime start = LocalDateTime.of(2025, 3, 23, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 23, 11, 0);
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Unsupported Test")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .build();
    calendarModel.addEvent(eventDTO);

    // Attempt to edit with an unsupported property "foo"
    calendarModel.editEvents("foo", "Unsupported Test", start, "Some Value");
  }


  @Test
  public void testEditEventsNameValid() {
    // Add two events with the same name and start time.
    ICalendarEventDTO eventDTO1 = ICalendarEventDTO.builder()
            .setEventName("Group Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 21, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 21, 11, 0))
            .build();
    ICalendarEventDTO eventDTO2 = ICalendarEventDTO.builder()
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
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
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
  public void testPrintEventsInSpecificRange_WithEventsAndLocation() {
    // Create two events that fall within the range.
    CalendarEventDTO eventDTO1 = CalendarEventDTO.builder()
            .setEventName("Event 1")
            .setStartDateTime(LocalDateTime.of(2025, 3, 16, 9, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 16, 10, 0))
            .setEventLocation("Room A")
            .build();
    CalendarEventDTO eventDTO2 = CalendarEventDTO.builder()
            .setEventName("Event 2")
            .setStartDateTime(LocalDateTime.of(2025, 3, 16, 11, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 16, 12, 0))
            .setEventLocation("Room B")
            .build();

    calendarModel.addEvent(eventDTO1);
    calendarModel.addEvent(eventDTO2);

    // Define the range from 2025-03-16T08:00 to 2025-03-16T13:00.
    String expected = "- Event 1: 2025-03-16T09:00 to 2025-03-16T10:00 at Room A\n" +
            "- Event 2: 2025-03-16T11:00 to 2025-03-16T12:00 at Room B\n";
    String actual = calendarModel.printEventsInSpecificRange(
            LocalDateTime.of(2025, 3, 16, 8, 0),
            LocalDateTime.of(2025, 3, 16, 13, 0)
    );
    assertEquals(expected, actual);
  }

  @Test
  public void testPrintEventsInSpecificRange_EventWithoutLocation() {
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("No Loc Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 16, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 16, 11, 0))
            // No location provided.
            .build();
    calendarModel.addEvent(eventDTO);

    String expected = "- No Loc Event: 2025-03-16T10:00 to 2025-03-16T11:00\n";
    String actual = calendarModel.printEventsInSpecificRange(
            LocalDateTime.of(2025, 3, 16, 8, 0),
            LocalDateTime.of(2025, 3, 16, 12, 0)
    );
    assertEquals(expected, actual);
  }

  @Test
  public void testPrintEventsInSpecificRange_BoundaryCondition_LowerEqual() {
    // Create an event whose start time equals the lower bound.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Boundary Lower")
            .setStartDateTime(LocalDateTime.of(2025, 3, 16, 8, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 16, 9, 0))
            .setEventLocation("Room X")
            .build();
    calendarModel.addEvent(eventDTO);

    String expected = "- Boundary Lower: 2025-03-16T08:00 to 2025-03-16T09:00 at Room X\n";
    String actual = calendarModel.printEventsInSpecificRange(
            LocalDateTime.of(2025, 3, 16, 8, 0),
            LocalDateTime.of(2025, 3, 16, 10, 0)
    );
    assertEquals(expected, actual);
  }

  @Test
  public void testPrintEventsInSpecificRange_BoundaryCondition_UpperEqual() {
    // Create an event whose start time equals the upper bound.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Boundary Upper")
            .setStartDateTime(LocalDateTime.of(2025, 3, 16, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 16, 11, 0))
            .setEventLocation("Room Y")
            .build();
    calendarModel.addEvent(eventDTO);

    // If we call the range with toDateTime exactly 10:00, then the event start equals toDateTime.
    // According to our condition (equals(toDateTime) OR isBefore(toDateTime)), the event should be included.
    String expected = "- Boundary Upper: 2025-03-16T10:00 to 2025-03-16T11:00 at Room Y\n";
    String actual = calendarModel.printEventsInSpecificRange(
            LocalDateTime.of(2025, 3, 16, 9, 0),
            LocalDateTime.of(2025, 3, 16, 10, 0)
    );
    // Since our condition uses equals on the upper boundary, the event should be printed.
    assertEquals(expected, actual);
  }

  @Test
  public void testPrintEventsInSpecificRange_NoEvents() {
    // No events added.
    String actual = calendarModel.printEventsInSpecificRange(
            LocalDateTime.of(2025, 3, 16, 8, 0),
            LocalDateTime.of(2025, 3, 16, 12, 0)
    );
    assertEquals("", actual);
  }




  @Test
  public void testPrintEventsInSpecificRangeValid() {
    ICalendarEventDTO eventDTO1 = ICalendarEventDTO.builder()
            .setEventName("Range Event 1")
            .setStartDateTime(LocalDateTime.of(2025, 3, 16, 9, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 16, 10, 0))
            .build();
    ICalendarEventDTO eventDTO2 = ICalendarEventDTO.builder()
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

  @Test
  public void testExportEventsValid() throws Exception {
    // Create a single event.
    CalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Export Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 23, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 23, 11, 0))
            .setEventDescription("Final Exam")
            .setEventLocation("Room 305")
            .setPrivate(true)
            .setAutoDecline(false)
            .build();
    calendarModel.addEvent(eventDTO);

    // Call exportEvents.
    String exportPath = calendarModel.exportEvents("something.csv");
    assertNotNull(exportPath);
    File file = new File(exportPath);
    assertTrue(file.exists());

    // Read the file's content.
    String content = new String(Files.readAllBytes(Paths.get(exportPath)));

    // Expected output:
    // Header: Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private
    // For our event:
    // "Export Event",03/23/2025,10:00 AM,03/23/2025,11:00 AM,False,"Final Exam","Room 305",True
    // (with newlines at the end of each line)
    String expectedHeader = "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private";
    String expectedRow = "\"Export Event\",03/23/2025,10:00 AM,03/23/2025,11:00 AM,False,\"Final Exam\",\"Room 305\",True";
    String expectedContent = expectedHeader + "\n" + expectedRow + "\n";

    assertTrue(content.contains(expectedHeader));
    assertTrue(content.contains(expectedRow));
  }

  // ====================================================
  // Show Status Tests
  // ====================================================

  @Test
  public void testShowStatusBusy() {
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
            .setEventName("Status Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 24, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 24, 11, 0))
            .build();
    calendarModel.addEvent(eventDTO);
    assertEquals("Busy", calendarModel.showStatus(LocalDateTime.of(2025, 3, 24, 10, 30)));
  }

  @Test
  public void testAddNonConflictingEvents() {
    // First event: 10:00 to 11:00
    CalendarEventDTO event1 = CalendarEventDTO.builder()
            .setEventName("Event 1")
            .setStartDateTime(LocalDateTime.of(2025, 3, 20, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 20, 11, 0))
            .build();
    assertTrue(calendarModel.addEvent(event1));

    // Second event: 11:00 to 12:00 (no overlap with first event)
    CalendarEventDTO event2 = CalendarEventDTO.builder()
            .setEventName("Event 2")
            .setStartDateTime(LocalDateTime.of(2025, 3, 20, 11, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 20, 12, 0))
            .build();
    assertTrue(calendarModel.addEvent(event2));

    // Verify that both events are printed.
    String expected = "- Event 1: 2025-03-20T10:00 to 2025-03-20T11:00\n" +
            "- Event 2: 2025-03-20T11:00 to 2025-03-20T12:00\n";
    assertEquals(expected, calendarModel.printEventsOnSpecificDate(LocalDate.of(2025, 3, 20)));
  }

  @Test(expected = IllegalStateException.class)
  public void testAddConflictingEventAutoDecline() {
    // First event: 10:00 to 11:00
    CalendarEventDTO event1 = CalendarEventDTO.builder()
            .setEventName("Conflict Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 20, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 20, 11, 0))
            .setAutoDecline(true)
            .build();
    calendarModel.addEvent(event1);

    // Second event: overlapping time 10:30 to 11:30; autoDecline true causes conflict check to fail.
    CalendarEventDTO event2 = CalendarEventDTO.builder()
            .setEventName("Conflict Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 20, 10, 30))
            .setEndDateTime(LocalDateTime.of(2025, 3, 20, 11, 30))
            .setAutoDecline(true)
            .build();
    // This call should throw an IllegalStateException due to conflict.
    calendarModel.addEvent(event2);
  }

  @Test(expected = IllegalStateException.class)
  public void testAddEventConflictAutoDecline() {
    // Add an existing event from 10:00 to 11:00.
    CalendarEventDTO event1 = CalendarEventDTO.builder()
            .setEventName("Conflict Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 20, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 20, 11, 0))
            .setAutoDecline(true)
            .build();
    calendarModel.addEvent(event1);

    // Attempt to add a new event that overlaps with the existing one (10:30 to 11:30).
    CalendarEventDTO event2 = CalendarEventDTO.builder()
            .setEventName("Conflict Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 20, 10, 30))
            .setEndDateTime(LocalDateTime.of(2025, 3, 20, 11, 30))
            .setAutoDecline(true)
            .build();

    // Since auto-decline is true and there is an overlapping event,
    // this call should throw an IllegalStateException.
    calendarModel.addEvent(event2);
  }

  @Test
  public void testShowStatusAvailable() {
    ICalendarEventDTO eventDTO = ICalendarEventDTO.builder()
            .setEventName("Status Event")
            .setStartDateTime(LocalDateTime.of(2025, 3, 24, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 3, 24, 11, 0))
            .build();
    calendarModel.addEvent(eventDTO);
    assertEquals("Available", calendarModel.showStatus(LocalDateTime.of(2025, 3, 24, 12, 0)));
  }

  @Test
  public void testGetNextRecurrenceDate_EmptyRecurrenceDays() throws Exception {
    // When recurrenceDays is empty, the method should return currentDate.plusDays(1)
    LocalDate currentDate = LocalDate.of(2025, 3, 10); // Example date: Monday
    List<DayOfWeek> emptyList = new ArrayList<>();

    Method method = CalendarModel.class.getDeclaredMethod("getNextRecurrenceDate", LocalDate.class, List.class);
    method.setAccessible(true);
    LocalDate nextDate = (LocalDate) method.invoke(calendarModel, currentDate, emptyList);

    assertEquals(currentDate.plusDays(1), nextDate);
  }

  @Test
  public void testGetNextRecurrenceDate_NonEmptyRecurrenceDays_ImmediateMatch() throws Exception {
    // If recurrenceDays contains a day that is exactly currentDate.plusDays(1), expect that day.
    LocalDate currentDate = LocalDate.of(2025, 3, 10); // Monday
    // Set recurrenceDays to include Tuesday.
    List<DayOfWeek> recurrenceDays = Arrays.asList(DayOfWeek.TUESDAY);

    Method method = CalendarModel.class.getDeclaredMethod("getNextRecurrenceDate", LocalDate.class, List.class);
    method.setAccessible(true);
    LocalDate nextDate = (LocalDate) method.invoke(calendarModel, currentDate, recurrenceDays);

    // Expected: Tuesday (Monday plus 1 day)
    LocalDate expected = currentDate.plusDays(1);
    assertEquals(expected, nextDate);
  }

  @Test
  public void testGetNextRecurrenceDate_NonEmptyRecurrenceDays_NonImmediateMatch() throws Exception {
    // If recurrenceDays does not contain currentDate.plusDays(1), the method should iterate until it finds a match.
    // For example, currentDate is Monday and recurrenceDays only contains Wednesday.
    LocalDate currentDate = LocalDate.of(2025, 3, 10); // Monday
    List<DayOfWeek> recurrenceDays = Arrays.asList(DayOfWeek.WEDNESDAY);

    Method method = CalendarModel.class.getDeclaredMethod("getNextRecurrenceDate", LocalDate.class, List.class);
    method.setAccessible(true);
    LocalDate nextDate = (LocalDate) method.invoke(calendarModel, currentDate, recurrenceDays);

    // Expected: Wednesday, which is currentDate plus 2 days.
    LocalDate expected = currentDate.plusDays(2);
    assertEquals(expected, nextDate);
  }


  @Test(expected = IllegalArgumentException.class)
  public void testShowStatus_NullDateTime() {
    calendarModel.showStatus(null);
  }

  @Test
  public void testCreateInstance_ValidType() {
    ICalendarModel model = ICalendarModel.createInstance("listBased");
    assertNotNull(model);
    assertEquals("class model.CalendarModel",model.getClass().toString());
  }

  @Test
  public void testCreateInstance_InvalidType() {

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ICalendarModel.createInstance("invalidType");
    });

    String expectedMessage = "Invalid CalendarModel type.";
    String actualMessage = exception.getMessage();
    assertEquals(expectedMessage, actualMessage);
  }
}
