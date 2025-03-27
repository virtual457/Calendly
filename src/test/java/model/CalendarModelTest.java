package model;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

public class CalendarModelTest {

  private CalendarModel model;

  @Before
  public void setUp() {
    model = new CalendarModel();
  }

  // ===== Calendar Creation Tests =====

  @Test
  public void testCreateCalendarValid() {
    assertTrue(model.createCalendar("Home", "America/New_York"));
    assertTrue(model.isCalendarPresent("Home"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarInvalidTimezone() {
    model.createCalendar("Office", "Invalid/Timezone");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarDuplicateName() {
    model.createCalendar("Home", "America/New_York");
    model.createCalendar("home", "America/New_York"); // Duplicate by case-insensitive check.
  }

  // ===== Add Event Tests =====

  @Test
  public void testAddNonRecurringEvent() {
    model.createCalendar("Work", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 3, 18, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 18, 11, 0);

    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Meeting")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Team meeting")
            .setEventLocation("Conference Room")
            .setPrivate(false)
            .build();

    assertTrue(model.addEvent("Work", eventDTO));

    List<ICalendarEventDTO> events = model.getEventsInRange("Work", start.minusMinutes(1), end.plusMinutes(1));
    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getEventName());
  }

  @Test
  public void testAddRecurringEventWithCount() {
    model.createCalendar("Work", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 4, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 4, 1, 10, 0);

    // Create a recurring event on Tuesday with recurrence count = 3.
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Standup")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(true)
            .setRecurrenceCount(3)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.TUESDAY))
            .setEventDescription("Weekly standup")
            .setEventLocation("Room A")
            .setPrivate(false)
            .build();

    assertTrue(model.addEvent("Work", eventDTO));

    // Verify that exactly 3 occurrences were added.
    List<ICalendarEventDTO> events = model.getEventsInRange("Work", start.minusDays(1), end.plusDays(21));
    assertEquals(3, events.size());
  }

  @Test(expected = IllegalStateException.class)
  public void testAddEvent_WithAutoDeclineAndConflict_ShouldThrowException() {
    model.createCalendar("MyCal", "America/New_York");

    // Add existing event
    LocalDateTime existingStart = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime existingEnd = existingStart.plusHours(1);
    ICalendarEventDTO existing = CalendarEventDTO.builder()
            .setEventName("Existing")
            .setStartDateTime(existingStart)
            .setEndDateTime(existingEnd)
            .setAutoDecline(true)
            .setRecurring(false)
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("MyCal", existing));

    // Add conflicting event with autoDecline = true
    ICalendarEventDTO conflicting = CalendarEventDTO.builder()
            .setEventName("Conflict")
            .setStartDateTime(existingStart.plusMinutes(30)) // Overlaps with existing
            .setEndDateTime(existingEnd.plusMinutes(30))
            .setAutoDecline(true)
            .setRecurring(false)
            .setPrivate(false)
            .build();

    model.addEvent("MyCal", conflicting); // should throw exception
  }

  @Test
  public void testAddEvent_WithConflictButNoAutoDecline_ShouldNotSucceed() {
    model.createCalendar("MyCal", "America/New_York");

    // Add existing event
    LocalDateTime existingStart = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime existingEnd = existingStart.plusHours(1);
    ICalendarEventDTO existing = CalendarEventDTO.builder()
            .setEventName("Existing")
            .setStartDateTime(existingStart)
            .setEndDateTime(existingEnd)
            .setAutoDecline(true)
            .setRecurring(false)
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("MyCal", existing));

    // Add conflicting event but with autoDecline = false
    ICalendarEventDTO conflicting = CalendarEventDTO.builder()
            .setEventName("Conflict")
            .setStartDateTime(existingStart.plusMinutes(30))
            .setEndDateTime(existingEnd.plusMinutes(30))
            .setAutoDecline(false)  // allow overlapping
            .setRecurring(false)
            .setPrivate(false)
            .build();
    try {
      assertTrue(model.addEvent("MyCal", conflicting));
      fail("dint throw illegalstate exception");
    }catch (IllegalStateException e){
      assertEquals("Conflict detected, event not created", e.getMessage());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddNonRecurringEventWithRecurrenceInfo() {
    model.createCalendar("Work", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 3, 18, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 18, 11, 0);

    // Non-recurring event but with recurrence info provided.
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Meeting")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setRecurrenceCount(2)  // Should not be provided for non-recurring event.
            .setEventDescription("Team meeting")
            .setEventLocation("Conference Room")
            .setPrivate(false)
            .build();

    model.addEvent("Work", eventDTO);
  }

  // ===== Edit Events Tests =====

  @Test
  public void testEditSingleEvent() {
    model.createCalendar("Work", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 4, 10, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 4, 10, 10, 0);

    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Standup")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Daily standup")
            .setEventLocation("Room A")
            .setPrivate(true)
            .build();

    assertTrue(model.addEvent("Work", eventDTO));

    // Edit event's location.
    boolean edited = model.editEvents("Work", "location", "Standup", start, "Room B", false);
    assertTrue(edited);
    List<ICalendarEventDTO> events = model.getEventsInRange("Work", start.minusMinutes(1), end.plusMinutes(1));
    assertEquals("Room B", events.get(0).getEventLocation());
  }

  @Test
  public void testEditAllEvents() {
    model.createCalendar("Work", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 4, 10, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 4, 10, 10, 0);

    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Standup")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Daily standup")
            .setEventLocation("Room A")
            .setPrivate(true)
            .build();

    // Add one event.
    assertTrue(model.addEvent("Work", eventDTO));

    // Attempt to edit all events matching the criteria.
    boolean edited = model.editEvents("Work", "location", "Standup", start, "Room C", true);
    assertTrue(edited);
    List<ICalendarEventDTO> events = model.getEventsInRange("Work", start.minusMinutes(1), end.plusMinutes(1));
    for (ICalendarEventDTO event : events) {
      assertEquals("Room C", event.getEventLocation());
    }
  }

  // ===== isCalendarAvailable Tests =====

  @Test
  public void testIsCalendarAvailable() {
    // Create a calendar named "Personal"
    model.createCalendar("Personal", "America/New_York");

    // Check availability for a date when no events have been added.
    // Since there are no events on June 1, 2025, the calendar should be free (available).
    assertTrue(model.isCalendarAvailable("Personal", LocalDate.of(2025, 6, 1)));

    // Add an event on June 1, 2025.
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 8, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 9, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Breakfast")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Morning meal")
            .setEventLocation("Home")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("Personal", eventDTO));

    // Now the calendar has an event on June 1, 2025, so it should be busy (not available).
    assertFalse(model.isCalendarAvailable("Personal", LocalDate.of(2025, 6, 1)));

    // For a day with no events (June 2, 2025), the calendar should be free.
    assertTrue(model.isCalendarAvailable("Personal", LocalDate.of(2025, 6, 2)));

    // When date is null, the method checks for calendar existence, so it should return true.
    assertTrue(model.isCalendarAvailable("Personal", null));

    // For a non-existing calendar, it should return false.
    assertFalse(model.isCalendarAvailable("NonExisting", LocalDate.of(2025, 6, 1)));
  }

  // ===== Delete Calendar Tests =====

  @Test
  public void testDeleteCalendar() {
    model.createCalendar("Temp", "America/New_York");
    assertTrue(model.deleteCalendar("Temp"));
    assertFalse(model.isCalendarPresent("Temp"));
  }

  // ===== getEventsInRange Tests =====

  @Test
  public void testGetEventsInRange() {
    model.createCalendar("Work", "America/New_York");

    LocalDateTime start1 = LocalDateTime.of(2025, 7, 1, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 7, 1, 10, 0);
    ICalendarEventDTO eventDTO1 = CalendarEventDTO.builder()
            .setEventName("Meeting")
            .setStartDateTime(start1)
            .setEndDateTime(end1)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Team meeting")
            .setEventLocation("Office")
            .setPrivate(true)
            .build();
    assertTrue(model.addEvent("Work", eventDTO1));

    LocalDateTime start2 = LocalDateTime.of(2025, 7, 2, 9, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 7, 2, 10, 0);
    ICalendarEventDTO eventDTO2 = CalendarEventDTO.builder()
            .setEventName("Meeting")
            .setStartDateTime(start2)
            .setEndDateTime(end2)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Team meeting")
            .setEventLocation("Office")
            .setPrivate(true)
            .build();
    assertTrue(model.addEvent("Work", eventDTO2));

    List<ICalendarEventDTO> eventsInRange = model.getEventsInRange("Work", start1.minusHours(1), end2.plusHours(1));
    assertEquals(2, eventsInRange.size());
  }

  // ===== Copy Events Tests =====

  @Test
  public void testCopyEvents() {
    model = new CalendarModel();
    model.createCalendar("Fall2024", "America/New_York");
    model.createCalendar("Spring2025", "America/New_York");

    // Original event in Fall2024 calendar
    LocalDateTime fallStart = LocalDateTime.of(2024, 9, 5, 10, 0);
    LocalDateTime fallEnd = LocalDateTime.of(2024, 9, 5, 11, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Lecture")
            .setStartDateTime(fallStart)
            .setEndDateTime(fallEnd)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Lecture on CS5010")
            .setEventLocation("Room 101")
            .setPrivate(true)
            .build();
    assertTrue(model.addEvent("Fall2024", eventDTO));

    // New target date
    LocalDate targetDate = LocalDate.of(2025, 1, 8);

    // Copy event to Spring2025 calendar
    boolean copied = model.copyEvents("Fall2024", fallStart, fallEnd, "Spring2025", targetDate);
    assertTrue(copied);

    // Expected new start time is 10:00 AM on 2025-01-08
    LocalDateTime expectedStart = LocalDateTime.of(2025, 1, 8, 10, 0);
    LocalDateTime expectedEnd = LocalDateTime.of(2025, 1, 8, 11, 0);

    List<ICalendarEventDTO> copiedEvents = model.getEventsInRange(
            "Spring2025",
            expectedStart.minusMinutes(1),
            expectedEnd.plusMinutes(1)
    );
    assertEquals(1, copiedEvents.size());

    ICalendarEventDTO copiedEvent = copiedEvents.get(0);
    assertEquals("Lecture", copiedEvent.getEventName());
    assertEquals(expectedStart, copiedEvent.getStartDateTime());
    assertEquals(expectedEnd, copiedEvent.getEndDateTime());
    assertEquals("Lecture on CS5010", copiedEvent.getEventDescription());
    assertEquals("Room 101", copiedEvent.getEventLocation());
    assertTrue(copiedEvent.isAutoDecline());
    assertTrue(copiedEvent.isPrivate());
  }

  // ===== Edit Calendar Tests =====

  @Test
  public void testEditCalendarName() {
    model.createCalendar("OldName", "America/New_York");
    assertTrue(model.editCalendar("OldName", "name", "NewName"));
    assertTrue(model.isCalendarPresent("NewName"));
    assertFalse(model.isCalendarPresent("OldName"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarDuplicateName() {
    model.createCalendar("Cal1", "America/New_York");
    model.createCalendar("Cal2", "America/New_York");
    model.editCalendar("Cal1", "name", "Cal2");
  }

  @Test
  public void testEditCalendarTimezone() {
    model.createCalendar("Work", "America/New_York");
    assertTrue(model.editCalendar("Work", "timezone", "Europe/London"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarUnsupportedProperty() {
    model.createCalendar("Work", "America/New_York");
    model.editCalendar("Work", "unsupported", "Value");
  }

  // ===== isCalendarPresent Tests =====

  @Test
  public void testIsCalendarPresent() {
    model.createCalendar("TestCal", "America/New_York");
    assertTrue(model.isCalendarPresent("TestCal"));
    assertFalse(model.isCalendarPresent("NonExisting"));
  }

  @Test
  public void testGetEventsInRange_InclusiveBoundaries() {
    model.createCalendar("BoundaryCal", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 8, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 8, 1, 10, 0);

    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("EventBoundary")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Boundary test")
            .setEventLocation("Loc1")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("BoundaryCal", eventDTO));

    // Test that an event starting exactly at the fromDateTime is included.
    List<ICalendarEventDTO> events = model.getEventsInRange("BoundaryCal", start, end.plusMinutes(30));
    assertFalse(events.isEmpty());
    assertEquals("EventBoundary", events.get(0).getEventName());

    // Test that an event starting exactly at the toDateTime is included.
    events = model.getEventsInRange("BoundaryCal", start.minusMinutes(30), end);
    assertFalse(events.isEmpty());
    assertEquals("EventBoundary", events.get(0).getEventName());
  }

  @Test
  public void testGetEventsInRange_NoEvents() {
    model.createCalendar("EmptyCal", "America/New_York");
    LocalDateTime from = LocalDateTime.of(2025, 8, 1, 9, 0);
    LocalDateTime to = LocalDateTime.of(2025, 8, 1, 10, 0);
    List<ICalendarEventDTO> events = model.getEventsInRange("EmptyCal", from, to);
    assertTrue(events.isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetEventsInRange_NullParameters() {
    model.createCalendar("NullCal", "America/New_York");
    model.getEventsInRange("NullCal", null, LocalDateTime.now());
  }

  // ---------- Invalid Parameter Handling ----------

  @Test(expected = IllegalArgumentException.class)
  public void testAddEvent_MissingEventName() {
    model.createCalendar("TestCal", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 9, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 9, 1, 11, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName(null)  // missing event name
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Test")
            .setEventLocation("TestRoom")
            .setPrivate(false)
            .build();
    model.addEvent("TestCal", eventDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddEvent_MissingStartTime() {
    model.createCalendar("TestCal", "America/New_York");
    LocalDateTime end = LocalDateTime.of(2025, 9, 1, 11, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("TestEvent")
            .setStartDateTime(null)  // missing start time
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Test")
            .setEventLocation("TestRoom")
            .setPrivate(false)
            .build();
    model.addEvent("TestCal", eventDTO);
  }

  // ---------- Conflict Detection Tests ----------

  @Test(expected = IllegalStateException.class)
  public void testAddEvent_ConflictDetection() {
    model.createCalendar("ConflictCal", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 10, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 10, 1, 10, 0);
    ICalendarEventDTO eventDTO1 = CalendarEventDTO.builder()
            .setEventName("Event")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Test event")
            .setEventLocation("Room 1")
            .setPrivate(false)
            .build();
    ICalendarEventDTO eventDTO2 = CalendarEventDTO.builder()
            .setEventName("Event")
            .setStartDateTime(start.plusMinutes(15)) // overlapping time
            .setEndDateTime(end.plusMinutes(15))
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Test event conflict")
            .setEventLocation("Room 2")
            .setPrivate(false)
            .build();
    // Add first event.
    assertTrue(model.addEvent("ConflictCal", eventDTO1));
    // Adding second event should throw conflict.
    model.addEvent("ConflictCal", eventDTO2);
  }

  @Test(expected = IllegalStateException.class)
  public void testCopyEvents_ConflictDetection() {
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    ICalendarEventDTO sourceEvent = CalendarEventDTO.builder()
            .setEventName("CopyEvent")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Event to copy")
            .setEventLocation("Room X")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("SourceCal", sourceEvent));

    ICalendarEventDTO conflictEvent = CalendarEventDTO.builder()
            .setEventName("ConflictEvent")
            .setStartDateTime(LocalDateTime.of(2025, 1, 8, 10, 0))
            .setEndDateTime(LocalDateTime.of(2025, 1, 8, 11, 0))
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Conflicting event")
            .setEventLocation("Room Y")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TargetCal", conflictEvent));

    // Attempt to copy the source event to TargetCal should throw a conflict exception.
    model.copyEvents("SourceCal", start, end, "TargetCal", LocalDate.of(2025, 1, 8));
  }

  // ---------- Recurring Event Edge Cases ----------

  @Test
  public void testRecurringEventWithRecurrenceEndDate() {
    model.createCalendar("RecurrCal", "America/New_York");

    // May 1, 2025 is a Thursday.
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 10, 0);

    // Set recurrence end date to May 15, 2025, which is also a Thursday.
    LocalDateTime recEnd = LocalDateTime.of(2025, 5, 15, 10, 0);

    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Biweekly Standup")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(true)
            .setRecurrenceCount(null)  // Use recurrence end date instead of count.
            .setRecurrenceEndDate(recEnd)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.THURSDAY))
            .setEventDescription("Standup meeting")
            .setEventLocation("Room Z")
            .setPrivate(false)
            .build();

    assertTrue(model.addEvent("RecurrCal", eventDTO));

    // Query a range that should capture all occurrences.
    List<ICalendarEventDTO> events = model.getEventsInRange("RecurrCal", start.minusDays(1), recEnd.plusDays(1));
    assertEquals(3, events.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRecurringEventInvalidCount() {
    model.createCalendar("RecurrCal", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 10, 0);
    // Invalid recurrence count (zero).
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Invalid Recurrence")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(true)
            .setRecurrenceCount(0)
            .setRecurrenceEndDate(null)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.FRIDAY))
            .setEventDescription("Should fail")
            .setEventLocation("Room X")
            .setPrivate(false)
            .build();
    model.addEvent("RecurrCal", eventDTO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRecurringEventDifferentDays() {
    model.createCalendar("RecurrCal", "America/New_York");
    // Start and end on different days.
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 2, 10, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Bad Recurrence")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(true)
            .setRecurrenceCount(3)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.FRIDAY))
            .setEventDescription("Invalid recurrence dates")
            .setEventLocation("Room Y")
            .setPrivate(false)
            .build();
    model.addEvent("RecurrCal", eventDTO);
  }

  // ---------- Editing Events Edge Cases ----------

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventsUnsupportedProperty() {
    model.createCalendar("EditCal", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 8, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 8, 1, 11, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Event")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Test")
            .setEventLocation("Room A")
            .setPrivate(true)
            .build();
    assertTrue(model.addEvent("EditCal", eventDTO));
    // Attempt to edit an unsupported property.
    model.editEvents("EditCal", "unsupported", "Event", start, "NewValue", false);
  }

  @Test(expected = IllegalStateException.class)
  public void testEditEventsNoMatchingEvent() {
    model.createCalendar("EditCal", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 8, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 8, 1, 11, 0);
    // No event added, so editing should fail.
    model.editEvents("EditCal", "location", "NonExistent", start, "Room X", false);
  }

  // ---------- Calendar Availability and Presence ----------

  @Test
  public void testIsCalendarAvailableNoEvents() {
    model.createCalendar("AvailCal", "America/New_York");
    // Calendar exists but no events; now we consider the calendar available when it exists.
    assertTrue(model.isCalendarAvailable("AvailCal", LocalDate.of(2025, 9, 1)));
  }

  @Test
  public void testIsCalendarPresentCaseSensitive() {
    model.createCalendar("TestCal", "America/New_York");
    // Using the exact same case should return true.
    assertTrue(model.isCalendarPresent("TestCal"));
    // Using a different case should return false.
    assertFalse(model.isCalendarPresent("testcal"));
  }

  // ---------- Copy Events Edge Cases ----------

  @Test
  public void testCopyEventsNoEventsFound() {
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    try {
      model.copyEvents(
            "SourceCal",
            LocalDateTime.of(2025, 1, 1, 10, 0),
            LocalDateTime.of(2025, 1, 1, 11, 0),
            "TargetCal",
            LocalDate.of(2025, 2, 1)
      );
      fail("Expected IllegalStateException not thrown");
    } catch (IllegalArgumentException e) {
      assertEquals("Events to be copied are empty", e.getMessage());
    }
  }


  @Test
  public void testCopyEventsRecurringEvent() {
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    // Original recurring event starts on March 3, 2025 (Monday) at 9:00 AM
    LocalDateTime start = LocalDateTime.of(2025, 3, 3, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 3, 10, 0);

    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Weekly Sync")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setRecurring(true)
            .setRecurrenceCount(2)
            .setRecurrenceDays(List.of(DayOfWeek.MONDAY))
            .setEventDescription("Recurring meeting")
            .setEventLocation("Room Z")
            .setPrivate(false)
            .setAutoDecline(true)
            .build();
    assertTrue(model.addEvent("SourceCal", eventDTO));

    // Copy window: March 2 to March 11 (covers both occurrences)
    LocalDateTime intervalStart = LocalDateTime.of(2025, 3, 3, 0, 0);
    LocalDateTime intervalEnd = LocalDateTime.of(2025, 3, 11, 0, 0);

    // Target start date: April 7, 2025 (Monday) — aligns with first recurrence
    LocalDate targetStart = LocalDate.of(2025, 4, 7);

    assertTrue(model.copyEvents("SourceCal", intervalStart, intervalEnd, "TargetCal", targetStart));

    // Retrieve events from April 6 to April 21 in TargetCal
    List<ICalendarEventDTO> copiedEvents = model.getEventsInRange(
            "TargetCal",
            LocalDateTime.of(2025, 4, 7, 0, 0),
            LocalDateTime.of(2025, 4, 15, 0, 0)
    );

    assertEquals(2, copiedEvents.size());

    // First copied event: April 7, 2025 at 9:00 AM
    ICalendarEventDTO event1 = copiedEvents.get(0);
    assertEquals("Weekly Sync", event1.getEventName());
    assertEquals(LocalDateTime.of(2025, 4, 7, 9, 0), event1.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 4, 7, 10, 0), event1.getEndDateTime());

    // Second copied event: April 14, 2025 at 9:00 AM
    ICalendarEventDTO event2 = copiedEvents.get(1);
    assertEquals("Weekly Sync", event2.getEventName());
    assertEquals(LocalDateTime.of(2025, 4, 14, 9, 0), event2.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 4, 14, 10, 0), event2.getEndDateTime());
  }

  // ---------- Delete Calendar Edge Cases ----------

  @Test(expected = IllegalArgumentException.class)
  public void testDeleteCalendarAndUseIt() {
    model.createCalendar("DeleteCal", "America/New_York");
    assertTrue(model.deleteCalendar("DeleteCal"));
    // Now try to add an event to the deleted calendar.
    LocalDateTime start = LocalDateTime.of(2025, 10, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 10, 1, 10, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("TestEvent")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Should fail")
            .setEventLocation("Nowhere")
            .setPrivate(false)
            .build();
    model.addEvent("DeleteCal", eventDTO);
  }

  @Test
  public void testConvertToDTO() throws Exception {
    // Create a CalendarEvent instance manually.
    LocalDateTime start = LocalDateTime.of(2025, 3, 18, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 18, 11, 0);
    CalendarEvent event = new CalendarEvent("TestEvent", start, end, "Desc", "RoomX", true, false, null, true);

    // Use reflection to call the private convertToDTO method.
    Method method = CalendarModel.class.getDeclaredMethod("convertToDTO", CalendarEvent.class);
    method.setAccessible(true);
    ICalendarEventDTO dto = (ICalendarEventDTO) method.invoke(model, event);

    assertEquals("TestEvent", dto.getEventName());
    assertEquals(start, dto.getStartDateTime());
    assertEquals(end, dto.getEndDateTime());
    // Verify that location and description were copied.
    assertEquals("RoomX", dto.getEventLocation());
    assertEquals("Desc", dto.getEventDescription());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRecurringEventMissingCountAndEndDate() {
    model.createCalendar("RecurrCal", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 10, 0);

    // Neither recurrence count nor recurrence end date provided.
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("RecurringEvent")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(true)
            .setRecurrenceCount(null)
            .setRecurrenceEndDate(null)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.FRIDAY))
            .setEventDescription("Recurring event test")
            .setEventLocation("Room A")
            .setPrivate(false)
            .build();

    model.addEvent("RecurrCal", eventDTO);
  }

  @Test
  public void testRecurringEventCountOne() {
    model.createCalendar("RecurrCal", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 9, 0); // assume this is a Friday
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 10, 0);

    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("SingleOccurrence")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(true)
            .setRecurrenceCount(1)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.FRIDAY))
            .setEventDescription("Occurs only once")
            .setEventLocation("Room C")
            .setPrivate(false)
            .build();

    assertTrue(model.addEvent("RecurrCal", eventDTO));
    List<ICalendarEventDTO> events = model.getEventsInRange("RecurrCal", start.minusDays(1), end.plusDays(1));
    assertEquals(1, events.size());
  }

  // --- 5. Deletion and subsequent operations ---
  @Test(expected = IllegalArgumentException.class)
  public void testDeleteCalendarThenAddEvent() {
    model.createCalendar("TempCal", "America/New_York");
    assertTrue(model.deleteCalendar("TempCal"));
    // Now try to add an event to the deleted calendar.
    LocalDateTime start = LocalDateTime.of(2025, 10, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 10, 1, 10, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("TestEvent")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Should fail")
            .setEventLocation("Room X")
            .setPrivate(false)
            .build();
    model.addEvent("TempCal", eventDTO);
  }

  @Test(expected = IllegalStateException.class)
  public void testEditEventToOverlap() {
    model.createCalendar("OverlapCal", "America/New_York");
    LocalDateTime start1 = LocalDateTime.of(2025, 11, 1, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 11, 1, 11, 0);

    ICalendarEventDTO event1 = CalendarEventDTO.builder()
            .setEventName("Event1")
            .setStartDateTime(start1)
            .setEndDateTime(end1)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("First event")
            .setEventLocation("Room 1")
            .setPrivate(false)
            .build();
    ICalendarEventDTO event2 = CalendarEventDTO.builder()
            .setEventName("Event2")
            .setStartDateTime(start2)
            .setEndDateTime(end2)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Second event")
            .setEventLocation("Room 2")
            .setPrivate(false)
            .build();

    assertTrue(model.addEvent("OverlapCal", event1));
    assertTrue(model.addEvent("OverlapCal", event2));

    // Now attempt to edit Event2 so that it overlaps with Event1.
    // For example, change its start time to 9:30, which overlaps with Event1.
    model.editEvents("OverlapCal", "start", "Event2", start2, "2025-11-01T09:30:00", false);
  }

  //check if the event is not created in an unspecified calender

  @Test
  public void testCopyEvents_success2() {
    ICalendarModel calendarModel = new CalendarModel();

    // Create source and target calendars
    calendarModel.createCalendar("SourceCal", "America/New_York");
    calendarModel.createCalendar("TargetCal", "Asia/Kolkata");

    // Add events to source calendar
    LocalDateTime start1 = LocalDateTime.of(2025, 3, 25, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 3, 25, 11, 0);
    ICalendarEventDTO eventDTO1 = CalendarEventDTO.builder()
            .setEventName("Meeting A")
            .setStartDateTime(start1)
            .setEndDateTime(end1)
            .setEventDescription("First meeting")
            .setEventLocation("Room A")
            .setAutoDecline(true)
            .setPrivate(false)
            .build();

    calendarModel.addEvent("SourceCal", eventDTO1);

    // Another event
    LocalDateTime start2 = LocalDateTime.of(2025, 3, 25, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 3, 25, 15, 30);
    ICalendarEventDTO eventDTO2 = CalendarEventDTO.builder()
            .setEventName("Meeting B")
            .setStartDateTime(start2)
            .setEndDateTime(end2)
            .setEventDescription("Second meeting")
            .setEventLocation("Room B")
            .setAutoDecline(true)
            .setPrivate(false)
            .build();

    calendarModel.addEvent("SourceCal", eventDTO2);
    LocalDateTime sourceStart = LocalDateTime.of(2025, 3, 25, 0, 0);
    LocalDateTime sourceEnd = LocalDateTime.of(2025, 3, 25, 23, 59);
    LocalDate targetStart = LocalDate.of(2025, 3, 26);

    boolean result = calendarModel.copyEvents("SourceCal", sourceStart, sourceEnd, "TargetCal", targetStart);

    assertTrue(result);

    // Ensure the target calendar now has the copied events
    var copiedEvents = calendarModel.getEventsInRange("TargetCal",
            LocalDateTime.of(2025, 3, 26, 0, 0),
            LocalDateTime.of(2025, 3, 27, 0, 0));

    assertEquals(2, copiedEvents.size());

    ICalendarEventDTO copiedEvent1 = copiedEvents.get(0);
    ICalendarEventDTO copiedEvent2 = copiedEvents.get(1);

    assertEquals("Meeting A", copiedEvent1.getEventName());
    assertEquals("Meeting B", copiedEvent2.getEventName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEvents_SourceCalendarNotFound() {
    model = new CalendarModel();
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");
    model.copyEvents("NonExistentSource",
            LocalDateTime.of(2025, 1, 1, 9, 0),
            LocalDateTime.of(2025, 1, 1, 10, 0),
            "TargetCal",
            LocalDate.of(2025, 2, 1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEvents_TargetCalendarNotFound() {
    model.copyEvents("SourceCal",
            LocalDateTime.of(2025, 1, 1, 9, 0),
            LocalDateTime.of(2025, 1, 1, 10, 0),
            "NonExistentTarget",
            LocalDate.of(2025, 2, 1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEvents_NullParameters() {
    // Passing a null sourceStart should trigger an exception.
    model.copyEvents("SourceCal",
            null,
            LocalDateTime.of(2025, 1, 1, 10, 0),
            "TargetCal",
            LocalDate.of(2025, 2, 1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEvents_SourceEndBeforeSourceStart() {
    // sourceEnd is before sourceStart.
    model.copyEvents("SourceCal",
            LocalDateTime.of(2025, 1, 1, 10, 0),
            LocalDateTime.of(2025, 1, 1, 9, 0),
            "TargetCal",
            LocalDate.of(2025, 2, 1));
  }

  // ---------- No Events Found Test ----------

  @Test
  public void testCopyEvents_NoEventsFound() {
    model = new CalendarModel();
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    try {
      model.copyEvents(
            "SourceCal",
            LocalDateTime.of(2025, 1, 1, 9, 0),
            LocalDateTime.of(2025, 1, 1, 10, 0),
            "TargetCal",
            LocalDate.of(2025, 2, 1)
      );
      fail("Expected IllegalArgumentException not thrown");
    } catch (IllegalArgumentException e) {
      assertEquals("Events to be copied are empty", e.getMessage());
    }
  }

  // ---------- Successful Copy Tests ----------

  @Test
  public void testCopyEvents_SingleEventSuccess() {
    model = new CalendarModel();
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    // Create an event in SourceCal on Jan 1, 2025 from 9:00 to 10:00
    LocalDateTime sourceStart = LocalDateTime.of(2025, 1, 1, 9, 0);
    LocalDateTime sourceEnd = LocalDateTime.of(2025, 1, 1, 10, 0);

    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Meeting")
            .setStartDateTime(sourceStart)
            .setEndDateTime(sourceEnd)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Team meeting")
            .setEventLocation("Room 101")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("SourceCal", eventDTO));

    // Copy to Feb 1, 2025 (same time of day, different date)
    LocalDate targetDate = LocalDate.of(2025, 2, 1);
    boolean copied = model.copyEvents("SourceCal", sourceStart, sourceEnd, "TargetCal", targetDate);
    assertTrue(copied);

    // Expected new start = 9:00 AM on Feb 1, 2025 (same time of day)
    LocalDateTime expectedStart = LocalDateTime.of(2025, 2, 1, 9, 0);
    LocalDateTime expectedEnd = expectedStart.plusHours(1);

    List<ICalendarEventDTO> events = model.getEventsInRange(
            "TargetCal",
            expectedStart.minusMinutes(1),
            expectedEnd.plusMinutes(1)
    );

    assertEquals(1, events.size());
    ICalendarEventDTO copiedEvent = events.get(0);

    assertEquals("Meeting", copiedEvent.getEventName());
    assertEquals(expectedStart, copiedEvent.getStartDateTime());
    assertEquals(expectedEnd, copiedEvent.getEndDateTime());
    assertEquals("Team meeting", copiedEvent.getEventDescription());
    assertEquals("Room 101", copiedEvent.getEventLocation());
  }

  @Test
  public void testCopyEvents_MultipleEventsSuccess() {
    model = new CalendarModel();
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    // Add two events to SourceCal
    model.addEvent("SourceCal", CalendarEventDTO.builder()
            .setEventName("Event1")
            .setStartDateTime(LocalDateTime.of(2025, 1, 1, 9, 0))
            .setEndDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .setRecurring(false)
            .setAutoDecline(true)
            .setEventDescription("First")
            .setEventLocation("Room A")
            .setPrivate(false)
            .build());

    model.addEvent("SourceCal", CalendarEventDTO.builder()
            .setEventName("Event2")
            .setStartDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .setEndDateTime(LocalDateTime.of(2025, 1, 1, 12, 0))
            .setRecurring(false)
            .setAutoDecline(true)
            .setEventDescription("Second")
            .setEventLocation("Room B")
            .setPrivate(false)
            .build());

    // Copy both events
    boolean copied = model.copyEvents(
            "SourceCal",
            LocalDateTime.of(2025, 1, 1, 8, 0),
            LocalDateTime.of(2025, 1, 1, 13, 0),
            "TargetCal",
            LocalDate.of(2025, 2, 1));
    assertTrue(copied);

    // Check that both events exist in target calendar
    List<ICalendarEventDTO> events = model.getEventsInRange(
            "TargetCal",
            LocalDateTime.of(2025, 2, 1, 0, 0),
            LocalDateTime.of(2025, 2, 1, 23, 59));
    assertEquals(2, events.size());
  }

  @Test
  public void testCopyEvents_DifferentTimezones_Success() {
    model = new CalendarModel();
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "Asia/Kolkata");

    // Source event in New York timezone: 9 AM to 10 AM EST (UTC-5)
    LocalDateTime sourceStart = LocalDateTime.of(2025, 3, 1, 9, 0);
    LocalDateTime sourceEnd = LocalDateTime.of(2025, 3, 1, 10, 0);

    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("MorningMeeting")
            .setStartDateTime(sourceStart)
            .setEndDateTime(sourceEnd)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Morning sync")
            .setEventLocation("NY Office")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("SourceCal", eventDTO));

    // Target day in IST (Asia/Kolkata) to align with original time
    LocalDate targetStart = LocalDate.of(2025, 3, 2); // IST

    assertTrue(model.copyEvents("SourceCal", sourceStart, sourceEnd, "TargetCal", targetStart));

    // Time difference between EST (UTC-5) and IST (UTC+5:30) = +10.5 hours
    // 2025-03-01 09:00 EST + 10.5h = 2025-03-02 19:30 IST
    LocalDateTime expectedStart = LocalDateTime.of(2025, 3, 2, 19, 30);
    LocalDateTime expectedEnd = expectedStart.plusHours(1);

    List<ICalendarEventDTO> copiedEvents = model.getEventsInRange("TargetCal",
            expectedStart.minusHours(1), expectedEnd.plusHours(1));

    assertEquals(1, copiedEvents.size());
    ICalendarEventDTO copiedEvent = copiedEvents.get(0);
    assertEquals("MorningMeeting", copiedEvent.getEventName());
    assertEquals(expectedStart, copiedEvent.getStartDateTime());
    assertEquals(expectedEnd, copiedEvent.getEndDateTime());
    assertEquals("Morning sync", copiedEvent.getEventDescription());
    assertEquals("NY Office", copiedEvent.getEventLocation());
  }

  @Test
  public void testCopyEvents_TimeZoneAdjustedSimple() {
    model = new CalendarModel();
    model.createCalendar("SourceCal", "America/Los_Angeles");
    model.createCalendar("TargetCal", "Asia/Tokyo");

    // Original event in LA (PDT)
    LocalDateTime laStart = LocalDateTime.of(2025, 5, 10, 10, 0);
    LocalDateTime laEnd = laStart.plusHours(1);

    ICalendarEventDTO event = CalendarEventDTO.builder()
            .setEventName("Global Call")
            .setStartDateTime(laStart)
            .setEndDateTime(laEnd)
            .setRecurring(false)
            .setAutoDecline(true)
            .setPrivate(false)
            .setEventDescription("Call across time zones")
            .setEventLocation("Zoom")
            .build();

    assertTrue(model.addEvent("SourceCal", event));

    // Copy to Tokyo, using May 11 as base
    LocalDate targetDate = LocalDate.of(2025, 5, 11);
    assertTrue(model.copyEvents("SourceCal", laStart, laEnd, "TargetCal", targetDate));

    // Expected Tokyo time: 10 AM PDT = 2 AM JST (UTC+9)
    LocalDateTime expectedStart = LocalDateTime.of(2025, 5, 12, 2, 0);
    LocalDateTime expectedEnd = expectedStart.plusHours(1);

    List<ICalendarEventDTO> events = model.getEventsInRange("TargetCal", expectedStart.minusMinutes(1), expectedEnd.plusMinutes(1));
    assertEquals(1, events.size());

    ICalendarEventDTO copied = events.get(0);
    assertEquals("Global Call", copied.getEventName());
    assertEquals(expectedStart, copied.getStartDateTime());
    assertEquals(expectedEnd, copied.getEndDateTime());
  }

  @Test
  public void testCopyEvents_NoEventsInRange_ShouldThrowException() {
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    try {
      model.copyEvents(
            "SourceCal",
            LocalDateTime.of(2025, 6, 1, 10, 0),
            LocalDateTime.of(2025, 6, 1, 11, 0),
            "TargetCal",
            LocalDate.of(2025, 6, 2)
      );
      fail("Expected IllegalStateException not thrown");
    } catch (IllegalArgumentException e) {
      assertEquals("Events to be copied are empty", e.getMessage());
    }
  }


  @Test
  public void testCopyEvents_WithConflictInTarget_ShouldThrowException() {
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    // Source event: 9–10 AM on Aug 10, 2025
    LocalDateTime sourceStart = LocalDateTime.of(2025, 8, 10, 9, 0);
    LocalDateTime sourceEnd = LocalDateTime.of(2025, 8, 10, 10, 0);

    ICalendarEventDTO sourceEvent = CalendarEventDTO.builder()
            .setEventName("Morning Meeting")
            .setStartDateTime(sourceStart)
            .setEndDateTime(sourceEnd)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Team Sync")
            .setEventLocation("Room A")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("SourceCal", sourceEvent));

    // Conflict in target calendar: 9–10 AM on Sept 1, 2025 (same time as mapped source event)
    ICalendarEventDTO conflicting = CalendarEventDTO.builder()
            .setEventName("Blocker")
            .setStartDateTime(LocalDateTime.of(2025, 9, 1, 9, 0))
            .setEndDateTime(LocalDateTime.of(2025, 9, 1, 10, 0))
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Overlap")
            .setEventLocation("Room B")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TargetCal", conflicting));

    // This should create a conflict, because the copied event will also land at 9–10 AM on Sept 1, 2025
    LocalDate targetStart = LocalDate.of(2025, 9, 1);
    assertThrows(IllegalStateException.class, () -> {
      model.copyEvents("SourceCal", sourceStart, sourceEnd, "TargetCal", targetStart);
    });
  }

  @Test
  public void testCopyEvents_NullStart_ThrowsException() {
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    assertThrows(IllegalArgumentException.class, () -> {
      model.copyEvents("SourceCal", null, LocalDateTime.now(), "TargetCal", LocalDate.now());
    });
  }

  @Test(expected = IllegalStateException.class)
  public void testCopyEvents_ConflictWithExistingEvent_ShouldFail() {
    model = new CalendarModel();
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = start.plusHours(1); // 10:00 AM

    // Event in SourceCal
    ICalendarEventDTO sourceEvent = CalendarEventDTO.builder()
            .setEventName("ToCopy")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setRecurring(false)
            .setEventDescription("Copy me")
            .setEventLocation("Room 1")
            .setPrivate(false)
            .setAutoDecline(true)
            .build();
    assertTrue(model.addEvent("SourceCal", sourceEvent));

    // Conflicting event already in TargetCal — this overlaps with the copied event
    ICalendarEventDTO conflict = CalendarEventDTO.builder()
            .setEventName("Blocker")
            .setStartDateTime(LocalDateTime.of(2025, 7, 1, 9, 30))
            .setEndDateTime(LocalDateTime.of(2025, 7, 1, 10, 30))
            .setRecurring(false)
            .setEventDescription("Already here")
            .setEventLocation("Room 2")
            .setPrivate(false)
            .setAutoDecline(true)
            .build();
    assertTrue(model.addEvent("TargetCal", conflict));

    // Copying to July 1, 2025 — should try to place event at 9:00 AM
    model.copyEvents("SourceCal", start, end, "TargetCal", LocalDate.of(2025, 7, 1));
  }

  @Test
  public void testCopyEvents_RecurringSpansMonths() {
    model = new CalendarModel();
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    // Original recurring event on Jan 30, 2025 (Thursday), repeating weekly 4 times
    LocalDateTime start = LocalDateTime.of(2025, 1, 30, 10, 0);
    LocalDateTime end = start.plusHours(1);

    ICalendarEventDTO recurring = CalendarEventDTO.builder()
            .setEventName("MultiMonthEvent")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setRecurring(true)
            .setRecurrenceCount(4)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.THURSDAY))
            .setEventDescription("Spans months")
            .setEventLocation("Board Room")
            .setPrivate(false)
            .setAutoDecline(true)
            .build();
    assertTrue(model.addEvent("SourceCal", recurring));

    // Define copy window to include all 4 original occurrences
    LocalDateTime sourceEnd = start.plusWeeks(3);
    LocalDate targetStart = LocalDate.of(2025, 3, 6); // First Thursday of copy

    assertTrue(model.copyEvents("SourceCal", start, sourceEnd, "TargetCal", targetStart));

    // Each copied event will land on a Thursday starting from 2025-03-06, at 10:00 AM
    List<ICalendarEventDTO> events = model.getEventsInRange(
            "TargetCal",
            targetStart.atStartOfDay().minusDays(1),
            targetStart.plusWeeks(4).atTime(23, 59)
    );

    assertEquals(4, events.size());

    assertEquals(LocalDateTime.of(2025, 3, 6, 10, 0), events.get(0).getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 3, 13, 10, 0), events.get(1).getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 3, 20, 10, 0), events.get(2).getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 3, 27, 10, 0), events.get(3).getStartDateTime());
  }

  @Test
  public void testCopyEvents_WithDSTTransition() {
    model = new CalendarModel();
    model.createCalendar("SourceCal", "America/New_York"); // Has DST
    model.createCalendar("TargetCal", "Asia/Kolkata");     // No DST

    // DST starts at 2:00 AM on March 9, 2025
    LocalDateTime dstStart = LocalDateTime.of(2025, 3, 9, 1, 30);
    LocalDateTime dstEnd = LocalDateTime.of(2025, 3, 9, 2, 30); // Actually interpreted as 3:30 AM EDT

    ICalendarEventDTO event = CalendarEventDTO.builder()
            .setEventName("DST Event")
            .setStartDateTime(dstStart)
            .setEndDateTime(dstEnd)
            .setRecurring(false)
            .setAutoDecline(true)
            .setPrivate(false)
            .setEventLocation("Room DST")
            .setEventDescription("Spring forward")
            .build();
    assertTrue(model.addEvent("SourceCal", event));

    LocalDate targetStart = LocalDate.of(2025, 3, 10);
    assertTrue(model.copyEvents("SourceCal", dstStart.minusMinutes(10), dstEnd.plusMinutes(10), "TargetCal", targetStart));

    // Convert start: 2025-03-09 01:30 EST → 06:30 UTC → 12:00 IST
    // Convert end:   2025-03-09 03:30 EDT → 07:30 UTC → 13:00 IST
    LocalDateTime expectedStart = LocalDateTime.of(2025, 3, 10, 12, 0);
    LocalDateTime expectedEnd = LocalDateTime.of(2025, 3, 10, 13, 0);

    List<ICalendarEventDTO> events = model.getEventsInRange(
            "TargetCal",
            expectedStart.minusHours(1),
            expectedEnd.plusHours(1)
    );

    assertEquals(1, events.size());
    assertEquals("DST Event", events.get(0).getEventName());
    assertEquals(expectedStart, events.get(0).getStartDateTime());
    assertEquals(expectedEnd, events.get(0).getEndDateTime());
    assertEquals("Room DST", events.get(0).getEventLocation());
    assertEquals("Spring forward", events.get(0).getEventDescription());
  }

  @Test
  public void testCopyEvents_ToHalfHourTimezone() {
    model.createCalendar("SourceCal", "America/Chicago");        // UTC-5 in July (CDT)
    model.createCalendar("TargetCal", "Asia/Kathmandu");         // UTC+5:45

    // Original source event in Chicago: 9 AM to 10 AM CDT
    LocalDateTime srcStart = LocalDateTime.of(2025, 7, 15, 9, 0);
    LocalDateTime srcEnd = LocalDateTime.of(2025, 7, 15, 10, 0);

    ICalendarEventDTO event = CalendarEventDTO.builder()
            .setEventName("Half-Hour TZ Event")
            .setStartDateTime(srcStart)
            .setEndDateTime(srcEnd)
            .setRecurring(false)
            .setAutoDecline(true)
            .setPrivate(false)
            .setEventDescription("Kathmandu time test")
            .setEventLocation("Room Half")
            .build();
    assertTrue(model.addEvent("SourceCal", event));

    // Target date to align the first copied event to: August 1, 2025
    LocalDate targetStart = LocalDate.of(2025, 8, 1);
    boolean copied = model.copyEvents("SourceCal",
            srcStart.minusMinutes(1), srcEnd.plusMinutes(1),
            "TargetCal", targetStart);
    assertTrue(copied);

    // Calculate expected time:
    // Source event is 9:00 CDT → UTC = 14:00
    // Target calendar is Kathmandu (UTC+5:45) → expected local = 14:00 + 5:45 = 19:45
    // So the new event on August 1 should be from 19:45 to 20:45 local time
    LocalDateTime expectedStart = LocalDateTime.of(2025, 8, 1, 19, 45);
    LocalDateTime expectedEnd = expectedStart.plusHours(1);

    // Validate copied event
    List<ICalendarEventDTO> copiedEvents = model.getEventsInRange(
            "TargetCal",
            expectedStart.minusMinutes(30),
            expectedEnd.plusMinutes(30)
    );
    assertEquals(1, copiedEvents.size());
    ICalendarEventDTO copiedEvent = copiedEvents.get(0);
    assertEquals("Half-Hour TZ Event", copiedEvent.getEventName());
    assertEquals(expectedStart, copiedEvent.getStartDateTime());
    assertEquals(expectedEnd, copiedEvent.getEndDateTime());
    assertEquals("Room Half", copiedEvent.getEventLocation());
  }

  @Test
  public void testCopyEvents_TargetTimezoneBehind() {
    model = new CalendarModel();
    model.createCalendar("SourceCal", "Asia/Tokyo");           // UTC+9
    model.createCalendar("TargetCal", "America/Los_Angeles");  // UTC-7 in June (PDT)

    // Source event: 2025-05-01 10:00–11:00 in Tokyo
    LocalDateTime srcStart = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime srcEnd = LocalDateTime.of(2025, 5, 1, 11, 0);

    ICalendarEventDTO event = CalendarEventDTO.builder()
            .setEventName("TimeZone Jump")
            .setStartDateTime(srcStart)
            .setEndDateTime(srcEnd)
            .setRecurring(false)
            .setAutoDecline(true)
            .setPrivate(false)
            .setEventLocation("Room TZ")
            .setEventDescription("Jump test")
            .build();
    assertTrue(model.addEvent("SourceCal", event));

    // Copy to 2025-06-01 in LA
    LocalDate targetStartDate = LocalDate.of(2025, 6, 1);
    assertTrue(model.copyEvents("SourceCal", srcStart, srcEnd, "TargetCal", targetStartDate));

    // Time conversion:
    // srcStart = 2025-05-01 10:00 JST → 2025-05-01 01:00 UTC
    // Target date = 2025-06-01 in LA (PDT = UTC-7)
    // New start = 2025-06-01 01:00 UTC → 2025-05-31 18:00 PDT
    LocalDateTime expectedStart = LocalDateTime.of(2025, 5, 31, 18, 0);
    LocalDateTime expectedEnd = expectedStart.plusHours(1);

    List<ICalendarEventDTO> events = model.getEventsInRange(
            "TargetCal",
            expectedStart.minusHours(1),
            expectedEnd.plusHours(1)
    );

    assertEquals(1, events.size());
    ICalendarEventDTO copied = events.get(0);
    assertEquals("TimeZone Jump", copied.getEventName());
    assertEquals("Room TZ", copied.getEventLocation());
    assertEquals("Jump test", copied.getEventDescription());
    assertEquals(expectedStart, copied.getStartDateTime());
    assertEquals(expectedEnd, copied.getEndDateTime());
  }

  @Test(expected = IllegalStateException.class)
  public void testCopyEvents_RecurringEvent_Conflict() {
    model.createCalendar("Src", "America/New_York");
    model.createCalendar("Dst", "America/New_York");

    LocalDateTime start = LocalDateTime.of(2025, 4, 7, 10, 0); // Monday
    LocalDateTime end = LocalDateTime.of(2025, 4, 7, 11, 0);

    ICalendarEventDTO recurringEvent = CalendarEventDTO.builder()
            .setEventName("Weekly Update")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setRecurring(true)
            .setRecurrenceCount(2)
            .setRecurrenceDays(List.of(DayOfWeek.MONDAY))
            .setEventDescription("Update series")
            .setEventLocation("NY Room")
            .setAutoDecline(true)
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("Src", recurringEvent));

    // Add a conflicting event in Dst
    LocalDateTime conflictStart = LocalDateTime.of(2025, 5, 5, 10, 0); // Should match second copy
    ICalendarEventDTO conflict = CalendarEventDTO.builder()
            .setEventName("Blocked Slot")
            .setStartDateTime(conflictStart)
            .setEndDateTime(conflictStart.plusHours(1))
            .setRecurring(false)
            .setEventDescription("Busy slot")
            .setEventLocation("Conflict Room")
            .setAutoDecline(true)
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("Dst", conflict));

    // Copy should throw due to conflict
    model.copyEvents("Src", start, start.plusDays(7), "Dst", LocalDate.of(2025, 4, 28));
  }







  @Test
  public void testCopyEventAtSourceBoundary() {
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    LocalDateTime start = LocalDateTime.of(2025, 12, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 12, 1, 11, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("BoundaryEvent")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Event at boundary")
            .setEventLocation("Room Boundary")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("SourceCal", eventDTO));

    // Use a source interval that exactly starts at the event's start time.
    boolean copied = model.copyEvents("SourceCal", start, end, "TargetCal", start.plusDays(1).toLocalDate());
    assertTrue(copied);
    List<ICalendarEventDTO> copiedEvents = model.getEventsInRange("TargetCal", start.plusDays(1).minusHours(1), start.plusDays(1).plusHours(1));
    assertEquals(1, copiedEvents.size());
  }

  @Test
  public void testCopyEventToSameCalendarWithDifferentName() {
    model.createCalendar("SameCal", "America/New_York");

    LocalDateTime start = LocalDateTime.of(2025, 4, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 4, 5, 11, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("OriginalEvent")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Original")
            .setEventLocation("Room A")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("SameCal", eventDTO));

    LocalDateTime newStart = LocalDateTime.of(2025, 4, 6, 10, 0);
    assertTrue(model.copyEvent("SameCal", start, "OriginalEvent", "SameCal", newStart));

    List<ICalendarEventDTO> events = model.getEventsInRange("SameCal", newStart.minusMinutes(1), newStart.plusHours(1));
    assertEquals(1, events.size());
    assertEquals("OriginalEvent", events.get(0).getEventName());
  }

  @Test
  public void testCopyEventWithEmptyFields() {
    model.createCalendar("TestCal", "America/New_York");
    model.createCalendar("CopyCal", "America/New_York");

    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 14, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 15, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("EmptyEvent")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(false)
            .setRecurring(false)
            .setEventDescription("")
            .setEventLocation("")
            .setPrivate(true)
            .build();
    assertTrue(model.addEvent("TestCal", eventDTO));

    LocalDateTime newStart = LocalDateTime.of(2025, 6, 5, 14, 0);
    assertTrue(model.copyEvent("TestCal", start, "EmptyEvent", "CopyCal", newStart));

    List<ICalendarEventDTO> events = model.getEventsInRange("CopyCal", newStart.minusMinutes(1), newStart.plusHours(1));
    assertEquals(1, events.size());
    assertEquals("", events.get(0).getEventDescription());
    assertEquals("", events.get(0).getEventLocation());
  }

  @Test
  public void testCopySingleOccurrenceOfRecurringEvent() {
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    LocalDateTime start = LocalDateTime.of(2025, 5, 12, 10, 0); // Monday
    LocalDateTime end = LocalDateTime.of(2025, 5, 12, 11, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("WeeklyCall")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(true)
            .setRecurrenceCount(3)
            .setRecurrenceDays(Collections.singletonList(DayOfWeek.MONDAY))
            .setEventDescription("Recurring call")
            .setEventLocation("Room 303")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("SourceCal", eventDTO));

    // Copy only first occurrence to target calendar
    LocalDateTime targetStart = LocalDateTime.of(2025, 6, 1, 9, 0);
    assertTrue(model.copyEvent("SourceCal", start, "WeeklyCall", "TargetCal", targetStart));

    List<ICalendarEventDTO> targetEvents = model.getEventsInRange("TargetCal", targetStart.minusMinutes(1), targetStart.plusHours(1));
    assertEquals(1, targetEvents.size());
    assertEquals("WeeklyCall", targetEvents.get(0).getEventName());
  }

  @Test
  public void testCalendarEventDTOBuilderMissingEventName() {
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName(null)  // explicitly setting eventName to null
            .setStartDateTime(LocalDateTime.of(2025, 10, 1, 9, 0))
            .setEndDateTime(LocalDateTime.of(2025, 10, 1, 10, 0))
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Missing event name")
            .setEventLocation("Room X")
            .setPrivate(false)
            .build();

    assertNull("Expected event name to be null", eventDTO.getEventName());
  }

  @Test
  public void testCopyEventSuccess() {
    // Create source and target calendars.
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    // Create a non-recurring event in the source calendar.
    LocalDateTime sourceStart = LocalDateTime.of(2025, 1, 1, 9, 0);
    LocalDateTime sourceEnd = LocalDateTime.of(2025, 1, 1, 10, 0);
    ICalendarEventDTO sourceEvent = CalendarEventDTO.builder()
            .setEventName("Meeting")
            .setStartDateTime(sourceStart)
            .setEndDateTime(sourceEnd)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Team meeting")
            .setEventLocation("Room 101")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("SourceCal", sourceEvent));

    // Copy the event from SourceCal to TargetCal.
    // In the copy, the event should start at targetStart.
    LocalDateTime targetStart = LocalDateTime.of(2025, 2, 1, 11, 0);
    boolean copied = model.copyEvent("SourceCal", sourceStart, "Meeting", "TargetCal", targetStart);
    assertTrue(copied);

    // Verify that the event in the target calendar has the correct new start and end times.
    List<ICalendarEventDTO> targetEvents = model.getEventsInRange("TargetCal",
            targetStart.minusMinutes(1), targetStart.plusHours(1));
    assertEquals(1, targetEvents.size());
    ICalendarEventDTO copiedEvent = targetEvents.get(0);
    // The new event's start should equal targetStart.
    assertEquals(targetStart, copiedEvent.getStartDateTime());
    // The duration should be the same as the original (1 hour).
    assertEquals(targetStart.plusHours(1), copiedEvent.getEndDateTime());
    // The event name, description, and location should be the same.
    assertEquals("Meeting", copiedEvent.getEventName());
    assertEquals("Team meeting", copiedEvent.getEventDescription());
    assertEquals("Room 101", copiedEvent.getEventLocation());
  }

  // Test 2: Attempt to copy an event that does not exist in the source calendar.
  @Test(expected = IllegalStateException.class)
  public void testCopyEventNotFound() {
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    // There is no event in SourceCal starting at this time with the given name.
    LocalDateTime eventTime = LocalDateTime.of(2025, 1, 1, 9, 0);
    LocalDateTime targetStart = LocalDateTime.of(2025, 2, 1, 11, 0);
    model.copyEvent("SourceCal", eventTime, "NonExistentEvent", "TargetCal", targetStart);
  }

  // Test 3: Attempt to copy an event when the target calendar already has a conflicting event.
  @Test(expected = IllegalStateException.class)
  public void testCopyEventConflict() {
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    // Add a non-recurring event in the source calendar.
    LocalDateTime sourceStart = LocalDateTime.of(2025, 1, 1, 9, 0);
    LocalDateTime sourceEnd = LocalDateTime.of(2025, 1, 1, 10, 0);
    ICalendarEventDTO sourceEvent = CalendarEventDTO.builder()
            .setEventName("Meeting")
            .setStartDateTime(sourceStart)
            .setEndDateTime(sourceEnd)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Team meeting")
            .setEventLocation("Room 101")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("SourceCal", sourceEvent));

    // Add an event in the target calendar that conflicts with the copied event.
    // For example, the target event occupies the time slot we want to copy into.
    LocalDateTime targetStart = LocalDateTime.of(2025, 2, 1, 11, 0);
    ICalendarEventDTO conflictingEvent = CalendarEventDTO.builder()
            .setEventName("ConflictingEvent")
            .setStartDateTime(targetStart)
            .setEndDateTime(targetStart.plusHours(1))
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Conflicting event")
            .setEventLocation("Room X")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TargetCal", conflictingEvent));

    // Attempt to copy the event from SourceCal to TargetCal.
    model.copyEvent("SourceCal", sourceStart, "Meeting", "TargetCal", targetStart);
  }

  // Test 4: Attempt to copy an event with invalid (null) parameters.
  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventNullParameters() {
    // If any parameter is null, the method should throw an exception.
    model.copyEvent(null, LocalDateTime.now(), "Meeting", "TargetCal", LocalDateTime.now());
  }

  @Test
  public void testCopyEventWithinSameCalendar() {
    model.createCalendar("SameCal", "America/New_York");

    LocalDateTime originalStart = LocalDateTime.of(2025, 3, 1, 9, 0);
    LocalDateTime originalEnd = LocalDateTime.of(2025, 3, 1, 10, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Team Meeting")
            .setStartDateTime(originalStart)
            .setEndDateTime(originalEnd)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Monthly team sync")
            .setEventLocation("Conference Room 1")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("SameCal", eventDTO));

    // Copy the event within the same calendar, moving it one day later.
    LocalDateTime newStart = LocalDateTime.of(2025, 3, 2, 9, 0);
    boolean copied = model.copyEvent("SameCal", originalStart, "Team Meeting", "SameCal", newStart);
    assertTrue(copied);

    // Verify new event exists in SameCal at the new time.
    List<ICalendarEventDTO> events = model.getEventsInRange("SameCal", newStart.minusMinutes(1), newStart.plusHours(1));
    assertEquals(1, events.size());
    assertEquals("Team Meeting", events.get(0).getEventName());
    assertEquals(newStart, events.get(0).getStartDateTime());
  }

  @Test
  public void testCopyEventCaseInsensitiveName() {
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    LocalDateTime sourceStart = LocalDateTime.of(2025, 4, 1, 14, 0);
    LocalDateTime sourceEnd = LocalDateTime.of(2025, 4, 1, 15, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Workshop")
            .setStartDateTime(sourceStart)
            .setEndDateTime(sourceEnd)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Technical workshop")
            .setEventLocation("Lab 1")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("SourceCal", eventDTO));

    // Use a different case for eventName parameter.
    LocalDateTime targetStart = LocalDateTime.of(2025, 5, 1, 14, 0);
    boolean copied = model.copyEvent("SourceCal", sourceStart, "workshop", "TargetCal", targetStart);
    assertTrue(copied);
  }

  @Test(expected = IllegalStateException.class)
  public void testCopyEventBoundaryConflict() {
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    LocalDateTime sourceStart = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime sourceEnd = LocalDateTime.of(2025, 6, 1, 10, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Seminar")
            .setStartDateTime(sourceStart)
            .setEndDateTime(sourceEnd)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Educational seminar")
            .setEventLocation("Auditorium")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("SourceCal", eventDTO));

    // In TargetCal, add an event that starts exactly at the target time.
    LocalDateTime conflictStart = LocalDateTime.of(2025, 7, 1, 11, 0);
    ICalendarEventDTO conflictEvent = CalendarEventDTO.builder()
            .setEventName("ExistingEvent")
            .setStartDateTime(conflictStart)
            .setEndDateTime(conflictStart.plusHours(1))
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Already scheduled")
            .setEventLocation("Room Z")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TargetCal", conflictEvent));

    // Attempt to copy the "Seminar" event so that its new start equals conflictStart.
    model.copyEvent("SourceCal", sourceStart, "Seminar", "TargetCal", conflictStart);
  }

  @Test
  public void testCopyRecurringEventOccurrence() {
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    // Create a recurring event in SourceCal.
    LocalDateTime start = LocalDateTime.of(2025, 8, 4, 10, 0); // Assume this is a Monday.
    LocalDateTime end = LocalDateTime.of(2025, 8, 4, 11, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Weekly Meeting")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(true)
            .setRecurrenceCount(3)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.MONDAY))
            .setEventDescription("Recurring meeting")
            .setEventLocation("Room 101")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("SourceCal", eventDTO));

    // Assume we want to copy the occurrence that starts on the first Monday (start).
    LocalDateTime targetStart = LocalDateTime.of(2025, 9, 1, 10, 0); // New start time for the copied occurrence.
    boolean copied = model.copyEvent("SourceCal", start, "Weekly Meeting", "TargetCal", targetStart);
    assertTrue(copied);

    // Verify that the target calendar now has an event with the same details.
    List<ICalendarEventDTO> targetEvents = model.getEventsInRange("TargetCal", targetStart.minusMinutes(1), targetStart.plusHours(1));
    assertEquals(1, targetEvents.size());
    ICalendarEventDTO copiedEvent = targetEvents.get(0);
    assertEquals("Weekly Meeting", copiedEvent.getEventName());
    assertEquals(targetStart, copiedEvent.getStartDateTime());
    assertEquals(targetStart.plusHours(1), copiedEvent.getEndDateTime());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventNullParametersforEvent() {
    model.copyEvent(null, LocalDateTime.now(), "Event", "TargetCal", LocalDateTime.now());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventSourceCalendarNotFound() {
    // Attempt to copy an event from a non-existing source calendar.
    LocalDateTime eventTime = LocalDateTime.of(2025, 1, 1, 9, 0);
    LocalDateTime targetStart = LocalDateTime.of(2025, 2, 1, 11, 0);
    // "NonExistentSource" does not exist.
    model.copyEvent("NonExistentSource", eventTime, "SomeEvent", "TargetCal", targetStart);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventTargetCalendarNotFound() {
    // Create a source calendar and add an event.
    model.createCalendar("SourceCal", "America/New_York");
    LocalDateTime sourceStart = LocalDateTime.of(2025, 1, 1, 9, 0);
    LocalDateTime sourceEnd = LocalDateTime.of(2025, 1, 1, 10, 0);
    ICalendarEventDTO sourceEvent = CalendarEventDTO.builder()
            .setEventName("Meeting")
            .setStartDateTime(sourceStart)
            .setEndDateTime(sourceEnd)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Team meeting")
            .setEventLocation("Room 101")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("SourceCal", sourceEvent));

    // Attempt to copy into a non-existing target calendar.
    LocalDateTime targetStart = LocalDateTime.of(2025, 2, 1, 11, 0);
    model.copyEvent("SourceCal", sourceStart, "Meeting", "NonExistentTarget", targetStart);
  }

  @Test(expected = IllegalStateException.class)
  public void testCopyEventNameMismatch() {
    // Create source and target calendars.
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    // Add an event to the source calendar.
    LocalDateTime sourceStart = LocalDateTime.of(2025, 1, 1, 9, 0);
    LocalDateTime sourceEnd = LocalDateTime.of(2025, 1, 1, 10, 0);
    ICalendarEventDTO sourceEvent = CalendarEventDTO.builder()
            .setEventName("Meeting")
            .setStartDateTime(sourceStart)
            .setEndDateTime(sourceEnd)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Team meeting")
            .setEventLocation("Room 101")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("SourceCal", sourceEvent));

    // Attempt to copy with a name that doesn't match (even though the time is correct).
    LocalDateTime targetStart = LocalDateTime.of(2025, 2, 1, 11, 0);
    model.copyEvent("SourceCal", sourceStart, "NonMatchingName", "TargetCal", targetStart);
  }

  @Test
  public void testEditEvents_UpdateLocation_SingleOccurrence() {
    // Create the calendar "TestCal" before adding or editing events.
    model.createCalendar("TestCal", "America/New_York");

    LocalDateTime start = LocalDateTime.of(2025, 6, 10, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 10, 11, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Meeting")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Team meeting")
            .setEventLocation("Room A")
            .setPrivate(false)
            .build();

    // Now, add the event using the correct calendar name.
    assertTrue(model.addEvent("TestCal", eventDTO));

    // Edit event's location.
    boolean edited = model.editEvents("TestCal", "location", "Meeting", start, "Room B", false);
    assertTrue(edited);

    List<ICalendarEventDTO> events = model.getEventsInRange("TestCal", start.minusMinutes(1), end.plusMinutes(1));
    assertEquals("Room B", events.get(0).getEventLocation());
  }


  // Test: Update ispublic with a numeric value ("1") should throw an exception.
  @Test(expected = IllegalArgumentException.class)
  public void testEditEvents_IsPublicWithNumericValue_ShouldFail() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 12, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 12, 11, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("BooleanTest")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Boolean test")
            .setEventLocation("Room Y")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TestCal", eventDTO));
    model.editEvents("TestCal", "ispublic", "BooleanTest", start, "1", false);
  }

  // Test: Update event end time to an invalid time (not after start) should throw an exception.
  @Test(expected = IllegalArgumentException.class)
  public void testEditEvents_InvalidEndTime_ShouldFail() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 13, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 13, 11, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("TimeTest")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Time test")
            .setEventLocation("Room Z")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TestCal", eventDTO));
    // Attempt to set end time equal to start time.
    model.editEvents("TestCal", "end", "TimeTest", start, "2025-06-13T10:00:00", false);
  }

  // Test: No matching event due to incorrect fromDateTime should throw exception.
  @Test(expected = IllegalStateException.class)
  public void testEditEvents_NoMatchingEvent_ShouldFail() {
    // Create the calendar so it exists.
    model.createCalendar("TestCal", "America/New_York");
    LocalDateTime fromTime = LocalDateTime.of(2025, 6, 1, 10, 0);
    // Since no event with name "NonExistent" exists in TestCal, the method should throw
    // an IllegalStateException indicating no matching event found.
    model.editEvents("TestCal", "location", "NonExistent", fromTime, "Room X", false);
  }


  // Test: Editing an event so that it overlaps with another should trigger a conflict.
  @Test(expected = IllegalStateException.class)
  public void testEditEvents_ConflictAfterEdit_ShouldFail() {
    // Create the calendar "TestCal" so that it can be found.
    model.createCalendar("TestCal", "America/New_York");

    LocalDateTime start1 = LocalDateTime.of(2025, 6, 16, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 16, 11, 0);
    ICalendarEventDTO eventDTO1 = CalendarEventDTO.builder()
            .setEventName("EventOne")
            .setStartDateTime(start1)
            .setEndDateTime(end1)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("First event")
            .setEventLocation("Room A")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TestCal", eventDTO1));

    LocalDateTime start2 = LocalDateTime.of(2025, 6, 16, 11, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 16, 12, 0);
    ICalendarEventDTO eventDTO2 = CalendarEventDTO.builder()
            .setEventName("EventTwo")
            .setStartDateTime(start2)
            .setEndDateTime(end2)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Second event")
            .setEventLocation("Room B")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TestCal", eventDTO2));

    // Now attempt to edit EventTwo's start time to 10:30, which would overlap with EventOne.
    model.editEvents("TestCal", "start", "EventTwo", start2, "2025-06-16T10:30:00", false);
  }

  // ----- New Value Validation -----

  // Test: Editing with the literal "null" as new value should throw exception.
  @Test(expected = IllegalArgumentException.class)
  public void testEditEvents_NullNewValue_ShouldFail() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 17, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 17, 11, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("TestNull")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Null test")
            .setEventLocation("Room N")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TestCal", eventDTO));
    model.editEvents("TestCal", "location", "TestNull", start, "null", false);
  }

  // Test: Editing with an empty new value for a non-name property should fail.
  @Test(expected = IllegalArgumentException.class)
  public void testEditEvents_EmptyNewValueForNonName_ShouldFail() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 18, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 18, 11, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("TestEmpty")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Desc")
            .setEventLocation("Room O")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TestCal", eventDTO));
    model.editEvents("TestCal", "location", "TestEmpty", start, "", false);
  }

  @Test
  public void testEditEvents_UpdateNameToEmpty_ShouldUpdate() {
    model = new CalendarModel();
    model.createCalendar("TestCal", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 6, 11, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 11, 11, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Meeting")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Team meeting")
            .setEventLocation("Room A")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TestCal", eventDTO));

    // For name property, empty string is allowed.
    boolean edited = model.editEvents("TestCal", "name", "Meeting", start, "", false);
    assertTrue(edited);

    List<ICalendarEventDTO> events = model.getEventsInRange("TestCal", start.minusMinutes(1), end.plusMinutes(1));
    assertEquals("", events.get(0).getEventName());
  }



  // Test: Editing with an empty new value (for non-name property) should throw an exception.
  @Test(expected = IllegalArgumentException.class)
  public void testEditEvents_EmptyNewValue_ShouldFail() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 13, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 13, 11, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("TimeTest")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Test event")
            .setEventLocation("Room Z")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TestCal", eventDTO));
    // Empty new value for location should trigger an exception.
    model.editEvents("TestCal", "location", "TimeTest", start, "", false);
  }



  // Test: Attempting to edit an unsupported property should throw an exception.
  @Test(expected = IllegalArgumentException.class)
  public void testEditEvents_UnsupportedProperty_ShouldFail() {
    model = new CalendarModel();
    model.createCalendar("TestCal", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 6, 18, 10, 0);
    // Create an event.
    LocalDateTime end = LocalDateTime.of(2025, 6, 18, 11, 0);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Demo")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Demo event")
            .setEventLocation("Room D")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TestCal", eventDTO));
    // Attempt to edit property "color" which is unsupported.
    model.editEvents("TestCal", "color", "Demo", start, "Red", false);
  }

  // Test: Update the event description.
  @Test
  public void testEditEvents_UpdateDescription_SingleOccurrence() {
    model = new CalendarModel();
    model.createCalendar("TestCal", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 7, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 1, 10, 0);

    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Briefing")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Morning briefing")
            .setEventLocation("Room 101")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TestCal", eventDTO));

    // Update description to "Updated briefing".
    boolean edited = model.editEvents("TestCal", "description", "Briefing", start, "Updated briefing", false);
    assertTrue(edited);

    List<ICalendarEventDTO> events = model.getEventsInRange("TestCal", start.minusMinutes(1), end.plusMinutes(1));
    assertEquals("Updated briefing", events.get(0).getEventDescription());
  }

  // Test: Update the event's start time to a valid new time.
  @Test
  public void testEditEvents_UpdateStartTime_SingleOccurrence() {
    model = new CalendarModel();
    model.createCalendar("TestCal", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 7, 2, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 2, 10, 0);

    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Workshop")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Tech workshop")
            .setEventLocation("Lab")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TestCal", eventDTO));

    // Change start time to 08:45.
    boolean edited = model.editEvents("TestCal", "start", "Workshop", start, "2025-07-02T08:45:00", false);
    assertTrue(edited);

    List<ICalendarEventDTO> events = model.getEventsInRange("TestCal", LocalDateTime.parse("2025-07-02T08:40:00"), end.plusMinutes(1));
    assertEquals(LocalDateTime.parse("2025-07-02T08:45:00"), events.get(0).getStartDateTime());
  }

  // Test: Update the event's end time to a valid new time.
  @Test
  public void testEditEvents_UpdateEndTime_SingleOccurrence() {
    model = new CalendarModel();
    model.createCalendar("TestCal", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 7, 3, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 3, 10, 0);

    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Training")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Skill training")
            .setEventLocation("Room 202")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TestCal", eventDTO));

    // Change end time to 10:15.
    boolean edited = model.editEvents("TestCal", "end", "Training", start, "2025-07-03T10:15:00", false);
    assertTrue(edited);

    List<ICalendarEventDTO> events = model.getEventsInRange("TestCal", start.minusMinutes(1), LocalDateTime.parse("2025-07-03T10:20:00"));
    assertEquals(LocalDateTime.parse("2025-07-03T10:15:00"), events.get(0).getEndDateTime());
  }

  // Test: Update the "ispublic" property to true.
  @Test
  public void testEditEvents_UpdateIsPrivate_SingleOccurrence() {
    model = new CalendarModel();
    model.createCalendar("TestCal", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 7, 4, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 4, 10, 0);

    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Briefing")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Morning briefing")
            .setEventLocation("Room 101")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TestCal", eventDTO));

    // Update ispublic by setting the property with the string "true".
    boolean edited = model.editEvents("TestCal", "isprivate", "Briefing", start, "true", false);
    assertTrue(edited);

    List<ICalendarEventDTO> events = model.getEventsInRange("TestCal", start.minusMinutes(1), end.plusMinutes(1));
    // Since the event was initially not private, setting ispublic to true means isPrivate remains false.
    assertTrue(events.get(0).isPrivate());
  }

  // ----- Multiple Occurrence (Recurring) Edit Tests -----

  // Test: Edit a recurring event's name for all occurrences.
  @Test
  public void testEditEvents_MultipleOccurrences_EditAllTrue_ShouldUpdateAll() {
    model = new CalendarModel();
    model.createCalendar("TestCal", "America/New_York");
    // Create a recurring event with 3 occurrences.
    LocalDateTime start = LocalDateTime.of(2025, 7, 5, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 5, 9, 30);
    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Standup")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(true)
            .setRecurrenceCount(3)
            // Ensure the recurrence day matches the start day (e.g., Saturday if 2025-07-05 is Saturday).
            .setRecurrenceDays(Arrays.asList(DayOfWeek.SATURDAY))
            .setEventDescription("Recurring meeting")
            .setEventLocation("Room A")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("TestCal", eventDTO));

    // Update the event name to "DailySync" for all occurrences.
    boolean edited = model.editEvents("TestCal", "name", "Standup", start, "DailySync", true);
    assertTrue(edited);

    List<ICalendarEventDTO> events = model.getEventsInRange("TestCal", start.minusDays(1), end.plusDays(7));
    // All occurrences should have the new name.
    for (ICalendarEventDTO event : events) {
      assertEquals("DailySync", event.getEventName());
    }

  }

  //Across calendar tests

  @Test
  public void testCopyEventAcrossTimezones_Simple() {
    model = new CalendarModel();
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "Asia/Kolkata");

    LocalDateTime sourceStart = LocalDateTime.of(2025, 5, 5, 19, 0); // 7 PM EDT
    LocalDateTime sourceEnd = sourceStart.plusHours(1);

    ICalendarEventDTO event = CalendarEventDTO.builder()
            .setEventName("TimeShiftedMeeting")
            .setStartDateTime(sourceStart)
            .setEndDateTime(sourceEnd)
            .setRecurring(false)
            .setAutoDecline(true)
            .setPrivate(false)
            .setEventDescription("Timezone test")
            .setEventLocation("NY Office")
            .build();

    assertTrue(model.addEvent("SourceCal", event));

    LocalDate targetDate = LocalDate.of(2025, 5, 6); // Target in IST
    assertTrue(model.copyEvents("SourceCal", sourceStart, sourceEnd, "TargetCal", targetDate));

    // Correct time in IST = 2025-05-06 04:30
    LocalDateTime expectedStart = LocalDateTime.of(2025, 5, 7, 4, 30);
    LocalDateTime expectedEnd = expectedStart.plusHours(1);

    List<ICalendarEventDTO> events = model.getEventsInRange("TargetCal", expectedStart.minusMinutes(1), expectedEnd.plusMinutes(1));
    assertEquals(1, events.size());

    ICalendarEventDTO copied = events.get(0);
    assertEquals("TimeShiftedMeeting", copied.getEventName());
    assertEquals(expectedStart, copied.getStartDateTime());
    assertEquals(expectedEnd, copied.getEndDateTime());
  }

  @Test
  public void testCopyEventWithinSameCalendar_NonOverlapping() {
    model.createCalendar("MyCal", "America/New_York");

    LocalDateTime start = LocalDateTime.of(2025, 7, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 1, 10, 0);

    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Planning")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Self copy")
            .setEventLocation("Room Z")
            .setPrivate(false)
            .build();

    assertTrue(model.addEvent("MyCal", eventDTO));

    LocalDateTime targetStart = start.plusDays(1);
    assertTrue(model.copyEvents("MyCal", start, end, "MyCal", targetStart.toLocalDate()));

    List<ICalendarEventDTO> result = model.getEventsInRange("MyCal", targetStart.minusMinutes(10), targetStart.plusHours(1));
    assertEquals(1, result.size());
    assertEquals("Planning", result.get(0).getEventName());
  }

  @Test(expected = IllegalStateException.class)
  public void testCopyEventsConflictInTargetCalendar_ShouldFail() {
    model = new CalendarModel();
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    // Source event: April 1, 2025, 9–10 AM
    LocalDateTime srcStart = LocalDateTime.of(2025, 4, 1, 9, 0);
    LocalDateTime srcEnd = LocalDateTime.of(2025, 4, 1, 10, 0);

    ICalendarEventDTO event1 = CalendarEventDTO.builder()
            .setEventName("Morning Brief")
            .setStartDateTime(srcStart)
            .setEndDateTime(srcEnd)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Briefing")
            .setEventLocation("Room A")
            .setPrivate(false)
            .build();

    assertTrue(model.addEvent("SourceCal", event1));

    // This event in TargetCal will conflict with the copied event's time
    // We're assuming the copy aligns srcStart (9:00) to targetStartDate (May 1)
    // So expected copied start = May 1, 9:00 — which will overlap with 11–12 below if durations overlap
    // To trigger conflict, align event1 to 11:00–12:00
    ICalendarEventDTO conflicting = CalendarEventDTO.builder()
            .setEventName("Conflict")
            .setStartDateTime(LocalDateTime.of(2025, 5, 1, 9, 30))
            .setEndDateTime(LocalDateTime.of(2025, 5, 1, 10, 30))
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Existing")
            .setEventLocation("Room X")
            .setPrivate(false)
            .build();

    assertTrue(model.addEvent("TargetCal", conflicting));

    // Copy event from SourceCal (9:00–10:00) into May 1 (targetStart = May 1)
    // Since both calendars are in the same timezone, newStart = 2025-05-01 09:00
    // This will overlap with existing event (9:30–10:30) → should throw conflict exception
    model.copyEvents("SourceCal", srcStart, srcEnd, "TargetCal", LocalDate.of(2025, 5, 1));
  }

  @Test
  public void testCopyRecurringEventAcrossCalendars_TimezoneShifted() {
    model = new CalendarModel();
    model.createCalendar("SourceCal", "America/New_York");  // UTC-4 (EDT in March)
    model.createCalendar("TargetCal", "Europe/London");     // UTC+0 (still GMT before Mar 30)

    // Original recurring event: 10:00–11:00 EDT on March 10 and March 17
    LocalDateTime start = LocalDateTime.of(2025, 3, 10, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 10, 11, 0);

    ICalendarEventDTO recurring = CalendarEventDTO.builder()
            .setEventName("Sync")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setRecurring(true)
            .setRecurrenceCount(2)
            .setRecurrenceDays(Arrays.asList(DayOfWeek.MONDAY))
            .setEventDescription("Cross timezones")
            .setEventLocation("HQ")
            .setPrivate(false)
            .setAutoDecline(true)
            .build();

    assertTrue(model.addEvent("SourceCal", recurring));

    // Target calendar's first event will align with 2025-03-17 in London
    LocalDate targetStartDate = LocalDate.of(2025, 3, 17);
    boolean copied = model.copyEvents("SourceCal", start, start.plusWeeks(1), "TargetCal", targetStartDate);
    assertTrue(copied);

    // New York time was 10:00 EDT (UTC-4) → 14:00 London time (UTC+0)
    // So the copied event should be on 2025-03-17 and 2025-03-24 at 14:00–15:00 in London time

    LocalDateTime expectedStart1 = LocalDateTime.of(2025, 3, 17, 14, 0);
    LocalDateTime expectedEnd1 = expectedStart1.plusHours(1);
    LocalDateTime expectedStart2 = LocalDateTime.of(2025, 3, 24, 14, 0);
    LocalDateTime expectedEnd2 = expectedStart2.plusHours(1);

    List<ICalendarEventDTO> result = model.getEventsInRange("TargetCal", expectedStart1.minusDays(1), expectedEnd2.plusHours(1));
    assertEquals(2, result.size());

    // Validate both recurring instances
    boolean foundFirst = false, foundSecond = false;

    for (ICalendarEventDTO e : result) {
      if (e.getStartDateTime().equals(expectedStart1)) {
        assertEquals("Sync", e.getEventName());
        assertEquals(expectedEnd1, e.getEndDateTime());
        assertEquals("Cross timezones", e.getEventDescription());
        foundFirst = true;
      } else if (e.getStartDateTime().equals(expectedStart2)) {
        assertEquals("Sync", e.getEventName());
        assertEquals(expectedEnd2, e.getEndDateTime());
        assertEquals("Cross timezones", e.getEventDescription());
        foundSecond = true;
      }
    }

    assertTrue(foundFirst && foundSecond);
  }

  @Test
  public void testCopyEvent_NameCaseMismatch_ShouldCopy() {
    model.createCalendar("Source", "America/New_York");
    model.createCalendar("Target", "America/New_York");

    LocalDateTime start = LocalDateTime.of(2025, 9, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 9, 1, 10, 0);
    ICalendarEventDTO event = CalendarEventDTO.builder()
            .setEventName("ProjectMeeting")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Project sync-up")
            .setEventLocation("Room A")
            .setPrivate(false)
            .build();
    model.addEvent("Source", event);

    // Use lowercase name during copy
    LocalDateTime targetStart = LocalDateTime.of(2025, 10, 1, 9, 0);
    boolean copied = model.copyEvent("Source", start, "projectmeeting", "Target", targetStart);
    assertTrue(copied);

    List<ICalendarEventDTO> copiedEvents = model.getEventsInRange("Target", targetStart.minusMinutes(1), targetStart.plusMinutes(90));
    assertEquals(1, copiedEvents.size());
    assertEquals("ProjectMeeting", copiedEvents.get(0).getEventName());
  }

  @Test
  public void testCopyEvent_DifferentTimeZones_DSTBoundary() {
    model.createCalendar("PSTCal", "America/Los_Angeles");
    model.createCalendar("ISTCal", "Asia/Kolkata");

    // DST ends in US around November 2, 2025
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 1, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 2, 0);
    ICalendarEventDTO event = CalendarEventDTO.builder()
            .setEventName("DST Test")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setRecurring(false)
            .setAutoDecline(true)
            .setEventLocation("Virtual")
            .setEventDescription("DST edge case")
            .setPrivate(false)
            .build();

    model.addEvent("PSTCal", event);

    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 2, 13, 30); // IST
    boolean copied = model.copyEvent("PSTCal", start, "DST Test", "ISTCal", targetStart);
    assertTrue(copied);
  }

  @Test
  public void testCopyRecurringEvent_UTCtoEST_NoConflict() {
    model.createCalendar("UTC_Cal", "UTC");
    model.createCalendar("EST_Cal", "America/New_York");

    // Step 1: Create a recurring event in the source (UTC)
    LocalDateTime start = LocalDateTime.of(2025, 10, 6, 14, 0); // Monday UTC
    LocalDateTime end = LocalDateTime.of(2025, 10, 6, 15, 0);   // 1-hour event

    ICalendarEventDTO event = CalendarEventDTO.builder()
            .setEventName("Global Sync")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setRecurring(true)
            .setRecurrenceCount(2)
            .setRecurrenceDays(List.of(DayOfWeek.MONDAY))
            .setEventDescription("Cross-timezone sync")
            .setEventLocation("Zoom")
            .setAutoDecline(true)
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("UTC_Cal", event));

    // Step 2: Ensure there is no conflicting event in the target calendar
    // (If there was, we would remove or skip this setup.)
    // For safety, we make sure target calendar is empty initially.

    // Step 3: Define the copy interval and target start
    LocalDateTime copyIntervalStart = start.minusDays(1); // Oct 5, 2025
    LocalDateTime copyIntervalEnd = start.plusDays(8);    // Oct 14, 2025
    LocalDate targetStartDate = LocalDate.of(2025, 11, 3); // First copied event should align here

    boolean copied = model.copyEvents("UTC_Cal", copyIntervalStart, copyIntervalEnd, "EST_Cal", targetStartDate);
    assertTrue(copied);

    // Expected times in target calendar after conversion (UTC → EST)
    // 14:00 UTC → 09:00 EST in Fall (UTC-5)
    LocalDateTime expectedStart1 = LocalDateTime.of(2025, 11, 4, 10, 0);
    LocalDateTime expectedEnd1 = expectedStart1.plusHours(1);
    LocalDateTime expectedStart2 = LocalDateTime.of(2025, 11, 11, 10, 0);
    LocalDateTime expectedEnd2 = expectedStart2.plusHours(1);

    List<ICalendarEventDTO> copiedEvents = model.getEventsInRange("EST_Cal",
            expectedStart1.minusHours(1), expectedEnd2.plusHours(1));
    assertEquals(2, copiedEvents.size());

    // Validate events
    boolean foundFirst = false;
    boolean foundSecond = false;

    for (ICalendarEventDTO e : copiedEvents) {
      if (e.getStartDateTime().equals(expectedStart1) && e.getEndDateTime().equals(expectedEnd1)) {
        foundFirst = true;
        assertEquals("Global Sync", e.getEventName());
      } else if (e.getStartDateTime().equals(expectedStart2) && e.getEndDateTime().equals(expectedEnd2)) {
        foundSecond = true;
        assertEquals("Global Sync", e.getEventName());
      }
    }

    assertTrue(foundFirst);
    assertTrue(foundSecond);
  }

  @Test
  public void testCopyEvent_DaylightSavingShift() {
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "Europe/London");

    LocalDateTime sourceStart = LocalDateTime.of(2025, 3, 9, 1, 30); // DST shift in NY
    LocalDateTime sourceEnd = LocalDateTime.of(2025, 3, 9, 2, 30);

    ICalendarEventDTO event = CalendarEventDTO.builder()
            .setEventName("DST Meeting")
            .setStartDateTime(sourceStart)
            .setEndDateTime(sourceEnd)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Spring DST shift")
            .setEventLocation("NY Office")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("SourceCal", event));

    LocalDateTime targetStart = LocalDateTime.of(2025, 3, 10, 6, 30); // London time

    assertTrue(model.copyEvent("SourceCal", sourceStart, "DST Meeting", "TargetCal", targetStart));

    List<ICalendarEventDTO> copiedEvents = model.getEventsInRange("TargetCal", targetStart.minusMinutes(1), targetStart.plusHours(2));
    assertEquals(1, copiedEvents.size());
    assertEquals("DST Meeting", copiedEvents.get(0).getEventName());
    assertEquals(targetStart, copiedEvents.get(0).getStartDateTime());
  }

  @Test
  public void testCopyEvents_MultipleEventsAcrossTimezones() {
    model = new CalendarModel();
    model.createCalendar("SourceCal", "Asia/Kolkata");
    model.createCalendar("TargetCal", "America/Los_Angeles");

    LocalDateTime sourceStart1 = LocalDateTime.of(2025, 4, 1, 9, 0);
    LocalDateTime sourceEnd1 = sourceStart1.plusHours(1);
    LocalDateTime sourceStart2 = LocalDateTime.of(2025, 4, 1, 16, 0);
    LocalDateTime sourceEnd2 = sourceStart2.plusHours(2);

    ICalendarEventDTO event1 = CalendarEventDTO.builder()
            .setEventName("Morning Call")
            .setStartDateTime(sourceStart1)
            .setEndDateTime(sourceEnd1)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Daily sync")
            .setEventLocation("Zoom")
            .setPrivate(false)
            .build();

    ICalendarEventDTO event2 = CalendarEventDTO.builder()
            .setEventName("Evening Wrap")
            .setStartDateTime(sourceStart2)
            .setEndDateTime(sourceEnd2)
            .setAutoDecline(true)
            .setRecurring(false)
            .setEventDescription("Wrap-up meeting")
            .setEventLocation("Office")
            .setPrivate(false)
            .build();

    assertTrue(model.addEvent("SourceCal", event1));
    assertTrue(model.addEvent("SourceCal", event2));

    LocalDate targetStartDate = LocalDate.of(2025, 4, 2);
    assertTrue(model.copyEvents("SourceCal", sourceStart1, sourceEnd2, "TargetCal", targetStartDate));

    LocalDateTime expectedStart1 = LocalDateTime.of(2025, 4, 1, 20, 30);  // 9 AM PDT
    LocalDateTime expectedEnd1 = expectedStart1.plusHours(1);

    LocalDateTime expectedStart2 = LocalDateTime.of(2025, 4, 2, 3, 30); // 4 PM PDT
    LocalDateTime expectedEnd2 = expectedStart2.plusHours(2);

    List<ICalendarEventDTO> copiedEvents = model.getEventsInRange(
            "TargetCal",
            targetStartDate.minusDays(1).atStartOfDay(),
            targetStartDate.atTime(23, 59)
    );

    assertEquals(2, copiedEvents.size());

    boolean foundEvent1 = false, foundEvent2 = false;

    for (ICalendarEventDTO e : copiedEvents) {
      if (e.getEventName().equals("Morning Call")) {
        assertEquals(expectedStart1, e.getStartDateTime());
        assertEquals(expectedEnd1, e.getEndDateTime());
        assertEquals("Zoom", e.getEventLocation());
        foundEvent1 = true;
      } else if (e.getEventName().equals("Evening Wrap")) {
        assertEquals(expectedStart2, e.getStartDateTime());
        assertEquals(expectedEnd2, e.getEndDateTime());
        assertEquals("Office", e.getEventLocation());
        foundEvent2 = true;
      }
    }

    assertTrue(foundEvent1 && foundEvent2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEvent_NullEventName_ShouldFail() {
    model.createCalendar("Source", "UTC");
    model.createCalendar("Target", "UTC");

    LocalDateTime time = LocalDateTime.of(2025, 5, 5, 10, 0);
    model.copyEvent("Source", time, null, "Target", time.plusDays(1));
  }

  @Test
  public void testCopyEvent_TimeZoneRoundTripAccuracy() {
    model.createCalendar("India", "Asia/Kolkata");
    model.createCalendar("Tokyo", "Asia/Tokyo");

    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 15, 0); // IST
    LocalDateTime end = start.plusHours(1);

    ICalendarEventDTO event = CalendarEventDTO.builder()
            .setEventName("RoundTrip")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(false)
            .setRecurring(false)
            .setEventDescription("Check timezone logic")
            .setEventLocation("Room 404")
            .setPrivate(true)
            .build();

    assertTrue(model.addEvent("India", event));
    LocalDateTime targetStart = LocalDateTime.of(2025, 6, 2, 18, 30); // Expected converted time

    assertTrue(model.copyEvent("India", start, "RoundTrip", "Tokyo", targetStart));

    List<ICalendarEventDTO> targetEvents = model.getEventsInRange("Tokyo", targetStart.minusMinutes(1), targetStart.plusHours(1));
    assertEquals(1, targetEvents.size());
    assertEquals(targetStart, targetEvents.get(0).getStartDateTime());
  }

  //edit calendar model tests
  @Test
  public void testEditCalendarTimezone_EventTimesAdjust() {
    model.createCalendar("ShiftCal", "America/New_York");

    // Original time in EST (UTC-5)
    LocalDateTime start = LocalDateTime.of(2025, 3, 28, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 28, 10, 0);

    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
          .setEventName("TimezoneTest")
          .setStartDateTime(start)
          .setEndDateTime(end)
          .setRecurring(false)
          .setAutoDecline(true)
          .setEventDescription("Test event for timezone shift")
          .setEventLocation("Room 1")
          .setPrivate(false)
          .build();

    assertTrue(model.addEvent("ShiftCal", eventDTO));

    // Capture the original event times
    List<ICalendarEventDTO> eventsBefore = model.getEventsInRange("ShiftCal", start.minusMinutes(1), end.plusMinutes(1));
    assertEquals(1, eventsBefore.size());
    LocalDateTime originalStart = eventsBefore.get(0).getStartDateTime();
    LocalDateTime originalEnd = eventsBefore.get(0).getEndDateTime();

    // Change the timezone to Asia/Tokyo (UTC+9)
    assertTrue(model.editCalendar("ShiftCal", "timezone", "Asia/Tokyo"));

    // Now get the updated times
    List<ICalendarEventDTO> eventsAfter = model.getEventsInRange("ShiftCal", originalStart.minusDays(1), originalEnd.plusDays(1));
    assertEquals(1, eventsAfter.size());

    LocalDateTime newStart = eventsAfter.get(0).getStartDateTime();
    LocalDateTime newEnd = eventsAfter.get(0).getEndDateTime();

    // Convert the original times to ZonedDateTime for comparison
    ZonedDateTime originalStartZoned = originalStart.atZone(ZoneId.of("America/New_York"));
    ZonedDateTime originalEndZoned = originalEnd.atZone(ZoneId.of("America/New_York"));

    ZonedDateTime expectedStartZoned = originalStartZoned.withZoneSameInstant(ZoneId.of("Asia/Tokyo"));
    ZonedDateTime expectedEndZoned = originalEndZoned.withZoneSameInstant(ZoneId.of("Asia/Tokyo"));

    assertEquals(expectedStartZoned.toLocalDateTime(), newStart);
    assertEquals(expectedEndZoned.toLocalDateTime(), newEnd);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendar_InvalidTimezone_ShouldThrow() {
    model.createCalendar("InvalidTZCal", "Mars/SpaceTime");  // Invalid IANA timezone
  }










}


