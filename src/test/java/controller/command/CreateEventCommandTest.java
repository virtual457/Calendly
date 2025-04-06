package controller.command;

import model.ICalendarEventDTO;
import model.ICalendarModel;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for the {@link CreateEventCommand} class.
 * Ensures correct parsing and execution of commands related to creating calendar events,
 * including validation of input arguments and proper interaction with the model.
 */

public class CreateEventCommandTest {

  private static class MockModel implements ICalendarModel {
    ICalendarEventDTO lastEvent = null;
    boolean shouldFail = false;

    @Override
    public boolean createCalendar(String calName, String timezone) {
      return false;
    }

    @Override
    public boolean addEvent(String calendarName, ICalendarEventDTO event) {
      if (shouldFail) {
        return false;
      }
      this.lastEvent = event;
      return true;
    }

    @Override
    public boolean editEvents(String calendarName, String property, String eventName,
                              LocalDateTime fromDateTime, String newValue, boolean editAll) {
      return false;
    }

    @Override
    public boolean editEvent(String calendarName, String property, String eventName,
                             LocalDateTime fromDateTime, LocalDateTime toDateTime,
                             String newValue) {
      return false;
    }

    @Override
    public boolean isCalendarAvailable(String calName, LocalDate date) {
      return false;
    }

    @Override
    public List<String> getCalendarNames() {
      return List.of();
    }

    @Override
    public boolean deleteCalendar(String calName) {
      return false;
    }

    @Override
    public List<ICalendarEventDTO> getEventsInRange(String calendarName,
                                                    LocalDateTime fromDateTime,
                                                    LocalDateTime toDateTime) {
      return List.of();
    }

    @Override
    public List<ICalendarEventDTO> getEventsInSpecificDateTime(String calendarName,
                                                               LocalDateTime dateTime) {
      return List.of();
    }

    @Override
    public boolean copyEvents(String sourceCalendarName, LocalDateTime sourceStart,
                              LocalDateTime sourceEnd, String targetCalendarName,
                              LocalDate targetStart) {
      return false;
    }

    @Override
    public boolean copyEvent(String sourceCalendarName, LocalDateTime sourceStart,
                             String eventName, String targetCalendarName,
                             LocalDateTime targetStart) {
      return false;
    }

    @Override
    public boolean isCalendarPresent(String calName) {
      return false;
    }

    @Override
    public boolean editCalendar(String calendarName, String property, String newValue) {
      return false;
    }

  }

  @Test
  public void testCreateSingleEventFromTo() {
    MockModel model = new MockModel();
    List<String> args = Arrays.asList("Meeting", "from", "2025-05-01T10:00", "to", "2025-05-01T11" +
        ":00");

    CreateEventCommand command = new CreateEventCommand(args, model, "Default");
    String result = command.execute();

    assertEquals("Event created successfully.", result);
    assertNotNull(model.lastEvent);
    assertEquals("Meeting", model.lastEvent.getEventName());
  }

  @Test
  public void testCreateAllDayEventWithOptionalFlags() {
    MockModel model = new MockModel();
    List<String> args = Arrays.asList(
        "--autoDecline", "Holiday", "on", "2025-07-04",
        "--description", "Independence Day",
        "--location", "USA",
        "--private"
    );

    CreateEventCommand command = new CreateEventCommand(args, model, "Default");
    String result = command.execute();

    assertEquals("Event created successfully.", result);
    assertTrue(model.lastEvent.isPrivate());
    assertEquals("USA", model.lastEvent.getEventLocation());
    assertEquals("Independence Day", model.lastEvent.getEventDescription());
  }

  @Test
  public void testCreateAllDayEventWithOptionalFlags2() {
    MockModel model = new MockModel();
    List<String> args = Arrays.asList(
        "--autoDecline", "Holiday", "on", "2025-07-04",
        "--description", "Independence Day",
        "--location"
    );

    try {
      new CreateEventCommand(args, model, "Default");
      fail("Exception Expected");
    } catch (Exception e) {
      assertEquals("Missing value for --location", e.getMessage());
    }
  }

  @Test
  public void testCreateAllDayEventWithErrorScenario() {
    MockModel model = new MockModel();
    List<String> args = Arrays.asList(
        "--autoDecline", "Holiday", "Blast", "2025-07-04",
        "--description", "Independence Day",
        "--location"
    );

    try {
      new CreateEventCommand(args, model, "Default");
      fail("Exception Expected");
    } catch (Exception e) {
      assertEquals("Expected 'from' or 'on' after event name", e.getMessage());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingWeekdaysAfterRepeats() {
    List<String> args = Arrays.asList("Yoga", "on", "2025-05-01", "repeats");
    new CreateEventCommand(args, new CreateEventCommandTest.MockModel(), "MyCalendar");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingRecurrenceCountAfterFor() {
    List<String> args = Arrays.asList(
        "Gym", "on", "2025-05-01", "repeats", "MWF", "for"
    );

    new CreateEventCommand(args, new CreateEventCommandTest.MockModel(), "Cal");
  }


}
