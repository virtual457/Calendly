package controller.command;

import controller.command.PrintEventsCommand;
import model.ICalendarEventDTO;
import model.ICalendarModel;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link PrintEventsCommand} class.
 * This test class verifies correct parsing and execution of commands that
 * print events either on a specific date or between a range of date-times.
 */

public class PrintEventsCommandTest {

  private static class MockEvent implements ICalendarEventDTO {
    private final String name;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final String location;

    MockEvent(String name, String start, String end, String location) {
      this.name = name;
      this.start = LocalDateTime.parse(start);
      this.end = LocalDateTime.parse(end);
      this.location = location;
    }

    @Override
    public String getEventName() {
      return name;
    }

    @Override
    public LocalDateTime getStartDateTime() {
      return start;
    }

    @Override
    public LocalDateTime getEndDateTime() {
      return end;
    }

    @Override
    public Boolean isRecurring() {
      return null;
    }

    @Override
    public List<DayOfWeek> getRecurrenceDays() {
      return List.of();
    }

    @Override
    public Integer getRecurrenceCount() {
      return 0;
    }

    @Override
    public LocalDateTime getRecurrenceEndDate() {
      return null;
    }

    @Override
    public Boolean isAutoDecline() {
      return null;
    }

    @Override
    public String getEventDescription() {
      return "";
    }

    @Override
    public String getEventLocation() {
      return location;
    }

    @Override
    public Boolean isPrivate() {
      return null;
    }
  }

  private static class MockModel implements ICalendarModel {
    List<ICalendarEventDTO> eventsToReturn;

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
    public boolean deleteCalendar(String calName) {
      return false;
    }

    @Override
    public List<ICalendarEventDTO> getEventsInRange(String calendar, LocalDateTime from,
                                                    LocalDateTime to) {
      return eventsToReturn;
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
    public boolean editCalendar(String calendarName, String property, String newValue) {
      return false;
    }

    @Override
    public boolean addEvents(String calendarName, List<ICalendarEventDTO> events, String timezone) {
      return false;
    }

  }

  @Test
  public void testPrintEventsOnNoEvents() {
    MockModel model = new MockModel();
    model.eventsToReturn = Collections.emptyList();

    PrintEventsCommand cmd = new PrintEventsCommand(
        Arrays.asList("on", "2025-05-01"),
        model, "Default"
    );

    String result = cmd.execute();
    assertEquals("No events found.", result);
  }

  @Test
  public void testPrintEventsOnWithEvents() {
    MockModel model = new MockModel();
    model.eventsToReturn = Arrays.asList(
        new MockEvent("Meeting", "2025-05-01T10:00", "2025-05-01T11:00", "Room 101")
    );

    PrintEventsCommand cmd = new PrintEventsCommand(
        Arrays.asList("on", "2025-05-01"),
        model, "Default"
    );

    String result = cmd.execute();
    assertTrue(result.contains("Meeting"));
    assertTrue(result.contains("2025-05-01T10:00"));
    assertTrue(result.contains("Room 101"));
  }

  @Test
  public void testExecute_WithMultipleEvents_FormatsProperly() {
    // Setup mock model with multiple events
    MockModel model = new MockModel();
    model.eventsToReturn = Arrays.asList(
        new MockEvent("Morning Meeting", "2025-07-01T09:00", "2025-07-01T10:00", "Room 101"),
        new MockEvent("Afternoon Workshop", "2025-07-01T14:00", "2025-07-01T16:00", "Training Room"),
        new MockEvent("Evening Call", "2025-07-01T18:00", "2025-07-01T19:00", "")
    );

    PrintEventsCommand cmd = new PrintEventsCommand(
        Arrays.asList("on", "2025-07-01"),
        model, "WorkCal"
    );

    // Execute the command
    String result = cmd.execute();

    // Verify that all events are included in the output
    assertTrue(result.contains("Morning Meeting"));
    assertTrue(result.contains("Afternoon Workshop"));
    assertTrue(result.contains("Evening Call"));

    // Verify times are included
    assertTrue(result.contains("2025-07-01T09:00"));
    assertTrue(result.contains("2025-07-01T14:00"));
    assertTrue(result.contains("2025-07-01T18:00"));

    // Verify locations are included when present
    assertTrue(result.contains("Room 101"));
    assertTrue(result.contains("Training Room"));

    // Verify the order of events (should be chronological)
    int positionMorning = result.indexOf("Morning Meeting");
    int positionAfternoon = result.indexOf("Afternoon Workshop");
    int positionEvening = result.indexOf("Evening Call");

    assertTrue(positionMorning < positionAfternoon);
    assertTrue(positionAfternoon < positionEvening);
  }

  @Test
  public void testExecute_WithIllegalArgumentException_ReturnsFormattedError() {
    // Setup mock model that throws IllegalArgumentException
    MockModel model = new MockModel() {
      @Override
      public List<ICalendarEventDTO> getEventsInRange(String calendar, LocalDateTime from, LocalDateTime to) {
        throw new IllegalArgumentException("Invalid date range");
      }
    };

    PrintEventsCommand cmd = new PrintEventsCommand(
        Arrays.asList("on", "2025-07-15"),
        model, "WorkCal"
    );

    // Execute the command
    String result = cmd.execute();

    // Verify the error message
    assertEquals("Error: Invalid date range", result);
  }

  @Test
  public void testExecute_WithRuntimeException_ReturnsUnexpectedError() {
    // Setup mock model that throws a runtime exception
    MockModel model = new MockModel() {
      @Override
      public List<ICalendarEventDTO> getEventsInRange(String calendar, LocalDateTime from, LocalDateTime to) {
        throw new RuntimeException("Database connection failure");
      }
    };

    PrintEventsCommand cmd = new PrintEventsCommand(
        Arrays.asList("from", "2025-08-01T09:00", "to", "2025-08-01T17:00"),
        model, "WorkCal"
    );

    // Execute the command
    String result = cmd.execute();

    // Verify the error message
    assertEquals("An unexpected error occurred: Database connection failure", result);
  }

  @Test
  public void testExecute_FromToRange_CorrectDateTimesPassed() {
    // Setup mock model that verifies the correct date range is passed
    final LocalDateTime[] capturedFrom = new LocalDateTime[1];
    final LocalDateTime[] capturedTo = new LocalDateTime[1];

    MockModel model = new MockModel() {
      @Override
      public List<ICalendarEventDTO> getEventsInRange(String calendar, LocalDateTime from, LocalDateTime to) {
        capturedFrom[0] = from;
        capturedTo[0] = to;
        return Collections.emptyList();
      }
    };

    LocalDateTime expectedFrom = LocalDateTime.parse("2025-09-01T09:00");
    LocalDateTime expectedTo = LocalDateTime.parse("2025-09-01T17:00");

    PrintEventsCommand cmd = new PrintEventsCommand(
        Arrays.asList("from", expectedFrom.toString(), "to", expectedTo.toString()),
        model, "WorkCal"
    );

    // Execute the command
    cmd.execute();

    // Verify the correct date range was passed to the model
    assertEquals(expectedFrom, capturedFrom[0]);
    assertEquals(expectedTo, capturedTo[0]);
  }

  @Test
  public void testExecute_OnDate_CorrectDateRangePassed() {
    // Setup mock model that verifies the correct date range is passed
    final LocalDateTime[] capturedFrom = new LocalDateTime[1];
    final LocalDateTime[] capturedTo = new LocalDateTime[1];

    MockModel model = new MockModel() {
      @Override
      public List<ICalendarEventDTO> getEventsInRange(String calendar, LocalDateTime from, LocalDateTime to) {
        capturedFrom[0] = from;
        capturedTo[0] = to;
        return Collections.emptyList();
      }
    };

    PrintEventsCommand cmd = new PrintEventsCommand(
        Arrays.asList("on", "2025-10-15"),
        model, "WorkCal"
    );

    // Execute the command
    cmd.execute();

    // Verify the date range spans the entire day
    assertEquals(LocalDateTime.of(2025, 10, 15, 0, 0), capturedFrom[0]);
    assertEquals(LocalDateTime.of(2025, 10, 15, 23, 59, 59), capturedTo[0]);
  }

  @Test
  public void testExecute_WithNullLocation_HandlesCorrectly() {
    // Setup mock model with an event that has null location
    MockModel model = new MockModel();
    model.eventsToReturn = Arrays.asList(
        new MockEvent("Null Location Event", "2025-11-01T10:00", "2025-11-01T11:00", null)
    );

    PrintEventsCommand cmd = new PrintEventsCommand(
        Arrays.asList("on", "2025-11-01"),
        model, "WorkCal"
    );

    // Execute the command
    String result = cmd.execute();

    // Verify the event is included without location
    assertTrue(result.contains("Null Location Event"));
    assertTrue(result.contains("2025-11-01T10:00"));

    // Output shouldn't contain "at null" or similar
    assertFalse(result.contains("at null"));
  }

  @Test
  public void testExecute_WithMultipleEventsWithSameName_ListsAll() {
    // Setup mock model with multiple events with the same name
    MockModel model = new MockModel();
    model.eventsToReturn = Arrays.asList(
        new MockEvent("Team Meeting", "2025-12-01T09:00", "2025-12-01T10:00", "Room A"),
        new MockEvent("Team Meeting", "2025-12-01T14:00", "2025-12-01T15:00", "Room B")
    );

    PrintEventsCommand cmd = new PrintEventsCommand(
        Arrays.asList("on", "2025-12-01"),
        model, "WorkCal"
    );

    // Execute the command
    String result = cmd.execute();

    // Both events should be included
    assertTrue(result.contains("2025-12-01T09:00"));
    assertTrue(result.contains("2025-12-01T14:00"));
    assertTrue(result.contains("Room A"));
    assertTrue(result.contains("Room B"));

    // Count occurrences of the event name
    int count = 0;
    int index = 0;
    while ((index = result.indexOf("Team Meeting", index)) != -1) {
      count++;
      index += "Team Meeting".length();
    }

    assertEquals(2, count);
  }

  @Test
  public void testExecute_WithCorrectCalendarName_UsesCorrectCalendar() {
    // Setup mock model that captures the calendar name
    final String[] capturedCalendarName = new String[1];

    MockModel model = new MockModel() {
      @Override
      public List<ICalendarEventDTO> getEventsInRange(String calendar, LocalDateTime from, LocalDateTime to) {
        capturedCalendarName[0] = calendar;
        return Collections.emptyList();
      }
    };

    PrintEventsCommand cmd = new PrintEventsCommand(
        Arrays.asList("on", "2025-12-15"),
        model, "SpecificCalendar"
    );

    // Execute the command
    cmd.execute();

    // Verify the correct calendar name was used
    assertEquals("SpecificCalendar", capturedCalendarName[0]);
  }

  @Test
  public void testPrintEventsFromTo() {
    MockModel model = new MockModel();
    model.eventsToReturn = Arrays.asList(
        new MockEvent("Lecture", "2025-06-01T09:00", "2025-06-01T10:30", "")
    );

    PrintEventsCommand cmd = new PrintEventsCommand(
        Arrays.asList("from", "2025-06-01T08:00", "to", "2025-06-01T12:00"),
        model, "Uni"
    );

    String result = cmd.execute();
    assertTrue(result.contains("Lecture"));
    assertTrue(result.contains("2025-06-01T09:00"));
    assertFalse(result.contains("at"));  // no location
  }

  // ------------------- Error Handling -----------------------

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyArgs() {
    new PrintEventsCommand(Collections.emptyList(), new MockModel(), "Cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidKeyword() {
    new PrintEventsCommand(Arrays.asList("next", "2025-05-01"), new MockModel(), "Cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidOnFormatTooManyArgs() {
    new PrintEventsCommand(Arrays.asList("on", "2025-05-01", "extra"), new MockModel(), "Cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromToWrongKeyword() {
    new PrintEventsCommand(
        Arrays.asList("from", "2025-06-01T08:00", "until", "2025-06-01T12:00"),
        new MockModel(), "Cal"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromToWrongArgCount() {
    new PrintEventsCommand(
        Arrays.asList("from", "2025-06-01T08:00", "to"),
        new MockModel(), "Cal"
    );
  }
}
