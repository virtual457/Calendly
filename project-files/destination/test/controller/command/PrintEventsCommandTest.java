package controller.command;

import controller.command.PrintEventsCommand;
import model.ICalendarEventDTO;
import model.ICalendarModel;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link PrintEventsCommand} class.
 * This test class verifies correct parsing and execution of commands that
 * print events either on a specific date or between a range of date-times.
 */

public class PrintEventsCommandTest {

  private static class MockEvent implements ICalendarEventDTO {
    private final String name;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final String location;

    MockEvent(String name, String start, String end, String location) {
      this.name = name;
      this.start = LocalDateTime.parse(start);
      this.end = LocalDateTime.parse(end);
      this.location = location;
    }

    @Override
    public String getEventName() {
      return name;
    }

    @Override
    public LocalDateTime getStartDateTime() {
      return start;
    }

    @Override
    public LocalDateTime getEndDateTime() {
      return end;
    }

    @Override
    public Boolean isRecurring() {
      return null;
    }

    @Override
    public List<DayOfWeek> getRecurrenceDays() {
      return List.of();
    }

    @Override
    public Integer getRecurrenceCount() {
      return 0;
    }

    @Override
    public LocalDateTime getRecurrenceEndDate() {
      return null;
    }

    @Override
    public Boolean isAutoDecline() {
      return null;
    }

    @Override
    public String getEventDescription() {
      return "";
    }

    @Override
    public String getEventLocation() {
      return location;
    }

    @Override
    public Boolean isPrivate() {
      return null;
    }
  }

  private static class MockModel implements ICalendarModel {
    List<ICalendarEventDTO> eventsToReturn;

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
    public boolean deleteCalendar(String calName) {
      return false;
    }

    @Override
    public List<ICalendarEventDTO> getEventsInRange(String calendar, LocalDateTime from,
                                                    LocalDateTime to) {
      return eventsToReturn;
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
  public void testPrintEventsOnNoEvents() {
    MockModel model = new MockModel();
    model.eventsToReturn = Collections.emptyList();

    PrintEventsCommand cmd = new PrintEventsCommand(
        Arrays.asList("on", "2025-05-01"),
        model, "Default"
    );

    String result = cmd.execute();
    assertEquals("No events found.", result);
  }

  @Test
  public void testPrintEventsOnWithEvents() {
    MockModel model = new MockModel();
    model.eventsToReturn = Arrays.asList(
        new MockEvent("Meeting", "2025-05-01T10:00", "2025-05-01T11:00", "Room 101")
    );

    PrintEventsCommand cmd = new PrintEventsCommand(
        Arrays.asList("on", "2025-05-01"),
        model, "Default"
    );

    String result = cmd.execute();
    assertTrue(result.contains("Meeting"));
    assertTrue(result.contains("2025-05-01T10:00"));
    assertTrue(result.contains("Room 101"));
  }

  @Test
  public void testPrintEventsFromTo() {
    MockModel model = new MockModel();
    model.eventsToReturn = Arrays.asList(
        new MockEvent("Lecture", "2025-06-01T09:00", "2025-06-01T10:30", "")
    );

    PrintEventsCommand cmd = new PrintEventsCommand(
        Arrays.asList("from", "2025-06-01T08:00", "to", "2025-06-01T12:00"),
        model, "Uni"
    );

    String result = cmd.execute();
    assertTrue(result.contains("Lecture"));
    assertTrue(result.contains("2025-06-01T09:00"));
    assertFalse(result.contains("at"));  // no location
  }

  // ------------------- Error Handling -----------------------

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyArgs() {
    new PrintEventsCommand(Collections.emptyList(), new MockModel(), "Cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidKeyword() {
    new PrintEventsCommand(Arrays.asList("next", "2025-05-01"), new MockModel(), "Cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidOnFormatTooManyArgs() {
    new PrintEventsCommand(Arrays.asList("on", "2025-05-01", "extra"), new MockModel(), "Cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromToWrongKeyword() {
    new PrintEventsCommand(
        Arrays.asList("from", "2025-06-01T08:00", "until", "2025-06-01T12:00"),
        new MockModel(), "Cal"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromToWrongArgCount() {
    new PrintEventsCommand(
        Arrays.asList("from", "2025-06-01T08:00", "to"),
        new MockModel(), "Cal"
    );
  }
}
