package controller.command;

import controller.command.EditCalendarCommand;
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
 * Unit tests for the {@link EditCalendarCommand} class.
 * Verifies the correctness of calendar property editing operations such as renaming a calendar
 * or changing its time zone.
 */

public class EditCalendarCommandTest {

  private static class MockModel implements ICalendarModel {
    String editedName;
    String editedProperty;
    String editedValue;
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
    public boolean editCalendar(String name, String property, String value) {
      this.editedName = name;
      this.editedProperty = property;
      this.editedValue = value;
      return shouldSucceed;
    }

  }

  @Test
  public void testEditCalendarNameSuccess() {
    MockModel model = new MockModel();
    EditCalendarCommand command = new EditCalendarCommand(
        Arrays.asList("--name", "Work", "--property", "name", "Work2025"),
        model, "Default"
    );

    String result = command.execute();
    assertEquals("Calendar updated successfully.", result);
    assertEquals("Work", model.editedName);
    assertEquals("name", model.editedProperty);
    assertEquals("Work2025", model.editedValue);
  }

  @Test
  public void testEditCalendarTimezoneSuccess() {
    MockModel model = new MockModel();
    EditCalendarCommand command = new EditCalendarCommand(
        Arrays.asList("--name", "Work", "--property", "timezone", "Asia/Kolkata"),
        model, "Default"
    );

    String result = command.execute();
    assertEquals("Calendar updated successfully.", result);
    assertEquals("timezone", model.editedProperty);
    assertEquals("Asia/Kolkata", model.editedValue);
  }

  @Test
  public void testEditFailsGracefully() {
    MockModel model = new MockModel();
    model.shouldSucceed = false;
    EditCalendarCommand command = new EditCalendarCommand(
        Arrays.asList("--name", "Work", "--property", "name", "NewName"),
        model, "Default"
    );

    String result = command.execute();
    assertTrue(result.contains("Failed to update calendar"));
  }


  @Test(expected = IllegalArgumentException.class)
  public void testMissingNameFlag() {
    new EditCalendarCommand(
        Arrays.asList("Work", "--property", "name", "NewName"),
        new MockModel(), "Default"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingCalendarName() {
    new EditCalendarCommand(
        Arrays.asList("--name", "", "--property", "name", "NewName"),
        new MockModel(), "Default"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingPropertyFlag() {
    new EditCalendarCommand(
        Arrays.asList("--name", "Work", "WRONG", "name", "NewName"),
        new MockModel(), "Default"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingPropertyName() {
    new EditCalendarCommand(
        Arrays.asList("--name", "Work", "--property"),
        new MockModel(), "Default"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidPropertyName() {
    new EditCalendarCommand(
        Arrays.asList("--name", "Work", "--property", "color", "blue"),
        new MockModel(), "Default"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingNewValue() {
    new EditCalendarCommand(
        Arrays.asList("--name", "Work", "--property", "name"),
        new MockModel(), "Default"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyNewValue() {
    new EditCalendarCommand(
        Arrays.asList("--name", "Work", "--property", "name", ""),
        new MockModel(), "Default"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyArgs() {
    new EditCalendarCommand(Collections.emptyList(), new MockModel(), "Default");
  }
}
