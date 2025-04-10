package controller;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandExecutorAdaptorTest2 {

  private MockCalendarController mockController;
  private ICommandExecutor adaptor;

  private static class MockCalendarController implements ICalendarController {
    private boolean executeCalled = false;
    private String lastCommandExecuted = null;
    private final ICalendarCommandAdapter mockAdapter = new MockCalendarCommandAdapter();

    @Override
    public void start() {
    }

    @Override
    public void executeCommand(String command) {
      executeCalled = true;
      lastCommandExecuted = command;
    }

    @Override
    public ICalendarCommandAdapter getCommandAdapter() {
      return mockAdapter;
    }

    public boolean isExecuteCalled() {
      return executeCalled;
    }

    public String getLastCommandExecuted() {
      return lastCommandExecuted;
    }
  }

  private static class MockCalendarCommandAdapter implements ICalendarCommandAdapter {
    private boolean methodCalled = false;
    private String lastCalledMethod = null;

    @Override
    public boolean createCalendar(String name, String timezone) {
      methodCalled = true;
      lastCalledMethod = "createCalendar";
      return true;
    }

    @Override
    public boolean useCalendar(String calendarName) {
      methodCalled = true;
      lastCalledMethod = "useCalendar";
      return true;
    }

    @Override
    public boolean createEvent(model.ICalendarEventDTO event) {
      methodCalled = true;
      lastCalledMethod = "createEvent";
      return true;
    }

    @Override
    public boolean editEvent(String property, String eventName, java.time.LocalDateTime fromDateTime,
                             java.time.LocalDateTime toDateTime, String newValue) {
      methodCalled = true;
      lastCalledMethod = "editEvent";
      return true;
    }

    @Override
    public boolean editEvents(String property, String eventName, java.time.LocalDateTime fromDateTime,
                              String newValue) {
      methodCalled = true;
      lastCalledMethod = "editEvents";
      return true;
    }

    @Override
    public boolean editEventsNoStartDate(String property, String eventName, String newValue) {
      methodCalled = true;
      lastCalledMethod = "editEventsNoStartDate";
      return true;
    }

    @Override
    public boolean exportCalendar(String filePath) {
      methodCalled = true;
      lastCalledMethod = "exportCalendar";
      return true;
    }

    @Override
    public boolean importCalendar(String filePath,String timezone) {
      methodCalled = true;
      lastCalledMethod = "importCalendar";
      return true;
    }

    @Override
    public void exit() {

    }

    public boolean isMethodCalled() {
      return methodCalled;
    }

    public String getLastCalledMethod() {
      return lastCalledMethod;
    }
  }

  @Before
  public void setUp() {
    mockController = new MockCalendarController();
    adaptor = new CommandExecutorAdaptor(mockController);
  }

  @Test
  public void testConstructorRequiresNonNullController() {
    assertThrows(NullPointerException.class, () -> {
      new CommandExecutorAdaptor(null);
    });
  }

  @Test
  public void testExecuteCommandDelegates() {
    adaptor.executeCommand("test command");

    assertTrue(mockController.isExecuteCalled());
    assertEquals("test command", mockController.getLastCommandExecuted());
  }

  @Test
  public void testExecuteCommandHandlesNull() {
    adaptor.executeCommand(null);

    assertTrue(mockController.isExecuteCalled());
    assertNull(mockController.getLastCommandExecuted());
  }

  @Test
  public void testExecuteCommandWhitespace() {
    adaptor.executeCommand("   test command   ");

    assertEquals("   test command   ", mockController.getLastCommandExecuted());
  }

  @Test
  public void testLazyInitialization() {
    ICalendarCommandAdapter adapter1 = adaptor.getCommandAdapter();

    mockController = new MockCalendarController();

    ICalendarCommandAdapter adapter2 = adaptor.getCommandAdapter();

    assertSame(adapter1, adapter2);
  }

  @Test
  public void testIndependentAdaptors() {
    CommandExecutorAdaptor adaptor1 = new CommandExecutorAdaptor(mockController);
    CommandExecutorAdaptor adaptor2 = new CommandExecutorAdaptor(mockController);

    ICalendarCommandAdapter adapter1 = adaptor1.getCommandAdapter();
    ICalendarCommandAdapter adapter2 = adaptor2.getCommandAdapter();

    assertNotSame(adapter1, adapter2);
  }

  @Test
  public void testCommandChainingThroughAdaptor() {
    adaptor.executeCommand("command 1");
    assertEquals("command 1", mockController.getLastCommandExecuted());

    adaptor.executeCommand("command 2");
    assertEquals("command 2", mockController.getLastCommandExecuted());
  }
}