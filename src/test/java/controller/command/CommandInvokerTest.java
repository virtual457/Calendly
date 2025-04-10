package controller.command;

import model.ICalendarEventDTO;
import model.ICalendarModel;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for the CommandInvoker class.
 * Tests registration, deregistration, and execution of various commands.
 */
public class CommandInvokerTest {

  private CommandInvoker invoker;
  private MockCalendarModel mockModel;

  /**
   * Mock model implementation for testing command execution.
   */
  private static class MockCalendarModel implements ICalendarModel {
    private String lastExecutedCommand;
    private boolean shouldSucceed = true;

    @Override
    public boolean createCalendar(String calName, String timezone) {
      lastExecutedCommand = "createCalendar:" + calName + "," + timezone;
      return shouldSucceed;
    }

    @Override
    public boolean addEvent(String calendarName, ICalendarEventDTO event) {
      lastExecutedCommand = "addEvent:" + calendarName;
      return shouldSucceed;
    }

    @Override
    public boolean editEvents(String calendarName, String property, String eventName,
                              LocalDateTime fromDateTime, String newValue, boolean editAll) {
      lastExecutedCommand = "editEvents:" + calendarName + "," + property + "," + eventName;
      return shouldSucceed;
    }

    @Override
    public boolean editEvent(String calendarName, String property, String eventName,
                             LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue) {
      lastExecutedCommand = "editEvent:" + calendarName + "," + property + "," + eventName;
      return shouldSucceed;
    }

    @Override
    public boolean isCalendarAvailable(String calName, LocalDate date) {
      lastExecutedCommand = "isCalendarAvailable:" + calName;
      return shouldSucceed;
    }

    @Override
    public List<String> getCalendarNames() {
      lastExecutedCommand = "getCalendarNames";
      return Arrays.asList("TestCalendar");
    }

    @Override
    public boolean deleteCalendar(String calName) {
      lastExecutedCommand = "deleteCalendar:" + calName;
      return shouldSucceed;
    }

    @Override
    public List<ICalendarEventDTO> getEventsInRange(String calendarName, LocalDateTime fromDateTime,
                                                    LocalDateTime toDateTime) {
      lastExecutedCommand = "getEventsInRange:" + calendarName;
      return List.of();
    }

    @Override
    public List<ICalendarEventDTO> getEventsInSpecificDateTime(String calendarName, LocalDateTime dateTime) {
      lastExecutedCommand = "getEventsInSpecificDateTime:" + calendarName;
      return List.of();
    }

    @Override
    public boolean copyEvents(String sourceCalendarName, LocalDateTime sourceStart, LocalDateTime sourceEnd,
                              String targetCalendarName, LocalDate targetStart) {
      lastExecutedCommand = "copyEvents:" + sourceCalendarName + "," + targetCalendarName;
      return shouldSucceed;
    }

    @Override
    public boolean copyEvent(String sourceCalendarName, LocalDateTime sourceStart, String eventName,
                             String targetCalendarName, LocalDateTime targetStart) {
      lastExecutedCommand = "copyEvent:" + sourceCalendarName + "," + targetCalendarName;
      return shouldSucceed;
    }

    @Override
    public boolean isCalendarPresent(String calName) {
      lastExecutedCommand = "isCalendarPresent:" + calName;
      return shouldSucceed;
    }

    @Override
    public boolean editCalendar(String calendarName, String property, String newValue) {
      lastExecutedCommand = "editCalendar:" + calendarName + "," + property;
      return shouldSucceed;
    }

    @Override
    public boolean addEvents(String calendarName, List<ICalendarEventDTO> events,
                             String timezone) {
      lastExecutedCommand = "addEvents:" + calendarName;
      return shouldSucceed;
    }

    public void setShouldSucceed(boolean shouldSucceed) {
      this.shouldSucceed = shouldSucceed;
    }

    public String getLastExecutedCommand() {
      return lastExecutedCommand;
    }
  }

  @Before
  public void setUp() {
    mockModel = new MockCalendarModel();
    invoker = new CommandInvoker("DefaultCalendar");

    // Register common commands
    invoker.registerCommand("create calendar", CreateCalendarCommand.class);
    invoker.registerCommand("create event", CreateEventCommand.class);
    invoker.registerCommand("use calendar", UseCalendarCommand.class);
    invoker.registerCommand("edit event", EditEventCommand.class);
    invoker.registerCommand("edit events", EditEventsCalendarCommand.class);
    invoker.registerCommand("show status", ShowStatusCommand.class);
    invoker.registerCommand("print events", PrintEventsCommand.class);
  }

  @Test
  public void testRegisterCommand() {
    // Register a new command
    invoker.registerCommand("test command", CreateCalendarCommand.class);

    // Verify it was registered by executing it
    String result = invoker.executeCommand("test command",
          List.of("--name", "TestCal", "--timezone", "UTC"), mockModel);

    assertTrue(result.contains("successfully"));
    assertEquals("createCalendar:TestCal,UTC", mockModel.getLastExecutedCommand());
  }

  @Test
  public void testDeregisterCommand() {
    // First register a command
    invoker.registerCommand("temp command", CreateCalendarCommand.class);

    // Execute to confirm registration
    String result = invoker.executeCommand("temp command",
          List.of("--name", "TempCal", "--timezone", "UTC"), mockModel);
    assertTrue(result.contains("successfully"));

    // Now deregister and verify it's no longer available
    invoker.deregisterCommand("temp command");
    result = invoker.executeCommand("temp command",
          List.of("--name", "TempCal", "--timezone", "UTC"), mockModel);
    assertEquals("Error: Unknown command.", result);
  }

  @Test
  public void testExecuteUnknownCommand() {
    String result = invoker.executeCommand("unknown command", List.of(), mockModel);
    assertEquals("Error: Unknown command.", result);
  }

  @Test
  public void testExecuteCreateCalendarCommand() {
    String result = invoker.executeCommand("create calendar",
          List.of("--name", "WorkCal", "--timezone", "America/New_York"), mockModel);

    assertTrue(result.contains("successfully"));
    assertEquals("createCalendar:WorkCal,America/New_York", mockModel.getLastExecutedCommand());
  }

  @Test
  public void testExecuteCreateEventCommand() {
    String result = invoker.executeCommand("create event",
          List.of("Meeting", "from", "2025-05-01T10:00", "to", "2025-05-01T11:00"), mockModel);

    assertTrue(result.contains("successfully"));
    assertEquals("addEvent:DefaultCalendar", mockModel.getLastExecutedCommand());
  }

  @Test
  public void testExecuteUseCalendarCommand() {
    // Set up the mockModel to return true for isCalendarPresent
    mockModel.setShouldSucceed(true);

    String result = invoker.executeCommand("use calendar",
          List.of("--name", "WorkCal"), mockModel);

    assertEquals("Using calendar: WorkCal", result);
    assertEquals("isCalendarPresent:WorkCal", mockModel.getLastExecutedCommand());

    // Verify the current calendar was updated
    assertEquals("Using calendar: WorkCal", invoker.executeCommand("use calendar",
          List.of("--name", "WorkCal"), mockModel));
  }

  @Test
  public void testExecuteCommandWithInvalidArguments() {
    String result = invoker.executeCommand("create calendar",
          List.of("BadArgs"), mockModel);

    assertTrue(result.contains("Error"));
  }

  @Test
  public void testExecuteCommandWithException() {
    // Create a command that will throw an exception during instantiation
    invoker.registerCommand("exception command", ExceptionThrowingCommand.class);

    String result = invoker.executeCommand("exception command", List.of(), mockModel);
    assertTrue(result.contains("Error Executing command"));
  }

  @Test
  public void testExecuteCommandWithRuntimeException() {
    // MockModel will throw during execution
    mockModel.setShouldSucceed(false);

    // Register a command that will trigger a runtime exception
    class RuntimeExceptionCommand implements ICommand {
      public RuntimeExceptionCommand(List<String> args, ICalendarModel model, String currentCalendar) {
        throw new RuntimeException("Test runtime exception");
      }

      @Override
      public String execute() {
        return "This should not be reached";
      }
    }

    invoker.registerCommand("runtime exception", RuntimeExceptionCommand.class);

    String result = invoker.executeCommand("runtime exception", List.of(), mockModel);
    assertTrue(result.contains("Error Executing command: controller.command.CommandInvokerTest$1RuntimeExceptionCommand.<init>(java.util.List, model.ICalendarModel, java.lang.String)"));
  }

  @Test
  public void testUseCalendarNullModelCheck() {
    String currentCalendar = "TestCal";
    CommandInvoker invoker = new CommandInvoker(currentCalendar);

    // No registered commands yet, should return unknown command
    String result = invoker.executeCommand("test", List.of(), null);
    assertEquals("Error: Unknown command.", result);
  }

  @Test
  public void testNoCalendarSelectedCheck() {
    // Create a new invoker with null current calendar
    CommandInvoker noCalInvoker = new CommandInvoker(null);
    noCalInvoker.registerCommand("print events", PrintEventsCommand.class);

    // This should fail since no calendar is selected and the command requires one
    String result = noCalInvoker.executeCommand("print events",
          List.of("on", "2025-05-01"), mockModel);

    assertTrue(result.contains("Please use somme calendar"));
  }

  @Test
  public void testCurrentCalendarUpdateAfterUse() {
    mockModel.setShouldSucceed(true);

    // First use a calendar
    String result = invoker.executeCommand("use calendar",
          List.of("--name", "NewCalendar"), mockModel);

    assertEquals("Using calendar: NewCalendar", result);

    // Now execute a command that requires a calendar
    result = invoker.executeCommand("create event",
          List.of("Meeting", "from", "2025-05-01T10:00", "to", "2025-05-01T11:00"), mockModel);

    // Verify the command used the new calendar
    assertEquals("addEvent:NewCalendar", mockModel.getLastExecutedCommand());
  }

  // Create a mock command class that throws an exception for testing
  private static class ExceptionThrowingCommand implements ICommand {
    public ExceptionThrowingCommand(List<String> args, ICalendarModel model, String currentCalendar)
          throws Exception {
      throw new Exception("Test exception");
    }

    @Override
    public String execute() {
      return "This won't be reached";
    }
  }
}