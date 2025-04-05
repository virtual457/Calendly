package model;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

/**
 * Unit tests for the {@link Calendar} class.
 * Verifies calendar creation, event management, and timezone behavior.
 */

public class CalendarTest {

  private Calendar calendar;


  @Before
  public void setup() {
    CalendarEvent sampleEvent1;
    CalendarEvent sampleEvent2;
    sampleEvent1 = CalendarEvent.builder()
          .setEventName("Meeting")
          .setStartDateTime(LocalDateTime.of(2025, 5, 1, 10, 0))
          .setEndDateTime(LocalDateTime.of(2025, 5, 1, 11, 0))
          .setEventLocation("Room 101")
          .setEventDescription("Project sync")
          .setPublic(false)
          .build();

    sampleEvent2 = CalendarEvent.builder()
          .setEventName("Lunch")
          .setStartDateTime(LocalDateTime.of(2025, 5, 1, 12, 30))
          .setEndDateTime(LocalDateTime.of(2025, 5, 1, 13, 30))
          .setEventLocation("Cafeteria")
          .setEventDescription("Team lunch")
          .setPublic(true)
          .build();

    calendar = Calendar.builder()
          .setCalendarName("Work")
          .setTimezone("America/New_York")
          .setEvents(Arrays.asList(sampleEvent1, sampleEvent2))
          .build();
  }

  @Test
  public void testGetCalendarNameAndSetCalendarName() {
    assertEquals("Work", calendar.getCalendarName());
    calendar.setCalendarName("Personal");
    assertEquals("Personal", calendar.getCalendarName());
  }

  @Test
  public void testGetEventsAndAddEvent() {
    assertEquals(2, calendar.getEvents().size());
    CalendarEvent newEvent = CalendarEvent.builder()
          .setEventName("Call")
          .setStartDateTime(LocalDateTime.of(2025, 5, 2, 15, 0))
          .setEndDateTime(LocalDateTime.of(2025, 5, 2, 16, 0))
          .setPublic(false)
          .build();
    calendar.addEvent(newEvent);
    assertEquals(3, calendar.getEvents().size());
  }

  @Test
  public void testAddEvents() {
    CalendarEvent event3 = CalendarEvent.builder()
          .setEventName("Event 3")
          .setStartDateTime(LocalDateTime.of(2025, 5, 3, 9, 0))
          .setEndDateTime(LocalDateTime.of(2025, 5, 3, 10, 0))
          .setPublic(true)
          .build();

    calendar.addEvents(new ArrayList<>(Arrays.asList(event3)));
    assertEquals(3, calendar.getEvents().size());
  }

  @Test
  public void testGetEventsCopyCreatesNewInstances() {
    List<ICalendarEvent> copy = calendar.getEventsCopy();
    assertEquals(2, copy.size());
    assertNotSame(copy.get(0), calendar.getEvents().get(0));
    assertEquals(calendar.getEvents().get(0).getEventName(), copy.get(0).getEventName());
  }

  @Test
  public void testTimezoneConversion() {

    CalendarEvent sampleEvent1;
    sampleEvent1 = CalendarEvent.builder()
        .setEventName("Meeting")
        .setStartDateTime(LocalDateTime.of(2025, 5, 1, 10, 0))
        .setEndDateTime(LocalDateTime.of(2025, 5, 1, 11, 0))
        .setEventLocation("Room 101")
        .setEventDescription("Project sync")
        .setPublic(false)
        .build();


    calendar.setTimezone("Europe/London"); // New timezone ahead of New York
    assertEquals("Europe/London", calendar.getTimezone());

    List<ICalendarEvent> updatedEvents = calendar.getEvents();
    assertNotNull(updatedEvents);
    assertEquals(2, updatedEvents.size());

    // Just ensure conversion actually happened — times will change
    assertNotEquals(sampleEvent1.getStartDateTime(), updatedEvents.get(0).getStartDateTime());
  }

  @Test
  public void testSetEventsOverridesList() {
    CalendarEvent sampleEvent1;
    sampleEvent1 = CalendarEvent.builder()
        .setEventName("Meeting")
        .setStartDateTime(LocalDateTime.of(2025, 5, 1, 10, 0))
        .setEndDateTime(LocalDateTime.of(2025, 5, 1, 11, 0))
        .setEventLocation("Room 101")
        .setEventDescription("Project sync")
        .setPublic(false)
        .build();

    calendar.setEvents(Arrays.asList(sampleEvent1));
    assertEquals(1, calendar.getEvents().size());
    assertEquals("Meeting", calendar.getEvents().get(0).getEventName());
  }

  @Test
  public void testBuilderCreatesCalendarProperly() {

    CalendarEvent sampleEvent1;
    sampleEvent1 = CalendarEvent.builder()
        .setEventName("Meeting")
        .setStartDateTime(LocalDateTime.of(2025, 5, 1, 10, 0))
        .setEndDateTime(LocalDateTime.of(2025, 5, 1, 11, 0))
        .setEventLocation("Room 101")
        .setEventDescription("Project sync")
        .setPublic(false)
        .build();

    ICalendar cal = Calendar.builder()
          .setCalendarName("TestCal")
          .setTimezone("Asia/Kolkata")
          .setEvents(Arrays.asList(sampleEvent1))
          .build();

    assertEquals("TestCal", cal.getCalendarName());
    assertEquals("Asia/Kolkata", cal.getTimezone());
    assertEquals(1, cal.getEvents().size());
  }
}
