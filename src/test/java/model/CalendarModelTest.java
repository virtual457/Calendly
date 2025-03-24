package model;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    boolean edited = model.editEvents("Work", "location", "Standup", start, end, "Room B", false);
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
    boolean edited = model.editEvents("Work", "location", "Standup", start, end, "Room C", true);
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
    model.createCalendar("Fall2024", "America/New_York");
    model.createCalendar("Spring2025", "America/New_York");

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

    LocalDateTime targetStart = LocalDateTime.of(2025, 1, 8, 10, 0);
    boolean copied = model.copyEvents("Fall2024", fallStart, fallEnd, "Spring2025", targetStart);
    assertTrue(copied);

    List<ICalendarEventDTO> copiedEvents = model.getEventsInRange("Spring2025", targetStart.minusHours(1), targetStart.plusHours(2));
    assertEquals(1, copiedEvents.size());
    ICalendarEventDTO copiedEvent = copiedEvents.get(0);
    assertEquals("Lecture", copiedEvent.getEventName());
    assertEquals(targetStart, copiedEvent.getStartDateTime());
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

    // Now add a conflicting event in the target calendar.
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
    model.copyEvents("SourceCal", start, end, "TargetCal", LocalDateTime.of(2025, 1, 8, 10, 0));
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
    model.editEvents("EditCal", "unsupported", "Event", start, end, "NewValue", false);
  }

  @Test(expected = IllegalStateException.class)
  public void testEditEventsNoMatchingEvent() {
    model.createCalendar("EditCal", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 8, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 8, 1, 11, 0);
    // No event added, so editing should fail.
    model.editEvents("EditCal", "location", "NonExistent", start, end, "Room X", false);
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
    // No events added to SourceCal.
    boolean copied = model.copyEvents("SourceCal", LocalDateTime.of(2025, 1, 1, 10, 0),
            LocalDateTime.of(2025, 1, 1, 11, 0), "TargetCal", LocalDateTime.of(2025, 2, 1, 10, 0));
    assertFalse(copied);
  }

  @Test
  public void testCopyEventsRecurringEvent() {
    model.createCalendar("SourceCal", "America/New_York");
    model.createCalendar("TargetCal", "America/New_York");

    // Create a recurring event in SourceCal with 2 occurrences on Monday.
    // For example, if March 3, 2025 is a Monday, the occurrences will be March 3 and March 10.
    LocalDateTime start = LocalDateTime.of(2025, 3, 3, 9, 0); // Monday, March 3, 2025
    LocalDateTime end = LocalDateTime.of(2025, 3, 3, 10, 0);

    ICalendarEventDTO eventDTO = CalendarEventDTO.builder()
            .setEventName("Weekly Sync")
            .setStartDateTime(start)
            .setEndDateTime(end)
            .setAutoDecline(true)
            .setRecurring(true)
            .setRecurrenceCount(2)  // Two occurrences
            .setRecurrenceDays(Arrays.asList(DayOfWeek.MONDAY))
            .setEventDescription("Recurring meeting")
            .setEventLocation("Room Z")
            .setPrivate(false)
            .build();
    assertTrue(model.addEvent("SourceCal", eventDTO));

    // Copy these events to TargetCal starting at a new time.
    LocalDateTime targetStart = LocalDateTime.of(2025, 4, 6, 9, 0); // Monday, April 6, 2025
    // Use a source interval that covers both occurrences.
    boolean copied = model.copyEvents("SourceCal", start, start.plusDays(7), "TargetCal", targetStart);
    assertTrue(copied);

    // Expect 2 occurrences in TargetCal.
    List<ICalendarEventDTO> events = model.getEventsInRange("TargetCal", targetStart.minusHours(1), targetStart.plusDays(7));
    assertEquals(2, events.size());
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

}


