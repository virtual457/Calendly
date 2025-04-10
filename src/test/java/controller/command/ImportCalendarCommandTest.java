package controller.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import model.ICalendarEventDTO;
import model.ICalendarModel;

public class ImportCalendarCommandTest {

  // Manual mock implementation of ICalendarModel
  private static class MockCalendarModel implements ICalendarModel {
    private boolean calendarPresent = true;
    private boolean addEventsSuccess = true;
    private String addEventsErrorMessage = null;
    private List<ICalendarEventDTO> addedEvents = new ArrayList<>();
    private String calendarNameUsed;
    private String timezoneUsed;

    @Override
    public boolean createCalendar(String calName, String timezone) {
      return false; // Not used in this test
    }

    @Override
    public boolean addEvent(String calendarName, ICalendarEventDTO event) {
      return false; // Not used in this test
    }

    @Override
    public boolean editEvents(String calendarName, String property, String eventName,
                              LocalDateTime fromDateTime, String newValue, boolean editAll) {
      return false; // Not used in this test
    }

    @Override
    public boolean editEvent(String calendarName, String property, String eventName,
                             LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue) {
      return false; // Not used in this test
    }

    @Override
    public boolean deleteCalendar(String calName) {
      return false; // Not used in this test
    }

    @Override
    public boolean copyEvents(String sourceCalendarName, LocalDateTime sourceStart,
                              LocalDateTime sourceEnd, String targetCalendarName, LocalDate targetStart) {
      return false; // Not used in this test
    }

    @Override
    public boolean copyEvent(String sourceCalendarName, LocalDateTime sourceStart,
                             String eventName, String targetCalendarName, LocalDateTime targetStart) {
      return false; // Not used in this test
    }

    @Override
    public boolean editCalendar(String calendarName, String property, String newValue) {
      return false; // Not used in this test
    }

    @Override
    public boolean addEvents(String calendarName, List<ICalendarEventDTO> events, String timezone) {
      this.calendarNameUsed = calendarName;
      this.timezoneUsed = timezone;
      this.addedEvents = new ArrayList<>(events);

      if (addEventsErrorMessage != null) {
        throw new IllegalStateException(addEventsErrorMessage);
      }

      return addEventsSuccess;
    }

    @Override
    public List<ICalendarEventDTO> getEventsInRange(String calendarName,
                                                    LocalDateTime fromDateTime, LocalDateTime toDateTime) {
      return null; // Not used in this test
    }

    @Override
    public List<ICalendarEventDTO> getEventsInSpecificDateTime(String calendarName,
                                                               LocalDateTime dateTime) {
      return null; // Not used in this test
    }

    @Override
    public boolean isCalendarPresent(String calName) {
      return calendarPresent;
    }

    @Override
    public boolean isCalendarAvailable(String calName, LocalDate date) {
      return false; // Not used in this test
    }

    @Override
    public List<String> getCalendarNames() {
      return null; // Not used in this test
    }

    @Override
    public String getCalendarTimeZone(String calendarName) {
      return "America/New_York"; // Default timezone for tests
    }

    // Methods to control the mock behavior
    public void setCalendarPresent(boolean present) {
      this.calendarPresent = present;
    }

    public void setAddEventsSuccess(boolean success) {
      this.addEventsSuccess = success;
    }

    public void setAddEventsErrorMessage(String message) {
      this.addEventsErrorMessage = message;
    }

    public List<ICalendarEventDTO> getAddedEvents() {
      return addedEvents;
    }

    public String getCalendarNameUsed() {
      return calendarNameUsed;
    }

    public String getTimezoneUsed() {
      return timezoneUsed;
    }
  }

  private MockCalendarModel mockModel;
  private final String calendarName = "TestCalendar";
  private final String validTimezone = "America/New_York";
  private final String tempDir = System.getProperty("java.io.tmpdir");
  private final String testCsvPath = tempDir + "/test_import.csv";

  @Before
  public void setUp() throws Exception {
    mockModel = new MockCalendarModel();
  }

  @After
  public void tearDown() throws Exception {
    // Clean up test files
    new File(testCsvPath).delete();
  }

  /**
   * Helper method to create a test CSV file with the given content
   */
  private void createTestCsvFile(String content) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(testCsvPath))) {
      writer.write(content);
    }
  }

  /**
   * Helper method to create command arguments
   */
  private List<String> createArgs(String filepath, String timezone) {
    List<String> args = new ArrayList<>();
    args.add(filepath);
    if (timezone != null) {
      args.add("--timezone");
      args.add(timezone);
    }
    return args;
  }

  @Test
  public void testValidImport() throws Exception {
    // Create a valid CSV file
    String csvContent =
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
                "\"Meeting\",05/01/2025,10:00 AM,05/01/2025,11:00 AM,FALSE,\"Project discussion\",\"Conference Room\",FALSE";
    createTestCsvFile(csvContent);

    // Create command with valid arguments
    List<String> args = createArgs(testCsvPath, validTimezone);
    ICommand command = new ImportCalendarCommand(args, mockModel, calendarName);

    // Execute command
    String result = command.execute();

    // Verify success
    assertTrue(result.contains("Successfully imported 1 events"));

    // Verify events were added to the model
    List<ICalendarEventDTO> addedEvents = mockModel.getAddedEvents();
    assertEquals(1, addedEvents.size());
    assertEquals("Meeting", addedEvents.get(0).getEventName());
    assertEquals("Project discussion", addedEvents.get(0).getEventDescription());
    assertEquals("Conference Room", addedEvents.get(0).getEventLocation());
    assertEquals(calendarName, mockModel.getCalendarNameUsed());
    assertEquals(validTimezone, mockModel.getTimezoneUsed());
  }

  @Test
  public void testMissingFilename() {
    // Create command with missing filename
    try {
      new ImportCalendarCommand(new ArrayList<>(), mockModel, calendarName);
      fail("Should have thrown exception for missing filename");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Missing filename for import"));
    }
  }

  @Test
  public void testMissingTimezone() {
    // Create command with missing timezone
    try {
      List<String> args = new ArrayList<>();
      args.add(testCsvPath);
      new ImportCalendarCommand(args, mockModel, calendarName);
      fail("Should have thrown exception for missing timezone");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Timezone must be specified for import"));
    }
  }

  @Test
  public void testInvalidTimezone() {
    // Create command with invalid timezone
    try {
      List<String> args = createArgs(testCsvPath, "InvalidTimezone");
      new ImportCalendarCommand(args, mockModel, calendarName);
      fail("Should have thrown exception for invalid timezone");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Invalid timezone"));
    }
  }

  @Test
  public void testInvalidHeaderLine() throws Exception {
    // Create CSV with invalid header
    String csvContent = "WrongHeader,Header2,Header3\n" +
          "\"Meeting\",05/01/2025,10:00 AM";
    createTestCsvFile(csvContent);

    // Create command with valid arguments
    List<String> args = createArgs(testCsvPath, validTimezone);
    ICommand command = new ImportCalendarCommand(args, mockModel, calendarName);

    // Execute command
    String result = command.execute();

    // Verify failure
    assertTrue(result.contains("Error importing calendar"));
    assertTrue(result.contains("Invalid Header line"));
  }

  @Test
  public void testMissingEventName() throws Exception {
    // Create CSV with missing event name
    String csvContent =
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
                "\"\",05/01/2025,10:00 AM,05/01/2025,11:00 AM,FALSE,\"Description\",\"Location\",FALSE";
    createTestCsvFile(csvContent);

    // Create and execute command
    List<String> args = createArgs(testCsvPath, validTimezone);
    ICommand command = new ImportCalendarCommand(args, mockModel, calendarName);
    String result = command.execute();

    // Verify error message
    assertTrue(result.contains("Error importing calendar"));
    assertTrue(result.contains("Event name is mandatory"));
  }

  @Test
  public void testMissingStartDate() throws Exception {
    // Create CSV with missing start date
    String csvContent =
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
                "\"Meeting\",,10:00 AM,05/01/2025,11:00 AM,FALSE,\"Description\",\"Location\",FALSE";
    createTestCsvFile(csvContent);

    // Create and execute command
    List<String> args = createArgs(testCsvPath, validTimezone);
    ICommand command = new ImportCalendarCommand(args, mockModel, calendarName);
    String result = command.execute();

    // Verify error message
    assertTrue(result.contains("Error importing calendar"));
    assertTrue(result.contains("Start date is mandatory"));
  }

  @Test
  public void testMissingEndDate() throws Exception {
    // Create CSV with missing end date
    String csvContent =
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
                "\"Meeting\",05/01/2025,10:00 AM,,11:00 AM,FALSE,\"Description\",\"Location\",FALSE";
    createTestCsvFile(csvContent);

    // Create and execute command
    List<String> args = createArgs(testCsvPath, validTimezone);
    ICommand command = new ImportCalendarCommand(args, mockModel, calendarName);
    String result = command.execute();

    // Verify error message
    assertTrue(result.contains("Error importing calendar"));
    assertTrue(result.contains("End date is mandatory"));
  }

  @Test
  public void testMissingStartTimeForNonAllDayEvent() throws Exception {
    // Create CSV with missing start time for non-all-day event
    String csvContent =
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
                "\"Meeting\",05/01/2025,,05/01/2025,11:00 AM,FALSE,\"Description\",\"Location\",FALSE";
    createTestCsvFile(csvContent);

    // Create and execute command
    List<String> args = createArgs(testCsvPath, validTimezone);
    ICommand command = new ImportCalendarCommand(args, mockModel, calendarName);
    String result = command.execute();

    // Verify error message
    assertTrue(result.contains("Error importing calendar"));
    assertTrue(result.contains("Start time is mandatory for non-all-day events"));
  }

  @Test
  public void testMissingEndTimeForNonAllDayEvent() throws Exception {
    // Create CSV with missing end time for non-all-day event
    String csvContent =
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
                "\"Meeting\",05/01/2025,10:00 AM,05/01/2025,,FALSE,\"Description\",\"Location\",FALSE";
    createTestCsvFile(csvContent);

    // Create and execute command
    List<String> args = createArgs(testCsvPath, validTimezone);
    ICommand command = new ImportCalendarCommand(args, mockModel, calendarName);
    String result = command.execute();

    // Verify error message
    assertTrue(result.contains("Error importing calendar"));
    assertTrue(result.contains("End time is mandatory for non-all-day events"));
  }

  @Test
  public void testValidAllDayEvent() throws Exception {
    // Create CSV with valid all-day event
    String csvContent =
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
                "\"All Day Meeting\",05/01/2025,,05/01/2025,,TRUE,\"Full day workshop\",\"Room 101\",FALSE";
    createTestCsvFile(csvContent);

    // Create and execute command
    List<String> args = createArgs(testCsvPath, validTimezone);
    ICommand command = new ImportCalendarCommand(args, mockModel, calendarName);
    String result = command.execute();

    // Verify success
    assertTrue(result.contains("Successfully imported 1 events"));

    // Verify correct times set for all day event
    List<ICalendarEventDTO> addedEvents = mockModel.getAddedEvents();
    assertEquals(1, addedEvents.size());

    ICalendarEventDTO event = addedEvents.get(0);
    assertEquals("All Day Meeting", event.getEventName());
    assertEquals(0, event.getStartDateTime().getHour());
    assertEquals(0, event.getStartDateTime().getMinute());
    assertEquals(23, event.getEndDateTime().getHour());
    assertEquals(59, event.getEndDateTime().getMinute());
  }

  @Test
  public void testInvalidAllDayEventValue() throws Exception {
    // Create CSV with invalid all-day event value
    String csvContent =
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
                "\"Meeting\",05/01/2025,10:00 AM,05/01/2025,11:00 AM,MAYBE,\"Description\",\"Location\",FALSE";
    createTestCsvFile(csvContent);

    // Create and execute command
    List<String> args = createArgs(testCsvPath, validTimezone);
    ICommand command = new ImportCalendarCommand(args, mockModel, calendarName);
    String result = command.execute();

    // Verify error message
    assertTrue(result.contains("Error importing calendar"));
    assertTrue(result.contains("All Day Event must be TRUE or FALSE"));
  }

  @Test
  public void testInvalidPrivateValue() throws Exception {
    // Create CSV with invalid private value
    String csvContent =
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
                "\"Meeting\",05/01/2025,10:00 AM,05/01/2025,11:00 AM,FALSE,\"Description\",\"Location\",MAYBE";
    createTestCsvFile(csvContent);

    // Create and execute command
    List<String> args = createArgs(testCsvPath, validTimezone);
    ICommand command = new ImportCalendarCommand(args, mockModel, calendarName);
    String result = command.execute();

    // Verify error message
    assertTrue(result.contains("Error importing calendar"));
    assertTrue(result.contains("Private must be TRUE or FALSE"));
  }

  @Test
  public void testEndTimeBeforeStartTime() throws Exception {
    // Create CSV with end time before start time
    String csvContent =
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
                "\"Meeting\",05/01/2025,10:00 AM,05/01/2025,09:00 AM,FALSE,\"Description\",\"Location\",FALSE";
    createTestCsvFile(csvContent);

    // Create and execute command
    List<String> args = createArgs(testCsvPath, validTimezone);
    ICommand command = new ImportCalendarCommand(args, mockModel, calendarName);
    String result = command.execute();

    // Verify error message
    assertTrue(result.contains("Error importing calendar"));
    assertTrue(result.contains("End date/time must be after start date/time"));
  }

  @Test
  public void testInvalidDateTimeFormat() throws Exception {
    // Create CSV with invalid date format
    String csvContent =
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
                "\"Meeting\",not-a-date,10:00 AM,05/01/2025,11:00 AM,FALSE,\"Description\",\"Location\",FALSE";
    createTestCsvFile(csvContent);

    // Create and execute command
    List<String> args = createArgs(testCsvPath, validTimezone);
    ICommand command = new ImportCalendarCommand(args, mockModel, calendarName);
    String result = command.execute();

    // Verify error message
    assertTrue(result.contains("Error importing calendar"));
    assertTrue(result.contains("Invalid date/time format"));
  }

  @Test
  public void testMultipleEvents() throws Exception {
    // Create CSV with multiple valid events
    String csvContent =
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
                "\"Meeting 1\",05/01/2025,10:00 AM,05/01/2025,11:00 AM,FALSE,\"Description 1\",\"Location 1\",FALSE\n" +
                "\"Meeting 2\",05/02/2025,02:00 PM,05/02/2025,03:00 PM,FALSE,\"Description 2\",\"Location 2\",TRUE";
    createTestCsvFile(csvContent);

    // Create and execute command
    List<String> args = createArgs(testCsvPath, validTimezone);
    ICommand command = new ImportCalendarCommand(args, mockModel, calendarName);
    String result = command.execute();

    // Verify success
    assertTrue(result.contains("Successfully imported 2 events"));

    // Verify events were added to the model
    List<ICalendarEventDTO> addedEvents = mockModel.getAddedEvents();
    assertEquals(2, addedEvents.size());
    assertEquals("Meeting 1", addedEvents.get(0).getEventName());
    assertEquals("Meeting 2", addedEvents.get(1).getEventName());
    assertFalse(addedEvents.get(0).isPrivate());
    assertTrue(addedEvents.get(1).isPrivate());
  }

  @Test
  public void testMultipleValidationErrors() throws Exception {
    // Create CSV with multiple validation errors
    String csvContent =
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
                "\"\",05/01/2025,10:00 AM,05/01/2025,11:00 AM,INVALID,\"Description\",\"Location\",FALSE\n" +
                "\"dasdddd\",05/01/2025,10:00 AM,05/01/2025,11:00 AM,INVALID," +
                "\"Description\",\"Location\",FALSE\n" +
                "\"dasd\",05/01/2025,10:00 AM,05/01/2025,11:00 AM,True," +
                "\"Description\"," +
                "\"Location\",balst\n" +
                "\"Meeting\",,10:00 AM,05/02/2025,09:00 AM,FALSE,\"Description\",\"Location\",INVALID";
    createTestCsvFile(csvContent);

    // Create and execute command
    List<String> args = createArgs(testCsvPath, validTimezone);
    ICommand command = new ImportCalendarCommand(args, mockModel, calendarName);
    String result = command.execute();

    // Verify error messages for both lines are included
    assertTrue(result.contains("Error importing calendar"));
    assertTrue(result.contains("Event name is mandatory"));
    assertTrue(result.contains("All Day Event must be TRUE or FALSE"));
    assertTrue(result.contains("Start date is mandatory"));
    assertTrue(result.contains("Private must be TRUE or FALSE"));
  }

  @Test
  public void testErrorWhenAddingEvents() throws Exception {
    // Create a valid CSV file
    String csvContent =
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
                "\"Meeting\",05/01/2025,10:00 AM,05/01/2025,11:00 AM,FALSE,\"Project discussion\",\"Conference Room\",FALSE";
    createTestCsvFile(csvContent);

    // Configure mock to throw exception when adding events
    mockModel.setAddEventsErrorMessage("Events conflict");

    // Create command with valid arguments
    List<String> args = createArgs(testCsvPath, validTimezone);
    ICommand command = new ImportCalendarCommand(args, mockModel, calendarName);

    // Execute command
    String result = command.execute();

    // Verify error message
    assertTrue(result.contains("Error importing calendar"));
    assertTrue(result.contains("Events conflict"));
  }

  @Test
  public void testNoEventsFound() throws Exception {
    // Create CSV with only header
    String csvContent =
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n";
    createTestCsvFile(csvContent);

    // Create and execute command
    List<String> args = createArgs(testCsvPath, validTimezone);
    ICommand command = new ImportCalendarCommand(args, mockModel, calendarName);
    String result = command.execute();

    // Verify message
    assertTrue(result.contains("No events found to import"));
  }
}