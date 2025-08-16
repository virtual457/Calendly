package controller;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandExecutorAdaptionTests {

  private MockCalendarController mockController;
  private CommandExecutorAdaptor adaptor;

  private static class MockCalendarController implements ICalendarController {
    private boolean executeCalled = false;
    private String lastCommandExecuted = null;

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
      return new MockCalendarCommandAdapter();
    }

    public boolean isExecuteCalled() {
      return executeCalled;
    }

    public String getLastCommandExecuted() {
      return lastCommandExecuted;
    }
  }

  private static class MockCalendarCommandAdapter implements ICalendarCommandAdapter {
    @Override
    public boolean createCalendar(String name, String timezone) {
      return true;
    }

    @Override
    public boolean useCalendar(String calendarName) {
      return true;
    }

    @Override
    public boolean createEvent(model.ICalendarEventDTO event) {
      return true;
    }

    @Override
    public boolean editEvent(String property, String eventName, java.time.LocalDateTime fromDateTime,
                             java.time.LocalDateTime toDateTime, String newValue) {
      return true;
    }

    @Override
    public boolean editEvents(String property, String eventName, java.time.LocalDateTime fromDateTime,
                              String newValue) {
      return true;
    }

    @Override
    public boolean editEventsNoStartDate(String property, String eventName, String newValue) {
      return true;
    }

    @Override
    public boolean exportCalendar(String filePath) {
      return true;
    }

    @Override
    public boolean importCalendar(String filePath,String timezone) {
      return true;
    }

    @Override
    public void exit() {

    }
  }

  @Before
  public void setUp() {
    mockController = new MockCalendarController();
    adaptor = new CommandExecutorAdaptor(mockController);
  }

  @Test
  public void testConstructorRequiresNonNullController() {
    Exception exception = assertThrows(NullPointerException.class, () -> {
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
  public void testGetCommandAdapter() {
    ICalendarCommandAdapter adapter = adaptor.getCommandAdapter();

    assertNotNull(adapter);
    assertTrue(adapter instanceof ObjectToCommandAdapter);
  }

  @Test
  public void testLazyInitialization() {
    ICalendarCommandAdapter adapter1 = adaptor.getCommandAdapter();
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