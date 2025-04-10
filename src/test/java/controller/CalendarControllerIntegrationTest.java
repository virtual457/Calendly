package controller;

import model.ICalendarEventDTO;
import model.ICalendarModel;
import org.junit.Before;
import org.junit.Test;
import view.IView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.*;

/**
 * Integration tests for the CalendarController class.
 * Tests the controller's interaction with models, commands, and views.
 */
public class CalendarControllerIntegrationTest {

  private ICalendarController controller;
  private MockModel mockModel;
  private MockView mockView;

  /**
   * Mock implementation of ICalendarModel to track method invocations
   */
  private static class MockModel implements ICalendarModel {
    private List<String> methodCalls = new ArrayList<>();
    private boolean operationSucceeds = true;
    private String currentCalendar = null;
    private List<String> calendars = new ArrayList<>();

    @Override
    public boolean createCalendar(String calName, String timezone) {
      methodCalls.add("createCalendar:" + calName + "," + timezone);
      calendars.add(calName);
      return operationSucceeds;
    }

    @Override
    public boolean addEvent(String calendarName, ICalendarEventDTO event) {
      methodCalls.add("addEvent:" + calendarName);
      return operationSucceeds;
    }

    @Override
    public boolean editEvents(String calendarName, String property, String eventName, LocalDateTime fromDateTime, String newValue, boolean editAll) {
      methodCalls.add("editEvents:" + calendarName + "," + property + "," + eventName);
      return operationSucceeds;
    }

    @Override
    public boolean editEvent(String calendarName, String property, String eventName, LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue) {
      methodCalls.add("editEvent:" + calendarName + "," + property + "," + eventName);
      return operationSucceeds;
    }

    @Override
    public boolean isCalendarAvailable(String calName, LocalDate date) {
      methodCalls.add("isCalendarAvailable:" + calName);
      return operationSucceeds;
    }

    @Override
    public List<String> getCalendarNames() {
      methodCalls.add("getCalendarNames");
      return calendars;
    }

    @Override
    public boolean deleteCalendar(String calName) {
      methodCalls.add("deleteCalendar:" + calName);
      calendars.remove(calName);
      return operationSucceeds;
    }

    @Override
    public List<ICalendarEventDTO> getEventsInRange(String calendarName, LocalDateTime fromDateTime, LocalDateTime toDateTime) {
      methodCalls.add("getEventsInRange:" + calendarName);
      return List.of();
    }

    @Override
    public List<ICalendarEventDTO> getEventsInSpecificDateTime(String calendarName, LocalDateTime dateTime) {
      methodCalls.add("getEventsInSpecificDateTime:" + calendarName);
      return List.of();
    }

    @Override
    public boolean copyEvents(String sourceCalendarName, LocalDateTime sourceStart, LocalDateTime sourceEnd, String targetCalendarName, LocalDate targetStart) {
      methodCalls.add("copyEvents:" + sourceCalendarName + "," + targetCalendarName);
      return operationSucceeds;
    }

    @Override
    public boolean copyEvent(String sourceCalendarName, LocalDateTime sourceStart, String eventName, String targetCalendarName, LocalDateTime targetStart) {
      methodCalls.add("copyEvent:" + sourceCalendarName + "," + targetCalendarName);
      return operationSucceeds;
    }

    @Override
    public boolean isCalendarPresent(String calName) {
      methodCalls.add("isCalendarPresent:" + calName);
      return calendars.contains(calName);
    }

    @Override
    public boolean editCalendar(String calendarName, String property, String newValue) {
      methodCalls.add("editCalendar:" + calendarName + "," + property + "," + newValue);
      if (property.equalsIgnoreCase("name")) {
        calendars.remove(calendarName);
        calendars.add(newValue);
      }
      return operationSucceeds;
    }

    @Override
    public boolean addEvents(String calendarName, List<ICalendarEventDTO> events, String timezone) {
      methodCalls.add("addEvents:" + calendarName);
      return operationSucceeds;
    }

    public List<String> getMethodCalls() {
      return methodCalls;
    }

    public void setOperationSucceeds(boolean succeeds) {
      this.operationSucceeds = succeeds;
    }

    public void setCurrentCalendar(String currentCalendar) {
      this.currentCalendar = currentCalendar;
    }
  }

  /**
   * Mock implementation of IView to track display and command executions
   */
  private static class MockView implements IView {
    private List<String> displayedMessages = new ArrayList<>();
    private boolean startCalled = false;
    private Consumer<String> onCommandReceived;
    private ICommandExecutor executor;

    @Override
    public void display(String message) {
      displayedMessages.add(message);
    }

    @Override
    public void start(ICommandExecutor commandExecutor) {
      startCalled = true;
      this.executor = commandExecutor;
      if (onCommandReceived != null) {
        onCommandReceived.accept("start called");
      }
    }

    public List<String> getDisplayedMessages() {
      return displayedMessages;
    }

    public boolean isStartCalled() {
      return startCalled;
    }

    public void setOnCommandReceived(Consumer<String> onCommandReceived) {
      this.onCommandReceived = onCommandReceived;
    }

    public void simulateCommand(String command) {
      if (executor != null) {
        executor.executeCommand(command);
      }
    }
  }

  @Before
  public void setUp() {
    mockModel = new MockModel();
    mockView = new MockView();
    controller = ICalendarController.createInstance("Advanced", mockModel, mockView);
  }

  @Test
  public void testStartInitializesCorrectly() {
    // Call the start method
    controller.start();

    // Verify that view's start method was called
    assertTrue("View's start method should be called", mockView.isStartCalled());

    // Verify welcome message was displayed
    assertTrue(mockView.getDisplayedMessages().contains("Welcome to the Calendar App!"));
  }

  @Test
  public void testExecuteCommandWithValidInput() {
    // Execute a valid command
    controller.executeCommand("create calendar --name WorkCal --timezone America/New_York");

    // Check that the model method was called correctly
    assertTrue(mockModel.getMethodCalls().contains("createCalendar:WorkCal,America/New_York"));

    // Check that success message was displayed to the view
    String lastMessage = mockView.getDisplayedMessages().get(mockView.getDisplayedMessages().size() - 1);
    assertTrue(lastMessage.contains("successfully"));
  }

  @Test
  public void testExecuteMultipleCommands() {
    // Execute a series of commands
    controller.executeCommand("create calendar --name WorkCal --timezone America/New_York");
    controller.executeCommand("use calendar --name WorkCal");
    controller.executeCommand("create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00");

    // Check the model calls
    List<String> calls = mockModel.getMethodCalls();
    assertTrue(calls.contains("createCalendar:WorkCal,America/New_York"));
    assertTrue(calls.contains("isCalendarPresent:WorkCal"));
    assertTrue(calls.contains("addEvent:WorkCal"));

    // Ensure the view shows appropriate success messages
    List<String> messages = mockView.getDisplayedMessages();
    assertTrue(messages.stream().anyMatch(m -> m.contains("Calendar created successfully")));
    assertTrue(messages.stream().anyMatch(m -> m.contains("Using calendar: WorkCal")));
    assertTrue(messages.stream().anyMatch(m -> m.contains("Event created successfully")));
  }

  @Test
  public void testExecuteCommandWithInvalidInput() {
    // Execute a command with invalid arguments
    controller.executeCommand("create calendar --name");

    // Should display an error message
    String lastMessage = mockView.getDisplayedMessages().get(mockView.getDisplayedMessages().size() - 1);
    assertTrue(lastMessage.contains("Error"));
  }

  @Test
  public void testExecuteCommandWithNonexistentCalendar() {
    // Try to use a calendar that doesn't exist
    controller.executeCommand("use calendar --name NonExistentCal");

    // Check the error message
    String lastMessage = mockView.getDisplayedMessages().get(mockView.getDisplayedMessages().size() - 1);
    assertTrue(lastMessage.contains("Error: calendar not found"));
  }

  @Test
  public void testCommandAdapterIntegration() {
    // Get the command adapter
    ICalendarCommandAdapter adapter = controller.getCommandAdapter();
    assertNotNull("Command adapter should not be null", adapter);

    // Use the adapter to execute operations
    boolean success = adapter.createCalendar("WorkCal", "America/New_York");
    assertTrue("Calendar creation through adapter should succeed", success);

    // Check that the model was called correctly
    assertTrue(mockModel.getMethodCalls().contains("createCalendar:WorkCal,America/New_York"));

    // Use the adapter for a calendar
    success = adapter.useCalendar("WorkCal");
    assertTrue("Using calendar through adapter should succeed", success);

    // Check that the model was called correctly
    assertTrue(mockModel.getMethodCalls().contains("isCalendarPresent:WorkCal"));
  }

  @Test
  public void testMultipleCommandExecutorsShareState() {
    // Get two command executors from the same controller
    ICommandExecutor executor1 = new CommandExecutorAdaptor(controller);
    ICommandExecutor executor2 = new CommandExecutorAdaptor(controller);

    // Use one executor to create a calendar
    executor1.executeCommand("create calendar --name SharedCal --timezone UTC");

    // Use the other executor to use the calendar
    executor2.executeCommand("use calendar --name SharedCal");

    // Check that the model shows both operations
    List<String> calls = mockModel.getMethodCalls();
    assertTrue(calls.contains("createCalendar:SharedCal,UTC"));
    assertTrue(calls.contains("isCalendarPresent:SharedCal"));

    // Check that the view shows both success messages
    List<String> messages = mockView.getDisplayedMessages();
    assertTrue(messages.stream().anyMatch(m -> m.contains("Calendar created successfully")));
    assertTrue(messages.stream().anyMatch(m -> m.contains("Using calendar: SharedCal")));
  }

  @Test
  public void testFailedModelOperationsHandledGracefully() {
    // Make model operations fail
    mockModel.setOperationSucceeds(false);

    // Try to create a calendar
    controller.executeCommand("create calendar --name FailCal --timezone UTC");

    // Check that an error message was displayed
    String lastMessage = mockView.getDisplayedMessages().get(mockView.getDisplayedMessages().size() - 1);
    assertTrue(lastMessage.contains("Error") || lastMessage.contains("failed"));
  }

  @Test
  public void testCommandAdapterErrorHandling() {
    // Make model operations fail
    mockModel.setOperationSucceeds(false);

    // Get the command adapter
    ICalendarCommandAdapter adapter = controller.getCommandAdapter();

    // Operations should return false when the model fails
    adapter.createCalendar("FailCal", "UTC");
    assertEquals(this.mockView.getDisplayedMessages().get(
          mockView.getDisplayedMessages().size() - 1),
          "Error: Calendar creation failed.");

    // The model should still be called
    assertTrue(mockModel.getMethodCalls().contains("createCalendar:FailCal,UTC"));
  }
}