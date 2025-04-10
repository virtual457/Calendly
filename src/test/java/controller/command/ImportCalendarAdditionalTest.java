package controller.command;

import model.ICalendarEventDTO;
import model.ICalendarModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ImportCalendarAdditionalTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private TestCalendarModel model;
  private String currentCalendar;

  @Before
  public void setUp() {
    model = new TestCalendarModel();
    currentCalendar = "Default";

    // Create a default calendar for testing
    model.createCalendar(currentCalendar, "UTC");
  }

  // Test for line 86 increment - Header line processing
  @Test
  public void testHeaderLineIncrement() throws IOException {
    // Create a file with an invalid header
    File csvFile = tempFolder.newFile("invalid-header.csv");

    try (FileWriter writer = new FileWriter(csvFile)) {
      writer.write("Wrong,Header,Format\n");
      writer.write("Event,01/01/2023,10:00 AM\n");
    }

    // Create command
    List<String> args = Arrays.asList(csvFile.getAbsolutePath(), "--timezone", "UTC");
    ImportCalendarCommand command = new ImportCalendarCommand(args, model, currentCalendar);

    // Execute
    String result = command.execute();

    // Verify the error message contains correct line number
    assertTrue("Error should indicate invalid header", result.contains("Invalid Header line"));
  }

  // Test for line 98 increment - Field count validation
  @Test
  public void testFieldCountValidationIncrement() throws IOException {
    // Create a file with incorrect field count in line 2
    File csvFile = tempFolder.newFile("wrong-field-count.csv");

    try (FileWriter writer = new FileWriter(csvFile)) {
      writer.write("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n");
      writer.write("Too,Few,Fields\n"); // Line 2 has wrong field count
      writer.write("Valid Event,01/01/2023,10:00 AM,01/01/2023,11:00 AM,False,Description,Location,False\n"); // Line 3 is valid
    }

    // Create command
    List<String> args = Arrays.asList(csvFile.getAbsolutePath(), "--timezone", "UTC");
    ImportCalendarCommand command = new ImportCalendarCommand(args, model, currentCalendar);

    // Execute
    String result = command.execute();

    // Verify the error message contains the correct line number
    assertTrue("Error should mention line 2", result.contains("Line 2:"));
  }

  // Test for line 116, 122, 128 increments - Required fields validation
  @Test
  public void testRequiredFieldsIncrements() throws IOException {
    // Create a file with missing required fields
    File csvFile = tempFolder.newFile("missing-required-fields.csv");

    try (FileWriter writer = new FileWriter(csvFile)) {
      writer.write("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n");
      // Line 2: Missing event name
      writer.write(",01/01/2023,10:00 AM,01/01/2023,11:00 AM,False,Description,Location,False\n");
      // Line 3: Missing start date
      writer.write("Event 2,,10:00 AM,01/01/2023,11:00 AM,False,Description,Location,False\n");
      // Line 4: Missing end date
      writer.write("Event 3,01/01/2023,10:00 AM,,11:00 AM,False,Description,Location,False\n");
    }

    // Create command
    List<String> args = Arrays.asList(csvFile.getAbsolutePath(), "--timezone", "UTC");
    ImportCalendarCommand command = new ImportCalendarCommand(args, model, currentCalendar);

    // Execute
    String result = command.execute();

    // Check for specific line numbers in the error message
    assertTrue("Should have Line 2 error for event name", result.contains("Line 2: Event name is mandatory"));
    assertTrue("Should have Line 3 error for start date", result.contains("Line 3: Start date is mandatory"));
    assertTrue("Should have Line 4 error for end date", result.contains("Line 4: End date is mandatory"));
  }

  // Test for line 137, 147 increments - Boolean field validation
  @Test
  public void testBooleanFieldsIncrements() throws IOException {
    // Create a file with invalid boolean fields
    File csvFile = tempFolder.newFile("invalid-boolean-fields.csv");

    try (FileWriter writer = new FileWriter(csvFile)) {
      writer.write("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n");
      // Line 2: Invalid all-day event value
      writer.write("Event 1,01/01/2023,10:00 AM,01/01/2023,11:00 AM,NotABoolean,Description,Location,False\n");
      // Line 3: Invalid private value
      writer.write("Event 2,01/01/2023,10:00 AM,01/01/2023,11:00 AM,False,Description,Location,NotABoolean\n");
    }

    // Create command
    List<String> args = Arrays.asList(csvFile.getAbsolutePath(), "--timezone", "UTC");
    ImportCalendarCommand command = new ImportCalendarCommand(args, model, currentCalendar);

    // Execute
    String result = command.execute();

    // Check for specific line numbers in the error message
    assertTrue("Should have Line 2 error for all-day event", result.contains("Line 2: All Day Event must be TRUE or FALSE"));
    assertTrue("Should have Line 3 error for private field", result.contains("Line 3: Private must be TRUE or FALSE"));
  }

  // Test for line 162 increment - All-day event validation
  @Test
  public void testAllDayEventIncrement() throws IOException {
    // Create a file with all-day event missing start date
    File csvFile = tempFolder.newFile("allday-missing-date.csv");

    try (FileWriter writer = new FileWriter(csvFile)) {
      writer.write("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n");
      // All-day event missing start date
      writer.write("Event 1,,10:00 AM,01/01/2023,11:00 AM,True,Description,Location,False\n");
    }

    // Create command
    List<String> args = Arrays.asList(csvFile.getAbsolutePath(), "--timezone", "UTC");
    ImportCalendarCommand command = new ImportCalendarCommand(args, model, currentCalendar);

    // Execute
    String result = command.execute();

    // Check for specific line number in the error message
    assertTrue("Should have Line 2 error for all-day event", result.contains("Line 2: Start date is mandatory"));
  }

  // Test for line 174, 180 increments - Non-all-day event time validation
  @Test
  public void testNonAllDayEventTimeIncrements() throws IOException {
    // Create a file with non-all-day events missing times
    File csvFile = tempFolder.newFile("missing-times.csv");

    try (FileWriter writer = new FileWriter(csvFile)) {
      writer.write("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n");
      // Line 2: Non-all-day event missing start time
      writer.write("Event 1,01/01/2023,,01/01/2023,11:00 AM,False,Description,Location,False\n");
      // Line 3: Non-all-day event missing end time
      writer.write("Event 2,01/01/2023,10:00 AM,01/01/2023,,False,Description,Location,False\n");
    }

    // Create command
    List<String> args = Arrays.asList(csvFile.getAbsolutePath(), "--timezone", "UTC");
    ImportCalendarCommand command = new ImportCalendarCommand(args, model, currentCalendar);

    // Execute
    String result = command.execute();

    // Check for specific line numbers in the error message
    assertTrue("Should have Line 2 error for start time", result.contains("Line 2: Start time is mandatory for non-all-day events"));
    assertTrue("Should have Line 3 error for end time", result.contains("Line 3: End time is mandatory for non-all-day events"));
  }

  // Test for line 196, 201 increments - DateTime validation
  @Test
  public void testDateTimeValidationIncrements() throws IOException {
    // Create a file with datetime validation issues
    File csvFile = tempFolder.newFile("datetime-validation.csv");

    try (FileWriter writer = new FileWriter(csvFile)) {
      writer.write("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n");
      // Line 2: End time before start time
      writer.write("Event 1,01/01/2023,11:00 AM,01/01/2023,10:00 AM,False,Description,Location,False\n");
      // Line 3: Invalid date format
      writer.write("Event 2,not-a-date,10:00 AM,01/01/2023,11:00 AM,False,Description,Location,False\n");
    }

    // Create command
    List<String> args = Arrays.asList(csvFile.getAbsolutePath(), "--timezone", "UTC");
    ImportCalendarCommand command = new ImportCalendarCommand(args, model, currentCalendar);

    // Execute
    String result = command.execute();

    // Check for specific line numbers in the error message
    assertTrue("Should have Line 2 error for end time before start time",
          result.contains("Line 2: End date/time must be after start date/time"));
    assertTrue("Should have Line 3 error for invalid date format",
          result.contains("Line 3: Invalid date/time format"));
  }

  // Test for line 221 increment - Line number for general exceptions
  @Test
  public void testGeneralExceptionLineIncrement() throws IOException {
    // Create a file with a line that will cause a general exception
    File csvFile = tempFolder.newFile("general-exception.csv");

    try (FileWriter writer = new FileWriter(csvFile)) {
      writer.write("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n");
      // This line has invalid format for times that will cause DateTimeParseException
      writer.write("Event 1,01/01/2023,invalid-time,01/01/2023,also-invalid,False,Description,Location,False\n");
    }

    // Create command
    List<String> args = Arrays.asList(csvFile.getAbsolutePath(), "--timezone", "UTC");
    ImportCalendarCommand command = new ImportCalendarCommand(args, model, currentCalendar);

    // Execute
    String result = command.execute();

    // The exception should be caught and added to validation errors
    assertTrue("Error should indicate line number", result.contains("Line 2:"));
  }

  // Simple test implementation of ICalendarModel
  private static class TestCalendarModel implements ICalendarModel {
    private List<String> calendarNames = new ArrayList<>();
    private List<ICalendarEventDTO> events = new ArrayList<>();

    @Override
    public boolean createCalendar(String calName, String timezone) {
      calendarNames.add(calName);
      return true;
    }

    @Override
    public boolean addEvent(String calendarName, ICalendarEventDTO event) {
      events.add(event);
      return true;
    }

    @Override
    public boolean editEvents(String calendarName, String property, String eventName,
                              LocalDateTime fromDateTime, String newValue, boolean editAll) {
      return true;
    }

    @Override
    public boolean editEvent(String calendarName, String property, String eventName,
                             LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue) {
      return true;
    }

    @Override
    public boolean deleteCalendar(String calName) {
      calendarNames.remove(calName);
      return true;
    }

    @Override
    public boolean copyEvents(String sourceCalendarName, LocalDateTime sourceStart,
                              LocalDateTime sourceEnd, String targetCalendarName, LocalDate targetStart) {
      return true;
    }

    @Override
    public boolean copyEvent(String sourceCalendarName, LocalDateTime sourceStart,
                             String eventName, String targetCalendarName, LocalDateTime targetStart) {
      return true;
    }

    @Override
    public boolean isCalendarPresent(String calName) {
      return calendarNames.contains(calName);
    }

    @Override
    public boolean isCalendarAvailable(String calName, LocalDate date) {
      return true;
    }

    @Override
    public List<String> getCalendarNames() {
      return calendarNames;
    }

    @Override
    public String getCalendarTimeZone(String calendarName) {
      return "UTC";
    }

    @Override
    public List<ICalendarEventDTO> getEventsInRange(String calendarName,
                                                    LocalDateTime fromDateTime,
                                                    LocalDateTime toDateTime) {
      return events;
    }

    @Override
    public List<ICalendarEventDTO> getEventsInSpecificDateTime(String calendarName,
                                                               LocalDateTime dateTime) {
      return events;
    }

    @Override
    public boolean editCalendar(String calendarName, String property, String newValue) {
      return true;
    }

    @Override
    public boolean addEvents(String calendarName, List<ICalendarEventDTO> events, String timezone) {
      this.events.addAll(events);
      return true;
    }
  }
}