package controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import model.ICalendarEventDTO;

import static org.junit.Assert.*;

/**
 * Unit tests for ObjectToCommandAdapter.
 * Verifies that the adapter correctly translates method calls to command strings.
 */
public class CommandExecutorAdaptorTest {

  private ObjectToCommandAdapter adapter;
  private MockCommandExecutor mockExecutor;

  /**
   * Mock implementation of ICommandExecutor to track command execution
   */
  private static class MockCommandExecutor implements ICommandExecutor {
    private String lastExecutedCommand = null;
    private int executeCommandCallCount = 0;

    @Override
    public void executeCommand(String command) {
      lastExecutedCommand = command;
      executeCommandCallCount++;
    }

    @Override
    public ICalendarCommandAdapter getCommandAdapter() {
      return null; // Not needed for these tests
    }

    // Methods to verify execution
    public String getLastExecutedCommand() {
      return lastExecutedCommand;
    }

    public int getExecuteCommandCallCount() {
      return executeCommandCallCount;
    }

    public void reset() {
      lastExecutedCommand = null;
      executeCommandCallCount = 0;
    }
  }

  /**
   * Mock implementation of ICalendarEventDTO for testing
   */
  private static class MockCalendarEventDTO implements ICalendarEventDTO {
    private String name;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String description;
    private String location;
    private Boolean isPrivate;
    private Boolean isRecurring;
    private List<DayOfWeek> recurrenceDays;
    private Integer recurrenceCount;
    private LocalDateTime recurrenceEndDate;
    private Boolean autoDecline;

    public MockCalendarEventDTO(String name, LocalDateTime start, LocalDateTime end) {
      this.name = name;
      this.startDateTime = start;
      this.endDateTime = end;
      this.description = "";
      this.location = "";
      this.isPrivate = false;
      this.isRecurring = false;
      this.recurrenceDays = new ArrayList<>();
      this.recurrenceCount = null;
      this.recurrenceEndDate = null;
      this.autoDecline = true;
    }

    @Override
    public String getEventName() {
      return name;
    }

    @Override
    public LocalDateTime getStartDateTime() {
      return startDateTime;
    }

    @Override
    public LocalDateTime getEndDateTime() {
      return endDateTime;
    }

    @Override
    public Boolean isRecurring() {
      return isRecurring;
    }

    @Override
    public List<DayOfWeek> getRecurrenceDays() {
      return recurrenceDays;
    }

    @Override
    public Integer getRecurrenceCount() {
      return recurrenceCount;
    }

    @Override
    public LocalDateTime getRecurrenceEndDate() {
      return recurrenceEndDate;
    }

    @Override
    public Boolean isAutoDecline() {
      return autoDecline;
    }

    @Override
    public String getEventDescription() {
      return description;
    }

    @Override
    public String getEventLocation() {
      return location;
    }

    @Override
    public Boolean isPrivate() {
      return isPrivate;
    }

    // Builder-like methods for test setup
    public MockCalendarEventDTO withDescription(String description) {
      this.description = description;
      return this;
    }

    public MockCalendarEventDTO withLocation(String location) {
      this.location = location;
      return this;
    }

    public MockCalendarEventDTO withPrivate(boolean isPrivate) {
      this.isPrivate = isPrivate;
      return this;
    }

    public MockCalendarEventDTO withRecurring(boolean isRecurring) {
      this.isRecurring = isRecurring;
      return this;
    }

    public MockCalendarEventDTO withRecurrenceDays(List<DayOfWeek> days) {
      this.recurrenceDays = days;
      return this;
    }

    public MockCalendarEventDTO withRecurrenceCount(int count) {
      this.recurrenceCount = count;
      return this;
    }

    public MockCalendarEventDTO withRecurrenceEndDate(LocalDateTime endDate) {
      this.recurrenceEndDate = endDate;
      return this;
    }

    public MockCalendarEventDTO withAutoDecline(boolean autoDecline) {
      this.autoDecline = autoDecline;
      return this;
    }
  }

  @Before
  public void setUp() {
    mockExecutor = new MockCommandExecutor();
    adapter = new ObjectToCommandAdapter(mockExecutor);
  }

  @After
  public void tearDown() {
    mockExecutor = null;
    adapter = null;
  }

  @Test
  public void testCreateCalendar_GeneratesCorrectCommand() {
    
    String name = "TestCalendar";
    String timezone = "America/New_York";
    String expectedCommand = "create calendar --name \"TestCalendar\" --timezone \"America/New_York\"";

    
    boolean result = adapter.createCalendar(name, timezone);

    
    assertTrue("Method should return true", result);
    assertEquals("Command should be generated correctly",
          expectedCommand, mockExecutor.getLastExecutedCommand());
    assertEquals("Command should be executed once",
          1, mockExecutor.getExecuteCommandCallCount());
  }

  @Test
  public void testUseCalendar_GeneratesCorrectCommand() {
    
    String calendarName = "WorkCalendar";
    String expectedCommand = "use calendar --name \"WorkCalendar\"";

    
    boolean result = adapter.useCalendar(calendarName);

    
    assertTrue("Method should return true", result);
    assertEquals("Command should be generated correctly",
          expectedCommand, mockExecutor.getLastExecutedCommand());
    assertEquals("Command should be executed once",
          1, mockExecutor.getExecuteCommandCallCount());
  }

  @Test
  public void testCreateEvent_SimpleEvent_GeneratesCorrectCommand() {
    
    LocalDateTime start = LocalDateTime.of(2025, 6, 10, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 10, 11, 0);
    MockCalendarEventDTO event = new MockCalendarEventDTO("Team Meeting", start, end);

    String expectedCommand = "create event \"Team Meeting\" from 2025-06-10T10:00 to 2025-06-10T11:00";

    
    boolean result = adapter.createEvent(event);

    
    assertTrue("Method should return true", result);
    assertEquals("Command should be generated correctly",
          expectedCommand, mockExecutor.getLastExecutedCommand());
  }

  @Test
  public void testCreateEvent_WithAttributes_GeneratesCorrectCommand() {
    
    LocalDateTime start = LocalDateTime.of(2025, 6, 10, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 10, 11, 0);
    MockCalendarEventDTO event = new MockCalendarEventDTO("Team Meeting", start, end)
          .withDescription("Weekly team sync")
          .withLocation("Conference Room A")
          .withPrivate(true);

    String expectedCommand = "create event \"Team Meeting\" from 2025-06-10T10:00 to 2025-06-10T11:00" +
          " --description \"Weekly team sync\" --location \"Conference Room A\" --private";

    
    boolean result = adapter.createEvent(event);

    
    assertTrue("Method should return true", result);
    assertEquals("Command should be generated correctly",
          expectedCommand, mockExecutor.getLastExecutedCommand());
  }

  @Test
  public void testCreateEvent_RecurringEvent_GeneratesCorrectCommand() {
    
    LocalDateTime start = LocalDateTime.of(2025, 6, 10, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 10, 11, 0);
    LocalDateTime recurrenceEnd = LocalDateTime.of(2025, 8, 10, 0, 0);

    List<DayOfWeek> days = Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

    MockCalendarEventDTO event = new MockCalendarEventDTO("Recurring Meeting", start, end)
          .withRecurring(true)
          .withRecurrenceDays(days)
          .withRecurrenceEndDate(recurrenceEnd);

    // The pattern should include MWF for Monday, Wednesday, Friday
    String expectedCommand = "create event \"Recurring Meeting\" from 2025-06-10T10:00 to 2025-06-10T11:00" +
          " repeats MWF until 2025-08-10";

    
    boolean result = adapter.createEvent(event);

    
    assertTrue("Method should return true", result);
    assertEquals("Command should be generated correctly",
          expectedCommand, mockExecutor.getLastExecutedCommand());
  }

  @Test
  public void testEditEvent_GeneratesCorrectCommand() {
    
    String property = "name";
    String eventName = "Old Meeting Name";
    LocalDateTime from = LocalDateTime.of(2025, 6, 10, 10, 0);
    LocalDateTime to = LocalDateTime.of(2025, 6, 10, 11, 0);
    String newValue = "New Meeting Name";

    String expectedCommand = "edit event name \"Old Meeting Name\" from 2025-06-10T10:00 to 2025-06-10T11:00 with \"New Meeting Name\"";

    
    boolean result = adapter.editEvent(property, eventName, from, to, newValue);

    
    assertTrue("Method should return true", result);
    assertEquals("Command should be generated correctly",
          expectedCommand, mockExecutor.getLastExecutedCommand());
  }

  @Test
  public void testEditEvents_WithStartDateTime_GeneratesCorrectCommand() {
    
    String property = "start";
    String eventName = "Daily Standup";
    LocalDateTime from = LocalDateTime.of(2025, 6, 10, 10, 0);
    String newValue = "2025-06-10T09:30";

    String expectedCommand = "edit events start \"Daily Standup\" from 2025-06-10T10:00 with \"2025-06-10T09:30\"";

    
    boolean result = adapter.editEvents(property, eventName, from, newValue);

    
    assertTrue("Method should return true", result);
    assertEquals("Command should be generated correctly",
          expectedCommand, mockExecutor.getLastExecutedCommand());
  }

  @Test
  public void testEditEventsNoStartDate_GeneratesCorrectCommand() {
    
    String property = "location";
    String eventName = "Weekly Meeting";
    String newValue = "Conference Room B";

    String expectedCommand = "edit events location \"Weekly Meeting\" \"Conference Room B\"";

    
    boolean result = adapter.editEventsNoStartDate(property, eventName, newValue);

    
    assertTrue("Method should return true", result);
    assertEquals("Command should be generated correctly",
          expectedCommand, mockExecutor.getLastExecutedCommand());
  }

  @Test
  public void testExportCalendar_GeneratesCorrectCommand() {
    
    String filePath = "calendar_export.csv";
    String expectedCommand = "export cal \"calendar_export.csv\"";

    
    boolean result = adapter.exportCalendar(filePath);

    
    assertTrue("Method should return true", result);
    assertEquals("Command should be generated correctly",
          expectedCommand, mockExecutor.getLastExecutedCommand());
  }

  @Test
  public void testImportCalendar_GeneratesCorrectCommand() {
    
    String filePath = "calendar_import.csv";
    String expectedCommand = "import cal \"calendar_import.csv\" --timezone \"UTC\"";

    
    boolean result = adapter.importCalendar(filePath,"UTC");

    
    assertTrue("Method should return true", result);
    assertEquals("Command should be generated correctly",
          expectedCommand, mockExecutor.getLastExecutedCommand());
  }

  @Test
  public void testAdapter_HandlesExceptionGracefully() {
    ICommandExecutor badExecutor = new ICommandExecutor() {
      @Override
      public void executeCommand(String command) {
        throw new RuntimeException("Test exception");
      }

      @Override
      public ICalendarCommandAdapter getCommandAdapter() {
        return null;
      }
    };

    ObjectToCommandAdapter testAdapter = new ObjectToCommandAdapter(badExecutor);

    
    boolean result = testAdapter.createCalendar("TestCal", "UTC");

    
    assertFalse("Method should return false when exception occurs", result);
  }



}