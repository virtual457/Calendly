package calendarApp;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

import model.ICalendarModel;

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
    } catch (InterruptedException ignored) {}
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
  public void testCreateModel_ShouldNotReturnNull() throws Exception {
    Method method = CalendarApp.class.getDeclaredMethod("createModel");
    method.setAccessible(true);
    ICalendarModel model = (ICalendarModel) method.invoke(null);
    assertNotNull(model);
  }

  //Integrration tests


  private void runAppWithCommands(String[] commands) throws IOException {
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
      ByteArrayInputStream inContent = new ByteArrayInputStream(String.join("\n", commands).getBytes());
      System.setIn(inContent);
      String[] args = {"--mode", mode};
      CalendarApp.main(args);
    }
  }

  private String readExportedFile() throws IOException {
    return new String(Files.readAllBytes(Paths.get(OUTPUT_FILE)))
            .trim()
            .replace("\r\n", "\n")
            .replace("\r", "\n");
  }

  @Test
  public void testCreateAndExportSingleEvent() throws IOException {
    String[] commands = {
            "create event Meeting from 2025-04-10T10:00 to 2025-04-10T11:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
            "\"Meeting\",04/10/2025,10:00 AM,04/10/2025,11:00 AM,False,\"\",\"\",False", readExportedFile());
  }


  @Test
  public void testCreateSingleEvent() throws IOException {
    String[] commands = {
            "create event Meeting from 2025-03-20T10:00 to 2025-03-20T11:00",
            "print events on 2025-03-20",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Event created successfully."));
    assertTrue(outContent.toString().contains("Meeting: 2025-03-20T10:00 to 2025-03-20T11:00"));
  }

  @Test
  public void testCreateRecurringEvent() throws IOException {
    String[] commands = {
            "create event Standup from 2025-03-21T09:00 to 2025-03-21T09:30 repeats MTWRF for 10 times",
            "print events on 2025-03-21",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Event created successfully."));
    assertTrue(outContent.toString().contains("Standup: 2025-03-21T09:00 to 2025-03-21T09:30"));
  }

  @Test
  public void testCreateRecurringEventUntil() throws IOException {
    String[] commands = {
            "create event Yoga from 2025-03-21T07:00 to 2025-03-21T08:00 repeats MW until 2025-04-21T09:30",
            "print events on 2025-03-24",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Event created successfully."));
    assertTrue(outContent.toString().contains("Yoga: 2025-03-24T07:00 to 2025-03-24T08:00"));
  }

  @Test
  public void testCreateAllDayEvent() throws IOException {
    String[] commands = {
            "create event Conference on 2025-03-25",
            "print events on 2025-03-25",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Event created successfully."));
    assertTrue(outContent.toString().contains("Conference: 2025-03-25T00:00 to 2025-03-25T23:59:59"));
  }

  @Test
  public void testCreateRecurringAllDayEvent() throws IOException {
    String[] commands = {
            "create event Holiday on 2025-03-20 repeats SU for 5 times",
            "print events on 2025-03-23",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Event created successfully."));
    assertTrue(outContent.toString().contains("Holiday: 2025-03-23T00:00 to 2025-03-23T23:59:59"));
  }

  @Test
  public void testCreateRecurringAllDayEvent2() throws IOException {
    String[] commands = {
            "create event Holiday on 2025-03-20 repeats SU for 5 times",
            "print events on 2025-03-20",
            "exit"
    };
    runAppWithCommands(commands);
    assertTrue(outContent.toString().contains("Event created successfully."));
    if (LocalDate.of(2025, 3, 20).getDayOfWeek() == DayOfWeek.SUNDAY) {
      assertTrue(outContent.toString().contains("Holiday: 2025-03-20T00:00 to 2025-03-20T23:59:59"));
    } else {
      assertFalse(outContent.toString().contains("Holiday: 2025-03-20T00:00 to 2025-03-20T23:59:59"));
    }
  }

  @Test
  public void testCreateAndExportSingleEvent2() throws IOException {
    String[] commands = {
            "create event \"Meeting\" from 2025-04-10T10:00 to 2025-04-10T11:00",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
            "\"Meeting\",04/10/2025,10:00 AM,04/10/2025,11:00 AM,False,\"\",\"\",False", readExportedFile());
  }

  @Test
  public void testCreatePrivateEventAndExport() throws IOException {
    String[] commands = {
            "create event \"OneOnOne\" from 2025-04-11T14:00 to 2025-04-11T14:30 -private",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
            "\"OneOnOne\",04/11/2025,02:00 PM,04/11/2025,02:30 PM,False,\"\",\"\",True", readExportedFile());
  }

  @Test
  public void testCreateRecurringEventAndExport() throws IOException {
    String[] commands = {
            "create event \"Workout\" from 2025-04-15T07:00 to 2025-04-15T08:00 repeats MW for 2 times",
            "export cal " + OUTPUT_FILE,
            "exit"
    };
    runAppWithCommands(commands);
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n" +
            "\"Workout\",04/15/2025,07:00 AM,04/15/2025,08:00 AM,False,\"\",\"\",False\n" +
            "\"Workout\",04/17/2025,07:00 AM,04/17/2025,08:00 AM,False,\"\",\"\",False", readExportedFile());
  }
}
