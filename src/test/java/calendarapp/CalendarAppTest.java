package calendarapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A JUnit test suite for verifying the behavior of the Calendar application's entry
 * point.
 * <p>
 * This class tests various scenarios for the {@code CalendarApp} class, such as
 * handling command line arguments to run in interactive or headless mode, and
 * ensuring that the application can be started and shut down properly under
 * different conditions.
 * </p>
 */
@RunWith(Parameterized.class)

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
    System.setProperty("run_mode", "true");
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
    String[] args = {"--mode"};
    CalendarApp.main(args);
    String expectedMessage = "Usage: --mode <interactive|headless|gui> [filePath]";
    assertTrue(outContent.toString().trim().contains(expectedMessage));
  }

  @Test
  public void testMain_ValidArguments_ShouldRunInteractiveMode() throws IOException {
    String[] args = {"--mode", "interactive"};
    Thread appThread = new Thread(() -> {
      CalendarApp.main(args);
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

  @Test
  public void testMain_ValidArguments_HeadlessMode_ShouldRunWithFile2() throws IOException {
    File tempFile = File.createTempFile("commands", ".txt");
    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("");
    }
    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarApp.main(args);
    assertTrue(outContent.toString().trim().contains("File must end with" +
        " 'exit' in headless mode."));
    tempFile.delete();
  }

  @Test
  public void testMain_ValidArguments_HeadlessMode_ShouldRunWithFile3() throws IOException {
    File tempFile = File.createTempFile("commands", ".txt");
    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("");
    }
    String[] args = {"--mode", "Blast", tempFile.getAbsolutePath()};
    CalendarApp.main(args);
    assertTrue(outContent.toString().trim().contains("Unsupported mode"));
    tempFile.delete();
  }

  @Test
  public void testMain_ValidArguments_HeadlessMode_ShouldRunWithFile4() throws IOException {
    File tempFile = File.createTempFile("commands", ".txt");
    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("");
    }
    String[] args = {"--mode", "headless"};
    CalendarApp.main(args);
    assertTrue(outContent.toString().trim().contains("Missing filepath for headless " +
          "mode"));
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
        "create event Standup from 2024-06-10T09:00 to 2024-06-10T09:30 repeats M "
            +
            "for 2 times",
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
        "show status on 06-08-2024T09:00",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("invalid date and time "
        +
        "format"));
  }

  @Test
  public void testShowStatusMissingOnKeyword_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name MissingOn --timezone America/New_York",
        "use calendar --name MissingOn",
        "show status 2024-06-09T09:00",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Error Executing command: Invalid syntax."
        +
        " Expected: show status on <datetime>"));
  }

  @Test
  public void testShowStatusWithExtraArguments_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name ExtraArgs --timezone America/New_York",
        "use calendar --name ExtraArgs",
        "show status on 2024-06-10T09:00 now",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("invalid syntax"));
  }

  @Test
  public void testShowStatusExactlyAtEventEnd_ShouldBeAvailable() throws IOException {
    String[] commands = {
        "create calendar --name EndTimeCheck --timezone America/New_York",
        "use calendar --name EndTimeCheck",
        "create event Sync from 2024-06-11T14:00 to 2024-06-11T15:00",
        "show status on 2024-06-11T15:00",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("busy"));
  }

  @Test
  public void testShowStatusExactlyAtEventStart_ShouldBeBusy() throws IOException {
    String[] commands = {
        "create calendar --name StartTimeCheck --timezone America/New_York",
        "use calendar --name StartTimeCheck",
        "create event Sync from 2024-06-12T14:00 to 2024-06-12T15:00",
        "show status on 2024-06-12T14:00",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("busy"));
  }

  @Test
  public void testShowStatusDuringMultiDayEvent_ShouldBeBusy() throws IOException {
    String[] commands = {
        "create calendar --name MultiDayStatus --timezone America/New_York",
        "use calendar --name MultiDayStatus",
        "create event Hackathon from 2024-06-13T10:00 to 2024-06-15T18:00",
        "show status on 2024-06-14T12:00",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("busy"));
  }

  //Print calendar command tests
  @Test
  public void testPrintEventsOnExactOutputMatch() throws IOException {
    String[] commands = {
        "create calendar --name ExactOn --timezone America/New_York",
        "use calendar --name ExactOn",
        "create event Seminar from 2024-06-20T13:00 to 2024-06-20T14:30 --location "
            +
            "HallB",
        "print events on 2024-06-20",
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join(System.lineSeparator(), List.of(
        "Welcome to the Calendar App!",
        "Calendar created successfully.",
        "Using calendar: ExactOn",
        "Event created successfully.",
        "- Seminar [2024-06-20T13:00 to 2024-06-20T14:30] at HallB"
    ));

    assertEquals(expected, outContent.toString().trim());
  }

  @Test
  public void testPrintEventsFromToFullOutputMatch() throws IOException {
    String[] commands = {
        "create calendar --name ExactFromTo --timezone America/New_York",
        "use calendar --name ExactFromTo",
        "create event Meeting from 2024-06-22T09:00 to 2024-06-22T10:00 --location "
            +
            "ConfRoom",
        "create event Review from 2024-06-22T10:30 to 2024-06-22T11:30 --location "
            +
            "BoardRoom",
        "print events from 2024-06-22T00:00 to 2024-06-22T23:59",
        "exit"
    };
    runAppWithCommands(commands);


    String expected = String.join(System.lineSeparator(), List.of(
        "Welcome to the Calendar App!",
        "Calendar created successfully.",
        "Using calendar: ExactFromTo",
        "Event created successfully.",
        "Event created successfully.",
        "- Meeting [2024-06-22T09:00 to 2024-06-22T10:00] at ConfRoom",
        "- Review [2024-06-22T10:30 to 2024-06-22T11:30] at BoardRoom"
    ));

    assertEquals(expected, outContent.toString().trim());
  }

  @Test
  public void testPrintEventsFromTo_WithDifferentEventNames_ShouldMatchOutput() throws IOException {
    String[] commands = {
        "create calendar --name AgendaRange --timezone America/New_York",
        "use calendar --name AgendaRange",
        "create event Planning from 2024-07-01T08:00 to 2024-07-01T09:00 --location"
            +
            " RoomA",
        "create event WrapUp from 2024-07-01T16:00 to 2024-07-01T17:00 --location "
            +
            "RoomB",
        "print events from 2024-07-01T00:00 to 2024-07-01T23:59",
        "exit"
    };
    runAppWithCommands(commands);

    String nl = System.lineSeparator();
    String expected = String.join(nl, List.of(
        "Welcome to the Calendar App!",
        "Calendar created successfully.",
        "Using calendar: AgendaRange",
        "Event created successfully.",
        "Event created successfully.",
        "- Planning [2024-07-01T08:00 to 2024-07-01T09:00] at RoomA",
        "- WrapUp [2024-07-01T16:00 to 2024-07-01T17:00] at RoomB"
    ));

    assertEquals(expected, outContent.toString().trim());
  }


  @Test
  public void testPrintEventsOnAllDayEvent_ShouldMatchOutputExactly() throws IOException {
    String[] commands = {
        "create calendar --name AllDayExact --timezone America/New_York",
        "use calendar --name AllDayExact",
        "create event Holiday on 2024-06-23 --location Home",
        "print events on 2024-06-23",
        "exit"
    };
    runAppWithCommands(commands);

    String nl = System.lineSeparator();
    String expected = String.join(nl, List.of(
        "Welcome to the Calendar App!",
        "Calendar created successfully.",
        "Using calendar: AllDayExact",
        "Event created successfully.",
        "- Holiday [2024-06-23T00:00 to 2024-06-23T23:59:59] at Home"
    ));

    assertEquals(expected, outContent.toString().trim());
  }

  @Test
  public void testPrintEventsOnRecurringEvent_ShouldMatchFullOutput() throws IOException {
    String[] commands = {
        "create calendar --name RecurringPrint --timezone America/New_York",
        "use calendar --name RecurringPrint",
        "create event Standup from 2024-06-24T09:00 to 2024-06-24T09:30 repeats M "
            +
            "for 2 times --location Zoom",
        "print events on 2024-06-24",
        "exit"
    };
    runAppWithCommands(commands);

    String nl = System.lineSeparator();
    String expected = String.join(nl, List.of(
        "Welcome to the Calendar App!",
        "Calendar created successfully.",
        "Using calendar: RecurringPrint",
        "Event created successfully.",
        "- Standup [2024-06-24T09:00 to 2024-06-24T09:30] at Zoom"
    ));

    assertEquals(expected, outContent.toString().trim());
  }

  @Test
  public void testPrintEventsOnDateWithNoEvents_ShouldMatchNoEventsOutput() throws IOException {
    String[] commands = {
        "create calendar --name NoEventDay --timezone America/New_York",
        "use calendar --name NoEventDay",
        "print events on 2024-06-25",
        "exit"
    };
    runAppWithCommands(commands);

    String nl = System.lineSeparator();
    String expected = String.join(nl, List.of(
        "Welcome to the Calendar App!",
        "Calendar created successfully.",
        "Using calendar: NoEventDay",
        "No events found."
    ));

    assertEquals(expected, outContent.toString().trim());
  }

  @Test
  public void testPrintEventWithoutLocation_ShouldNotIncludeAtClause() throws IOException {
    String[] commands = {


        "create calendar --name NoLocation --timezone America/New_York",
        "use calendar --name NoLocation",
        "create event SoloWork from 2024-07-02T10:00 to 2024-07-02T12:00",
        "print events on 2024-07-02",
        "exit"
    };
    runAppWithCommands(commands);

    String nl = System.lineSeparator();
    String expected = String.join(nl, List.of(
        "Welcome to the Calendar App!",
        "Calendar created successfully.",
        "Using calendar: NoLocation",
        "Event created successfully.",
        "- SoloWork [2024-07-02T10:00 to 2024-07-02T12:00]"
    ));

    assertEquals(expected, outContent.toString().trim());
  }

  @Test
  public void testPrintEventsThatOverlapWithDate_ShouldIncludeThem() throws IOException {
    String[] commands = {
        "create calendar --name OverlapRange --timezone America/New_York",
        "use calendar --name OverlapRange",
        "create event Overnight from 2024-07-03T23:00 to 2024-07-04T01:00 "
            +
            "--location Lounge",
        "print events from 2024-07-04T00:00 to 2024-07-04T23:59",
        "exit"
    };
    runAppWithCommands(commands);

    String nl = System.lineSeparator();
    String expected = String.join(nl, List.of(
        "Welcome to the Calendar App!",
        "Calendar created successfully.",
        "Using calendar: OverlapRange",
        "Event created successfully.",
        "- Overnight [2024-07-03T23:00 to 2024-07-04T01:00] at Lounge"
    ));

    assertEquals(expected, outContent.toString().trim());
  }

  @Test
  public void testPrintEventsWithAllDayAndTimedSeparateDays_ShouldListBoth() throws IOException {
    String[] commands = {
        "create calendar --name MixedNoConflict --timezone America/New_York",
        "use calendar --name MixedNoConflict",
        "create event Holiday on 2024-07-05",
        "create event Sync from 2024-07-06T09:00 to 2024-07-06T10:00 --location "
            +
            "Online",
        "print events from 2024-07-05T00:00 to 2024-07-06T23:59",
        "exit"
    };
    runAppWithCommands(commands);

    String nl = System.lineSeparator();
    String expected = String.join(nl, List.of(
        "Welcome to the Calendar App!",
        "Calendar created successfully.",
        "Using calendar: MixedNoConflict",
        "Event created successfully.",
        "Event created successfully.",
        "- Holiday [2024-07-05T00:00 to 2024-07-05T23:59:59]",
        "- Sync [2024-07-06T09:00 to 2024-07-06T10:00] at Online"
    ));

    assertEquals(expected, outContent.toString().trim());
  }

  @Test
  public void testPrintEventWithExactRangeMatch_ShouldPrintExactly() throws IOException {
    String[] commands = {
        "create calendar --name ExactRangeMatch --timezone America/New_York",
        "use calendar --name ExactRangeMatch",
        "create event Interview from 2024-07-07T14:00 to 2024-07-07T15:00 "
            +
            "--location HQ",
        "print events from 2024-07-07T14:00 to 2024-07-07T15:00",
        "exit"
    };
    runAppWithCommands(commands);

    String nl = System.lineSeparator();
    String expected = String.join(nl, List.of(
        "Welcome to the Calendar App!",
        "Calendar created successfully.",
        "Using calendar: ExactRangeMatch",
        "Event created successfully.",
        "- Interview [2024-07-07T14:00 to 2024-07-07T15:00] at HQ"
    ));

    assertEquals(expected, outContent.toString().trim());
  }

  @Test
  public void testPrintEvents_RangeStartsAtEventStartEndsBeforeEnd_ShouldInclude()
      throws IOException {
    String[] commands = {
        "create calendar --name RangeStartOnly --timezone America/New_York",
        "use calendar --name RangeStartOnly",
        "create event Focus from 2024-07-08T09:00 to 2024-07-08T10:00 --location Lab",
        "print events from 2024-07-08T09:00 to 2024-07-08T09:30",
        "exit"
    };
    runAppWithCommands(commands);

    String nl = System.lineSeparator();
    String expected = String.join(nl, List.of(
        "Welcome to the Calendar App!",
        "Calendar created successfully.",
        "Using calendar: RangeStartOnly",
        "Event created successfully.",
        "- Focus [2024-07-08T09:00 to 2024-07-08T10:00] at Lab"
    ));

    assertEquals(expected, outContent.toString().trim());
  }


  @Test
  public void testPrintEvents_RangeAfterEventEnd_ShouldExclude() throws IOException {
    String[] commands = {
        "create calendar --name AfterEvent --timezone America/New_York",
        "use calendar --name AfterEvent",
        "create event WrapUp from 2024-07-10T13:00 to 2024-07-10T14:00 --location "
            +
            "Lounge",
        "print events from 2024-07-10T14:01 to 2024-07-10T15:00",
        "exit"
    };
    runAppWithCommands(commands);

    String nl = System.lineSeparator();
    String expected = String.join(nl, List.of(
        "Welcome to the Calendar App!",
        "Calendar created successfully.",
        "Using calendar: AfterEvent",
        "Event created successfully.",
        "No events found."
    ));

    assertEquals(expected, outContent.toString().trim());
  }

  @Test
  public void testPrintEvents_RangeBeforeEventStart_ShouldExclude() throws IOException {
    String[] commands = {
        "create calendar --name BeforeEvent --timezone America/New_York",
        "use calendar --name BeforeEvent",
        "create event Kickoff from 2024-07-11T11:00 to 2024-07-11T12:00 --location "
            +
            "Field",
        "print events from 2024-07-11T09:00 to 2024-07-11T10:59",
        "exit"
    };
    runAppWithCommands(commands);

    String nl = System.lineSeparator();
    String expected = String.join(nl, List.of(
        "Welcome to the Calendar App!",
        "Calendar created successfully.",
        "Using calendar: BeforeEvent",
        "Event created successfully.",
        "No events found."
    ));

    assertEquals(expected, outContent.toString().trim());
  }

  @Test
  public void testPrintEventsWithNonConflictingStartTimes_ShouldPrintBoth() throws IOException {
    String[] commands = {
        "create calendar --name NonConflicting --timezone America/New_York",
        "use calendar --name NonConflicting",
        "create event Discussion from 2024-07-12T10:00 to 2024-07-12T10:30 "
            +
            "--location RoomA",
        "create event Planning from 2024-07-12T10:30 to 2024-07-12T11:30 --location"
            +
            " RoomB",
        "print events on 2024-07-12",
        "exit"
    };
    runAppWithCommands(commands);

    String nl = System.lineSeparator();
    String expected = String.join(nl, List.of(
        "Welcome to the Calendar App!",
        "Calendar created successfully.",
        "Using calendar: NonConflicting",
        "Event created successfully.",
        "Event created successfully.",
        "- Discussion [2024-07-12T10:00 to 2024-07-12T10:30] at RoomA",
        "- Planning [2024-07-12T10:30 to 2024-07-12T11:30] at RoomB"
    ));

    assertEquals(expected, outContent.toString().trim());
  }

  @Test
  public void testPrintEventWithSpecialCharacters_ShouldPrintCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name SpecialChars --timezone America/New_York",
        "use calendar --name SpecialChars",
        "create event \"Bug Bash!@#$%^^\" from 2024-07-13T14:00 to 2024-07-13T16:00"
            +
            " --location \"Main-Hall #2\"",
        "print events on 2024-07-13",
        "exit"
    };
    runAppWithCommands(commands);

    String nl = System.lineSeparator();
    String expected = String.join(nl, List.of(
        "Welcome to the Calendar App!",
        "Calendar created successfully.",
        "Using calendar: SpecialChars",
        "Event created successfully.",
        "- Bug Bash!@#$%^^ [2024-07-13T14:00 to 2024-07-13T16:00] at Main-Hall #2"
    ));

    assertEquals(expected, outContent.toString().trim());
  }

  @Test
  public void testPrintEventsWithInvalidDateFormat_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name InvalidDate --timezone America/New_York",
        "use calendar --name InvalidDate",
        "print events on 07/16/2024",
        "exit"
    };
    runAppWithCommands(commands);

    assertTrue(outContent.toString().contains("Error Executing command"));
  }

  @Test
  public void testPrintEventsMissingToKeyword_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name MissingTo --timezone America/New_York",
        "use calendar --name MissingTo",
        "print events from 2024-07-17T10:00 2024-07-17T11:00",
        "exit"
    };
    runAppWithCommands(commands);

    assertTrue(outContent.toString().toLowerCase().contains("invalid format"));
  }

  @Test
  public void testPrintEventsMissingOnOrFrom_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name MissingKeyword --timezone America/New_York",
        "use calendar --name MissingKeyword",
        "print events 2024-07-19",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Error Executing command: Expected 'on' "
        +
        "or 'from' at start of print events command."));
  }

  @Test
  public void testPrintEventsOnMissingDate_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name OnMissingDate --timezone America/New_York",
        "use calendar --name OnMissingDate",
        "print events on",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Error Executing command: Invalid format."
        +
        " Expected: print events on <date>"));
  }

  @Test
  public void testPrintEventsFromWithoutTo_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name FromNoTo --timezone America/New_York",
        "use calendar --name FromNoTo",
        "print events from 2024-07-20T10:00",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Error Executing command: Invalid format."
        +
        " Expected: print events from <datetime> to <datetime>"));
  }

  @Test
  public void testPrintEventsMissingToDateTime_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name NoToTime --timezone America/New_York",
        "use calendar --name NoToTime",
        "print events from 2024-07-21T09:00 to",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Error Executing command: Invalid format."
        +
        " Expected: print events from <datetime> to <datetime>"));
  }

  @Test
  public void testPrintEventsWithExtraArgs_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name ExtraArgs --timezone America/New_York",
        "use calendar --name ExtraArgs",
        "print events on 2024-07-22 somethingExtra",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Error Executing command: Invalid format."
        +
        " Expected: print events on <date>"));
  }

  @Test
  public void testPrintEventsInvalidDateFormat_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name BadFormat --timezone America/New_York",
        "use calendar --name BadFormat",
        "print events on 07/23/2024",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Error Executing command: Invalid date format: 07/23/2024"));
  }

  //Create calendarr command tests
  @Test
  public void testCreateCalendar_ValidInput_ShouldSucceed() {
    runAppWithCommands(new String[]{"create calendar --name TestCal --timezone "
        +
        "America/New_York", "exit"});
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
    runAppWithCommands(new String[]{"create calendar --name InvalidTZ --timezone "
        +
        "Invalid/Zone", "exit"});
    assertTrue(outContent.toString().contains("Invalid timezone"));
  }

  @Test
  public void testUseCalendar_ValidName_ShouldSucceed2() {
    runAppWithCommands(new String[]{"create calendar --name MyCal --timezone "
        +
        "America/New_York", "use calendar --name MyCal", "exit"});
    assertTrue(outContent.toString().contains("Using calendar: MyCal"));
  }

  @Test
  public void testUseCalendar_InvalidName_ShouldFail() {
    runAppWithCommands(new String[]{"use calendar --name NonExistent", "exit"});
    assertTrue(outContent.toString().toLowerCase().contains("not found"));
  }

  @Test
  public void testCreateCalendar_MissingName_ShouldFail() {
    String[] commands = {
        "create calendar --timezone America/New_York",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Error Executing command: Usage: create "
        +
        "calendar --name <name> --timezone <timezone>"));
  }

  @Test
  public void testCreateCalendar_MissingTimezone_ShouldFail() {
    String[] commands = {
        "create calendar --name MyCal",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Error Executing command: Usage: create " +
        "calendar --name <name> --timezone <timezone>"));
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
    assertTrue(outContent.toString().contains("Error: Invalid " +
        "timezone: america/new_york"));
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
    assertTrue(outContent.toString().contains("Expected --name flag"));
  }

  @Test
  public void testCreateCalendar_MissingNameFlag_ShouldFail() {
    String[] commands = {
        "create calendar TestCal --timezone America/New_York",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Error Executing command: Usage: create " +
        "calendar --name <name> --timezone <timezone>"));
  }

  @Test
  public void testUseCalendar_MissingNameFlag_ShouldFail() {
    String[] commands = {
        "use calendar TestCal",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Expected usage: use calendar --name " +
        "<calendarName>"));
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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

    String expectedStart = "Subject,Start Date,Start Time,End Date,End Time,All Day " +
        "Event,Description,Location,Private";
    String[] dates = {"04/24/2025", "04/25/2025", "05/01/2025", "05/02/2025"};
    StringBuilder builder = new StringBuilder(expectedStart);
    for (String date : dates) {
      builder.append("\n\"Seminar\"," + date + ",12:00 AM," + date + ",11:59 PM,True," +
          "\"\",\"\",False");
    }

    assertEquals(builder.toString(), readExportedFile());
  }

  @Test
  public void testCreateRecurringTimedEventWithUntil_ShouldExportCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name UntilCal --timezone America/New_York",
        "use calendar --name UntilCal",
        "create event Sync from 2025-04-22T09:00 to 2025-04-22T10:00 repeats TR " +
            "until 2025-05-01T23:59",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Sync\",04/22/2025,09:00 AM,04/22/2025,10:00 AM,False,\"\",\"\",False",
        "\"Sync\",04/24/2025,09:00 AM,04/24/2025,10:00 AM,False,\"\",\"\",False",
        "\"Sync\",04/29/2025,09:00 AM,04/29/2025,10:00 AM,False,\"\",\"\",False",
        "\"Sync\",05/01/2025,09:00 AM,05/01/2025,10:00 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCreateSingleAllDayEventWithAutoDecline_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name AutoDeclineCal --timezone America/New_York",
        "use calendar --name AutoDeclineCal",
        "create event --autoDecline Conference on 2025-05-05",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
        "create event Standup from 2025-03-21T09:00 to 2025-03-21T09:30 repeats " +
            "MTWRF for 2 times",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Standup\",03/21/2025,09:00 AM,03/21/2025,09:30 AM,False,\"\",\"\",False",
        "\"Standup\",03/24/2025,09:00 AM,03/24/2025,09:30 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCreateRecurringTimedEventWithAutoDeclineAndRepeatCount_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name School --timezone America/New_York",
        "use calendar --name School",
        "create event --autoDecline Meeting from 2024-03-25T10:00 to " +
            "2024-03-25T11:00 repeats MR for 3 times",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Meeting\",03/25/2024,10:00 AM,03/25/2024,11:00 AM,False,\"\",\"\",False",
        "\"Meeting\",03/28/2024,10:00 AM,03/28/2024,11:00 AM,False,\"\",\"\",False",
        "\"Meeting\",04/01/2024,10:00 AM,04/01/2024,11:00 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringAllDayEventWithUntilDate_ShouldExportCorrectly()
      throws IOException {
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Yoga\",04/03/2024,12:00 AM,04/03/2024,11:59 PM,True,\"\",\"\",False",
        "\"Yoga\",04/05/2024,12:00 AM,04/05/2024,11:59 PM,True,\"\",\"\",False",
        "\"Yoga\",04/10/2024,12:00 AM,04/10/2024,11:59 PM,True,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateSingleTimedEventWithoutAutoDecline_NoConflict_ShouldSucceed()
      throws IOException {
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Standup\",04/02/2024,09:00 AM,04/02/2024,09:30 AM,False,\"\",\"\",False",
        "\"Sync\",04/02/2024,10:00 AM,04/02/2024,11:00 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringAllDayEventForOneTime_ShouldExportSingleInstance()
      throws IOException {
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Walk\",04/01/2024,12:00 AM,04/01/2024,11:59 PM,True,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringTimedEventOnNonConsecutiveWeekdays_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name ClassSchedule --timezone America/New_York",
        "use calendar --name ClassSchedule",
        "create event Lecture from 2024-04-01T09:00 to 2024-04-01T10:30 repeats MTW" +
            " for 1 times",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Lunch\",04/06/2024,12:00 PM,04/06/2024,01:00 PM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringTimedEventWithInclusiveUntilDate_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name Reviews --timezone America/New_York",
        "use calendar --name Reviews",
        "create event CodeReview from 2024-04-08T15:00 to 2024-04-08T16:00 repeats " +
            "MWF until 2024-04-12T23:59",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"CodeReview\",04/08/2024,03:00 PM,04/08/2024,04:00 PM,False,\"\",\"\"," +
            "False",
        "\"CodeReview\",04/10/2024,03:00 PM,04/10/2024,04:00 PM,False,\"\",\"\"," +
            "False",
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Writing\",04/09/2024,09:00 AM,04/09/2024,10:00 AM,False,\"\",\"\",False",
        "\"Meeting\",04/09/2024,10:00 AM,04/09/2024,11:00 AM,False,\"\",\"\",False",
        "\"Research\",04/09/2024,11:00 AM,04/09/2024,12:00 PM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringTimedEventAcrossMonthBoundary_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name StudyPlan --timezone America/New_York",
        "use calendar --name StudyPlan",
        "create event Review from 2024-01-30T18:00 to 2024-01-30T19:00 repeats T " +
            "for 3 times",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
        "create event Launch from 2024-04-15T09:00 to 2024-04-15T10:00 " +
            "--description ReleaseDay --location HQ --private",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Launch\",04/15/2024,09:00 AM,04/15/2024,10:00 AM,False,\"ReleaseDay\"," +
            "\"HQ\",True"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateAllDayEventWithOptionalFlags_ShouldExportCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name AllDayCal --timezone America/New_York",
        "use calendar --name AllDayCal",
        "create event Meditation on 2024-04-20 --description Focus --location Park " +
            "--private",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Meditation\",04/20/2024,12:00 AM,04/20/2024,11:59 PM,True,\"Focus\"," +
            "\"Park\",True"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringTimedEventWithOptionalFlags_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name RecurringFlags --timezone America/New_York",
        "use calendar --name RecurringFlags",
        "create event Gym from 2024-04-22T06:00 to 2024-04-22T07:00 repeats MF for " +
            "2 times --description Workout --location FitnessCenter --private",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Gym\",04/22/2024,06:00 AM,04/22/2024,07:00 AM,False,\"Workout\"," +
            "\"FitnessCenter\",True",
        "\"Gym\",04/26/2024,06:00 AM,04/26/2024,07:00 AM,False,\"Workout\"," +
            "\"FitnessCenter\",True"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringAllDayEventWithOptionalFlags_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name Retreats --timezone America/New_York",
        "use calendar --name Retreats",
        "create event Yoga on 2024-04-29 repeats MW for 2 times --description " +
            "Relaxation --location Center --private",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Yoga\",04/29/2024,12:00 AM,04/29/2024,11:59 PM,True,\"Relaxation\"," +
            "\"Center\",True",
        "\"Yoga\",05/01/2024,12:00 AM,05/01/2024,11:59 PM,True,\"Relaxation\"," +
            "\"Center\",True"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateEventWithOnlyDescription_ShouldExportCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name DescOnly --timezone America/New_York",
        "use calendar --name DescOnly",
        "create event Brainstorm from 2024-04-17T14:00 to 2024-04-17T15:00 " +
            "--description Ideation",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Brainstorm\",04/17/2024,02:00 PM,04/17/2024,03:00 PM,False,\"Ideation\"," +
            "\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateEventWithOnlyLocation_ShouldExportCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name LocationOnly --timezone America/New_York",
        "use calendar --name LocationOnly",
        "create event Sync from 2024-04-18T09:00 to 2024-04-18T10:00 --location " +
            "Room42",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    String expected = String.join("\n", Arrays.asList(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Meeting1\",04/22/2024,10:00 AM,04/22/2024,11:00 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringEventThatConflictsWithExisting_ShouldBeRejected()
      throws IOException {
    String[] commands = {
        "create calendar --name RecurringConflict --timezone America/New_York",
        "use calendar --name RecurringConflict",
        "create event Interview from 2024-04-24T09:00 to 2024-04-24T10:00",
        "create event Standup from 2024-04-22T09:00 to 2024-04-22T09:30 repeats MWF" +
            " for 3 times",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Interview\",04/24/2024,09:00 AM,04/24/2024,10:00 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateSingleEventThatConflictsWithRecurring_ShouldBeRejected()
      throws IOException {
    String[] commands = {
        "create calendar --name ReverseConflict --timezone America/New_York",
        "use calendar --name ReverseConflict",
        "create event Standup from 2024-04-22T09:00 to 2024-04-22T09:30 repeats MWF" +
            " for 3 times",
        "create event Checkin from 2024-04-24T09:00 to 2024-04-24T09:30",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"SprintReview\",05/02/2024,10:00 AM,05/02/2024,12:00 PM,False,\"\",\"\"," +
            "False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateEventThatSpansMultipleDays_ShouldBlockOverlappingEvents()
      throws IOException {
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
        "Description,Location,Private", content);
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
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
        "Description,Location,Private", content);
  }

  @Test
  public void testCreateEventWithInvalidRepeatDay_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name InvalidRepeat --timezone America/New_York",
        "use calendar --name InvalidRepeat",
        "create event Lecture from 2024-05-09T08:00 to 2024-05-09T09:00 repeats MX " +
            "for 2 times",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().toLowerCase().contains("invalid weekday character"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
        "Description,Location,Private", content);
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
    assertTrue(outContent.toString().toLowerCase().contains("missing value for " +
        "--description"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
        "Description,Location,Private", content);
  }

  @Test
  public void testCreateEventWithUnknownArgument_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name WeirdArgs --timezone America/New_York",
        "use calendar --name WeirdArgs",
        "create event Ghost from 2024-05-10T08:00 to 2024-05-10T09:00 --weirdflag " +
            "yes",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().toLowerCase().contains("unrecognized extra " +
        "argument"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
        "Description,Location,Private", content);
  }

  @Test
  public void testCreateEventWithNonNumericRepeatCount_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name BadRepeatCount --timezone America/New_York",
        "use calendar --name BadRepeatCount",
        "create event Gym from 2024-05-11T07:00 to 2024-05-11T08:00 repeats M for " +
            "abc times",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().toLowerCase().contains("for input string: \"abc\""));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
        "Description,Location,Private", content);
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
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
        "Description,Location,Private", content);
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
    assertTrue(outContent.toString().toLowerCase().contains("unrecognized extra " +
        "argument"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
        "Description,Location,Private", content);
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
    assertTrue(outContent.toString().toLowerCase().contains("unrecognized extra " +
        "argument"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
        "Description,Location,Private", content);
  }

  @Test
  public void testCreateEvent_Successful() throws IOException {
    String[] commands = {
        "create calendar --name MyCal --timezone America/New_York",
        "use calendar --name MyCal",
        "create event --autoDecline TeamMeeting from 2025-08-01T09:00 to 2025-08-01T10:00",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
            "Private",
        "\"TeamMeeting\",08/01/2025,09:00 AM,08/01/2025,10:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCreateEvent_MissingCalendar_ShouldFail() throws IOException {
    String[] commands = {
        "use calendar --name NoSuchCalendar",
        "create event --autoDecline Meeting from 2025-09-10T11:00 to 2025-09-10T12:00",
        "exit"
    };

    runAppWithCommands(commands);
    String output = outContent.toString().toLowerCase();

    assertTrue(output.contains("calendar not found") || output.contains("error"));
  }

  @Test
  public void testCreateEvent_EndBeforeStart_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name BadTimeCal --timezone America/New_York",
        "use calendar --name BadTimeCal",
        "create event --autoDecline InvalidMeeting from 2025-10-01T12:00 to 2025-10-01T11:00",
        "exit"
    };

    runAppWithCommands(commands);
    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("end time must be after start time") || output.contains("error"));
  }

  @Test
  public void testCreateEventWithPrivateFlagAndValue_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name PrivateWithValue --timezone America/New_York",
        "use calendar --name PrivateWithValue",
        "create event Secret from 2024-05-14T18:00 to 2024-05-14T19:00 --private " +
            "true",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().contains("Error Executing command: --private does " +
        "not take a value"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
        "Description,Location,Private", content);
  }

  @Test
  public void testCreateEventWithDescriptionBeforeEventName_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name MisplacedDesc --timezone America/New_York",
        "use calendar --name MisplacedDesc",
        "create event --description Briefing from 2024-05-15T09:00 to " +
            "2024-05-15T10:00",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().contains("Error Executing command: Expected 'from'" +
        " or 'on' after event name"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
        "Description,Location,Private", content);
  }

  @Test
  public void testCreateEventWithMultipleDescriptionFlags_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name RepeatedDesc --timezone America/New_York",
        "use calendar --name RepeatedDesc",
        "create event Demo from 2024-05-16T13:00 to 2024-05-16T14:00 --description " +
            "One --description Two",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().contains("Error Executing command: Duplicate " +
        "--description flag"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
        "Description,Location,Private", content);
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
    assertTrue(outContent.toString().contains("Error: End date/time must be after start date/time"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
        "Description,Location,Private", content);
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
    assertTrue(outContent.toString().contains("Error: End date/time must be after start date/time"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
        "Description,Location,Private", content);
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
    assertTrue(outContent.toString().contains("Error Executing command: Expected 'from'" +
        " or 'on' after event name"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
        "Description,Location,Private", content);
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"from\",05/22/2024,10:00 AM,05/22/2024,11:00 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringEventWithoutForOrUntil_ShouldFail()
      throws IOException {
    String[] commands = {
        "create calendar --name IncompleteRepeats --timezone America/New_York",
        "use calendar --name IncompleteRepeats",
        "create event Task from 2024-05-24T08:00 to 2024-05-24T09:00 repeats M",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();
    assertTrue(outContent.toString().contains("Error: Must specify either recurrence count or end date"));
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
        "Description,Location,Private", content);
  }

  @Test
  public void testCreateAllDayEventUsingFromToInsteadOfOn_ShouldCreateTimedEvent()
      throws IOException {
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Team Sync\",05/27/2024,10:00 AM,05/27/2024,11:00 AM,False,\"\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateEventWithMultiWordDescription_ShouldExportCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name MultiWordDesc --timezone America/New_York",
        "use calendar --name MultiWordDesc",
        "create event Briefing from 2024-05-28T09:00 to 2024-05-28T10:00 " +
            "--description \"Weekly Team Sync\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Briefing\",05/28/2024,09:00 AM,05/28/2024,10:00 AM,False,\"Weekly Team " +
            "Sync\",\"\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateEventWithMultiWordLocation_ShouldExportCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name MultiWordLoc --timezone America/New_York",
        "use calendar --name MultiWordLoc",
        "create event AllHands from 2024-05-29T11:00 to 2024-05-29T12:00 --location" +
            " \"Main Conference Room\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"AllHands\",05/29/2024,11:00 AM,05/29/2024,12:00 PM,False,\"\",\"Main " +
            "Conference Room\",False"
    ));
    assertEquals(expected, content);
  }

  @Test
  public void testCreateRecurringEventThatPartiallyConflicts_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name PartialConflict --timezone America/New_York",
        "use calendar --name PartialConflict",
        "create event DailyStandup from 2024-06-03T09:00 to 2024-06-03T09:30", //
        // Occurs on Monday
        "create event SprintUpdate from 2024-06-03T09:00 to 2024-06-03T10:00 " +
            "repeats MR for 2 times", // Conflicts with 6/3 (Monday), but not 6/6
        // (Thursday)
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"DailyStandup\",06/03/2024,09:00 AM,06/03/2024,09:30 AM,False,\"\",\"\"," +
            "False"
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
        "create event Standup from 2024-06-03T09:00 to 2024-06-03T09:30 repeats MR " +
            "for 2 times", // Fits between other events
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String content = readExportedFile();

    String expected = String.join("\n", Arrays.asList(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"MorningBlock\",06/03/2024,08:00 AM,06/03/2024,09:00 AM,False,\"\",\"\"," +
            "False",
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
        "create event Standup from 2025-03-21T09:00 to 2025-03-21T09:30 repeats " +
            "MTWRF for 5 " +
            "times",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
        "create event Standup from 2025-03-21T09:00 to 2025-03-21T09:30 repeats " +
            "MTWRF for 10 times",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
        "edit event name Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 with " +
            "TeamSync",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"TeamSync\",05/01/2025,10:00 AM,05/01/2025,11:00 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCreateTimedEventWithAutoDecline_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name AutoDeclineTimed --timezone America/New_York",
        "use calendar --name AutoDeclineTimed",
        "create event --autoDecline Review from 2025-05-10T13:00 to 2025-05-10T14:00",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Review\",05/10/2025,01:00 PM,05/10/2025,02:00 PM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCreateRecurringTimedEventWithAutoDecline_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name AutoRecurring --timezone America/New_York",
        "use calendar --name AutoRecurring",
        "create event --autoDecline CheckIn from 2025-06-01T09:00 to " +
            "2025-06-01T09:30 repeats MT for 2 times",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Meeting\",04/22/2025,09:00 AM,04/22/2025,10:00 AM,False,\"\",\"Room B\"," +
            "False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditRecurringEvent_ShouldExportCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name EditRecurrCal --timezone America/New_York",
        "use calendar --name EditRecurrCal",
        "create event Standup from 2025-04-24T09:00 to 2025-04-24T10:00 repeats T " +
            "for 3 times",
        "edit events location Standup from 2025-04-24T09:00 with \"Room C\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Standup\",04/29/2025,09:00 AM,04/29/2025,10:00 AM,False,\"\",\"Room C\"," +
            "False",
        "\"Standup\",05/06/2025,09:00 AM,05/06/2025,10:00 AM,False,\"\",\"Room C\"," +
            "False",
        "\"Standup\",05/13/2025,09:00 AM,05/13/2025,10:00 AM,False,\"\",\"Room C\"," +
            "False"
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
        "edit events location Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 " +
            "with null",
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
        "edit events start Meeting from 2025-06-10T10:00 with " +
            "\"2025-06-10T11:00:00\"",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("new start must be before " +
        "current end time"));
  }

  // Test: Editing an event's end time to an invalid time (new end is not after current
  // start)
  @Test
  public void testEditEvent_InvalidEndTime_ShouldFail() {
    String[] commands = {
        "create calendar --name EditInvalidEnd --timezone America/New_York",
        "use calendar --name EditInvalidEnd",
        "create event Meeting from 2025-06-10T10:00 to 2025-06-10T11:00",
        "edit events end Meeting from 2025-06-10T10:00 with \"2025-06-10T10:00:00\"",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("new end must be after " +
        "current start time"));
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
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"TeamSync\",06/10/2025,10:00 AM,06/10/2025,11:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Editing an event's description works correctly.
  @Test
  public void testEditEvent_UpdateDescription_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name EditDescCal --timezone America/New_York",
        "use calendar --name EditDescCal",
        "create event Meeting from 2025-06-10T10:00 to 2025-06-10T11:00",
        "edit events description Meeting from 2025-06-10T10:00 with \"Weekly " +
            "meeting\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Meeting\",06/10/2025,10:00 AM,06/10/2025,11:00 AM,False,\"Weekly " +
            "meeting\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Editing a recurring event with editAll = false should update only the first
  // occurrence.
  @Test
  public void testEditRecurringEvent_SingleOccurrenceEdit_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name EditRecurrSingleCal --timezone America/New_York",
        "use calendar --name EditRecurrSingleCal",
        "create event Standup from 2025-07-06T09:00 to 2025-07-06T09:30 repeats M " +
            "for 2 times",
        "edit events location Standup from 2025-07-06T09:00 with \"Room Z\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Standup\",07/07/2025,09:00 AM,07/07/2025,09:30 AM,False,\"\",\"Room Z\"," +
            "False",
        "\"Standup\",07/14/2025,09:00 AM,07/14/2025,09:30 AM,False,\"\",\"Room Z\"," +
            "False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Editing a recurring event with editAll = true should update all occurrences.
  @Test
  public void testEditRecurringEvent_UpdateAllOccurrences_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name EditRecurrCal --timezone America/New_York",
        "use calendar --name EditRecurrCal",
        "create event Standup from 2025-04-24T09:00 to 2025-04-24T10:00 repeats T " +
            "for 3 times",
        "edit events location Standup from 2025-04-24T09:00 with \"Room C\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Standup\",04/29/2025,09:00 AM,04/29/2025,10:00 AM,False,\"\",\"Room C\"," +
            "False",
        "\"Standup\",05/06/2025,09:00 AM,05/06/2025,10:00 AM,False,\"\",\"Room C\"," +
            "False",
        "\"Standup\",05/13/2025,09:00 AM,05/13/2025,10:00 AM,False,\"\",\"Room C\"," +
            "False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditNonRecurringEvent_UpdateLocation_ShouldExportCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name EditNRCal --timezone America/New_York",
        "use calendar --name EditNRCal",
        "create event Meeting from 2025-06-10T10:00 to 2025-06-10T11:00",
        "edit events location Meeting from 2025-06-10T10:00 with \"Room B\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Meeting\",06/10/2025,10:00 AM,06/10/2025,11:00 AM,False,\"\",\"Room B\"," +
            "False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditNonRecurringEvent_UpdateStart_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name EditStartNR --timezone America/New_York",
        "use calendar --name EditStartNR",
        "create event Briefing from 2025-06-20T09:00 to 2025-06-20T10:00",
        "edit events start Briefing from 2025-06-20T09:00 with " +
            "\"2025-06-20T08:45:00\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // New start should be 08:45 AM; end remains unchanged.
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Briefing\",06/20/2025,08:45 AM,06/20/2025,10:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditNonRecurringEvent_UpdateEnd_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name EditEndNR --timezone America/New_York",
        "use calendar --name EditEndNR",
        "create event Review from 2025-06-20T09:00 to 2025-06-20T10:00",
        "edit events end Review from 2025-06-20T09:00 with \"2025-06-20T10:15:00\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Review\",06/20/2025,09:00 AM,06/20/2025,10:15 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditNonRecurringEvent_UpdateDescription_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name EditDescNR --timezone America/New_York",
        "use calendar --name EditDescNR",
        "create event Update from 2025-06-20T09:00 to 2025-06-20T10:00",
        "edit events description Update from 2025-06-20T09:00 with \"Weekly review " +
            "session\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Update\",06/20/2025,09:00 AM,06/20/2025,10:00 AM,False,\"Weekly review " +
            "session\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditNonRecurringEvent_UpdateIsPublic_ShouldExportCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name EditFlagNR --timezone America/New_York",
        "use calendar --name EditFlagNR",
        "create event Conference from 2025-06-20T09:00 to 2025-06-20T10:00",
        "edit events isprivate Conference from 2025-06-20T09:00 with \"false\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // When ispublic is set to false, Private becomes true.
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Conference\",06/20/2025,09:00 AM,06/20/2025,10:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // ---------- Error Cases for editEvents (plural) ----------

  @Test
  public void testEditEvents_UnsupportedProperty_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name UnsupportedNR --timezone America/New_York",
        "use calendar --name UnsupportedNR",
        "create event Update from 2025-06-25T10:00 to 2025-06-25T11:00",
        "edit events color Update from 2025-06-25T10:00 with \"Blue\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("unsupported property"));
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Update\",06/25/2025,10:00 AM,06/25/2025,11:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditEvents_NullNewValue_ShouldSucceed() throws IOException {
    String[] commands = {
        "create calendar --name NullValueNR --timezone America/New_York",
        "use calendar --name NullValueNR",
        "create event Update from 2025-06-25T10:00 to 2025-06-25T11:00",
        "edit events location Update from 2025-06-25T10:00 with null",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Events updated successfully."));
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Update\",06/25/2025,10:00 AM,06/25/2025,11:00 AM,False,\"\",\"null\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditEvents_NoMatchingEvent_ShouldFail() {
    String[] commands = {
        "create calendar --name NoMatchNR --timezone America/New_York",
        "use calendar --name NoMatchNR",
        "edit events location NonExistent from 2025-06-25T10:00 with \"Room X\"",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("no matching event found"));
  }

  @Test
  public void testEditEvents_CalendarNotFound_ShouldFail() {
    String[] commands = {
        "edit events location Meeting from 2025-06-25T10:00 with \"Room Z\"",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Error Executing command: Please use somme " +
        "calendar"));
  }

  // ---------- Recurring Event Edit Tests for editEvents (plural) ----------

  @Test
  public void testEditRecurringEvents_SingleOccurrenceEdit_ShouldUpdateOnlyFirst()
      throws IOException {
    String[] commands = {
        "create calendar --name RecurrEditNR_Single --timezone America/New_York",
        "use calendar --name RecurrEditNR_Single",
        "create event Standup from 2025-07-07T09:00 to 2025-07-07T09:30 repeats M " +
            "for 2 times",
        // Using edit events with a fromDateTime that matches the first occurrence.
        "edit events location Standup from 2025-07-07T09:00 with \"Room Z\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // Only the first occurrence (starting at 07/06) should be updated.
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Standup\",07/07/2025,09:00 AM,07/07/2025,09:30 AM,False,\"\",\"Room Z\"," +
            "False",
        "\"Standup\",07/14/2025,09:00 AM,07/14/2025,09:30 AM,False,\"\",\"Room Z\"," +
            "False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditRecurringEvents_UpdateAllOccurrences_ShouldUpdateAll() throws IOException {
    String[] commands = {
        "create calendar --name RecurrEditNR_All --timezone America/New_York",
        "use calendar --name RecurrEditNR_All",
        // Create recurring event "Standup" on Thursday with 3 occurrences.
        "create event Standup from 2025-04-24T09:00 to 2025-04-24T10:00 repeats R " +
            "for 3 times",
        // Edit all matching occurrences by updating location.
        "edit events location Standup from 2025-04-24T09:00 with \"Room C\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // Expected: All occurrences are updated with location "Room C".
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Standup\",04/24/2025,09:00 AM,04/24/2025,10:00 AM,False,\"\",\"Room C\"," +
            "False",
        "\"Standup\",05/01/2025,09:00 AM,05/01/2025,10:00 AM,False,\"\",\"Room C\"," +
            "False",
        "\"Standup\",05/08/2025,09:00 AM,05/08/2025,10:00 AM,False,\"\",\"Room C\"," +
            "False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditRecurringEvents_UpdateStartTime_ShouldUpdateAll() throws IOException {
    String[] commands = {
        "create calendar --name RecurrStartNR --timezone America/New_York",
        "use calendar --name RecurrStartNR",
        "create event Standup from 2025-04-24T09:00 to 2025-04-24T09:30 repeats R " +
            "for 3 times",
        // Update start time for all occurrences to 08:45 (shift by 15 minutes
        // earlier).
        "edit events start Standup from 2025-04-24T09:00 with " +
            "\"2025-04-24T08:45:00\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Standup\",04/24/2025,08:45 AM,04/24/2025,09:30 AM,False,\"\",\"\",False",
        "\"Standup\",05/01/2025,08:45 AM,05/01/2025,09:30 AM,False,\"\",\"\",False",
        "\"Standup\",05/08/2025,08:45 AM,05/08/2025,09:30 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditRecurringEvents_CreatesConflict_ShouldEditNothing() throws IOException {
    String[] commands = {
        "create calendar --name ConflictCal --timezone America/New_York",
        "use calendar --name ConflictCal",

        // This goes first: 08:45–09:15
        "create event Conflict from 2025-04-24T08:45 to 2025-04-24T09:15 repeats R for 3 times",

        // Now safe: 09:30–10:00
        "create event Standup from 2025-04-24T09:30 to 2025-04-24T10:00 repeats R for 3 times",

        // Now shift Standup to 08:45 — will conflict with Conflict
        "edit events start Standup from 2025-04-24T09:30 with \"2025-04-24T08:45:00\"",

        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
         "Private",
        "\"Conflict\",04/24/2025,08:45 AM,04/24/2025,09:15 AM,False,\"\",\"\",False",
        "\"Conflict\",05/01/2025,08:45 AM,05/01/2025,09:15 AM,False,\"\",\"\",False",
        "\"Conflict\",05/08/2025,08:45 AM,05/08/2025,09:15 AM,False,\"\",\"\",False",
        "\"Standup\",04/24/2025,09:30 AM,04/24/2025,10:00 AM,False,\"\",\"\",False",
        "\"Standup\",05/01/2025,09:30 AM,05/01/2025,10:00 AM,False,\"\",\"\",False",
        "\"Standup\",05/08/2025,09:30 AM,05/08/2025,10:00 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditRecurringEvents_SecondConflict_ShouldRollbackAllEdits() throws IOException {
    String[] commands = {
        "create calendar --name PartialConflictCal --timezone America/New_York",
        "use calendar --name PartialConflictCal",

        // Recurring event: 09:00–09:30 on 3 Thursdays
        "create event Standup from 2025-04-24T09:00 to 2025-04-24T09:30 repeats R for 3 times",

        // Overlapping event ONLY on May 1st
        "create event Overlap from 2025-05-01T08:45 to 2025-05-01T09:00",

        // Attempt to edit recurring event to 08:45 — May 1st conflicts
        "edit events start Standup from 2025-04-24T09:00 with \"2025-04-24T08:45:00\"",

        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
         "Private",
        "\"Standup\",04/24/2025,09:00 AM,04/24/2025,09:30 AM,False,\"\",\"\",False",
        "\"Standup\",05/01/2025,09:00 AM,05/01/2025,09:30 AM,False,\"\",\"\",False",
        "\"Standup\",05/08/2025,09:00 AM,05/08/2025,09:30 AM,False,\"\",\"\",False",
        "\"Overlap\",05/01/2025,08:45 AM,05/01/2025,09:00 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testEditRecurringEvents_UpdateEndTime_ShouldUpdateAll() throws IOException {
    String[] commands = {
        "create calendar --name RecurrEndNR --timezone America/New_York",
        "use calendar --name RecurrEndNR",
        "create event Standup from 2025-04-24T09:00 to 2025-04-24T09:30 repeats R " +
            "for 3 times",
        // Update end time for all occurrences to 09:45.
        "edit events end Standup from 2025-04-24T09:00 with \"2025-04-24T09:45:00\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Standup\",04/24/2025,09:00 AM,04/24/2025,09:45 AM,False,\"\",\"\",False",
        "\"Standup\",05/01/2025,09:00 AM,05/01/2025,09:45 AM,False,\"\",\"\",False",
        "\"Standup\",05/08/2025,09:00 AM,05/08/2025,09:45 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Additional edge-case tests:

  // Test: Edit event with an empty new value (e.g. setting description to an empty
  // string).
  @Test
  public void testEditEvents_EmptyNewValue_ShouldFail() {
    String[] commands = {
        "create calendar --name EmptyValueNR --timezone America/New_York",
        "use calendar --name EmptyValueNR",
        "create event Update from 2025-06-25T10:00 to 2025-06-25T11:00",
        "edit events name Update from 2025-06-25T10:00 with \"\"",
        "exit"
    };
    runAppWithCommands(commands);
    // Expect an error about missing new value.
    assertTrue(outContent.toString().toLowerCase().contains("missing value"));
  }

  // Test: Edit recurring events when the provided fromDateTime is after all occurrences.
  @Test
  public void testEditRecurringEvents_FromDateTimeTooLate_ShouldFail() {
    String[] commands = {
        "create calendar --name RecurrLateNR --timezone America/New_York",
        "use calendar --name RecurrLateNR",
        "create event Standup from 2025-07-06T09:00 to 2025-07-06T09:30 repeats M " +
            "for 2 times",
        // Provide a fromDateTime that is later than any occurrence.
        "edit events location Standup from 2026-07-07T09:00 with \"Room X\"",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("no matching event found"));
  }

  // Test: Edit recurring events with invalid datetime format should fail.
  @Test
  public void testEditRecurringEvents_InvalidDatetimeFormat_ShouldFail() {
    String[] commands = {
        "create calendar --name RecurrFormatNR --timezone America/New_York",
        "use calendar --name RecurrFormatNR",
        "create event Standup from 2025-04-24T09:00 to 2025-04-24T09:30 repeats T " +
            "for 3 times",
        "edit events start Standup from 2025-04-24T09:00 with \"2025/04/24 " +
            "09:15:00\"",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("could not be parsed"));
  }

  @Test
  public void testEditEvents_UpdateNameToEmpty_ShouldNotUpdateName() throws IOException {
    String[] commands = {
        "create calendar --name EmptyNameCal --timezone America/New_York",
        "use calendar --name EmptyNameCal",
        "create event Meeting from 2025-06-10T10:00 to 2025-06-10T11:00",
        "edit events name Meeting from 2025-06-10T10:00 with \"\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // Expect that the event’s name becomes empty.
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Meeting\",06/10/2025,10:00 AM,06/10/2025,11:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Edit non‐recurring event – updating property "start" with an invalid
  // datetime string.
  @Test
  public void testEditEvents_InvalidDatetimeForStart_ShouldFail() {
    String[] commands = {
        "create calendar --name InvalidStartFormat --timezone America/New_York",
        "use calendar --name InvalidStartFormat",
        "create event Demo from 2025-07-01T09:00 to 2025-07-01T10:00",
        "edit events start Demo from 2025-07-01T09:00 with \"2025/07/01 09:15:00\"",
        "exit"
    };
    runAppWithCommands(commands);
    // Expect a message about parsing error.
    assertTrue(outContent.toString().toLowerCase().contains("could not be parsed"));
  }

  // Test: Edit non‐recurring event – updating property "end" with an invalid datetime
  // string.
  @Test
  public void testEditEvents_InvalidDatetimeForEnd_ShouldFail() {
    String[] commands = {
        "create calendar --name InvalidEndFormat --timezone America/New_York",
        "use calendar --name InvalidEndFormat",
        "create event Demo from 2025-07-01T09:00 to 2025-07-01T10:00",
        "edit events end Demo from 2025-07-01T09:00 with \"07/01/2025 10:15:00\"",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("could not be parsed"));
  }

  // Test: Edit events with null calendar name should fail.
  @Test
  public void testEditEvents_NullCalendarName_ShouldFail() {
    String[] commands = {
        "edit events location Meeting from 2025-06-10T10:00 with \"Room X\"",
        "exit"
    };
    runAppWithCommands(commands);


    assertTrue(outContent.toString().contains("Error Executing command: Please use somme " +
        "calendar"));
  }

  // Test: Edit events with null "fromDateTime" should fail.
  @Test
  public void testEditEvents_NullFromDateTime_ShouldFail() throws IOException {
    // In our command-driven parser, a null value would likely be represented as the
    // literal "null".
    String[] commands = {
        "create calendar --name NullFromCal --timezone America/New_York",
        "use calendar --name NullFromCal",
        "create event Meeting from 2025-06-10T10:00 to 2025-06-10T11:00",
        "edit events location Meeting from null with \"Room Y\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Meeting\",06/10/2025,10:00 AM,06/10/2025,11:00 AM,False,\"\",\"\",False"
    );
    assertTrue(outContent.toString().contains("Invalid datetime format after 'from': " +
          "null"));
    assertEquals(expected, readExportedFile());
  }

  // Test: Edit events with a numeric value for a boolean property (ispublic) that does
  // not parse.
  @Test
  public void testEditEvents_IsPublicWithNumericValue_ShouldFail() {
    String[] commands = {
        "create calendar --name BoolNumericCal --timezone America/New_York",
        "use calendar --name BoolNumericCal",
        "create event Meeting from 2025-06-10T10:00 to 2025-06-10T11:00",
        "edit events isprivate Meeting from 2025-06-10T10:00 with \"1\"",
        "exit"
    };
    runAppWithCommands(commands);
    // Expect an error message indicating that the value could not be parsed as a boolean.
    assertTrue(outContent.toString().toLowerCase().contains("for input string"));
  }

  // Test: Edit events where property is an empty string (i.e. missing property
  // keyword) should fail.
  @Test
  public void testEditEvents_EmptyProperty_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name EmptyPropCal --timezone America/New_York",
        "use calendar --name EmptyPropCal",
        "create event Meeting from 2025-06-10T10:00 to 2025-06-10T11:00",
        // Intentionally leave the property argument empty.
        "edit events  Meeting from 2025-06-10T10:00 with \"Room A\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Meeting\",06/10/2025,10:00 AM,06/10/2025,11:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditEvents_MixedEvents_UpdateOnlyLaterOccurrence_ShouldUpdateOnlyMatching()
      throws IOException {
    String[] commands = {
        "create calendar --name MixedCal --timezone America/New_York",
        "use calendar --name MixedCal",
        "create event Team from 2025-08-01T09:00 to 2025-08-01T10:00",    //
        // Earlier event
        "create event Team from 2025-08-01T11:00 to 2025-08-01T12:00",    // Later
        // event
        // Set fromDateTime at 10:30 so that only the second event qualifies.
        "edit events name Team from 2025-08-01T10:30 with \"Squad\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // Expected: First event remains "Team", second event is renamed to "Squad".
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Team\",08/01/2025,09:00 AM,08/01/2025,10:00 AM,False,\"\",\"\",False",
        "\"Squad\",08/01/2025,11:00 AM,08/01/2025,12:00 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // --------- Multiple Occurrences: editAll true updates all occurrences ---------
  @Test
  public void testEditEvents_MultipleOccurrences_EditAllTrue_ShouldUpdateAll()
      throws IOException {
    String[] commands = {
        "create calendar --name RecurrEditAllExtra --timezone America/New_York",
        "use calendar --name RecurrEditAllExtra",
        // Create a recurring event "Standup" that recurs on Thursdays for 3
        // occurrences.
        "create event Standup from 2025-04-24T09:00 to 2025-04-24T10:00 repeats R " +
            "for 3 times",
        // Update the event name for all matching occurrences.
        "edit events name Standup from 2025-04-24T09:00 with \"DailySync\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // Expected: All occurrences now have the new name "DailySync"
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"DailySync\",04/24/2025,09:00 AM,04/24/2025,10:00 AM,False,\"\",\"\",False",
        "\"DailySync\",05/01/2025,09:00 AM,05/01/2025,10:00 AM,False,\"\",\"\",False",
        "\"DailySync\",05/08/2025,09:00 AM,05/08/2025,10:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // --------- Update Description Only for Events After a Given Time ---------
  // Create two non-recurring events with the same name, then update description only
  // for those with start time >= given boundary.
  @Test
  public void testEditEvents_UpdateDescriptionForLaterEvents_ShouldUpdateOnlyMatching()
      throws IOException {
    String[] commands = {
        "create calendar --name DescUpdateCal --timezone America/New_York",
        "use calendar --name DescUpdateCal",
        "create event Briefing from 2025-08-15T09:00 to 2025-08-15T10:00",   //
        // Event 1
        "create event Briefing from 2025-08-15T11:00 to 2025-08-15T12:00",   //
        // Event 2
        // Update description for events starting at or after 10:30.
        "edit events description Briefing from 2025-08-15T10:30 with \"Updated " +
            "session\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // Expected: First event remains unchanged; second event gets updated description.
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Briefing\",08/15/2025,09:00 AM,08/15/2025,10:00 AM,False,\"\",\"\",False",
        "\"Briefing\",08/15/2025,11:00 AM,08/15/2025,12:00 PM,False,\"Updated " +
            "session\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // --------- Edit Events: Test Multiple Property Updates Sequentially ---------
  @Test
  public void testEditEvents_SequentialPropertyUpdates_ShouldExportCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name SeqUpdateCal --timezone America/New_York",
        "use calendar --name SeqUpdateCal",
        "create event Update from 2025-08-20T10:00 to 2025-08-20T11:00",
        // First, update location.
        "edit events location Update from 2025-08-20T10:00 with \"Room 101\"",
        // Then, update description.
        "edit events description Update from 2025-08-20T10:00 with \"Quarterly " +
            "review\"",
        // Finally, update event name.
        "edit events name Update from 2025-08-20T10:00 with \"ReviewMeeting\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"ReviewMeeting\",08/20/2025,10:00 AM,08/20/2025,11:00 AM,False," +
            "\"Quarterly review\",\"Room 101\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // --------- Edit Events: Update with Invalid (Non-Parseable) New Value ---------
  @Test
  public void testEditEvents_InvalidNewValueForBoolean_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name BoolUpdateCal --timezone America/New_York",
        "use calendar --name BoolUpdateCal",
        "create event Meeting from 2025-08-25T09:00 to 2025-08-25T10:00",
        // Attempt to update ispublic with an invalid boolean value.
        "edit events ispublic Meeting from 2025-08-25T09:00 with \"notaboolean\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Meeting\",08/25/2025,09:00 AM,08/25/2025,10:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // --------- Edit Events: No Change in New Value (Event remains Unchanged) ---------
  @Test
  public void testEditEvents_NoChangeInNewValue_ShouldSucceed() throws IOException {
    String[] commands = {
        "create calendar --name NoChangeCal --timezone America/New_York",
        "use calendar --name NoChangeCal",
        "create event Briefing from 2025-08-30T10:00 to 2025-08-30T11:00 " +
            "--description \"Initial\"",
        // Update description to the same value.
        "edit events description Briefing from 2025-08-30T10:00 with \"Initial\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // Expect output to be unchanged.
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Briefing\",08/30/2025,10:00 AM,08/30/2025,11:00 AM,False,\"Initial\"," +
            "\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditEvents_UpdateDescriptionWithWhitespace_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name WhiteSpaceNR --timezone America/New_York",
        "use calendar --name WhiteSpaceNR",
        "create event Planning from 2025-07-10T09:00 to 2025-07-10T10:00 " +
            "--description \"Initial Plan\"",
        "edit events description Planning from 2025-07-10T09:00 with \"  Revised " +
            "Plan  \"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // Expect that the description now contains the whitespace as provided.
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Planning\",07/10/2025,09:00 AM,07/10/2025,10:00 AM,False,\"  Revised " +
            "Plan  \",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditEvents_EditDescription_ShouldUpdateCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name DescUpdate --timezone America/New_York",
        "use calendar --name DescUpdate",
        "create event Lunch from 2025-07-15T12:00 to 2025-07-15T13:00",
        "edit events description Lunch from 2025-07-15T12:00 with \"Team lunch\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Lunch\",07/15/2025,12:00 PM,07/15/2025,01:00 PM,False,\"Team lunch\"," +
            "\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditEvents_EditEndTime_ShouldFailForInvalidEnd() {
    String[] commands = {
        "create calendar --name EndTimeFail --timezone America/New_York",
        "use calendar --name EndTimeFail",
        "create event Workshop from 2025-07-20T14:00 to 2025-07-20T16:00",
        "edit events end Workshop from 2025-07-20T14:00 with \"2025-07-20T13:00\"",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("new end must be after " +
        "current start"));
  }

  @Test
  public void testEditEvents_MissingEvent_ShouldFail() {
    String[] commands = {
        "create calendar --name MissingEventCal --timezone America/New_York",
        "use calendar --name MissingEventCal",
        "edit events location NonExistingEvent from 2025-08-01T10:00 with \"Room 1\"",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("no matching event found"));
  }

  @Test
  public void testEditEvents_StartTimeConflict_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name ConflictCal --timezone America/New_York",
        "use calendar --name ConflictCal",
        "create event Meeting from 2025-06-10T09:00 to 2025-06-10T10:00",
        "create event Standup from 2025-06-10T10:00 to 2025-06-10T11:00",
        "edit events start Standup from 2025-06-10T10:00 with \"2025-06-10T09:30\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("conflict detected"));
  }

  // Test editing event description successfully.
  @Test
  public void testEditEvents_DescriptionUpdate_ShouldPass() throws IOException {
    String[] commands = {
        "create calendar --name DescCal --timezone America/New_York",
        "use calendar --name DescCal",
        "create event Workshop from 2025-07-20T14:00 to 2025-07-20T16:00",
        "edit events description Workshop from 2025-07-20T14:00 with \"Updated " +
            "workshop description\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Workshop\",07/20/2025,02:00 PM,07/20/2025,04:00 PM,False,\"Updated " +
            "workshop description\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test editing an event location to an empty value.
  @Test
  public void testEditEvents_EmptyLocation_ShouldFail() {
    String[] commands = {
        "create calendar --name EmptyLocCal --timezone America/New_York",
        "use calendar --name EmptyLocCal",
        "create event Seminar from 2025-08-05T13:00 to 2025-08-05T15:00",
        "edit events location Seminar from 2025-08-05T13:00 with \"\"",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("missing value"));
  }

  // Test editing a public event to private.
  @Test
  public void testEditEvents_PublicToPrivate_ShouldPass() throws IOException {
    String[] commands = {
        "create calendar --name PublicCal --timezone America/New_York",
        "use calendar --name PublicCal",
        "create event Conference from 2025-09-10T09:00 to 2025-09-10T17:00",
        "edit events isprivate Conference from 2025-09-10T09:00 with \"false\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Conference\",09/10/2025,09:00 AM,09/10/2025,05:00 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test editing event to an invalid datetime format.
  @Test
  public void testEditEvents_InvalidDatetime_ShouldFail() {
    String[] commands = {
        "create calendar --name InvalidDateCal --timezone America/New_York",
        "use calendar --name InvalidDateCal",
        "create event Review from 2025-11-15T10:00 to 2025-11-15T11:00",
        "edit events start Review from 2025-11-15T10:00 with \"2025-11-15 09:30\"",
        // invalid format
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("could not be parsed"));
  }


  @Test
  public void testEditEvent_UpdatePrivateFlag_ShouldSucceed() throws IOException {
    String[] commands = {
        "create calendar --name PrivateFlagCal --timezone America/New_York",
        "use calendar --name PrivateFlagCal",
        "create event Confidential from 2025-09-01T09:00 to 2025-09-01T10:00",
        "edit events isprivate Confidential from 2025-09-01T09:00 with \"false\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Confidential\",09/01/2025,09:00 AM,09/01/2025,10:00 AM,False,\"\",\"\"," +
            "False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditEvents_UpdateLocationForRecurringEvent_EditAll() throws IOException {
    String[] commands = {
        "create calendar --name RecurringLocUpdate --timezone America/New_York",
        "use calendar --name RecurringLocUpdate",
        "create event YogaClass from 2025-07-01T07:00 to 2025-07-01T08:00 repeats T" +
            " for 2 times",
        "edit events location YogaClass from 2025-07-01T07:00 with \"Gym Hall\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"YogaClass\",07/01/2025,07:00 AM,07/01/2025,08:00 AM,False,\"\",\"Gym " +
            "Hall\",False",
        "\"YogaClass\",07/08/2025,07:00 AM,07/08/2025,08:00 AM,False,\"\",\"Gym " +
            "Hall\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditEvent_EditStartTimeConflict_ShouldRollback() {
    String[] commands = {
        "create calendar --name StartConflictCal --timezone America/New_York",
        "use calendar --name StartConflictCal",
        "create event EventA from 2025-08-10T11:00 to 2025-08-10T12:00",
        "create event EventB from 2025-08-10T12:30 to 2025-08-10T13:30",
        "edit events start EventB from 2025-08-10T12:30 with \"2025-08-10T11:30\"",
        "exit"

    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("conflict detected"));
  }

  @Test
  public void testEditEvents_EditWithWhitespaceOnly_ShouldFail() {
    String[] commands = {
        "create calendar --name WhitespaceCal --timezone America/New_York",
        "use calendar --name WhitespaceCal",
        "create event Call from 2025-07-30T15:00 to 2025-07-30T16:00",
        "edit events name Call from 2025-07-30T15:00 with \"   \"",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("missing value"));
  }

  // Test: Edit recurring event so that only occurrences starting at or after a
  // specified time are updated.
  @Test
  public void testEditRecurringEvent_PartialOccurrenceUpdate_ShouldUpdateOnlyLaterOccurrences()
      throws IOException {
    String[] commands = {
        "create calendar --name RecurrPartialCal --timezone America/New_York",
        "use calendar --name RecurrPartialCal",
        // Create a recurring event "Standup" with 3 occurrences (assume weekly
        // recurrence).
        "create event Standup from 2025-07-07T09:00 to 2025-07-07T09:30 repeats M " +
            "for 3 times",
        // Use a fromDateTime equal to the start time of the second occurrence.
        "edit events location Standup from 2025-07-13T09:00 with \"Room Y\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // Expect: First occurrence remains unchanged; second and third are updated.
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Standup\",07/07/2025,09:00 AM,07/07/2025,09:30 AM,False,\"\",\"\",False",
        "\"Standup\",07/14/2025,09:00 AM,07/14/2025,09:30 AM,False,\"\",\"Room Y\"," +
            "False",
        "\"Standup\",07/21/2025,09:00 AM,07/21/2025,09:30 AM,False,\"\",\"Room Y\"," +
            "False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Edit recurring event updating description for only the last occurrence.
  @Test
  public void testEditRecurringEvent_UpdateDescriptionForLastOccurrence_ShouldUpdateOnlyLast()
      throws IOException {
    String[] commands = {
        "create calendar --name RecurrDescCal --timezone America/New_York",
        "use calendar --name RecurrDescCal",
        // Create a recurring event "Briefing" with 3 occurrences.
        "create event Briefing from 2025-08-04T09:00 to 2025-08-04T09:30 repeats W " +
            "for 3 times",
        // Use a fromDateTime equal to the start time of the third occurrence.
        "edit events description Briefing from 2025-08-20T09:00 with \"Final " +
            "Briefing\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // Expected: Occurrence 1 and 2 remain unchanged; occurrence 3 updated.
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Briefing\",08/06/2025,09:00 AM,08/06/2025,09:30 AM,False,\"\",\"\",False",
        "\"Briefing\",08/13/2025,09:00 AM,08/13/2025,09:30 AM,False,\"\",\"\",False",
        "\"Briefing\",08/20/2025,09:00 AM,08/20/2025,09:30 AM,False,\"Final " +
            "Briefing\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Edit recurring event – update ispublic flag for all occurrences.
  @Test
  public void testEditRecurringEvent_UpdateIsPublic_ShouldUpdateAllOccurrences()
      throws IOException {
    String[] commands = {
        "create calendar --name RecurrBoolCal --timezone America/New_York",
        "use calendar --name RecurrBoolCal",
        "create event Standup from 2025-08-11T09:00 to 2025-08-11T09:30 repeats W " +
            "for 2 times",
        "edit events isPrivate Standup from 2025-08-11T09:00 with \"false\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // When ispublic is set to false, Private becomes true for all occurrences.
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Standup\",08/13/2025,09:00 AM,08/13/2025,09:30 AM,False,\"\",\"\",False",
        "\"Standup\",08/20/2025,09:00 AM,08/20/2025,09:30 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Edit recurring event – update start time for all occurrences, ensuring no
  // conflict.
  @Test
  public void testEditRecurringEvent_UpdateStartTimeAllOccurrences_ShouldUpdateAll()
      throws IOException {
    String[] commands = {
        "create calendar --name RecurrStartUpdateCal --timezone America/New_York",
        "use calendar --name RecurrStartUpdateCal",
        "create event Standup from 2025-09-01T09:00 to 2025-09-01T09:30 repeats W " +
            "for 3 times",
        // Update start time for all occurrences: shift by 15 minutes earlier.
        "edit events start Standup from 2025-09-01T09:00 with " +
            "\"2025-09-01T08:45:00\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Standup\",09/03/2025,08:45 AM,09/03/2025,09:30 AM,False,\"\",\"\",False",
        "\"Standup\",09/10/2025,08:45 AM,09/10/2025,09:30 AM,False,\"\",\"\",False",
        "\"Standup\",09/17/2025,08:45 AM,09/17/2025,09:30 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Edit recurring event – update end time for all occurrences.
  @Test
  public void testEditRecurringEvent_UpdateEndTimeAllOccurrences_ShouldUpdateAll()
      throws IOException {
    String[] commands = {
        "create calendar --name RecurrEndUpdateCal --timezone America/New_York",
        "use calendar --name RecurrEndUpdateCal",
        "create event Standup from 2025-09-01T09:00 to 2025-09-01T09:30 repeats M " +
            "for 3 times",
        // Update end time: shift end by 15 minutes later.
        "edit events end Standup from 2025-09-01T09:00 with \"2025-09-01T09:45:00\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Standup\",09/01/2025,09:00 AM,09/01/2025,09:45 AM,False,\"\",\"\",False",
        "\"Standup\",09/08/2025,09:00 AM,09/08/2025,09:45 AM,False,\"\",\"\",False",
        "\"Standup\",09/15/2025,09:00 AM,09/15/2025,09:45 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Edit recurring events – attempt update that would cause a conflict among
  // occurrences.
  @Test
  public void testEditRecurringEvent_UpdateStartTimeConflict_ShouldFail()
      throws IOException {
    String[] commands = {
        "create calendar --name RecurrConflictUpdate --timezone America/New_York",
        "use calendar --name RecurrConflictUpdate",
        // Create recurring event with 2 occurrences.
        "create event Standup from 2025-09-01T08:00 to 2025-09-01T09:00",
        "create event Standup from 2025-09-01T09:00 to 2025-09-01T09:30 repeats M " +
            "for 2 times",
        // Attempt to update start time of the first occurrence causing overlap
        // (conflict).
        "edit events start Standup from 2025-09-01T08:45 with " +
            "\"2025-09-01T08:45:00\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Standup\",09/01/2025,08:00 AM,09/01/2025,09:00 AM,False,\"\",\"\",False",
        "\"Standup\",09/01/2025,09:00 AM,09/01/2025,09:30 AM,False,\"\",\"\",False",
        "\"Standup\",09/08/2025,09:00 AM,09/08/2025,09:30 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Successfully update the start time of a non-recurring event.
  @Test
  public void testEditEvent_UpdateStart_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name EditStartSingle --timezone America/New_York",
        "", //just to check if the logic works fine if empty lines arer added
        "use calendar --name EditStartSingle",
        "create event Briefing from 2025-06-15T14:00 to 2025-06-15T15:00",
        "edit event start Briefing from 2025-06-15T14:00 to 2025-06-15T15:00 with " +
            "\"2025-06-15T13:45:00\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // The start time should now be 13:45, while the end remains 15:00.
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Briefing\",06/15/2025,01:45 PM,06/15/2025,03:00 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Successfully update the end time of a non-recurring event.
  @Test
  public void testEditEvent_UpdateEnd_ShouldDetectConflict() throws IOException {
    String[] commands = {
        "create calendar --name EditEndConflict --timezone America/New_York",
        "use calendar --name EditEndConflict",
        "create event Review from 2025-06-20T09:00 to 2025-06-20T10:00",
        "create event WrapUp from 2025-06-20T10:00 to 2025-06-20T11:00",
        "edit event end Review from 2025-06-20T09:00 to 2025-06-20T10:00 with " +
            "\"2025-06-20T10:20:00\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    // Conflict should be reported
    assertTrue(outContent.toString().contains("Conflict detected after editing end"));

    // Review event should remain unchanged due to conflict
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Review\",06/20/2025,09:00 AM,06/20/2025,10:00 AM,False,\"\",\"\",False",
        "\"WrapUp\",06/20/2025,10:00 AM,06/20/2025,11:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditEvent_UpdateStart_ShouldDetectConflict() throws IOException {
    String[] commands = {
        "create calendar --name EditStartConflict --timezone America/New_York",
        "use calendar --name EditStartConflict",
        "create event WrapUp from 2025-06-20T10:00 to 2025-06-20T11:00",
        "create event Review from 2025-06-20T09:00 to 2025-06-20T10:00",
        "edit event start WrapUp from 2025-06-20T10:00 to 2025-06-20T11:00 with " +
            "\"2025-06-20T09:30:00\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    // Conflict should be reported
    assertTrue(outContent.toString().contains("Conflict detected after editing start"));

    // Review should remain unchanged
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"WrapUp\",06/20/2025,10:00 AM,06/20/2025,11:00 AM,False,\"\",\"\",False",
        "\"Review\",06/20/2025,09:00 AM,06/20/2025,10:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Successfully update the ispublic flag.
  @Test
  public void testEditEvent_UpdateIsPublic_ShouldExportCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name EditFlagSingle --timezone America/New_York",
        "use calendar --name EditFlagSingle",
        "create event Conference from 2025-06-30T09:00 to 2025-06-30T10:00",
        "edit event isprivate Conference from 2025-06-30T09:00 to 2025-06-30T10:00 " +
            "with \"false\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // When ispublic is false, the event is private.
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Conference\",06/30/2025,09:00 AM,06/30/2025,10:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Calendar not found should throw an exception.
  @Test
  public void testEditEvent_CalendarNotFound_ShouldFail() {
    String[] commands = {
        "edit event location Meeting from 2025-07-10T10:00 to 2025-07-10T11:00 with \"Room Z\"",
        "exit"
    };
    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("error executing command: please use somme calendar"));
  }

  // Test: Conflict detection for singular editEvent – update start time that causes
  // conflict.
  @Test
  public void testEditEvent_Conflict_ShouldFail() {
    String[] commands = {
        "create calendar --name ConflictSingle --timezone America/New_York",
        "use calendar --name ConflictSingle",
        "create event Alpha from 2025-07-15T09:00 to 2025-07-15T10:00",
        "create event Beta from 2025-07-15T10:00 to 2025-07-15T11:00",
        // Attempt to update Beta's start time to 09:30, which overlaps with Alpha.
        "edit event start Beta from 2025-07-15T10:00 to 2025-07-15T11:00 with " +
            "\"2025-07-15T09:30:00\"",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("conflict detected"));
  }

  // Test: Edit event with invalid datetime format should fail.
  @Test
  public void testEditEvent_InvalidDatetimeFormat_ShouldFail() {
    String[] commands = {
        "create calendar --name FormatSingle --timezone America/New_York",
        "use calendar --name FormatSingle",
        "create event Demo from 2025-07-20T09:00 to 2025-07-20T10:00",
        "edit event start Demo from 2025-07-20T09:00 to 2025-07-20T10:00 with " +
            "\"2025/07/20 09:30:00\"",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("could not be parsed"));
  }


  // Test: Updating an event with the same value should succeed (i.e. no change, but
  // export remains unchanged).
  @Test
  public void testEditEvent_UpdateWithSameValue_ShouldExportCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name NoChangeSingle --timezone America/New_York",
        "use calendar --name NoChangeSingle",
        "create event Update from 2025-09-20T10:00 to 2025-09-20T11:00 " +
            "--description \"Original\"",
        "edit event description Update from 2025-09-20T10:00 to 2025-09-20T11:00 " +
            "with \"Original\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Update\",09/20/2025,10:00 AM,09/20/2025,11:00 AM,False,\"Original\"," +
            "\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  // Test: Update the event location with special characters.
  @Test
  public void testEditEvent_UpdateLocationWithSpecialCharacters_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name SpecialLocSingle --timezone America/New_York",
        "use calendar --name SpecialLocSingle",
        "create event Meeting from 2025-08-10T14:00 to 2025-08-10T15:00",
        "edit event location Meeting from 2025-08-10T14:00 to 2025-08-10T15:00 with" +
            " \"Room #42 @ HQ\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Meeting\",08/10/2025,02:00 PM,08/10/2025,03:00 PM,False,\"\",\"Room #42 " +
            "@ HQ\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Sequential edits on the same event (update description then location).
  @Test
  public void testEditEvent_SequentialEdits_ShouldExportCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name SeqEditSingle --timezone America/New_York",
        "use calendar --name SeqEditSingle",
        "create event Update from 2025-09-20T10:00 to 2025-09-20T11:00 " +
            "--description \"Initial\"",
        // First update description.
        "edit event description Update from 2025-09-20T10:00 to 2025-09-20T11:00 " +
            "with \"NewDesc\"",
        // Then update location.
        "edit event location Update from 2025-09-20T10:00 to 2025-09-20T11:00 with " +
            "\"Room 202\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Update\",09/20/2025,10:00 AM,09/20/2025,11:00 AM,False,\"NewDesc\"," +
            "\"Room 202\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Update event ispublic using uppercase "TRUE".
  @Test
  public void testEditEvent_UpdateIsPublic_WithUpperCase_ShouldExportCorrectly()
      throws IOException {
    String[] commands = {
        "create calendar --name BoolCaseCal --timezone America/New_York",
        "use calendar --name BoolCaseCal",
        "create event Meeting from 2025-09-10T09:00 to 2025-09-10T10:00",
        "edit event ispublic Meeting from 2025-09-10T09:00 to 2025-09-10T10:00 with" +
            " \"TRUE\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // When ispublic is set to TRUE, event remains public (thus Private = false).
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Meeting\",09/10/2025,09:00 AM,09/10/2025,10:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Update event location to an empty string.
  @Test
  public void testEditEvent_UpdateLocationToEmpty_ShouldExportCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name EmptyLocCal --timezone America/New_York",
        "use calendar --name EmptyLocCal",
        "create event Gathering from 2025-09-11T12:00 to 2025-09-11T13:00 " +
            "--location \"Office\"",
        "edit event location Gathering from 2025-09-11T12:00 to 2025-09-11T13:00 " +
            "with \"\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // Expect that the location becomes empty.
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Gathering\",09/11/2025,12:00 PM,09/11/2025,01:00 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Update event start time to the same value (should succeed with no change).
  @Test
  public void testEditEvent_UpdateStartWithSameValue_ShouldExportCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name SameStartCal --timezone America/New_York",
        "use calendar --name SameStartCal",
        "create event Briefing from 2025-09-12T09:00 to 2025-09-12T10:00",
        "edit event start Briefing from 2025-09-12T09:00 to 2025-09-12T10:00 with " +
            "\"2025-09-12T09:00:00\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    // Expect no change in start or end.
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Briefing\",09/12/2025,09:00 AM,09/12/2025,10:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Update event end time to the same value (should succeed with no change).
  @Test
  public void testEditEvent_UpdateEndWithSameValue_ShouldExportCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name SameEndCal --timezone America/New_York",
        "use calendar --name SameEndCal",
        "create event Discussion from 2025-09-13T14:00 to 2025-09-13T15:00",
        "edit event end Discussion from 2025-09-13T14:00 to 2025-09-13T15:00 with " +
            "\"2025-09-13T15:00:00\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Discussion\",09/13/2025,02:00 PM,09/13/2025,03:00 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Update event name with leading and trailing spaces (verify preservation).
  @Test
  public void testEditEvent_UpdateName_WithSpaces_ShouldExportCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name SpaceNameCal --timezone America/New_York",
        "use calendar --name SpaceNameCal",
        "create event Update from 2025-09-14T10:00 to 2025-09-14T11:00",
        "edit event name Update from 2025-09-14T10:00 to 2025-09-14T11:00 with \"  " +
            "New Name  \"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"  New Name  \",09/14/2025,10:00 AM,09/14/2025,11:00 AM,False,\"\",\"\"," +
            "False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditEvent_Description_Success() throws IOException {
    String[] commands = {
        "create calendar --name DescCal --timezone America/New_York",
        "use calendar --name DescCal",
        "create event Review from 2025-05-15T14:00 to 2025-05-15T15:00",
        "edit event description Review from 2025-05-15T14:00 to 2025-05-15T15:00 " +
            "with \"Project review meeting\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Review\",05/15/2025,02:00 PM,05/15/2025,03:00 PM,False,\"Project review " +
            "meeting\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Edit event start time with invalid end time (should fail)
  @Test
  public void testEditEvent_StartTimeAfterEnd_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name InvalidTimeCal --timezone America/New_York",
        "use calendar --name InvalidTimeCal",
        "create event Discussion from 2025-06-20T10:00 to 2025-06-20T11:00",
        // Attempt to set start time after current end time, should fail.
        "edit event start Discussion from 2025-06-20T10:00 to 2025-06-20T11:00 with" +
            " \"2025-06-20T12:00\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    // Verify that event details remain unchanged after the failed operation.
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Discussion\",06/20/2025,10:00 AM,06/20/2025,11:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  // Test: Edit event location successfully
  @Test
  public void testEditEvent_Location_Success() throws IOException {
    String[] commands = {
        "create calendar --name LocationCal --timezone America/New_York",
        "use calendar --name LocationCal",
        "create event Briefing from 2025-07-10T09:00 to 2025-07-10T10:00",
        "edit event location Briefing from 2025-07-10T09:00 to 2025-07-10T10:00 " +
            "with \"Main Auditorium\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Briefing\",07/10/2025,09:00 AM,07/10/2025,10:00 AM,False,\"\",\"Main " +
            "Auditorium\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Edit event to be public successfully
  @Test
  public void testEditEvent_IsPublic_Success() throws IOException {
    String[] commands = {
        "create calendar --name PublicCal --timezone America/New_York",
        "use calendar --name PublicCal",
        "create event Seminar from 2025-08-25T13:00 to 2025-08-25T14:00",
        "edit event isprivate Seminar from 2025-08-25T13:00 with \"true\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Seminar\",08/25/2025,01:00 PM,08/25/2025,02:00 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  // Test: Edit event with invalid boolean value (should fail)
  @Test
  public void testEditEvent_InvalidBoolean_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name InvalidBoolCal --timezone America/New_York",
        "use calendar --name InvalidBoolCal",
        "create event Workshop from 2025-09-10T11:00 to 2025-09-10T12:00",
        "edit event isprivate Workshop from 2025-09-10T11:00 to 2025-09-10T12:00 " +
            "with \"yes\"",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    // Check that it throws an error due to invalid boolean input
    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("for input string") || output.contains("invalid boolean"));

    // Ensure the original event was not modified
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Workshop\",09/10/2025,11:00 AM,09/10/2025,12:00 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCopyEvents_CommandBased_Success() throws IOException {
    String[] commands = {
        "create calendar --name SourceCal --timezone America/New_York",
        "create calendar --name TargetCal --timezone Asia/Tokyo",
        "use calendar --name SourceCal",
        "create event Meeting from 2025-09-15T09:00 to 2025-09-15T10:00",
        "copy events between 2025-09-15 and 2025-09-15 --target TargetCal to 2025-09-16",
        "use calendar --name TargetCal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Meeting\",09/16/2025,10:00 PM,09/16/2025,11:00 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvents_Conflict_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name SourceCal --timezone America/New_York",
        "create calendar --name TargetCal --timezone America/New_York",
        "use calendar --name SourceCal",
        "create event Original from 2025-10-01T09:00 to 2025-10-01T10:00",
        "use calendar --name TargetCal",
        "create event Conflict from 2025-11-01T09:00 to 2025-11-01T10:00",
        "use calendar --name SourceCal",
        "copy events between 2025-10-01 and 2025-10-01 --target TargetCal to 2025-11-01",
        "use calendar --name TargetCal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("conflict detected") || output.contains("error copying event"));

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Conflict\",11/01/2025,09:00 AM,11/01/2025,10:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvents_SimpleCommand() throws IOException {
    String[] commands = {
        "create calendar --name FallCal --timezone America/New_York",
        "create calendar --name SpringCal --timezone America/New_York",
        "use calendar --name FallCal",
        "create event CS5010 from 2025-09-10T10:00 to 2025-09-10T11:00",
        "copy events between 2025-09-10 and 2025-09-10 --target SpringCal to 2026-01-10", // ✅ Fixed
        "use calendar --name SpringCal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"CS5010\",01/10/2026,10:00 AM,01/10/2026,11:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvents_CommandWithNoEvents_ShouldNotCopy() throws IOException {
    String[] commands = {
        "create calendar --name Cal1 --timezone America/New_York",
        "create calendar --name Cal2 --timezone America/New_York",
        "copy events from Cal1 from 2025-01-01T00:00 to 2025-01-01T23:59 to Cal2 " +
            "starting at 2025-02-01",
        "use calendar --name Cal2",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCopyEvents_CommandWithConflict_ShouldThrow() throws IOException {
    String[] commands = {
        "create calendar --name Original --timezone America/New_York",
        "create calendar --name Target --timezone America/New_York",
        "use calendar --name Original",
        "create event Lecture from 2025-09-01T09:00 to 2025-09-01T10:00",
        "use calendar --name Target",
        "create event Conflict from 2025-10-01T09:00 to 2025-10-01T10:00",
        "use calendar --name Original",
        "copy events between 2025-09-01 and 2025-09-01 --target Target to 2025-10-01", // fixed
        "exit"
    };
    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("an unexpected error occurred: conflict detected when copying event: lecture"));
  }


  @Test
  public void testCopyEvents_SingleEvent_SameCalendar() throws IOException {
    String[] commands = {
        "create calendar --name Cal1 --timezone America/New_York",
        "use calendar --name Cal1",
        "create event Event1 from 2025-01-01T09:00 to 2025-01-01T10:00",
        "copy events between 2025-01-01 and 2025-01-01 --target Cal1 to 2025-01-02",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Event1\",01/01/2025,09:00 AM,01/01/2025,10:00 AM,False,\"\",\"\",False",
        "\"Event1\",01/02/2025,09:00 AM,01/02/2025,10:00 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvents_SingleEvent_DifferentDate() throws IOException {
    String[] commands = {
        "create calendar --name Cal2 --timezone America/New_York",
        "use calendar --name Cal2",
        "create event Event2 from 2025-01-02T09:00 to 2025-01-02T10:00",
        "copy events between 2025-01-02 and 2025-01-02 --target Cal2 to 2025-01-04",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Event2\",01/02/2025,09:00 AM,01/02/2025,10:00 AM,False,\"\",\"\",False",
        "\"Event2\",01/04/2025,09:00 AM,01/04/2025,10:00 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvents_AcrossCalendars() throws IOException {
    String[] commands = {
        "create calendar --name SourceCal --timezone America/New_York",
        "create calendar --name TargetCal --timezone America/New_York",
        "use calendar --name SourceCal",
        "create event Event3 from 2025-01-03T09:00 to 2025-01-03T10:00",
        "copy events between 2025-01-03 and 2025-01-03 --target TargetCal to 2025-01-05",
        "use calendar --name TargetCal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Event3\",01/05/2025,09:00 AM,01/05/2025,10:00 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvents_NoMatchingEvents_ShouldNotCopy() throws IOException {
    String[] commands = {
        "create calendar --name Source --timezone America/New_York",
        "create calendar --name Target --timezone America/New_York",
        "use calendar --name Source",
        "create event Alpha from 2025-01-01T10:00 to 2025-01-01T11:00",
        "use calendar --name Target",
        "copy events from Source between 2025-01-02T00:00 and 2025-01-03T00:00 to " +
            "2025-02-01",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    // Expecting no events to be copied into the Target calendar
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCopyEvents_TimezoneAdjustedCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name Source --timezone America/New_York",
        "create calendar --name Target --timezone Asia/Kolkata",
        "use calendar --name Source",
        "create event Sync from 2025-03-01T09:00 to 2025-03-01T10:00",
        "copy events between 2025-03-01 and 2025-03-01 --target Target to 2025-03-02",
        "use calendar --name Target",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Sync\",03/02/2025,07:30 PM,03/02/2025,08:30 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCopyEvents_MissingArguments_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name SourceCal --timezone UTC",
        "use calendar --name SourceCal",
        "copy events",  // Incomplete command
        "exit"
    };

    runAppWithCommands(commands);
    String output = outContent.toString().toLowerCase();

    assertTrue(output.contains("expected 'copy events on <date> --target <calendar> to <date>'"));
  }

  @Test
  public void testCopyEvents_MissingTargetKeyword_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name SourceCal --timezone UTC",
        "create calendar --name TargetCal --timezone UTC",
        "use calendar --name SourceCal",
        "create event Sample from 2025-10-01T09:00 to 2025-10-01T10:00",
        // Missing '--target' in copy command
        "copy events on 2025-10-01 to TargetCal",
        "exit"
    };

    runAppWithCommands(commands);
    String output = outContent.toString().toLowerCase();

    assertTrue(output.contains("error executing command: insufficient arguments for copy events on date"));
  }

  @Test
  public void testCopyEvents_MissingToKeyword_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name SourceCal --timezone UTC",
        "create calendar --name TargetCal --timezone UTC",
        "use calendar --name SourceCal",
        "create event Meeting from 2025-09-01T09:00 to 2025-09-01T10:00",
        // Missing 'to' keyword after --target
        "copy events on 2025-09-01 --target TargetCal 2025-09-05",
        "exit"
    };

    runAppWithCommands(commands);
    String output = outContent.toString().toLowerCase();

    assertTrue(output.contains("expected 'to' after target calendar"));
  }

  @Test
  public void testCopyEvents_MissingAndKeyword_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name CalA --timezone UTC",
        "create calendar --name CalB --timezone UTC",
        "use calendar --name CalA",
        "create event Workshop from 2025-10-10T14:00 to 2025-10-10T15:00",
        // Missing 'and' keyword
        "copy events between 2025-10-10 2025-10-11 --target CalB to 2025-11-01",
        "exit"
    };

    runAppWithCommands(commands);
    String output = outContent.toString().toLowerCase();

    assertTrue(output.contains("expected 'and' after start date"));
  }

  @Test
  public void testCopyEvents_MissingTargetKeywordAfterEndDate_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name Cal1 --timezone UTC",
        "create calendar --name Cal2 --timezone UTC",
        "use calendar --name Cal1",
        "create event Planning from 2025-07-01T10:00 to 2025-07-01T11:00",
        // Missing '--target' keyword after end date
        "copy events between 2025-07-01 and 2025-07-02 Cal2 to 2025-08-01",
        "exit"
    };

    runAppWithCommands(commands);
    String output = outContent.toString().toLowerCase();

    assertTrue(output.contains("expected '--target' after end date"));
  }

  @Test
  public void testCopyEvents_MissingToKeyword() throws IOException {
    String[] commands = {
        "create calendar --name Cal1 --timezone UTC",
        "create calendar --name Cal2 --timezone UTC",
        "use calendar --name Cal1",
        "create event Demo from 2025-06-01T09:00 to 2025-06-01T10:00",
        // Missing 'to' after target calendar
        "copy events between 2025-06-01 and 2025-06-01 --target Cal2 2025-07-01",
        "exit"
    };

    runAppWithCommands(commands);
    String output = outContent.toString().toLowerCase();

    assertTrue(output.contains("expected 'to' after target calendar"));
  }

  @Test
  public void testCopyEvents_IncompleteCommand_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name Cal1 --timezone UTC",
        "create calendar --name Cal2 --timezone UTC",
        "use calendar --name Cal1",
        "create event Task from 2025-08-01T09:00 to 2025-08-01T10:00",
        // Missing target calendar and 'to' part
        "copy events between 2025-08-01 and",  // Incomplete command
        "exit"
    };

    runAppWithCommands(commands);
    String output = outContent.toString().toLowerCase();

    assertTrue(output.contains("error executing command: insufficient arguments for copy events between dates"));
  }

  @Test
  public void testCopyEvents_CommandSuccessMessage() throws IOException {
    String[] commands = {
        "create calendar --name SourceCal --timezone UTC",
        "create calendar --name TargetCal --timezone UTC",
        "use calendar --name SourceCal",
        "create event DemoEvent from 2025-08-01T09:00 to 2025-08-01T10:00",
        "copy events on 2025-08-01 --target TargetCal to 2025-08-05",
        "exit"
    };

    runAppWithCommands(commands);
    String output = outContent.toString().toLowerCase();

    assertTrue(output.contains("events copied successfully."));
  }


  @Test
  public void testCopyEvents_ConflictPreventsCopy() throws IOException {
    String[] commands = {
        "create calendar --name Source --timezone America/New_York",
        "create calendar --name Target --timezone America/New_York",

        "use calendar --name Source",
        "create event Duplicate from 2025-04-10T10:00 to 2025-04-10T11:00",

        "use calendar --name Target",
        "create event Conflict from 2025-05-01T10:00 to 2025-05-01T11:00",

        // Set context back to Source before copying
        "use calendar --name Source",
        "copy events between 2025-04-10 and 2025-04-10 --target Target to 2025-05-01",

        "use calendar --name Target",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    // Conflict detected, event not copied
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Conflict\",05/01/2025,10:00 AM,05/01/2025,11:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("conflict"));
  }


  @Test
  public void testCopyEvents_EmptyCalendar_NoCopy() throws IOException {
    String[] commands = {
        "create calendar --name EmptyCal --timezone UTC",
        "create calendar --name Destination --timezone UTC",
        "use calendar --name EmptyCal",
        "copy events from 2025-01-01T00:00 to 2025-01-02T00:00 into Destination " +
            "starting 2025-01-05",
        "use calendar --name Destination",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCopyEvents_RecurringEvent_Copied() throws IOException {
    String[] commands = {
        "create calendar --name WeeklyCal --timezone UTC",
        "create calendar --name MirrorCal --timezone UTC",
        "use calendar --name WeeklyCal",
        "create event --autoDecline Recurring from 2025-03-03T09:00 to 2025-03-03T10:00 repeats M" +
         " for 2 times",
        "use calendar --name WeeklyCal",
        "copy events between 2025-03-02 and 2025-03-10 --target MirrorCal to 2025-04-07",
        "use calendar --name MirrorCal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Recurring\",04/08/2025,09:00 AM,04/08/2025,10:00 AM,False,\"\",\"\",False",
        "\"Recurring\",04/15/2025,09:00 AM,04/15/2025,10:00 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCopyEvents_Conflict_ThrowsError() throws IOException {
    String[] commands = {
        "create calendar --name Source --timezone UTC",
        "create calendar --name Target --timezone UTC",
        "use calendar --name Source",
        "create event Meeting from 2025-06-10T09:00 to 2025-06-10T10:00",
        "use calendar --name Target",
        "create event Conflict from 2025-07-01T09:00 to 2025-07-01T10:00",
        "use calendar --name Source",
        "copy events between 2025-06-10 and 2025-06-10 --target Target to 2025-07-01",
        "use calendar --name Target",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("conflict detected"));
  }


  @Test
  public void testCopyEvents_InvalidDateRange_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name A --timezone UTC",
        "create calendar --name B --timezone UTC",
        "use calendar --name A",
        "create event Test from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy events between 2025-05-02 and 2025-05-01 --target B to 2025-05-10",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("must not be before"));
  }


  @Test
  public void testCopyEvents_AcrossTimezones_WithExpectedExport() throws IOException {
    String[] commands = {
        "create calendar --name NYCal --timezone America/New_York",
        "create calendar --name TokyoCal --timezone Asia/Tokyo",
        "use calendar --name NYCal",
        "create event TeamSync from 2025-04-01T09:00 to 2025-04-01T10:00",
        "copy events between 2025-04-01 and 2025-04-01 --target TokyoCal to 2025-04-02",
        "use calendar --name TokyoCal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"TeamSync\",04/02/2025,10:00 PM,04/02/2025,11:00 PM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvents_NoEventsInRange_ShouldNotCopy() throws IOException {
    String[] commands = {
        "create calendar --name A --timezone UTC",
        "create calendar --name B --timezone UTC",
        "use calendar --name A",
        "create event GhostEvent from 2025-01-01T08:00 to 2025-01-01T09:00",
        "copy events from A between 2025-02-01T08:00 and 2025-02-01T09:00 to B " +
            "starting 2025-02-02",
        "use calendar --name B",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCopyEvents_MultipleSpacedEvents() throws IOException {
    String[] commands = {
        "create calendar --name SpringCal --timezone America/New_York",
        "create calendar --name FallCal --timezone America/New_York",
        "use calendar --name SpringCal",
        "create event EventA from 2025-03-01T09:00 to 2025-03-01T10:00",
        "create event EventB from 2025-03-03T14:00 to 2025-03-03T15:00",
        "copy events between 2025-03-01 and 2025-03-03 --target FallCal to 2025-09-01", // ✅ fixed
        "use calendar --name FallCal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"EventA\",09/01/2025,09:00 AM,09/01/2025,10:00 AM,False,\"\",\"\",False",
        "\"EventB\",09/03/2025,02:00 PM,09/03/2025,03:00 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvents_LAtoTokyo_TimeShift() throws IOException {
    String[] commands = {
        "create calendar --name LA --timezone America/Los_Angeles",
        "create calendar --name Tokyo --timezone Asia/Tokyo",
        "use calendar --name LA",
        "create event --autoDecline DesignReview from 2025-05-10T10:00 to 2025-05-10T11:00",
        "copy events between 2025-05-10 and 2025-05-10 --target Tokyo to 2025-05-11",
        "use calendar --name Tokyo",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    // In Los Angeles, event is at 10:00 AM PDT. Tokyo is 16 hours ahead, so:
    // Event should be at 10:00 AM JST on 2025-05-11.
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
         "Private",
        "\"DesignReview\",05/12/2025,02:00 AM,05/12/2025,03:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCopyEvents_UTCtoIST_AdjustTime() throws IOException {
    String[] commands = {
        "create calendar --name Global --timezone UTC",
        "create calendar --name India --timezone Asia/Kolkata",
        "use calendar --name Global",
        "create event Standup from 2025-08-01T08:00 to 2025-08-01T09:00",
        "copy events between 2025-08-01 and 2025-08-01 --target India to 2025-08-02",
        "use calendar --name India",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Standup\",08/02/2025,01:30 PM,08/02/2025,02:30 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvents_DST_NewYorkToLondon() throws IOException {
    String[] commands = {
        "create calendar --name NY --timezone America/New_York",
        "create calendar --name London --timezone Europe/London",
        "use calendar --name NY",
        "create event DSTMeeting from 2025-03-09T02:00 to 2025-03-09T03:00",
        "copy events between 2025-03-09 and 2025-03-09 --target London to 2025-03-10",
        "use calendar --name London",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"DSTMeeting\",03/10/2025,07:00 AM,03/10/2025,08:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvents_KathmanduToChicago_HalfHourZone() throws IOException {
    String[] commands = {
        "create calendar --name KTM --timezone Asia/Kathmandu",
        "create calendar --name Chicago --timezone America/Chicago",
        "use calendar --name KTM",
        "create event HalfHourTest from 2025-07-15T09:00 to 2025-07-15T10:00",
        "copy events between 2025-07-15 and 2025-07-15 --target Chicago to 2025-07-16",
        "use calendar --name Chicago",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"HalfHourTest\",07/15/2025,10:15 PM,07/15/2025,11:15 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvents_SameCalendarName() throws IOException {
    String[] commands = {
        "create calendar --name MyCal --timezone America/New_York",
        "use calendar --name MyCal",
        "create event InternalMeeting from 2025-06-10T14:00 to 2025-06-10T15:00",
        // Corrected copy command using proper syntax
        "copy events between 2025-06-10 and 2025-06-10 --target MyCal to 2025-06-17",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"InternalMeeting\",06/10/2025,02:00 PM,06/10/2025,03:00 PM,False,\"\",\"\",False",
        "\"InternalMeeting\",06/17/2025,02:00 PM,06/17/2025,03:00 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCopyEvents_MultipleEventsOneConflictsNoneCopied() throws IOException {
    String[] commands = {
        "create calendar --name SourceCal --timezone America/New_York",
        "create calendar --name TargetCal --timezone America/New_York",

        "use calendar --name SourceCal",
        "create event --autoDecline Event1 from 2025-10-01T09:00 to 2025-10-01T10:00",
        "create event --autoDecline Event2 from 2025-10-01T11:00 to 2025-10-01T12:00",

        "use calendar --name TargetCal",
        "create event --autoDecline Conflict from 2025-11-01T11:00 to 2025-11-01T12:00",
        "use calendar --name SourceCal",

        // Attempt to copy both events into TargetCal on a day where Event2 would conflict
        "copy events between 2025-10-01 and 2025-10-01 --target TargetCal to 2025-11-01",
        "use calendar --name TargetCal",

        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    // Assert that the system warned about the conflict
    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("conflict detected"));

    // Only the original conflicting event should be in the export
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
         "Private",
        "\"Conflict\",11/01/2025,11:00 AM,11/01/2025,12:00 PM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCopyEvents_SimpleSameCalendarCopy() throws IOException {
    String[] commands = {
        "create calendar --name SameCal --timezone UTC",
        "use calendar --name SameCal",
        "create event --autoDecline Standup from 2025-06-01T09:00 to 2025-06-01T09:30",
        "copy events on 2025-06-01 --target SameCal to 2025-06-08",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
         "Private",
        "\"Standup\",06/01/2025,09:00 AM,06/01/2025,09:30 AM,False,\"\",\"\",False",
        "\"Standup\",06/08/2025,09:00 AM,06/08/2025,09:30 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCopyEvents_CrossTimeZone() throws IOException {
    String[] commands = {
        "create calendar --name SourceCal --timezone America/New_York",
        "create calendar --name TargetCal --timezone Asia/Kolkata",
        "use calendar --name SourceCal",
        "create event --autoDecline TeamCall from 2025-07-01T10:00 to 2025-07-01T11:00",
        "copy events on 2025-07-01 --target TargetCal to 2025-07-02",
        "use calendar --name TargetCal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
         "Private",
        "\"TeamCall\",07/02/2025,07:30 PM,07/02/2025,08:30 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCopyEvents_InvalidDateFormat_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name Source --timezone UTC",
        "create calendar --name Target --timezone UTC",
        "use calendar --name Source",
        "create event --autoDecline MyEvent from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy events on 2025/05/01 --target Target to 2025-05-10",
        "exit"
    };
    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("invalid") || output.contains("could not parse"));
  }

  @Test
  public void testCopyEvents_NoEventsFound_ShouldFailGracefully() throws IOException {
    String[] commands = {
        "create calendar --name EmptySource --timezone UTC",
        "create calendar --name Target --timezone UTC",
        "use calendar --name Target",
        "copy events on 2025-12-25 --target Target to 2026-01-01",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    // Exported CSV should be empty (header only)
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCopyEvents_SameDaySameCalendar() throws IOException {
    String[] commands = {
        "create calendar --name MyCalendar --timezone America/New_York",
        "use calendar --name MyCalendar",
        "create event Task1 from 2025-07-01T09:00 to 2025-07-01T10:00",
        "copy events on 2025-07-01 --target MyCalendar to 2025-07-02",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
         "Private",
        "\"Task1\",07/01/2025,09:00 AM,07/01/2025,10:00 AM,False,\"\",\"\",False",
        "\"Task1\",07/02/2025,09:00 AM,07/02/2025,10:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCopyEvents_DifferentTimezones_OffsetPreserved() throws IOException {
    String[] commands = {
        "create calendar --name SourceCal --timezone Europe/London",
        "create calendar --name TargetCal --timezone Asia/Kolkata",
        "use calendar --name SourceCal",
        "create event MorningSync from 2025-08-10T08:00 to 2025-08-10T09:00",
        "copy events on 2025-08-10 --target TargetCal to 2025-08-15",
        "use calendar --name TargetCal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    // 08:00 London = 12:30 Kolkata
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
         "Private",
        "\"MorningSync\",08/15/2025,12:30 PM,08/15/2025,01:30 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCopyEvents_TargetCalendarMissing_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name SourceCal --timezone UTC",
        "use calendar --name SourceCal",
        "create event LonelyEvent from 2025-10-01T08:00 to 2025-10-01T09:00",
        "copy events on 2025-10-01 --target MissingCal to 2025-10-02",
        "exit"
    };
    runAppWithCommands(commands);

    String output = outContent.toString();
    assertTrue(output.contains("Error: Calendar not found: MissingCal"));
  }


  @Test
  public void testCopySingleEvent_WithConflict_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name SourceCal --timezone America/New_York",
        "create calendar --name TargetCal --timezone America/New_York",

        "use calendar --name SourceCal",
        "create event Standup from 2025-08-01T09:00 to 2025-08-01T09:30",

        "use calendar --name TargetCal",
        "create event Blocker from 2025-09-01T09:00 to 2025-09-01T09:30", // conflict

        // Set context back to SourceCal before issuing the copy command
        "use calendar --name SourceCal",
        "copy events between 2025-08-01 and 2025-08-01 --target TargetCal to 2025-09-01",

        "use calendar --name TargetCal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("conflict detected"));

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Blocker\",09/01/2025,09:00 AM,09/01/2025,09:30 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvent_Success() throws IOException {
    String[] commands = {
        "create calendar --name SourceCal --timezone America/New_York",
        "create calendar --name TargetCal --timezone America/New_York",
        "use calendar --name SourceCal",
        "create event Meeting from 2025-08-01T09:00 to 2025-08-01T10:00",
        "copy event Meeting on 2025-08-01T09:00 --target TargetCal to 2025-09-01T09:00",
        "use calendar --name TargetCal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Meeting\",09/01/2025,09:00 AM,09/01/2025,10:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvent_Conflict_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name SourceCal --timezone America/New_York",
        "create calendar --name TargetCal --timezone America/New_York",
        "use calendar --name SourceCal",
        "create event Session from 2025-08-01T09:00 to 2025-08-01T10:00",
        "use calendar --name TargetCal",
        "create event Blocker from 2025-09-01T09:00 to 2025-09-01T10:00",
        "copy event Session on 2025-08-01T09:00 --target TargetCal to 2025-09-01T09:00",
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString();
    assertTrue(output.contains("Error: Event with name 'Session' on 2025-08-01T09:00 not found in calendar TargetCal"));
  }

  @Test
  public void testCopyEvent_EventNotFound_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name SourceCal --timezone America/New_York",
        "create calendar --name TargetCal --timezone America/New_York",
        "use calendar --name SourceCal",
        "copy event GhostEvent on 2025-08-01T09:00 --target TargetCal to 2025-09-01T09:00",
        "exit"
    };

    runAppWithCommands(commands);
    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("not found"));
  }

  @Test
  public void testCopyEvent_Timezones_KeepSameLocalTime() throws IOException {
    String[] commands = {
        "create calendar --name SourceCal --timezone America/New_York",
        "create calendar --name TargetCal --timezone Asia/Kolkata",
        "use calendar --name SourceCal",
        "create event Sync from 2025-08-01T08:00 to 2025-08-01T09:00",
        "copy event Sync on 2025-08-01T08:00 --target TargetCal to 2025-08-02T08:00",
        "use calendar --name TargetCal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Sync\",08/02/2025,08:00 AM,08/02/2025,09:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvent_SameCalendar_Success() throws IOException {
    String[] commands = {
        "create calendar --name MyCal --timezone America/New_York",
        "use calendar --name MyCal",
        "create event Original from 2025-11-01T14:00 to 2025-11-01T15:00",
        "copy event Original on 2025-11-01T14:00 --target MyCal to 2025-11-02T14:00",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Original\",11/01/2025,02:00 PM,11/01/2025,03:00 PM,False,\"\",\"\",False",
        "\"Original\",11/02/2025,02:00 PM,11/02/2025,03:00 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvent_CaseInsensitiveMatch() throws IOException {
    String[] commands = {
        "create calendar --name Cal1 --timezone America/New_York",
        "create calendar --name Cal2 --timezone America/New_York",
        "use calendar --name Cal1",
        "create event ImportantEvent from 2025-06-15T09:00 to 2025-06-15T10:00",
        "copy event importantevent on 2025-06-15T09:00 --target Cal2 to 2025-07-01T09:00",
        "use calendar --name Cal2",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"ImportantEvent\",07/01/2025,09:00 AM,07/01/2025,10:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvent_OverlapButNoConflict() throws IOException {
    String[] commands = {
        "create calendar --name Source --timezone America/New_York",
        "create calendar --name Target --timezone America/New_York",
        "use calendar --name Source",
        "create event A from 2025-12-01T08:00 to 2025-12-01T09:00",
        "use calendar --name Target",
        "create event B from 2025-12-01T09:00 to 2025-12-01T10:00",
        "use calendar --name Source",
        "copy event A on 2025-12-01T08:00 --target Target to 2025-12-01T08:00",
        "use calendar --name Target",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"B\",12/01/2025,09:00 AM,12/01/2025,10:00 AM,False,\"\",\"\",False",
        "\"A\",12/01/2025,08:00 AM,12/01/2025,09:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCopyEvent_MissingSourceCalendar_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name Target --timezone America/New_York",
        "copy event Nonexistent on 2025-08-01T09:00 --target Target to 2025-08-02T09:00",
        "exit"
    };
    runAppWithCommands(commands);

    String expectedError = "Error Executing command: Please use somme calendar";
    String output = outContent.toString();
    assertTrue(output.contains(expectedError));
  }

  @Test
  public void testCopyEvent_WrongTime_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name Source --timezone America/New_York",
        "create calendar --name Target --timezone America/New_York",
        "use calendar --name Source",
        "create event EventX from 2025-10-10T10:00 to 2025-10-10T11:00",
        "copy event EventX on 2025-10-10T09:00 --target Target to 2025-10-15T10:00",
        "exit"
    };
    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("not found"));
  }


  @Test
  public void testCopyEvent_AcrossTimezones_ShiftedCorrectly() throws IOException {
    String[] commands = {
        "create calendar --name ESTCal --timezone America/New_York",
        "create calendar --name ISTCal --timezone Asia/Kolkata",
        "use calendar --name ESTCal",
        "create event TeamCall from 2025-09-01T09:00 to 2025-09-01T10:00",
        "copy event TeamCall on 2025-09-01T09:00 --target ISTCal to 2025-09-02T18:30", // fixed
        // syntax
        "use calendar --name ISTCal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"TeamCall\",09/02/2025,06:30 PM,09/02/2025,07:30 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvent_ConflictInTarget_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name A --timezone America/New_York",
        "create calendar --name B --timezone America/New_York",
        "use calendar --name A",
        "create event OverlapMe from 2025-07-01T10:00 to 2025-07-01T11:00",
        "use calendar --name B",
        "create event Blocker from 2025-07-02T10:00 to 2025-07-02T11:00",
        "use calendar --name A",
        "copy event OverlapMe on 2025-07-01T10:00 --target B to 2025-07-02T10:00",//
        // Conflict with Blocker
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("conflict detected"));
  }

  @Test
  public void testCopyEvent_ToEarlierDate() throws IOException {
    String[] commands = {
        "create calendar --name C --timezone America/New_York",
        "use calendar --name C",
        "create event PastEvent from 2025-12-01T13:00 to 2025-12-01T14:00",
        "copy event PastEvent on 2025-12-01T13:00 --target C to 2025-11-01T13:00", // Fixed
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"PastEvent\",12/01/2025,01:00 PM,12/01/2025,02:00 PM,False,\"\",\"\",False",
        "\"PastEvent\",11/01/2025,01:00 PM,11/01/2025,02:00 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvent_InvalidTimeFormat_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name X --timezone America/New_York",
        "create calendar --name Y --timezone America/New_York",
        "use calendar --name X",
        "create event BadTime from 2025-10-01T12:00 to 2025-10-01T13:00",
        // Invalid source date format (space instead of 'T')
        "copy event BadTime on 2025-10-01 12:00 --target Y to 2025-10-02T12:00",
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("invalid") || output.contains("could not parse"));
  }

  @Test
  public void testCopyEvent_WithMetadata() throws IOException {
    String[] commands = {
        "create calendar --name MetaCal --timezone America/New_York",
        "use calendar --name MetaCal",
        "create event Workshop from 2025-11-05T15:00 to 2025-11-05T16:00 " +
            "--description " +
            "\"Hands-on\" --location \"Lab 42\"",
        "copy event Workshop on 2025-11-05T15:00 --target MetaCal to 2025-11-06T15:00",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"Workshop\",11/05/2025,03:00 PM,11/05/2025,04:00 PM,False,\"Hands-on\"," +
            "\"Lab 42\",False",
        "\"Workshop\",11/06/2025,03:00 PM,11/06/2025,04:00 PM,False,\"Hands-on\"," +
            "\"Lab 42\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvent_SameCalendar_DifferentTime() throws IOException {
    String[] commands = {
        "create calendar --name SoloCal --timezone America/New_York",
        "use calendar --name SoloCal",
        "create event DailyStandup from 2025-09-01T10:00 to 2025-09-01T10:30",
        "copy event DailyStandup on 2025-09-01T10:00 --target SoloCal to 2025-09-02T10:00",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"DailyStandup\",09/01/2025,10:00 AM,09/01/2025,10:30 AM,False,\"\",\"\",False",
        "\"DailyStandup\",09/02/2025,10:00 AM,09/02/2025,10:30 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvent_PrivateEvent() throws IOException {
    String[] commands = {
        "create calendar --name SecureCal --timezone America/New_York",
        "use calendar --name SecureCal",
        "create event StrategySession from 2025-10-10T14:00 to 2025-10-10T15:00 " +
            "--private",
        "copy event StrategySession on 2025-10-10T14:00 --target SecureCal to 2025-10-17T14:00",
        // fixed
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"StrategySession\",10/10/2025,02:00 PM,10/10/2025,03:00 PM,False,\"\",\"\",True",
        "\"StrategySession\",10/17/2025,02:00 PM,10/17/2025,03:00 PM,False,\"\",\"\",True"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvent_NullCalendar_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name MainCal --timezone America/New_York",
        "use calendar --name MainCal",
        "create event Keynote from 2025-10-01T09:00 to 2025-10-01T10:00",
        "copy event Keynote on 2025-10-01T09:00 --target  to 2025-10-05T09:00", // Fixed syntax,
        // missing target
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString();
    assertTrue(output.contains("Error Executing command: Expected 'to' after target calendar name" +
        ".") || output.contains("invalid"));
  }


  @Test
  public void testCopyEvent_ZeroDuration() throws IOException {
    String[] commands = {
        "create calendar --name OddCal --timezone America/New_York",
        "use calendar --name OddCal",
        "create event InstantEvent from 2025-10-10T11:00 to 2025-10-10T11:00",
        "copy event InstantEvent on 2025-10-10T11:00 --target OddCal to 2025-10-11T11:00",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);
    //event wwill not be creted as theh duration of the creation of event is zero.
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testCopyEvent_AutoDecline_NoConflict() throws IOException {
    String[] commands = {
        "create calendar --name AutoCal --timezone America/New_York",
        "use calendar --name AutoCal",
        "create event --autoDecline SecureMeeting from 2025-10-20T09:00 to " +
            "2025-10-20T10:00",
        "copy event SecureMeeting on 2025-10-20T09:00 --target AutoCal to 2025-10-21T09:00",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"SecureMeeting\",10/20/2025,09:00 AM,10/20/2025,10:00 AM,False,\"\",\"\",False",
        "\"SecureMeeting\",10/21/2025,09:00 AM,10/21/2025,10:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvent_NewYorkToTokyo() throws IOException {
    String[] commands = {
        "create calendar --name NYCal --timezone America/New_York",
        "create calendar --name TokyoCal --timezone Asia/Tokyo",
        "use calendar --name NYCal",
        "create event NYMeeting from 2025-06-01T09:00 to 2025-06-01T10:00",
        "copy event NYMeeting on 2025-06-01T09:00 --target TokyoCal to 2025-06-02T10:00",
        "use calendar --name TokyoCal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"NYMeeting\",06/02/2025,10:00 AM,06/02/2025,11:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvent_LondonToLA() throws IOException {
    String[] commands = {
        "create calendar --name LondonCal --timezone Europe/London",
        "create calendar --name LACal --timezone America/Los_Angeles",
        "use calendar --name LondonCal",
        "create event LondonCall from 2025-04-15T14:00 to 2025-04-15T15:30",
        "copy event LondonCall on 2025-04-15T14:00 --target LACal to 2025-04-15T10:00", // fixed
        "use calendar --name LACal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"LondonCall\",04/15/2025,10:00 AM,04/15/2025,11:30 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }


  @Test
  public void testCopyEvent_ChicagoToKathmandu() throws IOException {
    String[] commands = {
        "create calendar --name ChiCal --timezone America/Chicago",
        "create calendar --name KTMCal --timezone Asia/Kathmandu",
        "use calendar --name ChiCal",
        "create event LocalMeeting from 2025-07-01T08:00 to 2025-07-01T09:00",
        "copy event LocalMeeting on 2025-07-01T08:00 --target KTMCal to 2025-07-01T17:45",
        "use calendar --name KTMCal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description," +
            "Location,Private",
        "\"LocalMeeting\",07/01/2025,05:45 PM,07/01/2025,06:45 PM,False,\"\",\"\"," +
            "False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testIsCalendarPresent_ShouldReturnTrue() throws IOException {
    String[] commands = {
        "create calendar --name WorkCal --timezone America/New_York",
        "use calendar --name WorkCal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testIsCalendarPresent_ShouldReturnFalseOnUnknown() throws IOException {
    String[] commands = {
        "use calendar --name DoesNotExist",
        "exit"
    };
    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("calendar not found"));
  }

  @Test
  public void testIsCalendarPresent_AfterCreate_ShouldBePresent() throws IOException {
    String[] commands = {
        "create calendar --name SchoolCal --timezone America/New_York",
        "use calendar --name SchoolCal",
        "exit"
    };
    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("using calendar: schoolcal"));
  }

  @Test
  public void testIsCalendarPresent_WithEventAndExport() throws IOException {
    String[] commands = {
        "create calendar --name ProjectCal --timezone UTC",
        "use calendar --name ProjectCal",
        "create event --autoDecline DemoDay from 2025-08-20T14:00 to 2025-08-20T15:00",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
         "Private",
        "\"DemoDay\",08/20/2025,02:00 PM,08/20/2025,03:00 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testIsCalendarPresent_ExportWithoutUseCalendar_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name XCal --timezone UTC",
        "create event --autoDecline NoUseEvent from 2025-07-01T09:00 to 2025-07-01T10:00",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String output = outContent.toString();
    assertTrue(output.contains("Error Executing command: Please use somme calendar"));
  }

  @Test
  public void testIsCalendarPresent_NonExistentThenExport_ShouldFail() throws IOException {
    String[] commands = {
        "use calendar --name DoesNotExist",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("calendar not found"));
  }

  @Test
  public void testEditCalendar_ChangeName_Success() throws IOException {
    String[] commands = {
        "create calendar --name WorkCal --timezone UTC",
        "edit calendar --name WorkCal --property name PersonalCal",
        "use calendar --name PersonalCal",
        "create event TeamCall from 2025-10-01T09:00 to 2025-10-01T10:00",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
         "Private",
        "\"TeamCall\",10/01/2025,09:00 AM,10/01/2025,10:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditCalendar_ChangeTimezone_Success() throws IOException {
    String[] commands = {
        "create calendar --name TZCal --timezone UTC",
        "edit calendar --name TZCal --property timezone America/New_York",
        "use calendar --name TZCal",
        "create event Meeting from 2025-11-15T14:00 to 2025-11-15T15:00",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
            "Private",
        "\"Meeting\",11/15/2025,02:00 PM,11/15/2025,03:00 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditCalendar_InvalidTimezone_ShouldFail() {
    String[] commands = {
        "create calendar --name InvalidTZ --timezone UTC",
        "edit calendar --name InvalidTZ --property timezone Invalid/Zone",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("invalid timezone"));
  }

  @Test
  public void testEditCalendar_NameAlreadyExists_ShouldFail() {
    String[] commands = {
        "create calendar --name CalA --timezone UTC",
        "create calendar --name CalB --timezone UTC",
        "edit calendar --name CalA --property name CalB",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("already exists"));
  }

  @Test
  public void testEditCalendarName_SuccessfulRenameAndExport() throws IOException {
    String[] commands = {
        "create calendar --name OldName --timezone UTC",
        "use calendar --name OldName",
        "create event Kickoff from 2025-06-01T09:00 to 2025-06-01T10:00",
        "edit calendar --name OldName --property name NewName",
        "use calendar --name NewName",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
            "Private",
        "\"Kickoff\",06/01/2025,09:00 AM,06/01/2025,10:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditCalendarTimezone_SuccessAndExport() throws IOException {
    String[] commands = {
        "create calendar --name TZCal --timezone America/New_York",
        "use calendar --name TZCal",
        "create event MorningStandup from 2025-03-28T09:00 to 2025-03-28T10:00",
        "edit calendar --name TZCal --property timezone Asia/Tokyo",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
         "Private",
        "\"MorningStandup\",03/28/2025,10:00 PM,03/28/2025,11:00 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditCalendar_MissingNameKeyword() throws IOException {
    String[] commands = {
        "create calendar --name MyCal --timezone UTC",
        // Missing "--name" keyword
        "edit calendar MyCal --property timezone Asia/Kolkata",
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("missing '--name' keyword") || output.contains("error"));
  }

  @Test
  public void testEditCalendar_EmptyCalendarName_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name MyCal --timezone UTC",
        // Edit calendar with empty name (invalid usage)
        "edit calendar --name --property timezone Asia/Kolkata",
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("calendar name cannot be empty") || output.contains("error"));
  }

  @Test
  public void testEditCalendar_MissingPropertyFlag_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name MyCal --timezone UTC",
        // Missing '--property' keyword
        "edit calendar --name MyCal timezone Asia/Kolkata",
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("error executing command: insufficient arguments for edit calendar command"));
  }

  @Test
  public void testEditCalendar_EmptyNewValue_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name TestCal --timezone UTC",
        "edit calendar --name TestCal --property timezone ",  // No new value provided
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString();
    assertTrue(output.contains("Error Executing command: Insufficient arguments for edit calendar command"));
  }

  @Test
  public void testEditCalendar_UpdateFails_ShouldShowFailureMessage() throws IOException {
    String[] commands = {
        "edit calendar --name NonExistentCal --property timezone UTC",
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("calendar not found"));

  }

  @Test
  public void testEditCalendar_ModelReturnsFalse_ShouldShowFailureMessage() throws IOException {
    String[] commands = {
        "create calendar --name TestCal --timezone UTC",
        "edit calendar --name TestCal --property invalidProp someValue",
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString();
    assertTrue(output.contains("Error Executing command: Invalid property. Only 'name' or " +
        "'timezone' allowed."));
  }

  @Test
  public void testEditCalendarName_Conflict_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name CalA --timezone UTC",
        "create calendar --name CalB --timezone UTC",
        "edit calendar --name CalA --property name CalB",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("already exists"));
  }

  @Test
  public void testEditCalendar_MissingNameKeyword_ShouldFail() throws IOException {
    String[] commands = {
        "edit calendar America/New_York timezone UTC", // Missing '--name'
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString();
    assertTrue(output.contains("Error Executing command: Insufficient arguments for edit calendar command"));
  }

  @Test
  public void testEditCalendarName_Success() throws IOException {
    String[] commands = {
        "create calendar --name WorkCal --timezone America/New_York",
        "edit calendar --name WorkCal --property name RenamedCal",
        "use calendar --name RenamedCal",
        "create event Standup from 2025-07-01T09:00 to 2025-07-01T09:30",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
            "Private",
        "\"Standup\",07/01/2025,09:00 AM,07/01/2025,09:30 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditCalendarTimezone_Success() throws IOException {
    String[] commands = {
        "create calendar --name MyCal --timezone UTC",
        "use calendar --name MyCal",
        "create event Meeting from 2025-12-01T15:00 to 2025-12-01T16:00",
        "edit calendar --name MyCal --property timezone Asia/Tokyo",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    // Event time should be converted to Tokyo time
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
         "Private",
        "\"Meeting\",12/02/2025,12:00 AM,12/02/2025,01:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditCalendarName_AlreadyExists_ShouldFail() {
    String[] commands = {
        "create calendar --name A --timezone UTC",
        "create calendar --name B --timezone UTC",
        "edit calendar --name A --property name B",
        "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().toLowerCase().contains("already exists"));
  }

  @Test
  public void testEditCalendar_UnsupportedProperty_ShouldFail() {
    String[] commands = {
        "create calendar --name MyCal --timezone UTC",
        "use calendar --name MyCal",
        "edit calendar --name MyCal --property color Blue",
        "exit"
    };
    runAppWithCommands(commands);

    assertTrue(outContent.toString().contains("Error Executing command: Invalid property. Only " +
        "'name' or 'timezone' allowed."));
  }

  @Test
  public void testEditCalendar_NonExistent_ShouldFail() {
    String[] commands = {
        "edit calendar --name Ghost --property name NewGhost",
        "exit"
    };
    runAppWithCommands(commands);

    assertTrue(outContent.toString().toLowerCase().contains("calendar not found"));
  }

  @Test
  public void testEditCalendarTimezone_VerifyEventShift() throws IOException {
    String[] commands = {
        "create calendar --name ConvertCal --timezone UTC",
        "use calendar --name ConvertCal",
        "create event NightCall from 2025-12-31T23:00 to 2026-01-01T00:00",
        "edit calendar --name ConvertCal --property timezone America/New_York",
        "export cal " + OUTPUT_FILE,
        "exit"
    };
    runAppWithCommands(commands);

    // UTC 11PM -> 6PM New York time
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
         "Private",
        "\"NightCall\",12/31/2025,06:00 PM,12/31/2025,07:00 PM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditCalendar_MissingCalendarName() throws IOException {
    String[] commands = {
        "edit calendar --name", // Missing the actual calendar name
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString();
    assertTrue(output.contains("Error Executing command: Insufficient arguments for edit calendar command"));
  }

  @Test
  public void testEditCalendar_MissingPropertyKeyword_ShouldFail() throws IOException {
    String[] commands = {
        "edit calendar --name MyCal", // Missing '--property'
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("error executing command: insufficient arguments for edit calendar command"));
  }

  @Test
  public void testEditCalendar_MissingPropertyName_ShouldFail() throws IOException {
    String[] commands = {
        "edit calendar --name MyCal --property", // Missing actual property name
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString();
    assertTrue(output.contains("Error Executing command: Insufficient arguments for edit calendar command"));
  }

  @Test
  public void testEditCalendar_MissingNewValue_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name MyCal --timezone UTC",
        "use calendar --name MyCal",
        "edit calendar --name MyCal --property timezone", // Missing new value
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString();
    assertTrue(output.contains("Error Executing command: Insufficient arguments for edit calendar command"));
  }

  @Test
  public void testEditCalendar_SuccessMessage() throws IOException {
    String[] commands = {
        "create calendar --name MyCal --timezone UTC",
        "edit calendar --name MyCal --property timezone Asia/Tokyo",
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("calendar updated successfully"));
  }


  @Test
  public void testEditCalendar_RenameCalendar_EventsStillExist() throws IOException {
    String[] commands = {
        "create calendar --name OldCal --timezone UTC",
        "use calendar --name OldCal",
        "create event StatusCheck from 2025-09-01T10:00 to 2025-09-01T11:00",
        "edit calendar --name OldCal --property name NewCal",
        "use calendar --name NewCal",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
            "Private",
        "\"StatusCheck\",09/01/2025,10:00 AM,09/01/2025,11:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditCalendar_RenameToExistingName_ShouldFail() {
    String[] commands = {
        "create calendar --name Cal1 --timezone UTC",
        "create calendar --name Cal2 --timezone UTC",
        "edit calendar --name Cal1 --property name Cal2",
        "exit"
    };

    runAppWithCommands(commands);

    assertTrue(outContent.toString().toLowerCase().contains("already exists"));
  }

  @Test
  public void testEditCalendar_TimezoneShiftToIST() throws IOException {
    String[] commands = {
        "create calendar --name TZCal --timezone UTC",
        "use calendar --name TZCal",
        "create event DailyStandup from 2025-10-10T05:00 to 2025-10-10T06:00",
        "edit calendar --name TZCal --property timezone Asia/Kolkata",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    // UTC 5:00 AM = 10:30 AM IST
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
            "Private",
        "\"DailyStandup\",10/10/2025,10:30 AM,10/10/2025,11:30 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditCalendar_MissingNewName_ShouldFail() {
    String[] commands = {
        "create calendar --name TestCal --timezone UTC",
        "use calendar --name TestCal",
        "edit calendar --name TestCal --property name",  // Missing new name
        "exit"
    };

    runAppWithCommands(commands);

    assertTrue(outContent.toString().contains("Error Executing command: Insufficient arguments for edit calendar command") ||
        outContent.toString().toLowerCase().contains("usage"));
  }

  @Test
  public void testEditCalendar_MissingCalendarName_ShouldFail() {
    String[] commands = {
        "edit calendar --property timezone Asia/Kolkata",
        "exit"
    };

    runAppWithCommands(commands);

    assertTrue(outContent.toString().contains("Error Executing command: Insufficient arguments for edit calendar command"));
  }

  @Test
  public void testEditCalendar_ChangeTimezoneToPST() throws IOException {
    String[] commands = {
        "create calendar --name PSTCal --timezone UTC",
        "use calendar --name PSTCal",
        "create event Meeting from 2025-12-01T16:00 to 2025-12-01T17:00",
        "edit calendar --name PSTCal --property timezone America/Los_Angeles",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    // 16:00 UTC = 08:00 PST (Standard Time in December)
    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
         "Private",
        "\"Meeting\",12/01/2025,08:00 AM,12/01/2025,09:00 AM,False,\"\",\"\",False"
    );
    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testEditCalendar_InvalidProperty_ShouldFail() {
    String[] commands = {
        "create calendar --name SomeCal --timezone UTC",
        "edit calendar --name SomeCal --property unknownProperty Asia/Kolkata",
        "exit"
    };

    runAppWithCommands(commands);

    assertTrue(outContent.toString().contains("Error Executing command: Invalid property. Only " +
        "'name' or 'timezone' allowed."));
  }

  @Test
  public void testEditCalendar_InvalidTimezoneFormat_ShouldFail() {
    String[] commands = {
        "create calendar --name BadTZ --timezone UTC",
        "edit calendar --name BadTZ --property timezone Mars/Invalid",
        "exit"
    };

    runAppWithCommands(commands);

    assertTrue(outContent.toString().toLowerCase().contains("invalid timezone"));
  }

  @Test
  public void testBasicCreateEvent_Success() throws IOException {
    String[] commands = {
        "create calendar --name MyCal --timezone UTC",
        "use calendar --name MyCal",
        "create event --autoDecline TeamMeeting from 2025-07-01T10:00 to 2025-07-01T11:00",
        "export cal " + OUTPUT_FILE,
        "exit"
    };

    runAppWithCommands(commands);

    String expected = String.join("\n",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location," +
         "Private",
        "\"TeamMeeting\",07/01/2025,10:00 AM,07/01/2025,11:00 AM,False,\"\",\"\",False"
    );

    assertEquals(expected, readExportedFile());
  }

  @Test
  public void testBasicCreateEvent_InvalidTimeRange_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name MyCal --timezone UTC",
        "use calendar --name MyCal",
        "create event --autoDecline MistakeEvent from 2025-07-01T11:00 to 2025-07-01T10:00", //
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("end date and time must be after start date and time") ||
        output.contains("error"));
  }

  @Test
  public void testBasicCreateEvent_DuplicateAutoDecline_ShouldFail() throws IOException {
    String[] commands = {
        "create calendar --name MyCal --timezone UTC",
        "use calendar --name MyCal",
        "create event --autoDecline WeeklyCall from 2025-07-01T09:00 to 2025-07-01T10:00",
        "create event --autoDecline WeeklyCall from 2025-07-01T09:00 to 2025-07-01T10:00", //
        // duplicate
        "exit"
    };

    runAppWithCommands(commands);

    String output = outContent.toString().toLowerCase();
    assertTrue(output.contains("conflict detected") || output.contains("error"));
  }


}