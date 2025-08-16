package view;

import model.IReadOnlyCalendarModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.*;

public class ViewFactoryTest {

  private IReadOnlyCalendarModel mockModel;
  private String[] validHeadlessArgs;
  private String[] insufficientHeadlessArgs;

  @Before
  public void setUp() throws IOException {
    // Create a simple manual mock of IReadOnlyCalendarModel
    mockModel = new TestReadOnlyCalendarModel();

    // Create commands.txt file in the current directory
    File commandsFile = new File("commands2.txt");
    try (FileWriter writer = new FileWriter(commandsFile)) {
      // Add any test commands you want to execute
      writer.write("show status on 2023-01-01T10:00\n");
      writer.write("print events on 2023-01-01\n");
      // Make sure to end with exit command
      writer.write("exit");
    }

    // Make sure the file is deleted when the JVM exits
    commandsFile.deleteOnExit();

    validHeadlessArgs = new String[]{"--mode", "headless", "commands2.txt"};
    insufficientHeadlessArgs = new String[]{"--mode", "headless"};
  }

  @After
  public void tearDown() {
    File commandsFile = new File("commands.txt");
    if (commandsFile.exists()) {
      commandsFile.delete();
    }
  }

  @Test
  public void testCreateInteractiveView() throws FileNotFoundException {
    IView view = ViewFactory.createView("interactive", new String[]{}, mockModel);
    assertNotNull("Interactive view should not be null", view);
    assertTrue("Should return InteractiveConsoleView", view instanceof InteractiveConsoleView);
  }

  @Test
  public void testCreateHeadlessView() throws FileNotFoundException {
    IView view = ViewFactory.createView("headless", validHeadlessArgs, mockModel);
    assertNotNull("Headless view should not be null", view);
    assertTrue("Should return HeadlessConsoleView", view instanceof HeadlessConsoleView);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateHeadlessViewWithInsufficientArgs() throws FileNotFoundException {
    ViewFactory.createView("headless", insufficientHeadlessArgs, mockModel);
  }


  @Test(expected = IllegalArgumentException.class)
  public void testCreateViewWithNullType() throws FileNotFoundException {
    ViewFactory.createView(null, new String[]{}, mockModel);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateViewWithUnsupportedType() throws FileNotFoundException {
    ViewFactory.createView("unsupported", new String[]{}, mockModel);
  }


  @Test
  public void testCreateViewWithMixedCaseType() throws FileNotFoundException {
    // Additional case-insensitivity test
    IView view = ViewFactory.createView("InTeRaCtIvE", new String[]{}, mockModel);
    assertNotNull("Interactive view should not be null with mixed case type", view);
    assertTrue("Should return InteractiveConsoleView with mixed case type", view instanceof InteractiveConsoleView);
  }

  // Simple manual mock implementation of IReadOnlyCalendarModel
  private static class TestReadOnlyCalendarModel implements IReadOnlyCalendarModel {
    @Override
    public java.util.List<model.ICalendarEventDTO> getEventsInRange(String calendarName,
                                                                    java.time.LocalDateTime fromDateTime, java.time.LocalDateTime toDateTime) {
      return new java.util.ArrayList<>();
    }

    @Override
    public java.util.List<model.ICalendarEventDTO> getEventsInSpecificDateTime(String calendarName,
                                                                               java.time.LocalDateTime dateTime) {
      return new java.util.ArrayList<>();
    }

    @Override
    public boolean isCalendarPresent(String calName) {
      return true;
    }

    @Override
    public boolean isCalendarAvailable(String calName, java.time.LocalDate date) {
      return true;
    }

    @Override
    public java.util.List<String> getCalendarNames() {
      return new java.util.ArrayList<>();
    }

    @Override
    public String getCalendarTimeZone(String calendarName) {
      return "UTC";
    }
  }
}