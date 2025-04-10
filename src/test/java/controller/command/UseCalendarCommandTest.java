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

/**
 * Unit tests for the {@link UseCalendarCommand} class.
 * Verifies that the command correctly switches between calendars
 * and handles invalid inputs or nonexistent calendars appropriately.
 */

public class UseCalendarCommandTest {

  private static class MockModel implements ICalendarModel {
    boolean calendarExists = true;
    String checkedName;

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
    public String getCalendarTimeZone(String calendarName) {
      return "";
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
    public boolean isCalendarPresent(String name) {
      this.checkedName = name;
      return calendarExists;
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
  public void testValidCalendarUse() {
    MockModel model = new MockModel();
    UseCalendarCommand command = new UseCalendarCommand(
        Arrays.asList("--name", "Work"),
        model,
        "Default"
    );

    String result = command.execute();
    assertEquals("Using calendar: Work", result);
    assertEquals("Work", model.checkedName);
    assertEquals("Work", command.getCalendarName());
  }

  @Test
  public void testExecute_WhenCalendarExists_ReturnsSuccessMessage() {
    // Setup a mock model that returns true for isCalendarPresent
    MockModel model = new MockModel();
    model.calendarExists = true;

    UseCalendarCommand command = new UseCalendarCommand(
        Arrays.asList("--name", "Work"),
        model,
        "Default"
    );

    // Test the execute method
    String result = command.execute();

    // Verify the success message and that the correct calendar was checked
    assertEquals("Using calendar: Work", result);
    assertEquals("Work", model.checkedName);
  }

  @Test
  public void testExecute_WhenCalendarDoesNotExist_ReturnsErrorMessage() {
    // Setup a mock model that returns false for isCalendarPresent
    MockModel model = new MockModel();
    model.calendarExists = false;

    UseCalendarCommand command = new UseCalendarCommand(
        Arrays.asList("--name", "NonExistentCalendar"),
        model,
        "Default"
    );

    // Test the execute method
    String result = command.execute();

    // Verify the error message and that the correct calendar was checked
    assertEquals("Error: calendar not found", result);
    assertEquals("NonExistentCalendar", model.checkedName);
  }

  @Test
  public void testExecute_WhenIllegalArgumentExceptionThrown_ReturnsFormattedErrorMessage() {
    // Setup a mock model that throws IllegalArgumentException
    MockModel model = new MockModel() {
      @Override
      public boolean isCalendarPresent(String name) {
        this.checkedName = name;
        throw new IllegalArgumentException("Invalid calendar name format");
      }
    };

    UseCalendarCommand command = new UseCalendarCommand(
        Arrays.asList("--name", "InvalidName"),
        model,
        "Default"
    );

    // Test the execute method
    String result = command.execute();

    // Verify the formatted error message
    assertEquals("Error: Invalid calendar name format", result);
    assertEquals("InvalidName", model.checkedName);
  }

  @Test
  public void testExecute_WhenUnexpectedExceptionThrown_ReturnsUnexpectedErrorMessage() {
    // Setup a mock model that throws a different exception type
    MockModel model = new MockModel() {
      @Override
      public boolean isCalendarPresent(String name) {
        this.checkedName = name;
        throw new RuntimeException("Unexpected system error");
      }
    };

    UseCalendarCommand command = new UseCalendarCommand(
        Arrays.asList("--name", "Work"),
        model,
        "Default"
    );

    // Test the execute method
    String result = command.execute();

    // Verify the unexpected error message
    assertEquals("An unexpected error occurred: Unexpected system error", result);
    assertEquals("Work", model.checkedName);
  }

  @Test
  public void testExecute_WithEmptyCalendarName_StillProcesses() {
    // Setup a mock model to test empty (but not null) calendar name
    MockModel model = new MockModel();
    model.calendarExists = false; // Calendar doesn't exist

    UseCalendarCommand command = new UseCalendarCommand(
        Arrays.asList("--name", ""), // Empty string but not null
        model,
        "Default"
    );

    // Test the execute method
    String result = command.execute();

    // Verify the error message and the empty string was checked
    assertEquals("Error: calendar not found", result);
    assertEquals("", model.checkedName);
  }

  @Test
  public void testExecute_AfterGetCalendarName_ReturnsSameName() {
    // Setup
    MockModel model = new MockModel();
    model.calendarExists = true;

    UseCalendarCommand command = new UseCalendarCommand(
        Arrays.asList("--name", "ProjectCal"),
        model,
        "Default"
    );

    // First get the calendar name
    String calName = command.getCalendarName();
    assertEquals("ProjectCal", calName);

    // Then execute the command
    String result = command.execute();
    assertEquals("Using calendar: ProjectCal", result);

    // Verify the calendar name is still the same after execution
    assertEquals("ProjectCal", command.getCalendarName());
  }

  @Test
  public void testCalendarNotFound() {
    MockModel model = new MockModel();
    model.calendarExists = false;

    UseCalendarCommand command = new UseCalendarCommand(
        Arrays.asList("--name", "Ghost"),
        model,
        "Default"
    );

    String result = command.execute();
    assertEquals("Error: calendar not found", result);
  }


  @Test(expected = NullPointerException.class)
  public void testNullModelThrowsError() {
    new UseCalendarCommand(Arrays.asList("--name", "Work"), null, "Default");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingNameFlag() {
    new UseCalendarCommand(Arrays.asList("Work"), new MockModel(), "Default");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingCalendarName() {
    new UseCalendarCommand(Collections.singletonList("--name"), new MockModel(), "Default");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtraArguments() {
    new UseCalendarCommand(
        Arrays.asList("--name", "Work", "extraArg"),
        new MockModel(),
        "Default"
    );
  }
}
