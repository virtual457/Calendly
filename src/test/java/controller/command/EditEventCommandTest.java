package controller.command;

import controller.command.EditEventCommand;
import model.ICalendarEventDTO;
import model.ICalendarModel;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link EditEventCommand} class.
 * This test class verifies the behavior of editing individual calendar events,
 * including updates to event properties and validation of input.
 */

public class EditEventCommandTest {

  private static class MockModel implements ICalendarModel {
    String property;
    String eventName;
    String newValue;
    LocalDateTime from;
    LocalDateTime to;
    boolean shouldSucceed = true;

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
    public boolean editEvent(String calendar, String property, String name,
                             LocalDateTime fromTime, LocalDateTime toTime, String newValue) {
      this.property = property;
      this.eventName = name;
      this.from = fromTime;
      this.to = toTime;
      this.newValue = newValue;
      return shouldSucceed;
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

    @Override
    public boolean addEvents(String calendarName, List<ICalendarEventDTO> events,
                             String timezone) {
      return false;
    }
  }

  @Test
  public void testValidEditEventCommand() {
    MockModel model = new MockModel();
    EditEventCommand command = new EditEventCommand(
        Arrays.asList(
            "name", "Meeting",
            "from", "2025-05-01T10:00",
            "to", "2025-05-01T11:00",
            "with", "SyncCall"
        ),
        model, "Work"
    );

    String result = command.execute();
    assertEquals("Event(s) edited successfully.", result);
    assertEquals("Meeting", model.eventName);
    assertEquals("SyncCall", model.newValue);
  }

  @Test
  public void testEditEventFailure() {
    MockModel model = new MockModel();
    model.shouldSucceed = false;

    EditEventCommand command = new EditEventCommand(
        Arrays.asList(
            "location", "EventA",
            "from", "2025-06-01T09:00",
            "to", "2025-06-01T10:00",
            "with", "Room 202"
        ),
        model, "Default"
    );

    String result = command.execute();
    assertEquals("Error editing event(s).", result);
  }



  @Test(expected = IllegalArgumentException.class)
  public void testMissingFromKeyword() {
    new EditEventCommand(
        Arrays.asList("name", "Meeting", "WRONG", "2025-05-01T10:00", "to", "2025-05-01T11:00",
            "with", "NewName"),
        new MockModel(), "Cal"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingToKeyword() {
    new EditEventCommand(
        Arrays.asList("name", "Meeting", "from", "2025-05-01T10:00", "WRONG", "2025-05-01T11:00",
            "with", "NewName"),
        new MockModel(), "Cal"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingWithKeyword() {
    new EditEventCommand(
        Arrays.asList("name", "Meeting", "from", "2025-05-01T10:00", "to", "2025-05-01T11:00",
            "WRONG", "NewName"),
        new MockModel(), "Cal"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTooFewArguments() {
    new EditEventCommand(
        Collections.singletonList("name"),
        new MockModel(), "Cal"
    );
  }
}
