package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tests for the {@link ReadOnlyCalendarModel} class.
 */
public class ReadOnlyCalendarModelTest {

  @Test
  public void testReadOnlyCalendarModel_GetEventsInRange() {
    // Create a regular model first
    ICalendarModel originalModel = new CalendarModel();

    // Create a read-only wrapper for the model
    IReadOnlyCalendarModel readOnlyModel = new ReadOnlyCalendarModel(originalModel);

    // Create a calendar in the original model
    originalModel.createCalendar("TestCal", "America/New_York");

    // Add events to the calendar using the original model
    LocalDateTime event1Start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime event1End = LocalDateTime.of(2025, 5, 1, 11, 0);

    ICalendarEventDTO event1 = ICalendarEventDTO.builder()
        .setEventName("Meeting A")
        .setStartDateTime(event1Start)
        .setEndDateTime(event1End)
        .setAutoDecline(true)
        .setRecurring(false)
        .setEventDescription("First meeting")
        .setEventLocation("Room 101")
        .setPrivate(false)
        .build();

    originalModel.addEvent("TestCal", event1);

    // Add a second event
    LocalDateTime event2Start = LocalDateTime.of(2025, 5, 3, 14, 0);
    LocalDateTime event2End = LocalDateTime.of(2025, 5, 3, 15, 30);

    ICalendarEventDTO event2 = ICalendarEventDTO.builder()
        .setEventName("Workshop")
        .setStartDateTime(event2Start)
        .setEndDateTime(event2End)
        .setAutoDecline(true)
        .setRecurring(false)
        .setEventDescription("Technical workshop")
        .setEventLocation("Training Room")
        .setPrivate(true)
        .build();

    originalModel.addEvent("TestCal", event2);

    // Now test the read-only model's getEventsInRange

    // Case 1: Get all events in the range
    List<ICalendarEventDTO> allEvents = readOnlyModel.getEventsInRange(
        "TestCal",
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 5, 0, 0)
    );

    assertEquals(2, allEvents.size());

    // Verify event details are preserved
    boolean foundMeeting = false;
    boolean foundWorkshop = false;

    for (ICalendarEventDTO event : allEvents) {
      if ("Meeting A".equals(event.getEventName())) {
        foundMeeting = true;
        assertEquals(event1Start, event.getStartDateTime());
        assertEquals(event1End, event.getEndDateTime());
        assertEquals("Room 101", event.getEventLocation());
        assertFalse(event.isPrivate());
      } else if ("Workshop".equals(event.getEventName())) {
        foundWorkshop = true;
        assertEquals(event2Start, event.getStartDateTime());
        assertEquals(event2End, event.getEndDateTime());
        assertEquals("Technical workshop", event.getEventDescription());
        assertTrue(event.isPrivate());
      }
    }

    assertTrue(foundMeeting && foundWorkshop);

    // Case 2: Get events for a specific day (May 1)
    List<ICalendarEventDTO> day1Events = readOnlyModel.getEventsInRange(
        "TestCal",
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 1, 23, 59)
    );

    assertEquals(1, day1Events.size());
    assertEquals("Meeting A", day1Events.get(0).getEventName());

    // Case 3: Get events for a specific day (May 3)
    List<ICalendarEventDTO> day3Events = readOnlyModel.getEventsInRange(
        "TestCal",
        LocalDateTime.of(2025, 5, 3, 0, 0),
        LocalDateTime.of(2025, 5, 3, 23, 59)
    );

    assertEquals(1, day3Events.size());
    assertEquals("Workshop", day3Events.get(0).getEventName());

    // Case 4: Empty range (no events on May 2)
    List<ICalendarEventDTO> emptyDayEvents = readOnlyModel.getEventsInRange(
        "TestCal",
        LocalDateTime.of(2025, 5, 2, 0, 0),
        LocalDateTime.of(2025, 5, 2, 23, 59)
    );

    assertTrue(emptyDayEvents.isEmpty());
  }

  @Test
  public void testReadOnlyCalendarModel_GetEventsInRange_WithRecurringEvents() {
    // Create a regular model
    ICalendarModel originalModel = new CalendarModel();

    // Create a read-only wrapper
    IReadOnlyCalendarModel readOnlyModel = new ReadOnlyCalendarModel(originalModel);

    // Create a calendar
    originalModel.createCalendar("RecurringCal", "America/New_York");

    // Add a recurring event
    LocalDateTime recurringStart = LocalDateTime.of(2025, 6, 2, 9, 0); // Monday
    LocalDateTime recurringEnd = LocalDateTime.of(2025, 6, 2, 10, 0);

    ICalendarEventDTO recurringEvent = ICalendarEventDTO.builder()
        .setEventName("Weekly Status")
        .setStartDateTime(recurringStart)
        .setEndDateTime(recurringEnd)
        .setAutoDecline(true)
        .setRecurring(true)
        .setRecurrenceDays(List.of(DayOfWeek.MONDAY))
        .setRecurrenceCount(3) // 3 occurrences
        .setEventDescription("Team status meeting")
        .setEventLocation("Conference Room")
        .setPrivate(false)
        .build();

    originalModel.addEvent("RecurringCal", recurringEvent);

    // Now test the read-only model's getEventsInRange

    // Get all events for the month
    List<ICalendarEventDTO> allEvents = readOnlyModel.getEventsInRange(
        "RecurringCal",
        LocalDateTime.of(2025, 6, 1, 0, 0),
        LocalDateTime.of(2025, 6, 30, 23, 59)
    );

    // Should have 3 occurrences of the weekly meeting
    assertEquals(3, allEvents.size());

    // Verify events are on the expected dates (June 2, 9, 16)
    Set<LocalDate> meetingDates = allEvents.stream()
        .map(e -> e.getStartDateTime().toLocalDate())
        .collect(Collectors.toSet());

    assertTrue(meetingDates.contains(LocalDate.of(2025, 6, 2)));
    assertTrue(meetingDates.contains(LocalDate.of(2025, 6, 9)));
    assertTrue(meetingDates.contains(LocalDate.of(2025, 6, 16)));

    // All events should be on Mondays
    for (ICalendarEventDTO event : allEvents) {
      assertEquals(DayOfWeek.MONDAY, event.getStartDateTime().getDayOfWeek());
      assertEquals("Weekly Status", event.getEventName());
      assertEquals("Conference Room", event.getEventLocation());
    }

    // Get events for a specific week
    List<ICalendarEventDTO> secondWeekEvents = readOnlyModel.getEventsInRange(
        "RecurringCal",
        LocalDateTime.of(2025, 6, 8, 0, 0),
        LocalDateTime.of(2025, 6, 14, 23, 59)
    );

    assertEquals(1, secondWeekEvents.size());
    assertEquals(LocalDate.of(2025, 6, 9), secondWeekEvents.get(0).getStartDateTime().toLocalDate());
  }

  @Test
  public void testReadOnlyCalendarModel_GetEventsInRange_CalendarNotFound() {
    // Create a regular model
    ICalendarModel originalModel = new CalendarModel();

    // Create a read-only wrapper
    IReadOnlyCalendarModel readOnlyModel = new ReadOnlyCalendarModel(originalModel);

    try {
      readOnlyModel.getEventsInRange(
          "NonExistentCalendar",
          LocalDateTime.of(2025, 7, 1, 0, 0),
          LocalDateTime.of(2025, 7, 7, 23, 59)
      );
      fail("Expected IllegalArgumentException for non-existent calendar");
    } catch (IllegalArgumentException e) {
      // Expected exception
      assertTrue(e.getMessage().contains("Calendar not found"));
    }
  }

  @Test
  public void testReadOnlyCalendarModel_GetEventsInRange_InvalidRange() {
    // Create a regular model
    ICalendarModel originalModel = new CalendarModel();

    // Create a read-only wrapper
    IReadOnlyCalendarModel readOnlyModel = new ReadOnlyCalendarModel(originalModel);

    // Create a calendar
    originalModel.createCalendar("InvalidRangeCal", "America/New_York");

    try {
      readOnlyModel.getEventsInRange(
          "InvalidRangeCal",
          LocalDateTime.of(2025, 8, 7, 0, 0), // End date before start date
          LocalDateTime.of(2025, 8, 1, 0, 0)
      );
      fail("Expected IllegalArgumentException for invalid range");
    } catch (IllegalArgumentException e) {
      // Expected exception
      assertTrue(e.getMessage().contains("end date-time must not be before the start date-time"));
    }
  }

  @Test
  public void testReadOnlyCalendarModel_GetEventsInRange_NullParameters() {
    // Create a regular model
    ICalendarModel originalModel = new CalendarModel();

    // Create a read-only wrapper
    IReadOnlyCalendarModel readOnlyModel = new ReadOnlyCalendarModel(originalModel);

    // Create a calendar
    originalModel.createCalendar("NullParamCal", "America/New_York");

    try {
      readOnlyModel.getEventsInRange(
          "NullParamCal",
          null, // Null start time
          LocalDateTime.of(2025, 9, 1, 0, 0)
      );
      fail("Expected IllegalArgumentException for null start time");
    } catch (IllegalArgumentException e) {
      // Expected exception
      assertTrue(e.getMessage().contains("must be provided"));
    }

    try {
      readOnlyModel.getEventsInRange(
          "NullParamCal",
          LocalDateTime.of(2025, 9, 1, 0, 0),
          null // Null end time
      );
      fail("Expected IllegalArgumentException for null end time");
    } catch (IllegalArgumentException e) {
      // Expected exception
      assertTrue(e.getMessage().contains("must be provided"));
    }
  }

  @Test
  public void testReadOnlyCalendarModel_GetEventsInRange_EventProperties() {
    // Create a regular model
    ICalendarModel originalModel = new CalendarModel();

    // Create a read-only wrapper
    IReadOnlyCalendarModel readOnlyModel = new ReadOnlyCalendarModel(originalModel);

    // Create a calendar
    originalModel.createCalendar("PropertiesCal", "America/New_York");

    // Add an event with all properties set
    LocalDateTime eventStart = LocalDateTime.of(2025, 10, 15, 13, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2025, 10, 15, 14, 30);

    ICalendarEventDTO complexEvent = ICalendarEventDTO.builder()
        .setEventName("Complex Meeting")
        .setStartDateTime(eventStart)
        .setEndDateTime(eventEnd)
        .setAutoDecline(true)
        .setRecurring(false)
        .setEventDescription("Meeting with full details")
        .setEventLocation("Executive Suite")
        .setPrivate(true)
        .build();

    originalModel.addEvent("PropertiesCal", complexEvent);

    // Get the event through the read-only model
    List<ICalendarEventDTO> events = readOnlyModel.getEventsInRange(
        "PropertiesCal",
        LocalDateTime.of(2025, 10, 15, 0, 0),
        LocalDateTime.of(2025, 10, 15, 23, 59)
    );

    assertEquals(1, events.size());

    // Verify all properties are correctly preserved
    ICalendarEventDTO retrievedEvent = events.get(0);
    assertEquals("Complex Meeting", retrievedEvent.getEventName());
    assertEquals(eventStart, retrievedEvent.getStartDateTime());
    assertEquals(eventEnd, retrievedEvent.getEndDateTime());
    assertEquals("Meeting with full details", retrievedEvent.getEventDescription());
    assertEquals("Executive Suite", retrievedEvent.getEventLocation());
    assertTrue(retrievedEvent.isPrivate());
    assertFalse(retrievedEvent.isRecurring());
    assertTrue(retrievedEvent.isAutoDecline());
  }

  @Test
  public void testReadOnlyCalendarModel_GetEventsInSpecificDateTime() {
    // Create a regular model
    ICalendarModel originalModel = new CalendarModel();

    // Create a read-only wrapper
    IReadOnlyCalendarModel readOnlyModel = new ReadOnlyCalendarModel(originalModel);

    // Create a calendar
    originalModel.createCalendar("SpecificTimeCal", "America/New_York");

    // Add two events that overlap at a specific time
    LocalDateTime event1Start = LocalDateTime.of(2025, 11, 10, 10, 0);
    LocalDateTime event1End = LocalDateTime.of(2025, 11, 10, 11, 30);

    ICalendarEventDTO event1 = ICalendarEventDTO.builder()
        .setEventName("Morning Meeting")
        .setStartDateTime(event1Start)
        .setEndDateTime(event1End)
        .setAutoDecline(false) // Allow overlap
        .setRecurring(false)
        .setEventDescription("Team discussion")
        .setEventLocation("Room A")
        .setPrivate(false)
        .build();

    originalModel.addEvent("SpecificTimeCal", event1);

    LocalDateTime event2Start = LocalDateTime.of(2025, 11, 10, 11, 0); // Overlaps with event1
    LocalDateTime event2End = LocalDateTime.of(2025, 11, 10, 12, 0);

    ICalendarEventDTO event2 = ICalendarEventDTO.builder()
        .setEventName("Client Call")
        .setStartDateTime(event2Start)
        .setEndDateTime(event2End)
        .setAutoDecline(false) // Allow overlap
        .setRecurring(false)
        .setEventDescription("Call with client")
        .setEventLocation("Room B")
        .setPrivate(true)
        .build();

    originalModel.addEvent("SpecificTimeCal", event2);

    // Test getting events at specific times

    // Time 1: During first meeting only (10:30)
    List<ICalendarEventDTO> time1Events = readOnlyModel.getEventsInSpecificDateTime(
        "SpecificTimeCal",
        LocalDateTime.of(2025, 11, 10, 10, 30)
    );

    assertEquals(1, time1Events.size());
    assertEquals("Morning Meeting", time1Events.get(0).getEventName());

    // Time 2: During overlap (11:15)
    List<ICalendarEventDTO> time2Events = readOnlyModel.getEventsInSpecificDateTime(
        "SpecificTimeCal",
        LocalDateTime.of(2025, 11, 10, 11, 15)
    );

    assertEquals(2, time2Events.size());

    // Verify both events are returned
    boolean foundMeeting = false;
    boolean foundCall = false;

    for (ICalendarEventDTO event : time2Events) {
      if ("Morning Meeting".equals(event.getEventName())) {
        foundMeeting = true;
      } else if ("Client Call".equals(event.getEventName())) {
        foundCall = true;
      }
    }

    assertTrue(foundMeeting && foundCall);

    // Time 3: During no events (13:00)
    List<ICalendarEventDTO> time3Events = readOnlyModel.getEventsInSpecificDateTime(
        "SpecificTimeCal",
        LocalDateTime.of(2025, 11, 10, 13, 0)
    );

    assertTrue(time3Events.isEmpty());
  }

  @Test
  public void testReadOnlyCalendarModel_IsCalendarAvailable() {
    // Create a regular model
    ICalendarModel originalModel = new CalendarModel();

    // Create a read-only wrapper
    IReadOnlyCalendarModel readOnlyModel = new ReadOnlyCalendarModel(originalModel);

    // Create a calendar
    originalModel.createCalendar("AvailabilityCal", "America/New_York");

    // Initially, the calendar should be available on any date
    assertTrue(readOnlyModel.isCalendarAvailable("AvailabilityCal", LocalDate.of(2025, 12, 1)));

    // Add an event on December 1
    LocalDateTime eventStart = LocalDateTime.of(2025, 12, 1, 9, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2025, 12, 1, 10, 0);

    ICalendarEventDTO event = ICalendarEventDTO.builder()
        .setEventName("Busy Meeting")
        .setStartDateTime(eventStart)
        .setEndDateTime(eventEnd)
        .setAutoDecline(true)
        .setRecurring(false)
        .build();

    originalModel.addEvent("AvailabilityCal", event);

    // Now December 1 should be unavailable
    assertFalse(readOnlyModel.isCalendarAvailable("AvailabilityCal", LocalDate.of(2025, 12, 1)));

    // But December 2 should still be available
    assertTrue(readOnlyModel.isCalendarAvailable("AvailabilityCal", LocalDate.of(2025, 12, 2)));

    // Non-existent calendar should return false
    assertFalse(readOnlyModel.isCalendarAvailable("NonExistentCal", LocalDate.of(2025, 12, 1)));

    // Null date should check calendar existence
    assertTrue(readOnlyModel.isCalendarAvailable("AvailabilityCal", null));
    assertFalse(readOnlyModel.isCalendarAvailable("NonExistentCal", null));
  }

  @Test
  public void testReadOnlyCalendarModel_IsCalendarPresent() {
    // Create a regular model
    ICalendarModel originalModel = new CalendarModel();

    // Create a read-only wrapper
    IReadOnlyCalendarModel readOnlyModel = new ReadOnlyCalendarModel(originalModel);

    // Create a calendar
    originalModel.createCalendar("PresentCal", "America/New_York");

    // Verify calendar presence
    assertTrue(readOnlyModel.isCalendarPresent("PresentCal"));
    assertFalse(readOnlyModel.isCalendarPresent("AbsentCal"));

    // Case sensitivity check
    assertFalse(readOnlyModel.isCalendarPresent("presentcal"));
  }

  @Test
  public void testReadOnlyCalendarModel_GetCalendarNames() {
    // Create a regular model
    ICalendarModel originalModel = new CalendarModel();

    // Create a read-only wrapper
    IReadOnlyCalendarModel readOnlyModel = new ReadOnlyCalendarModel(originalModel);

    // Create several calendars
    originalModel.createCalendar("Work", "America/New_York");
    originalModel.createCalendar("Personal", "America/Los_Angeles");
    originalModel.createCalendar("Travel", "Europe/London");

    // Get the calendar names
    List<String> calendarNames = readOnlyModel.getCalendarNames();

    // Verify the list contains all calendar names
    assertEquals(3, calendarNames.size());
    assertTrue(calendarNames.contains("Work"));
    assertTrue(calendarNames.contains("Personal"));
    assertTrue(calendarNames.contains("Travel"));
  }
}