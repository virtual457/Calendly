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

import static org.junit.Assert.assertTrue;

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

    // Use an invalid filename to force an exception
    String badFile = "/dev/null/invalid.csv";
    ExportEventsCommand cmd = new ExportEventsCommand(Collections.singletonList(badFile), model,
        "Default");
    String result = cmd.execute();

    assertTrue(result.startsWith("Error exporting events:"));
  }
}
