package controller;

import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.ICalendarEventDTO;

import static org.junit.Assert.*;

/**
 * Comprehensive tests for the ObjectToCommandAdapter class.
 * Tests the translation of method calls to command strings.
 */
public class ObjectToCommandAdapterTest {

  private ObjectToCommandAdapter adapter;
  private MockCommandExecutor mockExecutor;

  /**
   * A mock implementation of ICalendarEventDTO for testing
   */
  private static class MockEventDTO implements ICalendarEventDTO {
    private final String name;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final Boolean isRecurring;
    private final List<DayOfWeek> recurrenceDays;
    private final Integer recurrenceCount;
    private final LocalDateTime recurrenceEndDate;
    private final Boolean autoDecline;
    private final String description;
    private final String location;
    private final Boolean isPrivate;

    public MockEventDTO(String name, LocalDateTime start, LocalDateTime end) {
      this.name = name;
      this.startTime = start;
      this.endTime = end;
      this.isRecurring = false;
      this.recurrenceDays = new ArrayList<>();
      this.recurrenceCount = null;
      this.recurrenceEndDate = null;
      this.autoDecline = true;
      this.description = "";
      this.location = "";
      this.isPrivate = false;
    }

    public MockEventDTO(String name, LocalDateTime start, LocalDateTime end,
                        Boolean isRecurring, List<DayOfWeek> recurrenceDays,
                        Integer recurrenceCount, LocalDateTime recurrenceEndDate,
                        Boolean autoDecline, String description, String location,
                        Boolean isPrivate) {
      this.name = name;
      this.startTime = start;
      this.endTime = end;
      this.isRecurring = isRecurring;
      this.recurrenceDays = recurrenceDays;
      this.recurrenceCount = recurrenceCount;
      this.recurrenceEndDate = recurrenceEndDate;
      this.autoDecline = autoDecline;
      this.description = description;
      this.location = location;
      this.isPrivate = isPrivate;
    }

    // Builder-style methods for test convenience
    public MockEventDTO withRecurring(boolean recurring) {
      return new MockEventDTO(name, startTime, endTime, recurring, recurrenceDays,
            recurrenceCount, recurrenceEndDate, autoDecline,
            description, location, isPrivate);
    }

    public MockEventDTO withRecurrenceDays(List<DayOfWeek> days) {
      return new MockEventDTO(name, startTime, endTime, isRecurring, days,
            recurrenceCount, recurrenceEndDate, autoDecline,
            description, location, isPrivate);
    }

    public MockEventDTO withRecurrenceCount(Integer count) {
      return new MockEventDTO(name, startTime, endTime, isRecurring, recurrenceDays,
            count, recurrenceEndDate, autoDecline,
            description, location, isPrivate);
    }

    public MockEventDTO withRecurrenceEndDate(LocalDateTime endDate) {
      return new MockEventDTO(name, startTime, endTime, isRecurring, recurrenceDays,
            recurrenceCount, endDate, autoDecline,
            description, location, isPrivate);
    }

    public MockEventDTO withDescription(String description) {
      return new MockEventDTO(name, startTime, endTime, isRecurring, recurrenceDays,
            recurrenceCount, recurrenceEndDate, autoDecline,
            description, location, isPrivate);
    }

    public MockEventDTO withLocation(String location) {
      return new MockEventDTO(name, startTime, endTime, isRecurring, recurrenceDays,
            recurrenceCount, recurrenceEndDate, autoDecline,
            description, location, isPrivate);
    }

    public MockEventDTO withPrivate(boolean isPrivate) {
      return new MockEventDTO(name, startTime, endTime, isRecurring, recurrenceDays,
            recurrenceCount, recurrenceEndDate, autoDecline,
            description, location, isPrivate);
    }

    public MockEventDTO withAutoDecline(boolean autoDecline) {
      return new MockEventDTO(name, startTime, endTime, isRecurring, recurrenceDays,
            recurrenceCount, recurrenceEndDate, autoDecline,
            description, location, isPrivate);
    }

    @Override
    public String getEventName() {
      return name;
    }

    @Override
    public LocalDateTime getStartDateTime() {
      return startTime;
    }

    @Override
    public LocalDateTime getEndDateTime() {
      return endTime;
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
  }

  /**
   * Mock CommandExecutor implementation to capture executed commands
   */
  private static class MockCommandExecutor implements ICommandExecutor {
    private final List<String> executedCommands = new ArrayList<>();
    private boolean shouldSucceed = true;

    @Override
    public void executeCommand(String command) {
      executedCommands.add(command);
      if (!shouldSucceed) {
        throw new RuntimeException("Command execution failed for testing");
      }
    }

    @Override
    public ICalendarCommandAdapter getCommandAdapter() {
      return null; // Not needed for these tests
    }

    public List<String> getExecutedCommands() {
      return executedCommands;
    }

    public void setShouldSucceed(boolean shouldSucceed) {
      this.shouldSucceed = shouldSucceed;
    }
  }

  @Before
  public void setUp() {
    mockExecutor = new MockCommandExecutor();
    adapter = new ObjectToCommandAdapter(mockExecutor);
  }

  @Test
  public void testCreateCalendar() {
    // Test with simple calendar name and timezone
    boolean result = adapter.createCalendar("WorkCalendar", "America/New_York");

    assertTrue("Create calendar should return true for success", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    assertEquals("create calendar --name \"WorkCalendar\" --timezone \"America/New_York\"",
          mockExecutor.getExecutedCommands().get(0));
  }

  @Test
  public void testCreateCalendarWithSpecialChars() {
    // Test with calendar name containing special chars and spaces
    boolean result = adapter.createCalendar("My Work & Home Calendar", "Europe/Paris");

    assertTrue("Create calendar should succeed with special chars", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    assertEquals("create calendar --name \"My Work & Home Calendar\" --timezone \"Europe/Paris\"",
          mockExecutor.getExecutedCommands().get(0));
  }

  @Test
  public void testUseCalendar() {
    // Test using a calendar
    boolean result = adapter.useCalendar("PersonalCalendar");

    assertTrue("Use calendar should return true for success", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    assertEquals("use calendar --name \"PersonalCalendar\"",
          mockExecutor.getExecutedCommands().get(0));
  }

  @Test
  public void testCreateSimpleEvent() {
    // Create a simple non-recurring event
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);

    ICalendarEventDTO event = new MockEventDTO("Team Meeting", start, end);

    boolean result = adapter.createEvent(event);

    assertTrue("Create event should succeed", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    String command = mockExecutor.getExecutedCommands().get(0);

    assertTrue(command.contains("create event \"Team Meeting\""));
    assertTrue(command.contains("from 2025-05-01T10:00 to 2025-05-01T11:00"));
  }

  @Test
  public void testCreateAllDayEvent() {
    // Create an all-day event (starts at midnight, ends at 23:59:59)
    LocalDate date = LocalDate.of(2025, 6, 15);
    LocalDateTime start = date.atStartOfDay();
    LocalDateTime end = date.atTime(23, 59, 59);

    ICalendarEventDTO event = new MockEventDTO("Company Holiday", start, end);

    boolean result = adapter.createEvent(event);

    assertTrue("Create all-day event should succeed", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    String command = mockExecutor.getExecutedCommands().get(0);

    assertTrue(command.contains("create event \"Company Holiday\""));
    assertTrue(command.contains("on 2025-06-15"));
    assertFalse(command.contains("from"));  // Should not use from/to format
  }

  @Test
  public void testCreateEventWithOptionalFields() {
    // Create an event with description, location, and private flag
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);

    ICalendarEventDTO event = new MockEventDTO("Strategy Meeting", start, end)
          .withDescription("Quarterly planning session")
          .withLocation("Conference Room A")
          .withPrivate(true);

    boolean result = adapter.createEvent(event);

    assertTrue("Create event with optional fields should succeed", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    String command = mockExecutor.getExecutedCommands().get(0);

    assertTrue(command.contains("create event \"Strategy Meeting\""));
    assertTrue(command.contains("from 2025-05-01T10:00 to 2025-05-01T11:00"));
    assertTrue(command.contains("--description \"Quarterly planning session\""));
    assertTrue(command.contains("--location \"Conference Room A\""));
    assertTrue(command.contains("--private"));
  }

  @Test
  public void testCreateRecurringEventWithCount() {
    // Create a recurring event with count
    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 9, 0); // Monday
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 10, 0);

    ICalendarEventDTO event = new MockEventDTO("Weekly Status", start, end)
          .withRecurring(true)
          .withRecurrenceDays(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
          .withRecurrenceCount(8);

    boolean result = adapter.createEvent(event);

    assertTrue("Create recurring event with count should succeed", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    String command = mockExecutor.getExecutedCommands().get(0);

    assertTrue(command.contains("create event \"Weekly Status\""));
    assertTrue(command.contains("from 2025-05-05T09:00 to 2025-05-05T10:00"));
    assertTrue(command.contains("repeats MWF for 8 times"));
  }

  @Test
  public void testCreateRecurringEventWithEndDate() {
    // Create a recurring event with end date
    LocalDateTime start = LocalDateTime.of(2025, 5, 6, 14, 0); // Tuesday
    LocalDateTime end = LocalDateTime.of(2025, 5, 6, 15, 0);
    LocalDateTime recurrenceEnd = LocalDateTime.of(2025, 6, 30, 0, 0);

    ICalendarEventDTO event = new MockEventDTO("Team Sync", start, end)
          .withRecurring(true)
          .withRecurrenceDays(Arrays.asList(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY))
          .withRecurrenceEndDate(recurrenceEnd);

    boolean result = adapter.createEvent(event);

    assertTrue("Create recurring event with end date should succeed", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    String command = mockExecutor.getExecutedCommands().get(0);

    assertTrue(command.contains("create event \"Team Sync\""));
    assertTrue(command.contains("from 2025-05-06T14:00 to 2025-05-06T15:00"));
    assertTrue(command.contains("repeats TR until 2025-06-30"));
  }

  @Test
  public void testEditEvent() {
    // Edit an event property
    String property = "location";
    String eventName = "Weekly Meeting";
    LocalDateTime fromDateTime = LocalDateTime.of(2025, 5, 7, 9, 0);
    LocalDateTime toDateTime = LocalDateTime.of(2025, 5, 7, 10, 0);
    String newValue = "Conference Room B";

    boolean result = adapter.editEvent(property, eventName, fromDateTime, toDateTime, newValue);

    assertTrue("Edit event should succeed", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    String command = mockExecutor.getExecutedCommands().get(0);

    assertEquals("edit event location \"Weekly Meeting\" from 2025-05-07T09:00 to 2025-05-07T10:00 with \"Conference Room B\"",
          command);
  }

  @Test
  public void testEditEvents() {
    // Edit events with a start time filter
    String property = "description";
    String eventName = "Status Update";
    LocalDateTime fromDateTime = LocalDateTime.of(2025, 5, 8, 10, 0);
    String newValue = "Team progress review";

    boolean result = adapter.editEvents(property, eventName, fromDateTime, newValue);

    assertTrue("Edit events should succeed", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    String command = mockExecutor.getExecutedCommands().get(0);

    assertEquals("edit events description \"Status Update\" from 2025-05-08T10:00 with \"Team progress review\"",
          command);
  }

  @Test
  public void testEditEventsNoStartDate() {
    // Edit all matching events without date filter
    String property = "name";
    String eventName = "Old Name";
    String newValue = "New Name";

    boolean result = adapter.editEventsNoStartDate(property, eventName, newValue);

    assertTrue("Edit events without start date should succeed", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    String command = mockExecutor.getExecutedCommands().get(0);

    assertEquals("edit events name \"Old Name\" \"New Name\"", command);
  }

  @Test
  public void testExportCalendar() {
    // Export calendar to CSV
    String filePath = "work_calendar.csv";

    boolean result = adapter.exportCalendar(filePath);

    assertTrue("Export calendar should succeed", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    String command = mockExecutor.getExecutedCommands().get(0);

    assertEquals("export cal \"work_calendar.csv\"", command);
  }

  @Test
  public void testImportCalendar() {
    // Import calendar from CSV
    String filePath = "import_data.csv";

    boolean result = adapter.importCalendar(filePath,"UTC");

    assertTrue("Import calendar should succeed", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    String command = mockExecutor.getExecutedCommands().get(0);

    assertEquals("import cal \"import_data.csv\" --timezone \"UTC\"", command);
  }

  @Test
  public void testCreateRecurringAllDayEvent() {
    // Create a recurring all-day event
    LocalDate date = LocalDate.of(2025, 6, 2); // Monday
    LocalDateTime start = date.atStartOfDay();
    LocalDateTime end = date.atTime(23, 59, 59);

    ICalendarEventDTO event = new MockEventDTO("Weekly Holiday", start, end)
          .withRecurring(true)
          .withRecurrenceDays(Arrays.asList(DayOfWeek.MONDAY))
          .withRecurrenceCount(4);

    boolean result = adapter.createEvent(event);

    assertTrue("Create recurring all-day event should succeed", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    String command = mockExecutor.getExecutedCommands().get(0);

    assertTrue(command.contains("create event \"Weekly Holiday\""));
    assertTrue(command.contains("on 2025-06-02"));
    assertTrue(command.contains("repeats M for 4 times"));
  }

  @Test
  public void testAdapterHandlesExceptions() {
    // Test that the adapter handles exceptions gracefully
    mockExecutor.setShouldSucceed(false);

    boolean result = adapter.createCalendar("Test", "UTC");

    assertFalse("Adapter should return false when executor throws", result);
  }

  @Test
  public void testSpecialCharactersEscaping() {
    // Test that special characters in event names are properly escaped
    LocalDateTime start = LocalDateTime.of(2025, 5, 10, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 10, 11, 0);

    ICalendarEventDTO event = new MockEventDTO("Meeting with quoted name", start, end);

    boolean result = adapter.createEvent(event);

    assertTrue("Create event with special chars should succeed", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    String command = mockExecutor.getExecutedCommands().get(0);

    // Verify that quotes are properly escaped in the command
    assertTrue(command.contains("\"Meeting with quoted name\""));
  }


  @Test
  public void testErrorHandlingInUseCalendar() {
    // Create a command executor that will throw an exception
    ICommandExecutor errorExecutor = new ICommandExecutor() {
      @Override
      public void executeCommand(String command) {
        throw new RuntimeException("Simulated error");
      }

      @Override
      public ICalendarCommandAdapter getCommandAdapter() {
        return null;
      }
    };

    ObjectToCommandAdapter errorAdapter = new ObjectToCommandAdapter(errorExecutor);

    // Test that false is returned when executor throws an exception
    boolean result = errorAdapter.useCalendar("Calendar");
    assertFalse("Should return false when executor throws", result);
  }

  @Test
  public void testErrorHandlingInCreateEvent() {
    // Create a command executor that will throw an exception
    ICommandExecutor errorExecutor = new ICommandExecutor() {
      @Override
      public void executeCommand(String command) {
        throw new RuntimeException("Simulated error");
      }

      @Override
      public ICalendarCommandAdapter getCommandAdapter() {
        return null;
      }
    };

    ObjectToCommandAdapter errorAdapter = new ObjectToCommandAdapter(errorExecutor);

    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);
    ICalendarEventDTO event = new MockEventDTO("Meeting", start, end);

    // Test that false is returned when executor throws an exception
    boolean result = errorAdapter.createEvent(event);
    assertFalse("Should return false when executor throws", result);
  }

  @Test
  public void testErrorHandlingInEditEvent() {
    // Create a command executor that will throw an exception
    ICommandExecutor errorExecutor = new ICommandExecutor() {
      @Override
      public void executeCommand(String command) {
        throw new RuntimeException("Simulated error");
      }

      @Override
      public ICalendarCommandAdapter getCommandAdapter() {
        return null;
      }
    };

    ObjectToCommandAdapter errorAdapter = new ObjectToCommandAdapter(errorExecutor);

    String property = "name";
    String eventName = "Meeting";
    LocalDateTime fromDateTime = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime toDateTime = LocalDateTime.of(2025, 5, 1, 11, 0);
    String newValue = "Updated Meeting";

    // Test that false is returned when executor throws an exception
    boolean result = errorAdapter.editEvent(property, eventName, fromDateTime, toDateTime, newValue);
    assertFalse("Should return false when executor throws", result);
  }

  @Test
  public void testErrorHandlingInEditEvents() {
    // Create a command executor that will throw an exception
    ICommandExecutor errorExecutor = new ICommandExecutor() {
      @Override
      public void executeCommand(String command) {
        throw new RuntimeException("Simulated error");
      }

      @Override
      public ICalendarCommandAdapter getCommandAdapter() {
        return null;
      }
    };

    ObjectToCommandAdapter errorAdapter = new ObjectToCommandAdapter(errorExecutor);

    String property = "description";
    String eventName = "Meeting";
    LocalDateTime fromDateTime = LocalDateTime.of(2025, 5, 1, 10, 0);
    String newValue = "Updated description";

    // Test that false is returned when executor throws an exception
    boolean result = errorAdapter.editEvents(property, eventName, fromDateTime, newValue);
    assertFalse("Should return false when executor throws", result);
  }

  @Test
  public void testErrorHandlingInEditEventsNoStartDate() {
    // Create a command executor that will throw an exception
    ICommandExecutor errorExecutor = new ICommandExecutor() {
      @Override
      public void executeCommand(String command) {
        throw new RuntimeException("Simulated error");
      }

      @Override
      public ICalendarCommandAdapter getCommandAdapter() {
        return null;
      }
    };

    ObjectToCommandAdapter errorAdapter = new ObjectToCommandAdapter(errorExecutor);

    String property = "name";
    String eventName = "Meeting";
    String newValue = "Updated Meeting";

    // Test that false is returned when executor throws an exception
    boolean result = errorAdapter.editEventsNoStartDate(property, eventName, newValue);
    assertFalse("Should return false when executor throws", result);
  }

  @Test
  public void testErrorHandlingInExportCalendar() {
    // Create a command executor that will throw an exception
    ICommandExecutor errorExecutor = new ICommandExecutor() {
      @Override
      public void executeCommand(String command) {
        throw new RuntimeException("Simulated error");
      }

      @Override
      public ICalendarCommandAdapter getCommandAdapter() {
        return null;
      }
    };

    ObjectToCommandAdapter errorAdapter = new ObjectToCommandAdapter(errorExecutor);

    String filePath = "export.csv";

    // Test that false is returned when executor throws an exception
    boolean result = errorAdapter.exportCalendar(filePath);
    assertFalse("Should return false when executor throws", result);
  }

  @Test
  public void testErrorHandlingInImportCalendar() {
    // Create a command executor that will throw an exception
    ICommandExecutor errorExecutor = new ICommandExecutor() {
      @Override
      public void executeCommand(String command) {
        throw new RuntimeException("Simulated error");
      }

      @Override
      public ICalendarCommandAdapter getCommandAdapter() {
        return null;
      }
    };

    ObjectToCommandAdapter errorAdapter = new ObjectToCommandAdapter(errorExecutor);

    String filePath = "import.csv";

    // Test that false is returned when executor throws an exception
    boolean result = errorAdapter.importCalendar(filePath,"UTC");
    assertFalse("Should return false when executor throws", result);
  }

  @Test
  public void testCreateRecurringEventWithSaturdayAndSunday() {
    // Create a recurring event with Saturday and Sunday recurrence days
    LocalDateTime start = LocalDateTime.of(2025, 5, 3, 9, 0); // Saturday
    LocalDateTime end = LocalDateTime.of(2025, 5, 3, 10, 0);

    ICalendarEventDTO event = new MockEventDTO("Weekend Meeting", start, end)
          .withRecurring(true)
          .withRecurrenceDays(Arrays.asList(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
          .withRecurrenceCount(4);

    boolean result = adapter.createEvent(event);

    assertTrue("Create recurring event with Saturday and Sunday should succeed", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    String command = mockExecutor.getExecutedCommands().get(0);

    assertTrue("Command should contain Saturday and Sunday codes", command.contains("repeats SU for 4 times"));
  }

  @Test
  public void testIsAllDayEventForDifferentDays() {
    // Test a non-all-day event that crosses day boundaries
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 23, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 2, 1, 0);

    ICalendarEventDTO event = new MockEventDTO("Overnight Event", start, end);

    boolean result = adapter.createEvent(event);

    assertTrue("Create event spanning multiple days should succeed", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    String command = mockExecutor.getExecutedCommands().get(0);

    // Should use "from/to" format, not "on" format for all-day event
    assertTrue(command.contains("from 2025-05-01T23:00 to 2025-05-02T01:00"));
    assertFalse(command.contains(" on "));
  }

  @Test
  public void testAllDayEventWithWrongEndTime() {
    // Test event with midnight start but wrong end time for all-day
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 23, 0); // Not 23:59:59

    ICalendarEventDTO event = new MockEventDTO("Almost All Day", start, end);

    boolean result = adapter.createEvent(event);

    assertTrue("Create almost all-day event should succeed", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    String command = mockExecutor.getExecutedCommands().get(0);

    // Should use "from/to" format, not "on" format since not a true all-day event
    assertTrue(command.contains("from 2025-05-01T00:00 to 2025-05-01T23:00"));
    assertFalse(command.contains(" on "));
  }

  @Test
  public void testEditEventWithNullValue() {
    // Test editing an event with a null value (should be treated gracefully)
    String property = "description";
    String eventName = "Meeting";
    LocalDateTime fromDateTime = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime toDateTime = LocalDateTime.of(2025, 5, 1, 11, 0);
    String newValue = null;

    boolean result = adapter.editEvent(property, eventName, fromDateTime, toDateTime, newValue);

    assertTrue("Edit event with null value should not fail", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    String command = mockExecutor.getExecutedCommands().get(0);

    assertTrue(command.contains("with \"null\""));
  }

  @Test
  public void testEditEventsWithNullFromDateTime() {
    // Test editing events with a null from date/time
    String property = "location";
    String eventName = "Meeting";
    LocalDateTime fromDateTime = null;
    String newValue = "New Location";

    boolean result = adapter.editEvents(property, eventName, fromDateTime, newValue);

    assertTrue("Edit events with null fromDateTime should succeed", result);
    assertEquals(1, mockExecutor.getExecutedCommands().size());
    String command = mockExecutor.getExecutedCommands().get(0);

    // Should not contain "from" clause
    assertFalse(command.contains(" from "));
    assertEquals("edit events location \"Meeting\" with \"New Location\"", command);
  }
}