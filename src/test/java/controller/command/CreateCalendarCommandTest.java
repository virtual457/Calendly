package controller.command;

import controller.command.CreateCalendarCommand;
import model.ICalendarEventDTO;
import model.ICalendarModel;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CreateCalendarCommandTest {

  private static class MockModel implements ICalendarModel {
    boolean shouldSucceed = true;
    String createdName;
    String createdTimezone;

    @Override
    public boolean createCalendar(String name, String timezone) {
      this.createdName = name;
      this.createdTimezone = timezone;
      return shouldSucceed;
    }

    @Override
    public boolean addEvent(String calendarName, ICalendarEventDTO event) {
      return false;
    }

    @Override
    public boolean editEvents(String calendarName, String property, String eventName, LocalDateTime fromDateTime, String newValue, boolean editAll) {
      return false;
    }

    @Override
    public boolean editEvent(String calendarName, String property, String eventName, LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue) {
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
    public List<ICalendarEventDTO> getEventsInRange(String calendarName, LocalDateTime fromDateTime, LocalDateTime toDateTime) {
      return List.of();
    }

    @Override
    public List<ICalendarEventDTO> getEventsInSpecificDateTime(String calendarName, LocalDateTime dateTime) {
      return List.of();
    }

    @Override
    public boolean copyEvents(String sourceCalendarName, LocalDateTime sourceStart, LocalDateTime sourceEnd, String targetCalendarName, LocalDate targetStart) {
      return false;
    }

    @Override
    public boolean copyEvent(String sourceCalendarName, LocalDateTime sourceStart, String eventName, String targetCalendarName, LocalDateTime targetStart) {
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
  public void testValidCreateCalendarCommand() {
    MockModel model = new MockModel();
    List<String> args = Arrays.asList("--name", "Work", "--timezone", "Asia/Kolkata");
    CreateCalendarCommand command = new CreateCalendarCommand(args, model, "Default");

    String result = command.execute();
    assertEquals("Calendar created successfully.", result);
    assertEquals("Work", model.createdName);
    assertEquals("Asia/Kolkata", model.createdTimezone);
  }

  @Test
  public void testCalendarCreationFailure() {
    MockModel model = new MockModel();
    model.shouldSucceed = false;
    List<String> args = Arrays.asList("--name", "Work", "--timezone", "Asia/Kolkata");

    CreateCalendarCommand command = new CreateCalendarCommand(args, model, "Default");
    String result = command.execute();
    assertEquals("Error: Calendar creation failed.", result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingArguments() {
    List<String> args = Collections.singletonList("--name");
    new CreateCalendarCommand(args, new MockModel(), "Default");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingNameFlag() {
    List<String> args = Arrays.asList("Work", "--timezone", "Asia/Kolkata");
    new CreateCalendarCommand(args, new MockModel(), "Default");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingTimezoneFlag() {
    List<String> args = Arrays.asList("--name", "Work", "Asia/Kolkata");
    new CreateCalendarCommand(args, new MockModel(), "Default");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtraArgumentsPresent() {
    List<String> args = Arrays.asList("--name", "Work", "--timezone", "Asia/Kolkata", "extraArg");
    new CreateCalendarCommand(args, new MockModel(), "Default");
  }
}
