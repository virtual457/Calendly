package controller.command;

import model.ICalendarEventDTO;
import model.ICalendarModel;

import org.junit.After;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for the {@link ExportEventsCommand} class.
 * This class verifies the correct export of calendar events into a CSV file format,
 * including edge cases like empty calendars or invalid file paths.
 */

public class ExportEventsCommandTest {

  private static final String TEST_FILE = "test_export.csv";

  private static class MockEvent implements ICalendarEventDTO {
    private final String name;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final String desc;
    private final String loc;
    private final boolean priv;

    public MockEvent(String name, String start, String end, String desc, String loc, boolean priv) {
      this.name = name;
      this.start = LocalDateTime.parse(start);
      this.end = LocalDateTime.parse(end);
      this.desc = desc;
      this.loc = loc;
      this.priv = priv;
    }

    public String getEventName() {
      return name;
    }

    public LocalDateTime getStartDateTime() {
      return start;
    }

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

    public String getEventDescription() {
      return desc;
    }

    public String getEventLocation() {
      return loc;
    }

    public Boolean isPrivate() {
      return priv;
    }
  }

  private static class MockModel implements ICalendarModel {
    List<ICalendarEventDTO> events;

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

    public List<ICalendarEventDTO> getEventsInRange(String cal, LocalDateTime start,
                                                    LocalDateTime end) {
      return events;
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
    public boolean addEvents(String calendarName, List<ICalendarEventDTO> events,
                             String timezone) {
      return false;
    }
  }

  @After
  public void tearDown() {
    File file = new File(TEST_FILE);
    if (file.exists()) {
      file.delete();
    }
  }

  @Test
  public void testSuccessfulExport() throws Exception {
    MockModel model = new MockModel();
    model.events = Arrays.asList(
        new MockEvent("Event A", "2025-05-01T10:00", "2025-05-01T11:00", "Description", "Room " +
            "101", true),
        new MockEvent("Event B", "2025-05-02T00:00", "2025-05-02T23:59:59", "", "", false) //

    );

    ExportEventsCommand cmd = new ExportEventsCommand(Collections.singletonList(TEST_FILE), model
        , "Work");
    String result = cmd.execute();

    assertTrue(result.contains("Events exported successfully"));
    assertTrue(new File(TEST_FILE).exists());

    try (BufferedReader reader = new BufferedReader(new FileReader(TEST_FILE))) {
      String header = reader.readLine();
      assertTrue(header.contains("Subject,Start Date"));
      String line1 = reader.readLine();
      assertTrue(line1.contains("Event A"));
      String line2 = reader.readLine();
      assertTrue(line2.contains("True")); // all-day event
    }
  }

  @Test
  public void testEmptyExport() {
    MockModel model = new MockModel();
    model.events = Collections.emptyList();

    ExportEventsCommand cmd = new ExportEventsCommand(Collections.singletonList(TEST_FILE), model
        , "Default");
    String result = cmd.execute();

    assertTrue(result.contains("exported"));
    assertTrue(new File(TEST_FILE).exists());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingFileName() {
    new ExportEventsCommand(Collections.emptyList(), new MockModel(), "Cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTooManyArgs() {
    new ExportEventsCommand(Arrays.asList("file.csv", "extra"), new MockModel(), "Cal");
  }

  @Test
  public void testFileWriteFailureHandled() {
    MockModel model = new MockModel();
    model.events = Collections.emptyList();

    String badFile = "/dev/null/invalid.csv";
    ExportEventsCommand cmd = new ExportEventsCommand(Collections.singletonList(badFile), model,
        "Default");
    String result = cmd.execute();

    assertTrue(result.startsWith("Error exporting events:"));
  }

  @Test
  public void testExecute_WithStandardEvents_ExportsSuccessfully() {
    MockModel model = new MockModel();
    // Create a variety of events with different properties
    model.events = Arrays.asList(
        new MockEvent("Meeting", "2025-06-01T09:00", "2025-06-01T10:00", "Team sync", "Room A", false),
        new MockEvent("Lunch", "2025-06-01T12:00", "2025-06-01T13:00", "Team lunch", "Cafeteria", false),
        new MockEvent("Private", "2025-06-01T15:00", "2025-06-01T16:00", "Personal", "Office", true)
    );

    ExportEventsCommand cmd = new ExportEventsCommand(
        Collections.singletonList(TEST_FILE),
        model,
        "WorkCal"
    );

    // Execute the command
    String result = cmd.execute();

    // Verify success message
    assertTrue(result.contains("Events exported successfully"));

    // Verify file exists
    File exportFile = new File(TEST_FILE);
    assertTrue(exportFile.exists());

    try (BufferedReader reader = new BufferedReader(new FileReader(exportFile))) {
      // Verify header
      String header = reader.readLine();
      assertTrue(header.contains("Subject,Start Date,Start Time,End Date,End Time"));

      // Verify all three events are present
      String line1 = reader.readLine();
      String line2 = reader.readLine();
      String line3 = reader.readLine();

      assertTrue(line1.contains("Meeting"));
      assertTrue(line2.contains("Lunch"));
      assertTrue(line3.contains("Private") && line3.contains("True")); // Private flag

      // Verify no more events
      assertNull(reader.readLine());
    } catch (Exception e) {
      fail("Exception reading export file: " + e.getMessage());
    }
  }

  @Test
  public void testExecute_WithIllegalArgumentException_ReturnsFormattedError() {
    // Create a mock model that throws IllegalArgumentException
    MockModel model = new MockModel() {
      @Override
      public List<ICalendarEventDTO> getEventsInRange(String cal, LocalDateTime start, LocalDateTime end) {
        throw new IllegalArgumentException("Calendar not found: " + cal);
      }
    };

    ExportEventsCommand cmd = new ExportEventsCommand(
        Collections.singletonList(TEST_FILE),
        model,
        "NonExistentCal"
    );

    // Execute the command
    String result = cmd.execute();

    // Verify error message format
    assertEquals("Error: Calendar not found: NonExistentCal", result);

    // Verify no file was created
    File exportFile = new File(TEST_FILE);
    assertFalse(exportFile.exists());
  }

  @Test
  public void testExecute_WithGenericException_ReturnsUnexpectedErrorMessage() {
    // Create a mock model that throws a generic exception
    MockModel model = new MockModel() {
      @Override
      public List<ICalendarEventDTO> getEventsInRange(String cal, LocalDateTime start, LocalDateTime end) {
        throw new RuntimeException("Database connection failed");
      }
    };

    ExportEventsCommand cmd = new ExportEventsCommand(
        Collections.singletonList(TEST_FILE),
        model,
        "WorkCal"
    );

    // Execute the command
    String result = cmd.execute();

    // Verify error message format
    assertEquals("An unexpected error occurred: Database connection failed", result);

    // Verify no file was created
    File exportFile = new File(TEST_FILE);
    assertFalse(exportFile.exists());
  }

  @Test
  public void testExecute_EventWithSpecialCharacters_ExportsCorrectly() {
    MockModel model = new MockModel();
    // Create events with special characters that might need escaping in CSV
    model.events = Arrays.asList(
        new MockEvent("Meeting with \"quotes\"", "2025-07-01T10:00", "2025-07-01T11:00",
            "Discussion about \"project\"", "Room \"A\"", false),
        new MockEvent("Event with, comma", "2025-07-01T12:00", "2025-07-01T13:00",
            "Description, with, commas", "Location, here", true)
    );

    ExportEventsCommand cmd = new ExportEventsCommand(
        Collections.singletonList(TEST_FILE),
        model,
        "WorkCal"
    );

    // Execute the command
    String result = cmd.execute();

    // Verify success
    assertTrue(result.contains("Events exported successfully"));

    try (BufferedReader reader = new BufferedReader(new FileReader(TEST_FILE))) {
      // Skip header
      reader.readLine();

      // Read events and verify special characters are properly escaped
      String line1 = reader.readLine();
      String line2 = reader.readLine();

      // Quotes should be doubled for proper CSV escaping
      assertTrue(line1.contains("\"Meeting with \"\"quotes\"\"\""));
      assertTrue(line1.contains("\"Discussion about \"\"project\"\"\""));

      // Commas should be contained within quotes
      assertTrue(line2.contains("\"Event with, comma\""));
      assertTrue(line2.contains("\"Description, with, commas\""));

    } catch (Exception e) {
      fail("Exception reading export file: " + e.getMessage());
    }
  }

  @Test
  public void testExecute_VerifyDateTimeFormatting() {
    MockModel model = new MockModel();
    // Create events with various times to test formatting
    model.events = Arrays.asList(
        new MockEvent("Morning Event", "2025-08-15T09:00", "2025-08-15T10:00", "", "", false),
        new MockEvent("Noon Event", "2025-08-15T12:00", "2025-08-15T13:00", "", "", false),
        new MockEvent("Evening Event", "2025-08-15T18:30", "2025-08-15T19:45", "", "", false),
        new MockEvent("Midnight Event", "2025-08-15T00:00", "2025-08-15T01:00", "", "", false)
    );

    ExportEventsCommand cmd = new ExportEventsCommand(
        Collections.singletonList(TEST_FILE),
        model,
        "WorkCal"
    );

    // Execute the command
    cmd.execute();

    try (BufferedReader reader = new BufferedReader(new FileReader(TEST_FILE))) {
      // Skip header
      reader.readLine();

      // Read events and verify date/time formatting
      String line1 = reader.readLine();
      String line2 = reader.readLine();
      String line3 = reader.readLine();
      String line4 = reader.readLine();

      // Format should be MM/DD/YYYY for dates and HH:MM AM/PM for times
      assertTrue(line1.contains("08/15/2025") && line1.contains("09:00 AM"));
      assertTrue(line2.contains("08/15/2025") && line2.contains("12:00 PM")); // Noon = PM
      assertTrue(line3.contains("08/15/2025") && line3.contains("06:30 PM")); // 18:30 = 6:30 PM
      assertTrue(line4.contains("08/15/2025") && line4.contains("12:00 AM")); // Midnight = 12:00 AM

    } catch (Exception e) {
      fail("Exception reading export file: " + e.getMessage());
    }
  }

  @Test
  public void testExecute_AllDayEvent_MarkedCorrectly() {
    MockModel model = new MockModel();
    // Create an all-day event (starts at 00:00 and ends at 23:59:59)
    model.events = Collections.singletonList(
        new MockEvent("All-day Conference", "2025-09-20T00:00", "2025-09-20T23:59:59",
            "Annual conference", "Convention Center", false)
    );

    ExportEventsCommand cmd = new ExportEventsCommand(
        Collections.singletonList(TEST_FILE),
        model,
        "WorkCal"
    );

    // Execute the command
    cmd.execute();

    try (BufferedReader reader = new BufferedReader(new FileReader(TEST_FILE))) {
      // Skip header
      reader.readLine();

      // Read the event line
      String line = reader.readLine();

      // The "All Day Event" column should be set to "True"
      assertTrue(line.contains("True"));

    } catch (Exception e) {
      fail("Exception reading export file: " + e.getMessage());
    }
  }

  @Test
  public void testExecute_VerifyCalendarNameAndDateRangePassedToModel() {
    // Setup a mock model that captures the parameters
    final String[] capturedCalendar = new String[1];
    final LocalDateTime[] capturedStart = new LocalDateTime[1];
    final LocalDateTime[] capturedEnd = new LocalDateTime[1];

    MockModel model = new MockModel() {
      @Override
      public List<ICalendarEventDTO> getEventsInRange(String cal, LocalDateTime start, LocalDateTime end) {
        capturedCalendar[0] = cal;
        capturedStart[0] = start;
        capturedEnd[0] = end;
        return Collections.emptyList();
      }
    };

    String testCalendarName = "TestCalendar";

    ExportEventsCommand cmd = new ExportEventsCommand(
        Collections.singletonList(TEST_FILE),
        model,
        testCalendarName
    );

    // Execute the command
    cmd.execute();

    // Verify correct calendar name was used
    assertEquals(testCalendarName, capturedCalendar[0]);

    // Verify that MIN and MAX date values were used (to get all events)
    assertEquals(LocalDateTime.MIN, capturedStart[0]);
    assertEquals(LocalDateTime.MAX, capturedEnd[0]);
  }
}
