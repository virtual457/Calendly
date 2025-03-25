package calendarapp;

import static org.junit.Assert.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;


@RunWith(Parameterized.class)
/**
 * A JUnit test suite for verifying the behavior of the Calendar application's entry point.
 * <p>
 * This class tests various scenarios for the {@code CalendarApp} class, such as
 * handling command line arguments to run in interactive or headless mode, and
 * ensuring that the application can be started and shut down properly under
 * different conditions.
 * </p>
 */
public class CalendarAppTest {

  private static final String OUTPUT_FILE = "events.csv";
  private final PrintStream originalOut = System.out;
  private ByteArrayOutputStream outContent;
  private final String mode;
  private final InputStream originalIn = System.in;

  public CalendarAppTest(String mode) {
    this.mode = mode;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> modes() {
    return Arrays.asList(new Object[][]{{"interactive"}, {"headless"}});
  }

  @Before
  public void setUp() throws IOException {
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
    Files.deleteIfExists(Paths.get(OUTPUT_FILE));
  }

  @After
  public void tearDown() throws IOException {
    System.setOut(originalOut);
    System.setIn(originalIn);
    Files.deleteIfExists(Paths.get(OUTPUT_FILE));
  }

  @Test
  public void testMain_InvalidArguments_ShouldPrintUsageMessage() throws IOException {
    String[] args = {};
    CalendarApp.main(args);
    String expectedMessage = "Usage: java CalendarApp --mode <interactive|headless> [filePath]";
    assertTrue(outContent.toString().trim().contains(expectedMessage));
  }

  @Test
  public void testMain_ValidArguments_ShouldRunInteractiveMode() throws IOException {
    String[] args = {"--mode", "interactive"};
    Thread appThread = new Thread(() -> {
      try {
        CalendarApp.main(args);
      } catch (IOException e) {
        fail("IOException occurred: " + e.getMessage());
      }
    });
    appThread.start();
    try {
      Thread.sleep(2000);
    } catch (InterruptedException ignored) {
    }
    appThread.interrupt();
    assertTrue(outContent.toString().trim().contains("Welcome to the Calendar App!"));
  }

  @Test
  public void testMain_ValidArguments_HeadlessMode_ShouldRunWithFile() throws IOException {
    File tempFile = File.createTempFile("commands", ".txt");
    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("exit\n");
    }
    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarApp.main(args);
    assertTrue(outContent.toString().trim().contains("Welcome to the Calendar App!"));
    tempFile.delete();
  }



  //Integrration tests

  private void runAppWithCommands(String[] commands) {
    try {
      if (mode.equals("headless")) {
        File tempFile = File.createTempFile("commands", ".txt");
        try (FileWriter writer = new FileWriter(tempFile)) {
          for (String command : commands) {
            writer.write(command + "\n");
          }
        }
        String[] args = {"--mode", mode, tempFile.getAbsolutePath()};
        CalendarApp.main(args);
        tempFile.delete();
      } else {
        ByteArrayInputStream inContent = new ByteArrayInputStream(String.join("\n",
                commands).getBytes());
        System.setIn(inContent);
        String[] args = {"--mode", mode};
        CalendarApp.main(args);
      }
    }
    catch (IOException e) {
      fail("IOException occurred: " + e.getMessage());
    }
  }

  private String readExportedFile() throws IOException {
    return new String(Files.readAllBytes(Paths.get(OUTPUT_FILE)))
            .trim()
            .replace("\r\n", "\n")
            .replace("\r", "\n");
  }
  //Create calendarr command tests
  @Test
  public void testCreateCalendar_ValidInput_ShouldSucceed() {
    runAppWithCommands(new String[]{"create calendar --name TestCal --timezone America/New_York", "exit"});
    assertTrue(outContent.toString().contains("Calendar created successfully"));
  }

  @Test
  public void testCreateCalendar_DuplicateName_ShouldFail() {
    runAppWithCommands(new String[]{
            "create calendar --name DuplicateCal --timezone America/New_York",
            "create calendar --name DuplicateCal --timezone America/New_York",
            "exit"});
    assertTrue(outContent.toString().contains("already exists"));
  }

  @Test
  public void testCreateCalendar_InvalidTimezone_ShouldFail() {
    runAppWithCommands(new String[]{"create calendar --name InvalidTZ --timezone Invalid/Zone", "exit"});
    assertTrue(outContent.toString().contains("Invalid timezone"));
  }

  @Test
  public void testUseCalendar_ValidName_ShouldSucceed2() {
    runAppWithCommands(new String[]{"create calendar --name MyCal --timezone America/New_York", "use calendar --name MyCal", "exit"});
    assertTrue(outContent.toString().contains("Using calendar: MyCal"));
  }

  @Test
  public void testUseCalendar_InvalidName_ShouldFail() {
    runAppWithCommands(new String[]{"use calendar --name NonExistent", "exit"});
    assertTrue(outContent.toString().toLowerCase().contains("not found"));
  }

  //
  @Test
  public void testCreateCalendar_MissingName_ShouldFail() {
    String[] commands = {
            "create calendar --timezone America/New_York",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Error Executing command: Usage: create calendar --name <name> --timezone <timezone>"));
  }

  @Test
  public void testCreateCalendar_MissingTimezone_ShouldFail() {
    String[] commands = {
            "create calendar --name MyCal",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Error Executing command: Usage: create calendar --name <name> --timezone <timezone>"));
  }


  @Test
  public void testUseCalendar_BeforeCreate_ShouldFail() {
    String[] commands = {
            "use calendar --name NotCreated",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Error: calendar not found"));
  }

  @Test
  public void testCreateCalendar_CaseInsensitiveTimezone_ShouldFail() {
    String[] commands = {
            "create calendar --name TZCal --timezone america/new_york",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Error Executing command: Invalid timezone: america/new_york"));
  }

  @Test
  public void testUseCalendar_ValidName_ShouldSucceed() {
    String[] commands = {
            "create calendar --name MyCal --timezone America/New_York",
            "use calendar --name MyCal",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Using calendar: MyCal"));
  }

  @Test
  public void testCreateCalendar_ExtraArgs_ShouldFail() {
    String[] commands = {
            "create calendar --name ExtraCal --timezone America/New_York extra",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Unrecognized extra arguments"));
  }

  @Test
  public void testUseCalendar_ExtraArgs_ShouldFail() {
    String[] commands = {
            "create calendar --name MyCal --timezone America/New_York",
            "use calendar --name MyCal extra",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Unrecognized extra arguments"));
  }

  @Test
  public void testCreateCalendar_QuotedName_ShouldSucceed() {
    String[] commands = {
            "create calendar --name \"Work Calendar\" --timezone America/New_York",
            "use calendar --name \"Work Calendar\"",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Using calendar: Work Calendar"));
  }

  @Test
  public void testCreateCalendar_MixedCaseFlag_ShouldFail() {
    String[] commands = {
            "create calendar --Name WrongFlag --timezone America/New_York",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Expected --name flag."));
  }

  @Test
  public void testCreateCalendar_MissingNameFlag_ShouldFail() {
    String[] commands = {
            "create calendar TestCal --timezone America/New_York",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Error Executing command: Usage: create calendar --name <name> --timezone <timezone>"));
  }

  @Test
  public void testUseCalendar_MissingNameFlag_ShouldFail() {
    String[] commands = {
            "use calendar TestCal",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Expected usage: use calendar --name <calendarName>"));
  }

  @Test
  public void testCreateCalendar_QuotedTimezone_ShouldSucceed() {
    String[] commands = {
            "create calendar --name TimeCal --timezone \"America/New_York\"",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Calendar created successfully"));
  }

  @Test
  public void testCreateCalendar_IncompleteTimezone_ShouldFail() {
    String[] commands = {
            "create calendar --name IncompleteTZ --timezone America/",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("invalid timezone"));
  }

  @Test
  public void testUseCalendar_CaseSensitiveName_ShouldFail() {
    String[] commands = {
            "create calendar --name MyCalendar --timezone America/New_York",
            "use calendar --name mycalendar",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Error: calendar not found"));
  }

  @Test
  public void testCreateAndUseMultipleCalendars_ShouldSucceed() {
    String[] commands = {
            "create calendar --name Cal1 --timezone America/New_York",
            "create calendar --name Cal2 --timezone Asia/Kolkata",
            "use calendar --name Cal2",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Using calendar: Cal2"));
  }

  @Test
  public void testCreateCalendar_WithSpecialCharacters_ShouldSucceed() {
    String[] commands = {
            "create calendar --name \"Team@2025!\" --timezone America/New_York",
            "use calendar --name \"Team@2025!\"",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Using calendar: Team@2025!"));
  }

  //Test Create and export methods

  @Test
  public void testCreateSingleEvent_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name TestCal --timezone America/New_York",
            "use calendar --name TestCal",
            "create event Meeting from 2025-04-20T10:00 to 2025-04-20T11:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Meeting\",04/20/2025,10:00 AM,04/20/2025,11:00 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }
  @Test
  public void testCreateSingleAllDayEvent_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name TestCal --timezone America/New_York",
            "use calendar --name TestCal",
            "create event Holiday on 2025-04-22",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Holiday\",04/22/2025,,04/22/2025,,True,\"\",\"\",False"
    );

            assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCreateRecurringAllDayEventWithUntil_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name AllDayUntilCal --timezone America/New_York",
            "use calendar --name AllDayUntilCal",
            "create event Seminar on 2025-04-24 repeats FR until 2025-05-03",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);

    String expectedStart = "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private";
    String[] dates = {"04/24/2025", "04/25/2025", "05/01/2025", "05/02/2025"};
    StringBuilder builder = new StringBuilder(expectedStart);
    for (String date : dates) {
      builder.append("\n\"Seminar\"," + date + ",," + date + ",,True,\"\",\"\",False");
    }

    assertEquals(builder.toString(), readExportedFile());
  }

  @Test
  public void testCreateRecurringTimedEventWithUntil_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name UntilCal --timezone America/New_York",
            "use calendar --name UntilCal",
            "create event Sync from 2025-04-22T09:00 to 2025-04-22T10:00 repeats TR until 2025-05-01",
            "export cal " + OUTPUT_FILE,
            "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Sync\",04/22/2025,09:00 AM,04/22/2025,10:00 AM,False,\"\",\"\",False",
            "\"Sync\",04/24/2025,09:00 AM,04/24/2025,10:00 AM,False,\"\",\"\",False",
            "\"Sync\",04/29/2025,09:00 AM,04/29/2025,10:00 AM,False,\"\",\"\",False",
            "\"Sync\",05/01/2025,09:00 AM,05/01/2025,10:00 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCreateSingleAllDayEventWithAutoDecline_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name AutoDeclineCal --timezone America/New_York",
            "use calendar --name AutoDeclineCal",
            "create event --autoDecline Conference on 2025-05-05",
            "export cal " + OUTPUT_FILE,
            "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Conference\",05/05/2025,,05/05/2025,,True,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCreateRecurringAllDayEventWithCount_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name AllDayCountCal --timezone America/New_York",
            "use calendar --name AllDayCountCal",
            "create event Holiday on 2025-04-26 repeats SU for 2 times",
            "export cal " + OUTPUT_FILE,
            "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Holiday\",04/26/2025,,04/26/2025,,True,\"\",\"\",False",
            "\"Holiday\",04/27/2025,,04/27/2025,,True,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCreateAndExportRecurringInstancesFor2Times() throws IOException {
    String[] commands = {
            "create calendar --name MyCal --timezone America/New_York",
            "use calendar --name MyCal",
            "create event Standup from 2025-03-21T09:00 to 2025-03-21T09:30 repeats MTWRF for 2 times",
            "export cal " + OUTPUT_FILE,
            "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Standup\",03/21/2025,09:00 AM,03/21/2025,09:30 AM,False,\"\",\"\",False",
            "\"Standup\",03/24/2025,09:00 AM,03/24/2025,09:30 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testExportRecurringEventWithFiveOccurrences() throws IOException {
    String[] commands = {
            "create calendar --name MyCal --timezone America/New_York",
            "use calendar --name MyCal",
            "create event Standup from 2025-03-21T09:00 to 2025-03-21T09:30 repeats MTWRF for 5 " +
                    "times",
            "export cal " + OUTPUT_FILE,
            "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Standup\",03/21/2025,09:00 AM,03/21/2025,09:30 AM,False,\"\",\"\",False",
            "\"Standup\",03/24/2025,09:00 AM,03/24/2025,09:30 AM,False,\"\",\"\",False",
            "\"Standup\",03/25/2025,09:00 AM,03/25/2025,09:30 AM,False,\"\",\"\",False",
            "\"Standup\",03/26/2025,09:00 AM,03/26/2025,09:30 AM,False,\"\",\"\",False",
            "\"Standup\",03/27/2025,09:00 AM,03/27/2025,09:30 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCreateRecurringTimedEventWithUntil_ShouldExportCorrectly2() throws IOException {
    String[] commands = {
            "create calendar --name MyCal --timezone America/New_York",
            "use calendar --name MyCal",
            "create event Standup from 2025-03-21T09:00 to 2025-03-21T09:30 repeats MTWRF for 10 times",
            "export cal " + OUTPUT_FILE,
            "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Standup\",03/21/2025,09:00 AM,03/21/2025,09:30 AM,False,\"\",\"\",False",
            "\"Standup\",03/24/2025,09:00 AM,03/24/2025,09:30 AM,False,\"\",\"\",False",
            "\"Standup\",03/25/2025,09:00 AM,03/25/2025,09:30 AM,False,\"\",\"\",False",
            "\"Standup\",03/26/2025,09:00 AM,03/26/2025,09:30 AM,False,\"\",\"\",False",
            "\"Standup\",03/27/2025,09:00 AM,03/27/2025,09:30 AM,False,\"\",\"\",False",
            "\"Standup\",03/28/2025,09:00 AM,03/28/2025,09:30 AM,False,\"\",\"\",False",
            "\"Standup\",03/31/2025,09:00 AM,03/31/2025,09:30 AM,False,\"\",\"\",False",
            "\"Standup\",04/01/2025,09:00 AM,04/01/2025,09:30 AM,False,\"\",\"\",False",
            "\"Standup\",04/02/2025,09:00 AM,04/02/2025,09:30 AM,False,\"\",\"\",False",
            "\"Standup\",04/03/2025,09:00 AM,04/03/2025,09:30 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  //tests for edit functionality

  @Test
  public void testEditSpecificEventPropertyAndExport() throws IOException {
    String[] commands = {
            "create calendar --name TestCal --timezone America/New_York",
            "use calendar --name TestCal",
            "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
            "edit event name Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 with TeamSync",
            "export cal " + OUTPUT_FILE
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"TeamSync\",05/01/2025,10:00 AM,05/01/2025,11:00 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCreateTimedEventWithAutoDecline_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name AutoDeclineTimed --timezone America/New_York",
            "use calendar --name AutoDeclineTimed",
            "create event --autoDecline Review from 2025-05-10T13:00 to 2025-05-10T14:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Review\",05/10/2025,01:00 PM,05/10/2025,02:00 PM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCreateRecurringTimedEventWithAutoDecline_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name AutoRecurring --timezone America/New_York",
            "use calendar --name AutoRecurring",
            "create event --autoDecline CheckIn from 2025-06-01T09:00 to 2025-06-01T09:30 repeats MT for 2 times",
            "export cal " + OUTPUT_FILE,
            "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"CheckIn\",06/03/2025,09:00 AM,06/03/2025,09:30 AM,False,\"\",\"\",False",
            "\"CheckIn\",06/04/2025,09:00 AM,06/04/2025,09:30 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditSingleEvent_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name EditCal --timezone America/New_York",
            "use calendar --name EditCal",
            "create event Meeting from 2025-04-22T09:00 to 2025-04-22T10:00",
            "edit events location Meeting from 2025-04-22T09:00 to 2025-04-22T10:00 with \"Room B\"",
            "export cal " + OUTPUT_FILE,
            "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Meeting\",04/22/2025,09:00 AM,04/22/2025,10:00 AM,False,\"\",\"Room B\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditRecurringEvent_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name EditRecurrCal --timezone America/New_York",
            "use calendar --name EditRecurrCal",
            "create event Standup from 2025-04-24T09:00 to 2025-04-24T10:00 repeats T for 3 times",
            "edit events location Standup from 2025-04-24T09:00 to 2025-04-24T10:00 with \"Room C\"",
            "export cal " + OUTPUT_FILE,
            "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            // Assuming the recurring event generates occurrences on 04/24/2025, 04/30/2025, and 05/07/2025.
            "\"Standup\",04/24/2025,09:00 AM,04/24/2025,10:00 AM,False,\"\",\"Room C\",False",
            "\"Standup\",04/30/2025,09:00 AM,04/30/2025,10:00 AM,False,\"\",\"Room C\",False",
            "\"Standup\",05/07/2025,09:00 AM,05/07/2025,10:00 AM,False,\"\",\"Room C\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditEvent_NullNewValue_ShouldFail() {
    // This test uses commands to attempt editing an event with a null new value.
    String[] commands = {
            "create calendar --name EditNullCal --timezone America/New_York",
            "use calendar --name EditNullCal",
            "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
            "edit events location Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 with null",
            "exit"
    };
    runAppWithCommands(commands);
    // Expect output to contain an error message about missing new value.
    assertTrue(outContent.toString().toLowerCase().contains("error executing command"));
  }

  @Test
  public void testEditEvent_UnsupportedProperty_ShouldFail() {
    String[] commands = {
            "create calendar --name EditCal --timezone America/New_York",
            "use calendar --name EditCal",
            "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
            "edit events color Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 with Red",
            "exit"
    };
    runAppWithCommands(commands);
    // Expect output to mention "Unsupported property"
    assertTrue(outContent.toString().toLowerCase().contains("unsupported property"));
  }

  @Test
  public void testEditEvent_NoMatchingEvent_ShouldFail() {
    String[] commands = {
            "create calendar --name EditCal --timezone America/New_York",
            "use calendar --name EditCal",
            // No event is created.
            "edit events location NonExistent from 2025-05-01T10:00 to 2025-05-01T11:00 with \"Room X\"",
            "exit"
    };
    runAppWithCommands(commands);
    // Expect output to mention "No matching event found"
    assertTrue(outContent.toString().toLowerCase().contains("no matching event found"));
  }

  @Test
  public void testEditEvent_InvalidStartTime_ShouldFail() {
    String[] commands = {
            "create calendar --name EditInvalidStart --timezone America/New_York",
            "use calendar --name EditInvalidStart",
            "create event Meeting from 2025-06-10T10:00 to 2025-06-10T11:00",
            // Attempt to edit start time to 11:00 which is equal to the current end time.
            "edit events start Meeting from 2025-06-10T10:00 to 2025-06-10T11:00 with \"2025-06-10T11:00:00\"",
            "exit"
    };
    runAppWithCommands(commands);
    // Expect output to mention an error about the new start time.
    assertTrue(outContent.toString().toLowerCase().contains("new start must be before current end time"));
  }

  // Test: Editing an event's end time to an invalid time (new end is not after current start)
  @Test
  public void testEditEvent_InvalidEndTime_ShouldFail() {
    String[] commands = {
            "create calendar --name EditInvalidEnd --timezone America/New_York",
            "use calendar --name EditInvalidEnd",
            "create event Meeting from 2025-06-10T10:00 to 2025-06-10T11:00",
            // Attempt to edit end time to 10:00 which is equal to the current start time.
            "edit events end Meeting from 2025-06-10T10:00 to 2025-06-10T11:00 with \"2025-06-10T10:00:00\"",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("new end must be after current start time"));
  }

  // Test: Editing an event's name works correctly.
  @Test
  public void testEditEvent_UpdateName_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name EditNameCal --timezone America/New_York",
            "use calendar --name EditNameCal",
            "create event Meeting from 2025-06-10T10:00 to 2025-06-10T11:00",
            "edit events name Meeting from 2025-06-10T10:00 to 2025-06-10T11:00 with \"TeamSync\"",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"TeamSync\",06/10/2025,10:00 AM,06/10/2025,11:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Editing an event's description works correctly.
  @Test
  public void testEditEvent_UpdateDescription_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name EditDescCal --timezone America/New_York",
            "use calendar --name EditDescCal",
            "create event Meeting from 2025-06-10T10:00 to 2025-06-10T11:00",
            "edit events description Meeting from 2025-06-10T10:00 to 2025-06-10T11:00 with \"Weekly meeting\"",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Meeting\",06/10/2025,10:00 AM,06/10/2025,11:00 AM,False,\"Weekly meeting\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Editing a recurring event with editAll = false should update only the first occurrence.
  @Test
  public void testEditRecurringEvent_SingleOccurrenceEdit_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name EditRecurrSingleCal --timezone America/New_York",
            "use calendar --name EditRecurrSingleCal",
            "create event Standup from 2025-07-06T09:00 to 2025-07-06T09:30 repeats M for 2 times",
            "edit events location Standup from 2025-07-06T09:00 to 2025-07-06T09:30 with \"Room Z\"",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Standup\",07/06/2025,09:00 AM,07/06/2025,09:30 AM,False,\"\",\"Room Z\",False",
            "\"Standup\",07/13/2025,09:00 AM,07/13/2025,09:30 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Editing a recurring event with editAll = true should update all occurrences.
  @Test
  public void testEditRecurringEvent_UpdateAllOccurrences_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name EditRecurrCal --timezone America/New_York",
            "use calendar --name EditRecurrCal",
            "create event Standup from 2025-04-24T09:00 to 2025-04-24T10:00 repeats T for 3 times",
            "edit events location Standup from 2025-04-24T09:00 to 2025-04-24T10:00 with \"Room C\"",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Standup\",04/24/2025,09:00 AM,04/24/2025,10:00 AM,False,\"\",\"Room C\",False",
            "\"Standup\",04/30/2025,09:00 AM,04/30/2025,10:00 AM,False,\"\",\"Room C\",False",
            "\"Standup\",05/07/2025,09:00 AM,05/07/2025,10:00 AM,False,\"\",\"Room C\",False"
    );
    assertEquals(expected, readExportedFile());
  }


}
