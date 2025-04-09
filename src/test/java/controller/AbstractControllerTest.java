package controller;

import controller.command.CommandInvoker;
import model.ICalendarModel;
import model.ICalendarEventDTO;
import org.junit.Before;
import org.junit.Test;
import view.IView;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.*;

/**
 * Tests for the AbstractController class to verify its core functionality
 * like command tokenization and scanner handling.
 */
public class AbstractControllerTest {

  private TestController controller;
  private MockModel mockModel;
  private MockView mockView;
  private CommandInvoker mockInvoker;

  /**
   * Test implementation of AbstractController with exposed methods for testing.
   */
  private static class TestController extends AbstractController {
    public List<String> publicTokenizeCommand(String input) {
      return tokenizeCommand(input);
    }

    public void publicRunScanner(Scanner scanner, boolean displayMessage,
                                 IView view, CommandInvoker invoker, ICalendarModel model) {
      runScanner(scanner, displayMessage, view, invoker, model);
    }
  }

  /**
   * Mock model implementation for testing.
   */
  private static class MockModel implements ICalendarModel {
    private List<String> commandsExecuted = new ArrayList<>();
    private boolean calendarExists = true;

    @Override
    public boolean createCalendar(String calName, String timezone) {
      commandsExecuted.add("createCalendar:" + calName);
      return true;
    }

    @Override
    public boolean addEvent(String calendarName, ICalendarEventDTO event) {
      commandsExecuted.add("addEvent:" + calendarName);
      return true;
    }

    @Override
    public boolean editEvents(String calendarName, String property, String eventName,
                              LocalDateTime fromDateTime, String newValue, boolean editAll) {
      commandsExecuted.add("editEvents:" + calendarName);
      return true;
    }

    @Override
    public boolean editEvent(String calendarName, String property, String eventName,
                             LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue) {
      commandsExecuted.add("editEvent:" + calendarName);
      return true;
    }

    @Override
    public boolean isCalendarAvailable(String calName, LocalDate date) {
      commandsExecuted.add("isCalendarAvailable:" + calName);
      return true;
    }

    @Override
    public List<String> getCalendarNames() {
      commandsExecuted.add("getCalendarNames");
      return List.of("TestCalendar");
    }

    @Override
    public boolean deleteCalendar(String calName) {
      commandsExecuted.add("deleteCalendar:" + calName);
      return true;
    }

    @Override
    public List<ICalendarEventDTO> getEventsInRange(String calendarName,
                                                    LocalDateTime fromDateTime, LocalDateTime toDateTime) {
      commandsExecuted.add("getEventsInRange:" + calendarName);
      return List.of();
    }

    @Override
    public List<ICalendarEventDTO> getEventsInSpecificDateTime(String calendarName, LocalDateTime dateTime) {
      commandsExecuted.add("getEventsInSpecificDateTime:" + calendarName);
      return List.of();
    }

    @Override
    public boolean copyEvents(String sourceCalendarName, LocalDateTime sourceStart, LocalDateTime sourceEnd,
                              String targetCalendarName, LocalDate targetStart) {
      commandsExecuted.add("copyEvents:" + sourceCalendarName + "," + targetCalendarName);
      return true;
    }

    @Override
    public boolean copyEvent(String sourceCalendarName, LocalDateTime sourceStart, String eventName,
                             String targetCalendarName, LocalDateTime targetStart) {
      commandsExecuted.add("copyEvent:" + sourceCalendarName + "," + targetCalendarName);
      return true;
    }

    @Override
    public boolean isCalendarPresent(String calName) {
      commandsExecuted.add("isCalendarPresent:" + calName);
      return calendarExists;
    }

    @Override
    public boolean editCalendar(String calendarName, String property, String newValue) {
      commandsExecuted.add("editCalendar:" + calendarName);
      return true;
    }

    @Override
    public boolean addEvents(String calendarName, List<ICalendarEventDTO> events) {
      commandsExecuted.add("addEvents:" + calendarName);
      return true;
    }

    public List<String> getCommandsExecuted() {
      return commandsExecuted;
    }

    public void setCalendarExists(boolean exists) {
      this.calendarExists = exists;
    }

    public void resetCommands() {
      commandsExecuted.clear();
    }
  }

  /**
   * Mock view implementation for testing.
   */
  private static class MockView implements IView {
    private List<String> displayedMessages = new ArrayList<>();
    private boolean startCalled = false;

    @Override
    public void display(String message) {
      displayedMessages.add(message);
    }

    @Override
    public void start(ICommandExecutor commandExecutor) {
      startCalled = true;
    }

    public List<String> getDisplayedMessages() {
      return displayedMessages;
    }

    public boolean isStartCalled() {
      return startCalled;
    }
  }

  /**
   * Mock implementation for testing instead of CommandInvoker.
   * This lets us track command execution without relying on CommandInvoker internals.
   */
  private static class MockCommandExecutor {
    private List<String> commandsExecuted = new ArrayList<>();
    private String response = "Command executed successfully";

    public String executeCommand(String commandName, List<String> parts, ICalendarModel model) {
      commandsExecuted.add(commandName + ":" + String.join(",", parts));
      return response;
    }

    public List<String> getCommandsExecuted() {
      return commandsExecuted;
    }

    public void setResponse(String response) {
      this.response = response;
    }
  }

  private MockCommandExecutor mockExecutor;

  @Before
  public void setUp() {
    controller = new TestController();
    mockModel = new MockModel();
    mockView = new MockView();
    mockExecutor = new MockCommandExecutor();
    // Since we can't modify CommandInvoker directly, we'll use our mock executor instead
  }

  @Test
  public void testTokenizeCommandSimple() {
    List<String> tokens = controller.publicTokenizeCommand("create calendar --name WorkCal --timezone UTC");

    assertEquals(6, tokens.size());
    assertEquals("create", tokens.get(0));
    assertEquals("calendar", tokens.get(1));
    assertEquals("--name", tokens.get(2));
    assertEquals("WorkCal", tokens.get(3));
    assertEquals("--timezone", tokens.get(4));
    assertEquals("UTC", tokens.get(5));
  }

  @Test
  public void testTokenizeCommandWithQuotes() {
    List<String> tokens = controller.publicTokenizeCommand("create event \"Team Meeting\" from 2025-05-01T10:00 to 2025-05-01T11:00");

    assertEquals(7, tokens.size());
    assertEquals("create", tokens.get(0));
    assertEquals("event", tokens.get(1));
    assertEquals("Team Meeting", tokens.get(2)); // Quotes are removed, but spaces preserved
    assertEquals("from", tokens.get(3));
    assertEquals("2025-05-01T10:00", tokens.get(4));
    assertEquals("to", tokens.get(5));
    assertEquals("2025-05-01T11:00", tokens.get(6));
  }

  @Test
  public void testTokenizeCommandWithEmptyInput() {
    List<String> tokens = controller.publicTokenizeCommand("");

    assertTrue(tokens.isEmpty());
  }

  @Test
  public void testTokenizeCommandWithNestedQuotes() {
    List<String> tokens = controller.publicTokenizeCommand("edit event description Meeting with \"This is a special meeting\"");

    assertEquals("with", tokens.get(4));
    assertEquals("This is a special meeting", tokens.get(5));
  }

  /**
   * A custom handler to intercept and track command handling in runScanner
   */
  private static class TestCommandHandler {
    private List<String> receivedCommands = new ArrayList<>();
    private String response = "Command executed successfully";

    public String handleCommand(String commandName, List<String> args) {
      receivedCommands.add(commandName + ":" + String.join(",", args));
      return response;
    }

    public List<String> getReceivedCommands() {
      return receivedCommands;
    }

    public void setResponse(String response) {
      this.response = response;
    }
  }

  @Test
  public void testRunScannerWithValidCommands() {
    String input = "create calendar --name WorkCal --timezone UTC\n" +
          "use calendar --name WorkCal\n" +
          "exit";

    Scanner scanner = new Scanner(new StringReader(input));

    // Create a test command handler to track what happens
    final TestCommandHandler handler = new TestCommandHandler();

    // Create a custom invoker implementation for testing
    CommandInvoker testInvoker = new CommandInvoker("TestCalendar") {
      @Override
      public String executeCommand(String commandName, List<String> parts, ICalendarModel model) {
        return handler.handleCommand(commandName, parts);
      }
    };

    controller.publicRunScanner(scanner, true, mockView, testInvoker, mockModel);

    assertEquals(2, handler.getReceivedCommands().size());
    assertEquals("create calendar:--name,WorkCal,--timezone,UTC", handler.getReceivedCommands().get(0));
    assertEquals("use calendar:--name,WorkCal", handler.getReceivedCommands().get(1));

    // Check that the view displayed the responses
    assertEquals(2, mockView.getDisplayedMessages().size());
  }

  @Test
  public void testRunScannerWithExitCommand() {
    String input = "exit";

    Scanner scanner = new Scanner(new StringReader(input));

    // Create a test command handler to track what happens
    final TestCommandHandler handler = new TestCommandHandler();

    // Create a custom invoker implementation for testing
    CommandInvoker testInvoker = new CommandInvoker("TestCalendar") {
      @Override
      public String executeCommand(String commandName, List<String> parts, ICalendarModel model) {
        return handler.handleCommand(commandName, parts);
      }
    };

    controller.publicRunScanner(scanner, true, mockView, testInvoker, mockModel);

    // No commands should be executed since 'exit' is a special command
    assertEquals(0, handler.getReceivedCommands().size());
    assertEquals(0, mockView.getDisplayedMessages().size());
  }

  @Test
  public void testRunScannerWithEmptyLine() {
    String input = "\n\nexit";

    Scanner scanner = new Scanner(new StringReader(input));

    // Create a test command handler to track what happens
    final TestCommandHandler handler = new TestCommandHandler();

    // Create a custom invoker implementation for testing
    CommandInvoker testInvoker = new CommandInvoker("TestCalendar") {
      @Override
      public String executeCommand(String commandName, List<String> parts, ICalendarModel model) {
        return handler.handleCommand(commandName, parts);
      }
    };

    controller.publicRunScanner(scanner, true, mockView, testInvoker, mockModel);

    // No commands should be executed for empty lines
    assertEquals(0, handler.getReceivedCommands().size());
    assertEquals(0, mockView.getDisplayedMessages().size());
  }

  @Test
  public void testRunScannerWithInsufficientTokens() {
    String input = "create\nexit";

    Scanner scanner = new Scanner(new StringReader(input));

    // Create a test command handler to track what happens
    final TestCommandHandler handler = new TestCommandHandler();

    // Create a custom invoker implementation for testing
    CommandInvoker testInvoker = new CommandInvoker("TestCalendar") {
      @Override
      public String executeCommand(String commandName, List<String> parts, ICalendarModel model) {
        return handler.handleCommand(commandName, parts);
      }
    };

    controller.publicRunScanner(scanner, true, mockView, testInvoker, mockModel);

    // Check that the error message about insufficient tokens was displayed
    assertEquals(1, mockView.getDisplayedMessages().size());
    assertTrue(mockView.getDisplayedMessages().get(0).contains("Enter at-least two tokens"));
  }

  @Test
  public void testTokenizeCommandWithMixedQuotesDoesntWork() {
    List<String> tokens = controller.publicTokenizeCommand("create event \"Team Meeting\" --description 'Quarterly Review'");

    assertEquals(6, tokens.size());
    assertEquals("Team Meeting", tokens.get(2));
    assertNotEquals("Quarterly Review", tokens.get(4));
  }

  @Test
  public void testRunScannerWithNoDisplayMessage() {
    String input = "create calendar --name WorkCal --timezone UTC\nexit";

    Scanner scanner = new Scanner(new StringReader(input));

    final TestCommandHandler handler = new TestCommandHandler();


    CommandInvoker testInvoker = new CommandInvoker("TestCalendar") {
      @Override
      public String executeCommand(String commandName, List<String> parts, ICalendarModel model) {
        return handler.handleCommand(commandName, parts);
      }
    };

    controller.publicRunScanner(scanner, false, mockView, testInvoker, mockModel);

    assertEquals(1, handler.getReceivedCommands().size());
    assertEquals(0, mockView.getDisplayedMessages().size());
  }

  @Test
  public void testTokenizeCommandWithEscapedQuotes() {
    List<String> tokens = controller.publicTokenizeCommand("create event \"Meeting with special clients\" from 2025-05-01T10:00 to 2025-05-01T11:00");

    assertEquals(7, tokens.size());
    assertEquals("Meeting with special clients", tokens.get(2));
  }
}