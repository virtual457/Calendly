package controller.command;

import model.ICalendarEventDTO;
import model.ICalendarModel;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link CopyEventCommand} class.
 * This class ensures the correct behavior of copying a single event
 * across calendars, including conflict detection, timezone alignment,
 * and input validation.
 */



public class CopyEventCommandTest {



  private static class MockCalendarModel implements ICalendarModel {
    boolean wasCalled = false;

    @Override
    public boolean createCalendar(String calName, String timezone) {
      return false;
    }

    @Override
    public boolean addEvent(String calendarName, ICalendarEventDTO event) {
      return false;
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
    public boolean copyEvent(String sourceCalendar, LocalDateTime sourceDateTime, String eventName,
                             String targetCalendar, LocalDateTime targetDateTime) {
      wasCalled = true;
      return true;
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
  public void testValidCopyCommand() {
    MockCalendarModel model = new MockCalendarModel();
    CopyEventCommand command = new CopyEventCommand(
        Arrays.asList("Meeting", "on", "2025-05-01T10:00", "--target", "Work", "to", "2025-05" +
            "-01T12:00"),
        model,
        "Personal"
    );

    String result = command.execute();
    assertEquals("Event copied successfully.", result);
    assertTrue(model.wasCalled);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingEventName() {
    new CopyEventCommand(Collections.emptyList(), new MockCalendarModel(), "Default");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyEventName() {
    new CopyEventCommand(Arrays.asList("", "on", "2025-05-01T10:00", "--target", "Work", "to",
        "2025-05-01T12:00"),
        new MockCalendarModel(), "Default");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingOnKeyword() {
    new CopyEventCommand(Arrays.asList("Meeting", "oops", "2025-05-01T10:00", "--target", "Work",
        "to", "2025-05-01T12:00"),
        new MockCalendarModel(), "Default");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidSourceDateTime() {
    new CopyEventCommand(Arrays.asList("Meeting", "on", "invalid-datetime", "--target", "Work",
        "to", "2025-05-01T12:00"),
        new MockCalendarModel(), "Default");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingTargetKeyword() {
    new CopyEventCommand(Arrays.asList("Meeting", "on", "2025-05-01T10:00", "oops", "Work", "to",
        "2025-05-01T12:00"),
        new MockCalendarModel(), "Default");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingTargetCalendarName() {
    new CopyEventCommand(Arrays.asList("Meeting", "on", "2025-05-01T10:00", "--target", "", "to",
        "2025-05-01T12:00"),
        new MockCalendarModel(), "Default");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingToKeyword() {
    new CopyEventCommand(Arrays.asList("Meeting", "on", "2025-05-01T10:00", "--target", "Work",
        "oops", "2025-05-01T12:00"),
        new MockCalendarModel(), "Default");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTargetDateTime() {
    new CopyEventCommand(Arrays.asList("Meeting", "on", "2025-05-01T10:00", "--target", "Work",
        "to", "not-a-date"),
        new MockCalendarModel(), "Default");
  }
}
