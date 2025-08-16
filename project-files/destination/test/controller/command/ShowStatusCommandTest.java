package controller.command;

import controller.command.ShowStatusCommand;
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

/**
 * Unit tests for the {@link ShowStatusCommand} class.
 * This class verifies that the status of calendars and events is displayed
 * correctly based on the provided date and time input.
 */

public class ShowStatusCommandTest {

  private static class MockEvent implements ICalendarEventDTO {
    private final LocalDateTime start;
    private final LocalDateTime end;

    public MockEvent(String start, String end) {
      this.start = LocalDateTime.parse(start);
      this.end = LocalDateTime.parse(end);
    }

    @Override
    public String getEventName() {
      return "Mock";
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
      return "";
    }

    @Override
    public Boolean isPrivate() {
      return null;
    }
  }

  private static class MockModel implements ICalendarModel {
    List<ICalendarEventDTO> returnEvents;

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
    public List<ICalendarEventDTO> getEventsInRange(String calendarName,
                                                    LocalDateTime fromDateTime,
                                                    LocalDateTime toDateTime) {
      return List.of();
    }

    @Override
    public List<ICalendarEventDTO> getEventsInSpecificDateTime(String cal, LocalDateTime dt) {
      return returnEvents;
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
  public void testShowStatusAvailable() {
    MockModel model = new MockModel();
    model.returnEvents = Collections.emptyList();

    ShowStatusCommand command = new ShowStatusCommand(
        Arrays.asList("on", "2025-05-01T10:00"),
        model, "Default"
    );

    String result = command.execute();
    assertEquals("Available", result);
  }

  @Test
  public void testShowStatusBusy() {
    MockModel model = new MockModel();
    model.returnEvents = Arrays.asList(new MockEvent("2025-05-01T09:00", "2025-05-01T11:00"));

    ShowStatusCommand command = new ShowStatusCommand(
        Arrays.asList("on", "2025-05-01T10:00"),
        model, "Default"
    );

    String result = command.execute();
    assertEquals("Busy", result);
  }

  // ------------------- Error Handling -------------------

  @Test(expected = IllegalArgumentException.class)
  public void testMissingArguments() {
    new ShowStatusCommand(Collections.singletonList("on"), new MockModel(), "Cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidKeyword() {
    new ShowStatusCommand(Arrays.asList("at", "2025-05-01T10:00"), new MockModel(), "Cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidDateTimeFormat() {
    new ShowStatusCommand(Arrays.asList("on", "May-01-2025 10:00AM"), new MockModel(), "Cal");
  }
}
