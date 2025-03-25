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
    } catch (IOException e) {
      fail("IOException occurred: " + e.getMessage());
    }
  }

  private String readExportedFile() throws IOException {
    return new String(Files.readAllBytes(Paths.get(OUTPUT_FILE)))
            .trim()
            .replace("\r\n", "\n")
            .replace("\r", "\n");
  }
  //Show status tests
  @Test
  public void testShowStatusCommandWhenBusy_ShouldPrintBusy() throws IOException {
    String[] commands = {
            "create calendar --name StatusCal --timezone America/New_York",
            "use calendar --name StatusCal",
            "create event Meeting from 2024-06-04T10:00 to 2024-06-04T11:00",
            "show status on 2024-06-04T10:30",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("busy"));
  }

  @Test
  public void testShowStatusReturnsBusyForTimedEvent() throws IOException {
    String[] commands = {
            "create calendar --name BusyTest --timezone America/New_York",
            "use calendar --name BusyTest",
            "create event Meeting from 2024-06-05T10:00 to 2024-06-05T11:00",
            "show status on 2024-06-05T10:30",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("busy"));
  }

  @Test
  public void testShowStatusReturnsAvailableWhenNoEventExists() throws IOException {
    String[] commands = {
            "create calendar --name FreeTest --timezone America/New_York",
            "use calendar --name FreeTest",
            "show status on 2024-06-06T09:00",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("available"));
  }

  @Test
  public void testShowStatusReturnsBusyForAllDayEvent() throws IOException {
    String[] commands = {
            "create calendar --name AllDayStatus --timezone America/New_York",
            "use calendar --name AllDayStatus",
            "create event Retreat on 2024-06-07",
            "show status on 2024-06-07T13:00",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("busy"));
  }

  @Test
  public void testShowStatusReturnsBusyForRecurringInstance() throws IOException {
    String[] commands = {
            "create calendar --name RecurringStatus --timezone America/New_York",
            "use calendar --name RecurringStatus",
            "create event Standup from 2024-06-10T09:00 to 2024-06-10T09:30 repeats M for 2 times",
            "show status on 2024-06-10T09:15",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("busy"));
  }

  @Test
  public void testShowStatusWithInvalidDatetimeFormat_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name BadFormat --timezone America/New_York",
            "use calendar --name BadFormat",
            "show status on 06-08-2024T09:00", // Invalid format
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("invalid date and time format"));
  }

  @Test
  public void testShowStatusMissingOnKeyword_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name MissingOn --timezone America/New_York",
            "use calendar --name MissingOn",
            "show status 2024-06-09T09:00", // Missing 'on'
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("missing 'on' keyword"));
  }

  @Test
  public void testShowStatusWithExtraArguments_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name ExtraArgs --timezone America/New_York",
            "use calendar --name ExtraArgs",
            "show status on 2024-06-10T09:00 now", // Extra token
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("invalid syntax"));
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
            "\"Holiday\",04/22/2025,12:00 AM,04/22/2025,11:59 PM,True,\"\",\"\",False"
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
      builder.append("\n\"Seminar\"," + date + ",12:00 AM," + date + ",11:59 PM,True,\"\",\"\",False");
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
            "\"Conference\",05/05/2025,12:00 AM,05/05/2025,11:59 PM,True,\"\",\"\",False"
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
            "\"Holiday\",04/26/2025,12:00 AM,04/26/2025,11:59 PM,True,\"\",\"\",False",
            "\"Holiday\",04/27/2025,12:00 AM,04/27/2025,11:59 PM,True,\"\",\"\",False"
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
  public void testCreateRecurringTimedEventWithAutoDeclineAndRepeatCount_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name School --timezone America/New_York",
            "use calendar --name School",
            "create event --autoDecline Meeting from 2024-03-25T10:00 to 2024-03-25T11:00 repeats MR for 3 times",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Meeting\",03/25/2024,10:00 AM,03/25/2024,11:00 AM,False,\"\",\"\",False",
            "\"Meeting\",03/28/2024,10:00 AM,03/28/2024,11:00 AM,False,\"\",\"\",False",
            "\"Meeting\",04/01/2024,10:00 AM,04/01/2024,11:00 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringAllDayEventWithUntilDate_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name Gym --timezone America/New_York",
            "use calendar --name Gym",
            "create event Yoga on 2024-04-01 repeats WF until 2024-04-10",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Yoga\",04/03/2024,12:00 AM,04/03/2024,11:59 PM,True,\"\",\"\",False",
            "\"Yoga\",04/05/2024,12:00 AM,04/05/2024,11:59 PM,True,\"\",\"\",False",
            "\"Yoga\",04/10/2024,12:00 AM,04/10/2024,11:59 PM,True,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateSingleTimedEventWithoutAutoDecline_NoConflict_ShouldSucceed() throws IOException {
    String[] commands = {
            "create calendar --name Work --timezone America/New_York",
            "use calendar --name Work",
            "create event Standup from 2024-04-02T09:00 to 2024-04-02T09:30",
            "create event Sync from 2024-04-02T10:00 to 2024-04-02T11:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Standup\",04/02/2024,09:00 AM,04/02/2024,09:30 AM,False,\"\",\"\",False",
            "\"Sync\",04/02/2024,10:00 AM,04/02/2024,11:00 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringAllDayEventForOneTime_ShouldExportSingleInstance() throws IOException {
    String[] commands = {
            "create calendar --name Health --timezone America/New_York",
            "use calendar --name Health",
            "create event Walk on 2024-04-01 repeats M for 1 times",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Walk\",04/01/2024,12:00 AM,04/01/2024,11:59 PM,True,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringTimedEventOnNonConsecutiveWeekdays_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name ClassSchedule --timezone America/New_York",
            "use calendar --name ClassSchedule",
            "create event Lecture from 2024-04-01T09:00 to 2024-04-01T10:30 repeats MTW for 1 times",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Lecture\",04/01/2024,09:00 AM,04/01/2024,10:30 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateSingleTimedEventAtMidnight_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name NightShift --timezone America/New_York",
            "use calendar --name NightShift",
            "create event Patrol from 2024-04-05T00:00 to 2024-04-05T02:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Patrol\",04/05/2024,12:00 AM,04/05/2024,02:00 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }
  
  @Test
  public void testCreateSingleTimedEventAtNoon_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name LunchEvents --timezone America/New_York",
            "use calendar --name LunchEvents",
            "create event Lunch from 2024-04-06T12:00 to 2024-04-06T13:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Lunch\",04/06/2024,12:00 PM,04/06/2024,01:00 PM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringTimedEventWithInclusiveUntilDate_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name Reviews --timezone America/New_York",
            "use calendar --name Reviews",
            "create event CodeReview from 2024-04-08T15:00 to 2024-04-08T16:00 repeats MWF until 2024-04-12",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"CodeReview\",04/08/2024,03:00 PM,04/08/2024,04:00 PM,False,\"\",\"\",False",
            "\"CodeReview\",04/10/2024,03:00 PM,04/10/2024,04:00 PM,False,\"\",\"\",False",
            "\"CodeReview\",04/12/2024,03:00 PM,04/12/2024,04:00 PM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateAllDayEventOnSunday_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name WeekendPlans --timezone America/New_York",
            "use calendar --name WeekendPlans",
            "create event Brunch on 2024-04-07",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Brunch\",04/07/2024,12:00 AM,04/07/2024,11:59 PM,True,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateMultipleBackToBackEvents_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name Productivity --timezone America/New_York",
            "use calendar --name Productivity",
            "create event Writing from 2024-04-09T09:00 to 2024-04-09T10:00",
            "create event Meeting from 2024-04-09T10:00 to 2024-04-09T11:00",
            "create event Research from 2024-04-09T11:00 to 2024-04-09T12:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Writing\",04/09/2024,09:00 AM,04/09/2024,10:00 AM,False,\"\",\"\",False",
            "\"Meeting\",04/09/2024,10:00 AM,04/09/2024,11:00 AM,False,\"\",\"\",False",
            "\"Research\",04/09/2024,11:00 AM,04/09/2024,12:00 PM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringTimedEventAcrossMonthBoundary_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name StudyPlan --timezone America/New_York",
            "use calendar --name StudyPlan",
            "create event Review from 2024-01-30T18:00 to 2024-01-30T19:00 repeats T for 3 times",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Review\",01/30/2024,06:00 PM,01/30/2024,07:00 PM,False,\"\",\"\",False",
            "\"Review\",02/06/2024,06:00 PM,02/06/2024,07:00 PM,False,\"\",\"\",False",
            "\"Review\",02/13/2024,06:00 PM,02/13/2024,07:00 PM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateSingleEventWithOptionalFlags_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name Events --timezone America/New_York",
            "use calendar --name Events",
            "create event Launch from 2024-04-15T09:00 to 2024-04-15T10:00 --description ReleaseDay --location HQ --private",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Launch\",04/15/2024,09:00 AM,04/15/2024,10:00 AM,False,\"ReleaseDay\",\"HQ\",True"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateAllDayEventWithOptionalFlags_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name AllDayCal --timezone America/New_York",
            "use calendar --name AllDayCal",
            "create event Meditation on 2024-04-20 --description Focus --location Park --private",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Meditation\",04/20/2024,12:00 AM,04/20/2024,11:59 PM,True,\"Focus\",\"Park\",True"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringTimedEventWithOptionalFlags_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name RecurringFlags --timezone America/New_York",
            "use calendar --name RecurringFlags",
            "create event Gym from 2024-04-22T06:00 to 2024-04-22T07:00 repeats MF for 2 times --description Workout --location FitnessCenter --private",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Gym\",04/22/2024,06:00 AM,04/22/2024,07:00 AM,False,\"Workout\",\"FitnessCenter\",True",
            "\"Gym\",04/26/2024,06:00 AM,04/26/2024,07:00 AM,False,\"Workout\",\"FitnessCenter\",True"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringAllDayEventWithOptionalFlags_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name Retreats --timezone America/New_York",
            "use calendar --name Retreats",
            "create event Yoga on 2024-04-29 repeats MW for 2 times --description Relaxation --location Center --private",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Yoga\",04/29/2024,12:00 AM,04/29/2024,11:59 PM,True,\"Relaxation\",\"Center\",True",
            "\"Yoga\",05/01/2024,12:00 AM,05/01/2024,11:59 PM,True,\"Relaxation\",\"Center\",True"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateEventWithOnlyDescription_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name DescOnly --timezone America/New_York",
            "use calendar --name DescOnly",
            "create event Brainstorm from 2024-04-17T14:00 to 2024-04-17T15:00 --description Ideation",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Brainstorm\",04/17/2024,02:00 PM,04/17/2024,03:00 PM,False,\"Ideation\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateEventWithOnlyLocation_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name LocationOnly --timezone America/New_York",
            "use calendar --name LocationOnly",
            "create event Sync from 2024-04-18T09:00 to 2024-04-18T10:00 --location Room42",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Sync\",04/18/2024,09:00 AM,04/18/2024,10:00 AM,False,\"\",\"Room42\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateEventWithOnlyPrivateFlag_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name PrivateOnly --timezone America/New_York",
            "use calendar --name PrivateOnly",
            "create event Therapy from 2024-04-19T16:00 to 2024-04-19T17:00 --private",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Therapy\",04/19/2024,04:00 PM,04/19/2024,05:00 PM,False,\"\",\"\",True"
    ));
    assertEquals(expected, content);
  }

  //create conflict scenarios
  @Test
  public void testCreateConflictingTimedEvents_ShouldRejectSecond() throws IOException {
    String[] commands = {
            "create calendar --name ConflictTest --timezone America/New_York",
            "use calendar --name ConflictTest",
            "create event Meeting1 from 2024-04-22T10:00 to 2024-04-22T11:00",
            "create event Meeting2 from 2024-04-22T10:30 to 2024-04-22T11:30", // Overlaps
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Meeting1\",04/22/2024,10:00 AM,04/22/2024,11:00 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringEventThatConflictsWithExisting_ShouldBeRejected() throws IOException {
    String[] commands = {
            "create calendar --name RecurringConflict --timezone America/New_York",
            "use calendar --name RecurringConflict",
            "create event Interview from 2024-04-24T09:00 to 2024-04-24T10:00",
            "create event Standup from 2024-04-22T09:00 to 2024-04-22T09:30 repeats MWF for 3 times",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Interview\",04/24/2024,09:00 AM,04/24/2024,10:00 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateSingleEventThatConflictsWithRecurring_ShouldBeRejected() throws IOException {
    String[] commands = {
            "create calendar --name ReverseConflict --timezone America/New_York",
            "use calendar --name ReverseConflict",
            "create event Standup from 2024-04-22T09:00 to 2024-04-22T09:30 repeats MWF for 3 times",
            "create event Checkin from 2024-04-24T09:00 to 2024-04-24T09:30",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Standup\",04/22/2024,09:00 AM,04/22/2024,09:30 AM,False,\"\",\"\",False",
            "\"Standup\",04/24/2024,09:00 AM,04/24/2024,09:30 AM,False,\"\",\"\",False",
            "\"Standup\",04/26/2024,09:00 AM,04/26/2024,09:30 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateConflictingAllDayEvents_ShouldRejectSecond() throws IOException {
    String[] commands = {
            "create calendar --name AllDayClash --timezone America/New_York",
            "use calendar --name AllDayClash",
            "create event Holiday on 2024-04-30",
            "create event Workshop on 2024-04-30",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Holiday\",04/30/2024,12:00 AM,04/30/2024,11:59 PM,True,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateTimedEventConflictingWithAllDayEvent_ShouldReject() throws IOException {
    String[] commands = {
            "create calendar --name AllDayBlock --timezone America/New_York",
            "use calendar --name AllDayBlock",
            "create event Conference on 2024-05-01",
            "create event Call from 2024-05-01T14:00 to 2024-05-01T15:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Conference\",05/01/2024,12:00 AM,05/01/2024,11:59 PM,True,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateAllDayEventConflictingWithTimedEvent_ShouldReject() throws IOException {
    String[] commands = {
            "create calendar --name TimedBlock --timezone America/New_York",
            "use calendar --name TimedBlock",
            "create event SprintReview from 2024-05-02T10:00 to 2024-05-02T12:00",
            "create event Offsite on 2024-05-02",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"SprintReview\",05/02/2024,10:00 AM,05/02/2024,12:00 PM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateEventThatSpansMultipleDays_ShouldBlockOverlappingEvents() throws IOException {
    String[] commands = {
            "create calendar --name MultiDayBlock --timezone America/New_York",
            "use calendar --name MultiDayBlock",
            "create event Retreat from 2024-05-03T09:00 to 2024-05-05T17:00",
            "create event CatchUp from 2024-05-04T10:00 to 2024-05-04T11:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Retreat\",05/03/2024,09:00 AM,05/05/2024,05:00 PM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testBackToBackEventsShouldBeAllowed() throws IOException {
    String[] commands = {
            "create calendar --name NoClash --timezone America/New_York",
            "use calendar --name NoClash",
            "create event First from 2024-05-06T08:00 to 2024-05-06T09:00",
            "create event Second from 2024-05-06T09:00 to 2024-05-06T10:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"First\",05/06/2024,08:00 AM,05/06/2024,09:00 AM,False,\"\",\"\",False",
            "\"Second\",05/06/2024,09:00 AM,05/06/2024,10:00 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  // Tests for invalid crreate commands
  @Test
  public void testCreateEventMissingFromOrOn_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name InvalidStart --timezone America/New_York",
            "use calendar --name InvalidStart",
            "create event TestEvent 2024-05-07T10:00 to 2024-05-07T11:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().toLowerCase().contains("expected 'from' or 'on'"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private", content);
  }

  @Test
  public void testCreateEventMissingEndTime_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name MissingEnd --timezone America/New_York",
            "use calendar --name MissingEnd",
            "create event Task from 2024-05-08T10:00 to",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().toLowerCase().contains("missing end datetime"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private", content);
  }

  @Test
  public void testCreateEventWithInvalidRepeatDay_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name InvalidRepeat --timezone America/New_York",
            "use calendar --name InvalidRepeat",
            "create event Lecture from 2024-05-09T08:00 to 2024-05-09T09:00 repeats MX for 2 times",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().toLowerCase().contains("invalid weekday character"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private", content);
  }

  @Test
  public void testCreateEventWithMissingDescriptionValue_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name BadDescription --timezone America/New_York",
            "use calendar --name BadDescription",
            "create event Solo from 2024-05-09T10:00 to 2024-05-09T11:00 --description",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().toLowerCase().contains("missing value for --description"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private", content);
  }

  @Test
  public void testCreateEventWithUnknownArgument_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name WeirdArgs --timezone America/New_York",
            "use calendar --name WeirdArgs",
            "create event Ghost from 2024-05-10T08:00 to 2024-05-10T09:00 --weirdflag yes",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().toLowerCase().contains("unrecognized extra argument"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private", content);
  }

  @Test
  public void testCreateEventWithNonNumericRepeatCount_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name BadRepeatCount --timezone America/New_York",
            "use calendar --name BadRepeatCount",
            "create event Gym from 2024-05-11T07:00 to 2024-05-11T08:00 repeats M for abc times",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().toLowerCase().contains("for input string: \"abc\""));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private", content);
  }

  @Test
  public void testCreateEventWithInvalidDateFormat_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name BadDate --timezone America/New_York",
            "use calendar --name BadDate",
            "create event InvalidDate from 2024-13-40T10:00 to 2024-13-40T11:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("could not be parsed"));
    String content = readExportedFile();
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private", content);
  }

  @Test
  public void testCreateCalendarWithInvalidTimezone_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name Glitch --timezone Mars/ColonyOne",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("invalid timezone"));
  }

  @Test
  public void testCreateEventWithDescriptionValueWithoutFlag_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name NoFlagDesc --timezone America/New_York",
            "use calendar --name NoFlagDesc",
            "create event Solo from 2024-05-12T14:00 to 2024-05-12T15:00 ChillSession",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().toLowerCase().contains("unrecognized extra argument"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private", content);
  }

  @Test
  public void testCreateEventWithLocationValueWithoutFlag_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name NoFlagLoc --timezone America/New_York",
            "use calendar --name NoFlagLoc",
            "create event Sync from 2024-05-13T09:00 to 2024-05-13T10:00 ConferenceRoom",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().toLowerCase().contains("unrecognized extra argument"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private", content);
  }

  @Test
  public void testCreateEventWithPrivateFlagAndValue_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name PrivateWithValue --timezone America/New_York",
            "use calendar --name PrivateWithValue",
            "create event Secret from 2024-05-14T18:00 to 2024-05-14T19:00 --private true",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().contains("Error Executing command: --private does not take a value"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private", content);
  }
  @Test
  public void testCreateEventWithDescriptionBeforeEventName_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name MisplacedDesc --timezone America/New_York",
            "use calendar --name MisplacedDesc",
            "create event --description Briefing from 2024-05-15T09:00 to 2024-05-15T10:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().contains("Error Executing command: Expected 'from' or 'on' after event name"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private", content);
  }

  @Test
  public void testCreateEventWithMultipleDescriptionFlags_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name RepeatedDesc --timezone America/New_York",
            "use calendar --name RepeatedDesc",
            "create event Demo from 2024-05-16T13:00 to 2024-05-16T14:00 --description One --description Two",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().contains("Error Executing command: Duplicate --description flag"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private", content);
  }

  @Test
  public void testCreateEventWithSameStartAndEndTime_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name SameTime --timezone America/New_York",
            "use calendar --name SameTime",
            "create event ZeroLength from 2024-05-17T10:00 to 2024-05-17T10:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().contains("Error: End date and time must be after start date and time."));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private", content);
  }

  @Test
  public void testCreateEventWithStartAfterEnd_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name BackwardsEvent --timezone America/New_York",
            "use calendar --name BackwardsEvent",
            "create event Broken from 2024-05-20T15:00 to 2024-05-20T13:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().contains("Error: End date and time must be after start date and time."));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private", content);
  }

  @Test
  public void testCreateEventWithNoName_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name NoName --timezone America/New_York",
            "use calendar --name NoName",
            "create event from 2024-05-21T10:00 to 2024-05-21T11:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().contains("Error Executing command: Expected 'from' or 'on' after event name"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private", content);
  }

  @Test
  public void testCreateEventWithReservedWordAsName_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name ReservedEvent --timezone America/New_York",
            "use calendar --name ReservedEvent",
            "create event from from 2024-05-22T10:00 to 2024-05-22T11:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"from\",05/22/2024,10:00 AM,05/22/2024,11:00 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringEventWithoutForOrUntil_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name IncompleteRepeats --timezone America/New_York",
            "use calendar --name IncompleteRepeats",
            "create event Task from 2024-05-24T08:00 to 2024-05-24T09:00 repeats M",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().contains("Error: Either recurrence count or recurrence end date must be defined for a recurring event."));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private", content);
  }

  @Test
  public void testCreateAllDayEventUsingFromToInsteadOfOn_ShouldCreateTimedEvent() throws IOException {
    String[] commands = {
            "create calendar --name FauxAllDay --timezone America/New_York",
            "use calendar --name FauxAllDay",
            "create event Stretch from 2024-05-25T00:00 to 2024-05-25T23:59",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Stretch\",05/25/2024,12:00 AM,05/25/2024,11:59 PM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateEventWithSpacesInName_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name QuotedNameTest --timezone America/New_York",
            "use calendar --name QuotedNameTest",
            "create event \"Team Sync\" from 2024-05-27T10:00 to 2024-05-27T11:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Team Sync\",05/27/2024,10:00 AM,05/27/2024,11:00 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateEventWithMultiWordDescription_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name MultiWordDesc --timezone America/New_York",
            "use calendar --name MultiWordDesc",
            "create event Briefing from 2024-05-28T09:00 to 2024-05-28T10:00 --description \"Weekly Team Sync\"",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Briefing\",05/28/2024,09:00 AM,05/28/2024,10:00 AM,False,\"Weekly Team Sync\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateEventWithMultiWordLocation_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name MultiWordLoc --timezone America/New_York",
            "use calendar --name MultiWordLoc",
            "create event AllHands from 2024-05-29T11:00 to 2024-05-29T12:00 --location \"Main Conference Room\"",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"AllHands\",05/29/2024,11:00 AM,05/29/2024,12:00 PM,False,\"\",\"Main Conference Room\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringEventThatPartiallyConflicts_ShouldFail() throws IOException {
    String[] commands = {
            "create calendar --name PartialConflict --timezone America/New_York",
            "use calendar --name PartialConflict",
            "create event DailyStandup from 2024-06-03T09:00 to 2024-06-03T09:30", // Occurs on Monday
            "create event SprintUpdate from 2024-06-03T09:00 to 2024-06-03T10:00 repeats MR for 2 times", // Conflicts with 6/3 (Monday), but not 6/6 (Thursday)
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"DailyStandup\",06/03/2024,09:00 AM,06/03/2024,09:30 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
    assertTrue(outContent.toString().toLowerCase().contains("conflict"));
  }

  @Test
  public void testCreateRecurringEventThatDoesNotConflict_ShouldSucceed() throws IOException {
    String[] commands = {
            "create calendar --name CleanRecurring --timezone America/New_York",
            "use calendar --name CleanRecurring",
            "create event MorningBlock from 2024-06-03T08:00 to 2024-06-03T09:00",
            "create event LateBlock from 2024-06-03T10:00 to 2024-06-03T11:00",
            "create event Standup from 2024-06-03T09:00 to 2024-06-03T09:30 repeats MR for 2 times", // Fits between other events
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"MorningBlock\",06/03/2024,08:00 AM,06/03/2024,09:00 AM,False,\"\",\"\",False",
            "\"LateBlock\",06/03/2024,10:00 AM,06/03/2024,11:00 AM,False,\"\",\"\",False",
            "\"Standup\",06/03/2024,09:00 AM,06/03/2024,09:30 AM,False,\"\",\"\",False",
            "\"Standup\",06/06/2024,09:00 AM,06/06/2024,09:30 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
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
            "\"CheckIn\",06/02/2025,09:00 AM,06/02/2025,09:30 AM,False,\"\",\"\",False",
            "\"CheckIn\",06/03/2025,09:00 AM,06/03/2025,09:30 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditSingleEvent_ShouldExportCorrectly() throws IOException {
    String[] commands = {
            "create calendar --name EditCal --timezone America/New_York",
            "use calendar --name EditCal",
            "create event Meeting from 2025-04-22T09:00 to 2025-04-22T10:00",
            "edit events location Meeting from 2025-04-22T09:00 with \"Room B\"",
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
            "edit events location Standup from 2025-04-24T09:00 with \"Room C\"",
            "export cal " + OUTPUT_FILE,
            "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Standup\",04/29/2025,09:00 AM,04/29/2025,10:00 AM,False,\"\",\"Room C\",False",
            "\"Standup\",05/06/2025,09:00 AM,05/06/2025,10:00 AM,False,\"\",\"Room C\",False",
            "\"Standup\",05/13/2025,09:00 AM,05/13/2025,10:00 AM,False,\"\",\"Room C\",False"
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
            "edit events color Meeting from 2025-05-01T10:00 with Red",
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
            "edit events location NonExistent from 2025-05-01T10:00 with \"Room X\"",
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
            "edit events start Meeting from 2025-06-10T10:00 with \"2025-06-10T11:00:00\"",
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
            "edit events end Meeting from 2025-06-10T10:00 with \"2025-06-10T10:00:00\"",
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
            "edit events name Meeting from 2025-06-10T10:00 with \"TeamSync\"",
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
            "edit events description Meeting from 2025-06-10T10:00 with \"Weekly meeting\"",
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
            "edit events location Standup from 2025-07-06T09:00 with \"Room Z\"",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Standup\",07/07/2025,09:00 AM,07/07/2025,09:30 AM,False,\"\",\"Room Z\",False",
            "\"Standup\",07/14/2025,09:00 AM,07/14/2025,09:30 AM,False,\"\",\"Room Z\",False"
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
            "edit events location Standup from 2025-04-24T09:00 with \"Room C\"",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
            "\"Standup\",04/29/2025,09:00 AM,04/29/2025,10:00 AM,False,\"\",\"Room C\",False",
            "\"Standup\",05/06/2025,09:00 AM,05/06/2025,10:00 AM,False,\"\",\"Room C\",False",
            "\"Standup\",05/13/2025,09:00 AM,05/13/2025,10:00 AM,False,\"\",\"Room C\",False"
    );
    assertEquals(expected, readExportedFile());
  }
}
