package calendarApp;

import static org.junit.Assert.*;
import org.junit.*;


import java.io.*;
import java.lang.reflect.Method;

import model.ICalendarModel;

public class CalendarAppTest {

  private final PrintStream originalOut = System.out;
  private ByteArrayOutputStream outContent;

  @Before
  public void setUp() {
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
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
}
