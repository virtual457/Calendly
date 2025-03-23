package controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.CalendarEvent;
import model.CalendarModel;
import model.ICalendarEventDTO;
import model.ICalendarModel;
import view.IView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A set of parameterized JUnit tests for verifying the behavior of the CalendarController.
 * <p>
 * This test class is run with the JUnit {@code Parameterized} runner, allowing
 * multiple parameter sets to be tested against the same test methods. It helps
 * cover a variety of input scenarios more comprehensively.
 * </p>
 */

@RunWith(Parameterized.class)
public class CalendarControllerTest {
  private ICalendarController controller;
  private TestCalendarModel model;
  private ByteArrayOutputStream outputStream;
  private PrintStream originalOut;
  private MockView view;
  private String mode;
  String[] options = {"-location Office", "-description Quarterlymeeting", "-private"};
  String[] spacedOptions = {"-location 'Off ice'", "-description 'Quarterly meeting'", "-private"};
  String[] spacedOptions2 = {"-location Office", "-description 'Quarterly meeting'", "-private"};
  String[] spacedOptions3 = {"-location 'Off ice'", "-description Quarterlymeeting", "-private"};

  public CalendarControllerTest(String mode) {
    this.mode = mode;
  }

  @Before
  public void setUp() {
    model = new TestCalendarModel();
    view = new MockView();
    controller = new CalendarController(model, view);
    outputStream = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(outputStream));
  }

  private void resetModels() {
    model = new TestCalendarModel();
    view = new MockView();
    controller = new CalendarController(model, view);
    outputStream = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(outputStream));
  }

  /**
   * A set of parameterized JUnit tests for verifying the behavior of the CalendarController.
   */

  @Parameterized.Parameters
  public static Collection<Object[]> modes() {
    return Arrays.asList(new Object[][]{
            {"interactive"},
            {"headless"}
    });
  }

  private void testCommandInBothModes(String mode, String command) {
    // Interactive mode
    try {
      resetModels();
      if (mode.equalsIgnoreCase("interactive")) {
        InputStream inputStream = new ByteArrayInputStream((command + "\nexit\n").getBytes());
        System.setIn(inputStream);
        controller.run("interactive", null);
        assertTrue(view.getDisplayedMessages().contains("Welcome to the Calendar App!"));
      } else if (mode.equalsIgnoreCase("headless")) {
        resetModels();
        File tempFile = File.createTempFile("commands", ".txt");
        try (FileWriter writer = new FileWriter(tempFile)) {
          writer.write(command + "\nexit\n");
        }
        controller.run("headless", tempFile.getAbsolutePath());
        assertTrue(view.getDisplayedMessages().contains("Welcome to the Calendar App!"));
      }
    } catch (IOException e) {
      fail("IOException thrown: " + e.getMessage());
    }
  }


  @Test
  public void testCreateEventVariations() {
    String baseCommand = "create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T11:00";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertTrue(model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("TeamMeeting", model.lastAddedEvent.getEventName());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateEventVariationsWithSpacedOptions() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 to 2024-03-20T11:00";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertTrue(model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());

      if (command.contains("-location")) {
        assertEquals("Off ice", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterly meeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateEventVariationsWithSpacedOptions1() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 to 2024-03-20T11:00";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertTrue(model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateEventVariationsWithSpacedOptions2() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 to 2024-03-20T11:00";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions2);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertTrue(model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterly meeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateEventVariationsWithSpacedOptions3() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 to 2024-03-20T11:00";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions3);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertTrue(model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());

      if (command.contains("-location")) {
        assertEquals("Off ice", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting",
                model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }


  //tests forr crrerate reccurring events repeates for times
  @Test
  public void testCreateRecurringEventForNTimes() {
    testCommandInBothModes(mode, "create event TeamMeeting from 2024-03-20T10:00"
            + " to 2024-03-20T11:00 repeats MRU for 5 times");
    assertNotNull(model.lastAddedEvent);
    assertTrue(model.lastAddedEvent.isRecurring());
    assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
            model.lastAddedEvent.getRecurrenceDays());
    assertEquals(Integer.valueOf(5), model.lastAddedEvent.getRecurrenceCount());
  }

  @Test
  public void testCreateRecurringEventForNTimesVariations() {
    String baseCommand = "create event TeamMeeting from 2024-03-20T10:00 to"
            + " 2024-03-20T11:00 repeats MRU for 5 times";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("TeamMeeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
              model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(5), model.lastAddedEvent.getRecurrenceCount());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());
      assertTrue(model.lastAddedEvent.isRecurring());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringEventForNTimesVariationsSpacedOptions() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 "
            + "to 2024-03-20T11:00 repeats MRU for 5 times";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
              model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(5), model.lastAddedEvent.getRecurrenceCount());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());
      assertTrue(model.lastAddedEvent.isRecurring());


      if (command.contains("-location")) {
        assertEquals("Off ice", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterly meeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringEventForNTimesVariationsSpacedOptions1() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 to"
            + " 2024-03-20T11:00 repeats MRU for 5 times";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
              model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(5), model.lastAddedEvent.getRecurrenceCount());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());
      assertTrue(model.lastAddedEvent.isRecurring());


      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringEventForNTimesVariationsSpacedOptions2() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 to 2"
            + "024-03-20T11:00 repeats MRU for 5 times";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions2);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
              model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(5), model.lastAddedEvent.getRecurrenceCount());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());
      assertTrue(model.lastAddedEvent.isRecurring());


      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterly meeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringEventForNTimesVariationsSpacedOptions3() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 "
            + "to 2024-03-20T11:00 repeats MRU for 5 times";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions3);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY,
              DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(5), model.lastAddedEvent.getRecurrenceCount());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Off ice", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  //TODO create a fuzzy test for N times

  //tests create a recurring date until a date
  @Test
  public void testCreateRecurringEventUntilDate() {
    testCommandInBothModes(mode, "create event TeamMeeting from "
            + "2024-03-20T10:00 to 2024-03-20T11:00 repeats MRU until 2024-04-20T10:00");
    assertNotNull(model.lastAddedEvent);
    assertTrue(model.lastAddedEvent.isRecurring());
    assertEquals(LocalDateTime.parse("2024-04-20T10:00"),
            model.lastAddedEvent.getRecurrenceEndDate());
  }

  @Test
  public void testCreateRecurringEventUntilDateVariations() {
    String baseCommand = "create event TeamMeeting from 2024-03-20T10:00 to "
            + "2024-03-20T11:00 repeats MRU until 2024-04-20T10:00";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("TeamMeeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
              model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T10:00"),
              model.lastAddedEvent.getRecurrenceEndDate());
      assertTrue(model.lastAddedEvent.isRecurring());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringEventUntilDateVariationsWithSpacedOptions() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 to "
            + "2024-03-20T11:00 repeats MRU until 2024-04-20T10:00";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY,
              DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T10:00"),
              model.lastAddedEvent.getRecurrenceEndDate());
      assertTrue(model.lastAddedEvent.isRecurring());

      if (command.contains("-location")) {
        assertEquals("Off ice", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterly meeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringEventUntilDateVariationsWithSpacedOptions1() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 to "
            + "2024-03-20T11:00 repeats MRU until 2024-04-20T10:00";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
              model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T10:00"),
              model.lastAddedEvent.getRecurrenceEndDate());
      assertTrue(model.lastAddedEvent.isRecurring());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringEventUntilDateVariationsWithSpacedOptions2() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 to "
            + "2024-03-20T11:00 repeats MRU until 2024-04-20T10:00";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions2);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY,
              DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T10:00"),
              model.lastAddedEvent.getRecurrenceEndDate());
      assertTrue(model.lastAddedEvent.isRecurring());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterly meeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringEventUntilDateVariationsWithSpacedOptions3() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 "
            + "to 2024-03-20T11:00 repeats MRU until 2024-04-20T10:00";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions3);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
              model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T10:00"),
              model.lastAddedEvent.getRecurrenceEndDate());
      assertTrue(model.lastAddedEvent.isRecurring());

      if (command.contains("-location")) {
        assertEquals("Off ice", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }


  //Tests for creating all day event

  @Test
  public void testCreateAllDayEvent() {
    testCommandInBothModes(mode, "create event TeamMeeting on 2024-03-20");
    assertNotNull(model.lastAddedEvent);
    assertEquals(LocalDateTime.parse("2024-03-20T00:00"),
            model.lastAddedEvent.getStartDateTime());
    assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"),
            model.lastAddedEvent.getEndDateTime());
  }

  @Test
  public void testCreateAllDayEventVariations() {
    String baseCommand = "create event TeamMeeting on 2024-03-20";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("TeamMeeting", model.lastAddedEvent.getEventName());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertTrue(model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateAllDayEventVariationsWithSpacedOptions() {
    String baseCommand = "create event 'Team Meeting' on 2024-03-20";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertTrue(model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Off ice", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterly meeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateAllDayEventVariationsWithSpacedOptions1() {
    String baseCommand = "create event 'Team Meeting' on 2024-03-20";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertTrue(model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateAllDayEventVariationsWithSpacedOptions2() {
    String baseCommand = "create event 'Team Meeting' on 2024-03-20";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions2);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertTrue(model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterly meeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateAllDayEventVariationsWithSpacedOptions3() {
    String baseCommand = "create event 'Team Meeting' on 2024-03-20";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions3);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertTrue(model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Off ice", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  //TODO Tests for event on datetime string for date string
  //TODO write testes to validate week of the day

  @Test
  public void testCreateRecurringAllDayEventForNTimes() {
    testCommandInBothModes(mode, "create event TeamMeeting on "
            + "2024-03-20 repeats MRU for 5 times");
    assertNotNull(model.lastAddedEvent);
    assertTrue(model.lastAddedEvent.isRecurring());
    assertEquals(Integer.valueOf(5), model.lastAddedEvent.getRecurrenceCount());
  }

  @Test
  public void testCreateRecurringAllDayEventForNTimesVariations() {
    String baseCommand = "create event TeamMeeting on 2024-03-20 repeats MRU for 5 times";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("TeamMeeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
              model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(5), model.lastAddedEvent.getRecurrenceCount());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringAllDayEventForNTimesVariationsWithSpacedOptions() {
    String baseCommand = "create event 'Team Meeting' on 2024-03-20 repeats MRU for 5 times";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
              model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(5), model.lastAddedEvent.getRecurrenceCount());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Off ice", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterly meeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringAllDayEventForNTimesVariationsWithSpacedOptions1() {
    String baseCommand = "create event 'Team Meeting' on 2024-03-20 repeats MRU for 5 times";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
              model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(5), model.lastAddedEvent.getRecurrenceCount());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringAllDayEventForNTimesVariationsWithSpacedOptions2() {
    String baseCommand = "create event 'Team Meeting' on 2024-03-20 repeats MRU for 5 times";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions2);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
              model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(5), model.lastAddedEvent.getRecurrenceCount());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterly meeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringAllDayEventForNTimesVariationsWithSpacedOptions3() {
    String baseCommand = "create event 'Team Meeting' on 2024-03-20 repeats MRU for 5 times";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions3);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
              model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(5), model.lastAddedEvent.getRecurrenceCount());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Off ice", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringAllDayEventUntilDate() {
    testCommandInBothModes(mode, "create event TeamMeeting on 2024-03-20 "
            + "repeats MRU until 2024-04-20");
    assertNotNull(model.lastAddedEvent);
    assertTrue(model.lastAddedEvent.isRecurring());
    assertEquals(LocalDateTime.parse("2024-04-20T23:59:59"),
            model.lastAddedEvent.getRecurrenceEndDate());
  }

  @Test
  public void testCreateRecurringAllDayEventForEventUntilDateVariations() {
    String baseCommand = "create event TeamMeeting on 2024-03-20 repeats MRU until 2024-04-20";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("TeamMeeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
              model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T23:59:59"),
              model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringAllDayEventForEventUntilDateVariationsWithSpacedOptions() {
    String baseCommand = "create event 'Team Meeting' on 2024-03-20 repeats MRU until 2024-04-20";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
              model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T23:59:59"),
              model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Off ice", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterly meeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringAllDayEventForEventUntilDateVariationsWithSpacedOptions1() {
    String baseCommand = "create event 'Team Meeting' on 2024-03-20 repeats MRU until 2024-04-20";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
              model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T23:59:59"),
              model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringAllDayEventForEventUntilDateVariationsWithSpacedOptions2() {
    String baseCommand = "create event 'Team Meeting' on 2024-03-20 repeats MRU until 2024-04-20";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions2);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
              model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T23:59:59"),
              model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterly meeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringAllDayEventForEventUntilDateVariationsWithSpacedOptions3() {
    String baseCommand = "create event 'Team Meeting' on 2024-03-20 repeats MRU until 2024-04-20";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions3);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"),
              model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"),
              model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
              model.lastAddedEvent.getRecurrenceDays());
      assertEquals(Integer.valueOf(0), model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T23:59:59"),
              model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Off ice", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if (command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }


  private List<String> generateCommandCombinations(String baseCommand, String[] options) {
    List<String> commands = new ArrayList<>();
    int n = options.length;
    for (int i = 0; i < (1 << n); i++) {
      StringBuilder command = new StringBuilder(baseCommand);
      for (int j = 0; j < n; j++) {
        if ((i & (1 << j)) != 0) {
          command.append(" ").append(options[j]);
        }
      }
      commands.add(command.toString().trim());
      commands.add(command.insert("create event".length(), " --autoDecline").toString().trim());
    }
    return commands;
  }


  //Edit Command scenarios
  @Test
  public void testEditEventCallsModelWithCorrectParams() {
    testCommandInBothModes(mode, "edit event name TeamMeeting from"
            + " 2024-03-20T10:00 to 2024-03-20T10:30 with UpdatedMeeting");
    assertEquals("name", model.lastEditEventProperty);
    assertEquals("TeamMeeting", model.lastEditEventName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventStartDateTime);
    assertEquals(LocalDateTime.parse("2024-03-20T10:30"), model.lastEditEventEndDateTime);
    assertEquals("UpdatedMeeting", model.lastEditEventNewValue);
  }

  @Test
  public void testEditEventName() {
    String command = "edit event name TeamMeeting from 2024-03-20T10:00 to 2024-03-20T10:30"
            + " with UpdatedMeeting";
    testCommandInBothModes(mode, command);
    assertEquals("name", model.lastEditEventProperty);
    assertEquals("UpdatedMeeting", model.lastEditEventNewValue);
    assertEquals("TeamMeeting", model.lastEditEventName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventStartDateTime);
    assertEquals(LocalDateTime.parse("2024-03-20T10:30"), model.lastEditEventEndDateTime);
  }

  @Test
  public void testEditEventDescription() {
    String command = "edit event description TeamMeeting from 2024-03-20T10:00 to "
            + "2024-03-20T10:30 with 'Updated Description'";

    testCommandInBothModes(mode, command);
    assertEquals("description", model.lastEditEventProperty);
    assertEquals("Updated Description", model.lastEditEventNewValue);
    assertEquals("TeamMeeting", model.lastEditEventName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventStartDateTime);
    assertEquals(LocalDateTime.parse("2024-03-20T10:30"), model.lastEditEventEndDateTime);
  }

  @Test
  public void testEditEventLocation() {
    String command = "edit event location TeamMeeting from 2024-03-20T10:00 to"
            + " 2024-03-20T10:30 with 'New Location'";
    testCommandInBothModes(mode, command);
    assertEquals("location", model.lastEditEventProperty);
    assertEquals("New Location", model.lastEditEventNewValue);
    assertEquals("TeamMeeting", model.lastEditEventName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventStartDateTime);
    assertEquals(LocalDateTime.parse("2024-03-20T10:30"), model.lastEditEventEndDateTime);
  }

  @Test
  public void testEditEventStart() {
    String command = "edit event start TeamMeeting from 2024-03-20T10:00 to "
            + "2024-03-20T10:30 with 2024-03-20T10:35";
    testCommandInBothModes(mode, command);
    assertEquals("start", model.lastEditEventProperty);
    assertEquals("2024-03-20T10:35", model.lastEditEventNewValue);
    assertEquals("TeamMeeting", model.lastEditEventName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventStartDateTime);
    assertEquals(LocalDateTime.parse("2024-03-20T10:30"), model.lastEditEventEndDateTime);
  }

  @Test
  public void testEditEventEnd() {
    String command = "edit event end TeamMeeting from 2024-03-20T10:00 to "
            + "2024-03-20T10:30 with 2024-03-20T10:35";
    testCommandInBothModes(mode, command);
    assertEquals("end", model.lastEditEventProperty);
    assertEquals("2024-03-20T10:35", model.lastEditEventNewValue);
    assertEquals("TeamMeeting", model.lastEditEventName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventStartDateTime);
    assertEquals(LocalDateTime.parse("2024-03-20T10:30"), model.lastEditEventEndDateTime);
  }

  @Test
  public void testEditEventIsPublic() {
    String command = "edit event isPublic TeamMeeting from 2024-03-20T10:00 "
            + "to 2024-03-20T10:30 with false";
    testCommandInBothModes(mode, command);
    assertEquals("isPublic", model.lastEditEventProperty);
    assertEquals("false", model.lastEditEventNewValue);
    assertEquals("TeamMeeting", model.lastEditEventName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventStartDateTime);
    assertEquals(LocalDateTime.parse("2024-03-20T10:30"), model.lastEditEventEndDateTime);
  }

  //Edit Events Scenarrio
  @Test
  public void testEditEventsCallsModelWithCorrectParams() {
    testCommandInBothModes(mode, "edit events name TeamMeeting "
            + "from 2024-03-20T10:00 with UpdatedMeeting");
    assertEquals("name", model.lastEditEventsProperty);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventsStartDateTime);
    assertEquals("UpdatedMeeting", model.lastEditEventsNewValue);
  }

  @Test
  public void testEditEventsName() {
    String command = "edit events name TeamMeeting from 2024-03-20T10:00 with UpdatedMeeting";
    testCommandInBothModes(mode, command);
    assertEquals("name", model.lastEditEventsProperty);
    assertEquals("UpdatedMeeting", model.lastEditEventsNewValue);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventsStartDateTime);
  }

  @Test
  public void testEditEventsDescription() {
    String command = "edit events description TeamMeeting from "
            + "2024-03-20T10:00 with 'Updated Description'";
    testCommandInBothModes(mode, command);
    assertEquals("description", model.lastEditEventsProperty);
    assertEquals("Updated Description", model.lastEditEventsNewValue);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventsStartDateTime);
  }

  @Test
  public void testEditEventsLocation() {
    String command = "edit events location TeamMeeting from 2024-03-20T10:00 with 'New Location'";
    testCommandInBothModes(mode, command);
    assertEquals("location", model.lastEditEventsProperty);
    assertEquals("New Location", model.lastEditEventsNewValue);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventsStartDateTime);
  }

  @Test
  public void testEditEventsStart() {
    String command = "edit events start TeamMeeting from 2024-03-20T10:00 with 2024-03-20T10:35";
    testCommandInBothModes(mode, command);
    assertEquals("start", model.lastEditEventsProperty);
    assertEquals("2024-03-20T10:35", model.lastEditEventsNewValue);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventsStartDateTime);
  }

  @Test
  public void testEditEventsEnd() {
    String command = "edit events end TeamMeeting from 2024-03-20T10:00 with 2024-03-20T10:35";
    testCommandInBothModes(mode, command);
    assertEquals("end", model.lastEditEventsProperty);
    assertEquals("2024-03-20T10:35", model.lastEditEventsNewValue);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventsStartDateTime);
  }

  @Test
  public void testEditEventsIsPublic() {
    String command = "edit events isPublic TeamMeeting from 2024-03-20T10:00 with false";
    testCommandInBothModes(mode, command);
    assertEquals("isPublic", model.lastEditEventsProperty);
    assertEquals("false", model.lastEditEventsNewValue);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventsStartDateTime);
  }

  @Test
  public void testEditEventsAtSpecificStartTime() throws IOException {
    String command = "edit events name TeamMeeting from 2024-03-20T10:00 with UpdatedMeeting";
    testCommandInBothModes(mode, command);
    assertEquals("name", model.lastEditEventsProperty);
    assertEquals("UpdatedMeeting", model.lastEditEventsNewValue);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventsStartDateTime);
  }

  @Test
  public void testEditAllMatchingEvents() throws IOException {
    String command = "edit events description TeamMeeting 'Updated Description'";
    testCommandInBothModes(mode, command);
    assertEquals("description", model.lastEditEventsProperty);
    assertEquals("Updated Description", model.lastEditEventsNewValue);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertNull(model.lastEditEventsStartDateTime);
  }

  // End of edit events function
  @Test
  public void testPrintEventsOnDateCallsModelWithCorrectParams() {
    testCommandInBothModes(mode, "print events on 2024-03-20");
    assertEquals(LocalDate.parse("2024-03-20"), model.lastPrintDate);
  }

  @Test
  public void testExportCalendarCallsModelWithCorrectParams() {
    testCommandInBothModes(mode, "export cal events.csv");
    assertEquals("events.csv", model.lastExportFilename);
  }

  @Test
  public void testShowStatusCallsModelWithCorrectParams() {
    testCommandInBothModes(mode, "show status on 2024-03-20T10:30");
    assertEquals(LocalDateTime.parse("2024-03-20T10:30"), model.lastShowStatusDateTime);
  }

  @Test
  public void testPrintEventsInSpecificRangeCallsModelWithCorrectParams() {
    testCommandInBothModes(mode, "print events from 2024-03-20T10:00 to 2024-03-20T12:00");
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastPrintStartDateTime);
    assertEquals(LocalDateTime.parse("2024-03-20T12:00"), model.lastPrintEndDateTime);
  }

  @Test
  public void testInvalidCommandDoesNotCallModel() {
    testCommandInBothModes(mode, "invalid command format");
    assertNull(model.lastAddedEvent);
    assertNull(model.lastEditEventName);
    assertNull(model.lastEditEventsName);
  }

  @Test
  public void testCreateEventAutoDeclineMissingWords() {
    String baseCommand = "create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T10:30";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("create", "Error: Unsupported command.");
    errorCases.put("event", "Error: Expected 'event' after 'create'.");
    errorCases.put("TeamMeeting", "Error: Missing 'from' or 'on' keyword. "
            + "Or EventName is required.");
    errorCases.put("from", "Error: Missing 'from' or 'on' keyword. Or EventName is required.");
    errorCases.put("2024-03-20T10:00", "Error processing command: Text 'to' "
            + "could not be parsed at index 0");
    errorCases.put("to", "Error: Missing 'to' keyword and end time.");
    errorCases.put("2024-03-20T10:30", "Error: Missing end date and time.");

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replace(entry.getKey(), "")
              .replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
  }

  @Test
  public void testCreateRecurringEventUntilMissingWords() {
    String baseCommand = "create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T10:30 "
            + "repeats MTWRFSU until 2024-04-20T10:30";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("create", "Error: Unsupported command.");
    errorCases.put("event", "Error: Expected 'event' after 'create'.");
    errorCases.put("TeamMeeting", "Error: Missing 'from' or 'on' keyword. "
            + "Or EventName is required.");
    errorCases.put("from", "Error: Missing 'from' or 'on' keyword. Or EventName is required.");
    errorCases.put("2024-03-20T10:00", "Error processing command: Text 'to' "
            + "could not be parsed at index 0");
    errorCases.put("to", "Error: Missing 'to' keyword and end time.");
    errorCases.put("2024-03-20T10:30", "Error processing command: Text 'repeats' "
            + "could not be parsed at index 0");
    errorCases.put("repeats", "Error: Unknown option: MTWRFSU");
    errorCases.put("MTWRFSU", "Error: Invalid recurrence day.");
    errorCases.put("until", "Error: command missing until or from.");
    errorCases.put("2024-04-20T10:30", "Error processing command: null");

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replace(entry.getKey(), "")
              .replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
  }

  @Test
  public void testCreateRecurringEventForNTimesMissingWords() {
    String baseCommand = "create event TeamMeeting from 2024-03-20T10:00 to "
            + "2024-03-20T10:30 repeats MTWR for 5 times";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("create", "Error: Unsupported command.");
    errorCases.put("event", "Error: Expected 'event' after 'create'.");
    errorCases.put("TeamMeeting", "Error: Missing 'from' or 'on' keyword. "
            + "Or EventName is required.");
    errorCases.put("from", "Error: Missing 'from' or 'on' keyword. "
            + "Or EventName is required.");
    errorCases.put("2024-03-20T10:00", "Error processing command: "
            + "Text 'to' could not be parsed at index 0");
    errorCases.put("to", "Error: Missing 'to' keyword and end time.");
    errorCases.put("2024-03-20T10:30", "Error processing command: Text "
            + "'repeats' could not be parsed at index 0");
    errorCases.put("repeats", "Error: Unknown option: MTWR");
    errorCases.put("MTWR", "Error: Invalid recurrence day.");
    errorCases.put("for", "Error: command missing until or from.");
    errorCases.put("5", "Error processing command: For input string: \"times\"");
    errorCases.put("times", "Error processing command: null");

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replace(entry.getKey(), "")
              .replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
  }

  @Test
  public void testCreateEventAllDayMissingWords() {
    String baseCommand = "create event TeamMeeting on 2024-03-20T10:00";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("create", "Error: Unsupported command.");
    errorCases.put("event", "Error: Expected 'event' after 'create'.");
    errorCases.put("TeamMeeting", "Error: Missing 'from' or 'on' keyword."
            + " Or EventName is required.");
    errorCases.put("on", "Error: Missing 'from' or 'on' keyword. Or EventName is required.");
    errorCases.put("2024-03-20T10:00", "Error: Missing date for all-day event.");

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replaceFirst("\\b" + entry.getKey()
              + "\\b", "").replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
  }

  @Test
  public void testCreateRecurringAllDayEventForNTimesMissingWords() {
    String baseCommand = "create event TeamMeeting on 2024-03-20 repeats MTWR for 5 times";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("create", "Error: Unsupported command.");
    errorCases.put("event", "Error: Expected 'event' after 'create'.");
    errorCases.put("TeamMeeting", "Error: Missing 'from' or 'on' keyword."
            + " Or EventName is required.");
    errorCases.put("on", "Error: Missing 'from' or 'on' keyword. Or EventName is required.");
    errorCases.put("2024-03-20", "Error processing command: Text "
            + "'repeats' could not be parsed at index 0");
    errorCases.put("repeats", "Error: Unknown option: MTWR");
    errorCases.put("MTWR", "Error: Invalid recurrence day.");
    errorCases.put("for", "Error: command missing until or from.");
    errorCases.put("5", "Error processing command: For input string: \"times\"");
    errorCases.put("times", "Error processing command: null");

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replaceFirst("\\b" +
                      entry.getKey() + "\\b", "")
              .replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
  }

  @Test
  public void testEditEventMissingWords2() {
    String baseCommand = "edit event name TeamMeeting from 2024-03-20T10:00 to "
            + "2024-03-20T10:30 with UpdatedMeeting";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("edit", "Error: Unsupported command.");
    errorCases.put("event", "Error: Invalid event type. Expected 'event' or 'events'.");
    errorCases.put("name", "Error: Missing 'from' keyword.");
    errorCases.put("TeamMeeting", "Error: Missing 'from' keyword.");
    errorCases.put("from", "Error: Missing 'from' keyword.");
    errorCases.put("2024-03-20T10:00", "Error processing command: Text 'to' "
            + "could not be parsed at index 0");
    errorCases.put("to", "Error: Missing 'to' keyword.");
    errorCases.put("2024-03-20T10:30", "Error processing command: Text 'with'"
            + " could not be parsed at index 0");
    errorCases.put("with", "Error: Missing 'with' keyword.");
    errorCases.put("UpdatedMeeting", "Error: Missing new property value.");

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replaceFirst("\\b" + entry.getKey() +
              "\\b", "").replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
  }

  @Test
  public void testCreateRecurringAllDayEventUntilMissingWords() {
    String baseCommand = "create event TeamMeeting on 2024-03-20 repeats MTWR until 2024-04-20";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("create", "Error: Unsupported command.");
    errorCases.put("event", "Error: Expected 'event' after 'create'.");
    errorCases.put("TeamMeeting", "Error: Missing 'from' or 'on' keyword." +
            " Or EventName is required.");
    errorCases.put("on", "Error: Missing 'from' or 'on' keyword. Or EventName is required.");
    errorCases.put("2024-03-20", "Error processing command: Text 'repeats' " +
            "could not be parsed at index 0");
    errorCases.put("repeats", "Error: Unknown option: MTWR");
    errorCases.put("MTWR", "Error: Invalid recurrence day.");
    errorCases.put("until", "Error: command missing until or from.");
    errorCases.put("2024-04-20", "Error processing command: null");

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replaceFirst("\\b" +
                      entry.getKey() + "\\b", "").
              replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
  }

  @Test
  public void testEditEventsFromMissingWords() {
    String baseCommand = "edit events name TeamMeeting from 2024-03-20T10:00 with UpdatedMeeting";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("edit", "Error: Unsupported command.");
    errorCases.put("events", "Error: Invalid event type. Expected 'event' or 'events'.");
    errorCases.put("name", "Error: Invalid Edit events command");
    errorCases.put("TeamMeeting", "Error: Invalid Edit events command");
    errorCases.put("from", "Error: Invalid Edit events command");
    errorCases.put("2024-03-20T10:00", "Error processing command: Text 'with' "
            + "could not be parsed at index 0");
    errorCases.put("with", "Error: Missing 'with' keyword.");
    errorCases.put("UpdatedMeeting", "Error: Missing new property value.");

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replaceFirst("\\b"
                      + entry.getKey() + "\\b", "")
              .replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
  }

  @Test
  public void testEditEventsAllMatchingMissingWords() {
    String baseCommand = "edit events name TeamMeeting UpdatedMeeting";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("edit", "Error: Unsupported command.");
    errorCases.put("events", "Error: Invalid event type. Expected 'event' or 'events'.");
    errorCases.put("name", "Error: Missing new property value.");
    errorCases.put("TeamMeeting", "Error: Missing new property value.");
    errorCases.put("UpdatedMeeting", "Error: Missing new property value.");

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replaceFirst("\\b"
                      + entry.getKey() + "\\b", "")
              .replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
  }


  @Test
  public void testPrintEventsOnMissingWords() {
    String baseCommand = "print events on 2024-03-20";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("print", "Error: Unsupported command.");
    errorCases.put("events", "Error: Expected 'events' after 'print'.");
    errorCases.put("on", "Error: Invalid print command format.");
    errorCases.put("2024-03-20", "Error: Missing date.");

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replaceFirst("\\b" + entry.getKey()
              + "\\b", "").replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
  }

  @Test
  public void testPrintEventsFromToMissingWords() {
    String baseCommand = "print events from 2024-03-20T10:00 to 2024-03-20T12:00";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("print", "Error: Unsupported command.");
    errorCases.put("events", "Error: Expected 'events' after 'print'.");
    errorCases.put("from", "Error: Invalid print command format.");
    errorCases.put("2024-03-20T10:00", "Error processing command: Text "
            + "'to' could not be parsed at index 0");
    errorCases.put("to", "Error: Missing 'to' keyword.");
    errorCases.put("2024-03-20T12:00", "Error: Missing end date and time.");

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replaceFirst("\\b" + entry.getKey()
              + "\\b", "").replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
  }

  @Test
  public void testExportCalMissingWords() {
    String baseCommand = "export cal events.csv";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("export", "Error: Unsupported command.");
    errorCases.put("cal", "Error: Expected 'cal' after 'export'.");
    errorCases.put("events.csv", "Error: Missing filename.");

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replaceFirst("\\b" + entry.getKey()
              + "\\b", "").replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
  }

  @Test
  public void testShowStatusOnMissingWords() {
    String baseCommand = "show status on 2024-03-20T14:00";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("show", "Error: Unsupported command.");
    errorCases.put("status", "Error: Expected 'status' after 'show'.");
    errorCases.put("on", "Error: Expected 'on' keyword.");
    errorCases.put("2024-03-20T14:00", "Error: Missing date and time.");

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replaceFirst("\\b" + entry.getKey()
              + "\\b", "").replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
  }

  @Test
  public void testTest() {
    testCommandInBothModes(mode, "create event TeamMeeting from 2024-03-20T10:00 "
            + "to 2024-03-20T10:30 repeats until 2024-04-20T10:30");
  }


  @Test
  public void testCreateEventMissingWords() {
    String baseCommand = "create event --autoDecline TeamMeeting from "
            + "2024-03-20T10:00 to 2024-03-20T10:30";

    String[] wordsToRemove = {"create", "event", "TeamMeeting", "from",
            "2024-03-20T10:00", "to", "2024-03-20T10:30"};

    for (String word : wordsToRemove) {
      String modifiedCommand = baseCommand.replace(word, "")
              .replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertTrue("Expected an error message", view.getLastDisplayedMessage()
              .toLowerCase().contains("error"));
    }
  }

  @Test
  public void testCreateEventRecurringMissingWords() {
    String baseCommand = "create event --autoDecline TeamMeeting from 2024-03-20T10:00"
            + " to 2024-03-20T10:30 repeats MTWRF for 5 times";

    String[] wordsToRemove = {"create", "event", "TeamMeeting", "from", "2024-03-20T10:00",
            "to", "2024-03-20T10:30", "repeats", "MTWRF", "for", "5", "times"};

    for (String word : wordsToRemove) {
      String modifiedCommand = baseCommand.replace(word, "").replaceAll("\\s+",
              " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertTrue("Expected an error message", view.getLastDisplayedMessage().toLowerCase()
              .contains("error"));
    }
  }

  @Test
  public void testCreateEventRecurringUntilMissingWords() {
    String baseCommand = "create event --autoDecline TeamMeeting from 2024-03-20T10:00 to "
            + "2024-03-20T10:30 repeats MTWRF until 2024-04-20T10:30";

    String[] wordsToRemove = {"create", "event", "TeamMeeting", "from", "2024-03-20T10:00",
            "to", "2024-03-20T10:30", "repeats", "MTWRF", "until", "2024-04-20T10:30"};

    for (String word : wordsToRemove) {
      String modifiedCommand = baseCommand.replace(word, "").replaceAll("\\s+",
              " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertTrue("Expected an error message", view.getLastDisplayedMessage().toLowerCase()
              .contains("error"));
    }
  }

  @Test
  public void testCreateAllDayEventMissingWords() {
    String baseCommand = "create event TeamMeeting on 2024-03-20";

    String[] wordsToRemove = {"create", "event", "TeamMeeting", "on", "2024-03-20"};

    for (String word : wordsToRemove) {
      String modifiedCommand = baseCommand.replace(word, "").replaceAll("\\s+",
              " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertTrue("Expected an error message", view.getLastDisplayedMessage().toLowerCase()
              .contains("error"));
    }
  }

  @Test
  public void testCreateRecurringAllDayEventForMissingWords() {
    String baseCommand = "create event TeamMeeting on 2024-03-20 repeats MTWRF for 5 times";

    String[] wordsToRemove = {"create", "event", "TeamMeeting", "on", "2024-03-20", "repeats",
            "MTWRF", "for", "5", "times"};

    for (String word : wordsToRemove) {
      String modifiedCommand = baseCommand.replace(word, "").replaceAll("\\s+",
              " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertTrue("Expected an error message", view.getLastDisplayedMessage().toLowerCase()
              .contains("error"));
    }
  }

  @Test
  public void testCreateRecurringAllDayEventUntilMissingWords1() {
    String baseCommand = "create event TeamMeeting on 2024-03-20 repeats MTWRF until 2024-04-20";

    String[] wordsToRemove = {"create", "event", "TeamMeeting", "on", "2024-03-20", "repeats",
            "MTWRF", "until", "2024-04-20"};

    for (String word : wordsToRemove) {
      String modifiedCommand = baseCommand.replace(word, "").replaceAll("\\s+",
              " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertTrue("Expected an error message", view.getLastDisplayedMessage().toLowerCase()
              .contains("error"));
    }
  }

  @Test
  public void testEditEventMissingWords() {
    String baseCommand = "edit event name TeamMeeting from 2024-03-20T10:00 to 2024-03-20T10:30"
            + " with UpdatedMeeting";

    String[] wordsToRemove = {"edit", "event", "name", "TeamMeeting", "from",
            "2024-03-20T10:00", "to", "2024-03-20T10:30", "with", "UpdatedMeeting"};

    for (String word : wordsToRemove) {
      String modifiedCommand = baseCommand.replace(word, "").replaceAll("\\s+",
              " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertTrue("Expected an error message", view.getLastDisplayedMessage().toLowerCase()
              .contains("error"));
    }
  }

  @Test
  public void testEditEventsFromMissingWords2() {
    String baseCommand = "edit events name TeamMeeting from 2024-03-20T10:00 with UpdatedMeeting";

    String[] wordsToRemove = {"edit", "events", "name", "TeamMeeting", "from", "2024-03-20T10:00",
            "with", "UpdatedMeeting"};

    for (String word : wordsToRemove) {
      String modifiedCommand = baseCommand.replace(word, "").replaceAll("\\s+",
              " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertTrue("Expected an error message", view.getLastDisplayedMessage().toLowerCase()
              .contains("error"));
    }
  }

  @Test
  public void testEditEventsAllMatchingMissingWords2() {
    String baseCommand = "edit events name TeamMeeting UpdatedMeeting";

    String[] wordsToRemove = {"edit", "events", "name", "TeamMeeting", "UpdatedMeeting"};

    for (String word : wordsToRemove) {
      String modifiedCommand = baseCommand.replace(word, "").replaceAll("\\s+",
              " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertTrue("Expected an error message", view.getLastDisplayedMessage().toLowerCase()
              .contains("error"));
    }
  }

  @Test
  public void testEmptyCommand() {
    String command = "";
    testCommandInBothModes(mode, command);
    assertTrue("Expected an error message for empty command",
            view.getLastDisplayedMessage().toLowerCase().contains("error"));
  }

  //Tests to verify correct output is returned if we are using string return statements
  @Test
  public void testPrintEventsOnSpecificDate() {
    String command = "print events on 2024-03-20";
    testCommandInBothModes(mode, command);
    assertEquals("Printed events.", view.getLastDisplayedMessage());
  }

  @Test
  public void testPrintEventsInSpecificRange() {
    String command = "print events from 2024-03-20T10:00 to 2024-03-20T12:00";
    testCommandInBothModes(mode, command);
    assertEquals("Printed events in range.", view.getLastDisplayedMessage());
  }

  @Test
  public void testExportEvents() {
    String command = "export cal events.csv";
    testCommandInBothModes(mode, command);
    assertEquals("Exported events.", view.getLastDisplayedMessage());
  }

  @Test
  public void testShowStatus() {
    String command = "show status on 2024-03-20T14:00";
    testCommandInBothModes(mode, command);
    assertEquals("Status checked.", view.getLastDisplayedMessage());
  }

  //Tests to validate the mode of the controlleer run
  @Test
  public void testRunWithInvalidMode() {
    controller.run("invalidMode", "");
    assertEquals("Invalid mode. Use --mode interactive OR --mode headless <filePath>",
            view.getLastDisplayedMessage());
  }
  //Repeated options checking

  @Test
  public void testDuplicateLocationOption() {
    String command = "create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T10:30"
            + " -location Office -location Home";
    testCommandInBothModes(mode, command);
    assertEquals("Error: Location specified multiple times.",
            view.getLastDisplayedMessage());
  }

  @Test
  public void testDuplicateDescriptionOption() {
    String command = "create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T10:30 "
            + "-description Meeting -description Important";
    testCommandInBothModes(mode, command);
    assertEquals("Error: Description specified multiple times.",
            view.getLastDisplayedMessage());
  }

  @Test
  public void testDuplicatePrivateOption() {
    String command = "create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T10:30 " +
            "-private -private";
    testCommandInBothModes(mode, command);
    assertEquals("Error: Private flag specified multiple times.",
            view.getLastDisplayedMessage());
  }

  //simulate io exceptions
  @Test
  public void testIOExceptionHandling() {
    Reader faultyReader = new Reader() {
      @Override
      public int read(char[] cbuf, int off, int len) throws IOException {
        throw new IOException("Simulated read error");
      }

      @Override
      public void close() throws IOException {
        throw new IOException("Simulated close error");
      }
    };

    BufferedReader bufferedReader = new BufferedReader(faultyReader);
    controller.run("headless", "faultyFile.txt");
    assertTrue(view.getLastDisplayedMessage().contains("Error reading input: faultyFile.txt"));
  }

  @Test
  public void testIOExceptionOnReaderClose() {
    Reader faultyReader = new Reader() {
      @Override
      public int read(char[] cbuf, int off, int len) throws IOException {
        return -1; // Simulate successful read
      }

      @Override
      public void close() throws IOException {
        throw new IOException("Simulated close error");
      }
    };

    BufferedReader bufferedReader = new BufferedReader(faultyReader);
    controller.run("headless", "faultyFile.txt");
    assertTrue(view.getLastDisplayedMessage().contains("Error reading input: faultyFile.txt "));
  }

  //Edit Invalid scenarios
  @Test
  public void testEditCommandMissingEventType() {
    String command = "edit";
    testCommandInBothModes(mode, command);
    assertEquals("Error: Missing event type (event/events).",
            view.getLastDisplayedMessage());
  }

  @Test
  public void testEditCommandMissingProperty() {
    String command = "edit event";
    testCommandInBothModes(mode, command);
    assertEquals("Error: Missing property.", view.getLastDisplayedMessage());
  }

  @Test
  public void testEditCommandMissingEventName() {
    String command = "edit event name";
    testCommandInBothModes(mode, command);
    assertEquals("Error: Missing event name.", view.getLastDisplayedMessage());
  }

  @Test
  public void testEditCommandMissingStartDateTime() {
    String command = "edit event name TeamMeeting from";
    testCommandInBothModes(mode, command);
    assertEquals("Error: Missing start date and time.", view.getLastDisplayedMessage());
  }

  @Test
  public void testEditCommandMissingEndDateTime() {
    String command = "edit event name TeamMeeting from 2024-03-20T10:00 to";
    testCommandInBothModes(mode, command);
    assertEquals("Error: Missing end date and time.", view.getLastDisplayedMessage());
  }

  @Test
  public void testEditCommandSuccessful() {
    String command = "edit event name TeamMeeting from 2024-03-20T10:00 to"
            + " 2024-03-20T10:30 with UpdatedMeeting";
    testCommandInBothModes(mode, command);
    assertEquals("Event edited successfully.", view.getLastDisplayedMessage());
  }

  @Test
  public void testEditEventsMissingStartDateTime() {
    String command = "edit events name TeamMeeting from";
    testCommandInBothModes(mode, command);
    assertEquals("Error: Missing start date and time.", view.getLastDisplayedMessage());
  }

  @Test
  public void testEditEventsSuccessful() {
    String command = "edit events name TeamMeeting from 2024-03-20T10:00 with UpdatedMeeting";
    testCommandInBothModes(mode, command);
    assertEquals("Events edited successfully.", view.getLastDisplayedMessage());
  }

  @Test
  public void testPrintEventsMissingStartDateTime() {
    String command = "print events from";
    testCommandInBothModes(mode, command);
    assertEquals("Error: Missing start date and time.", view.getLastDisplayedMessage());
  }

  @Test
  public void testCreateCommandMissingEventName() {
    String command = "create event";
    testCommandInBothModes(mode, command);
    assertEquals("Error: Missing event name.", view.getLastDisplayedMessage());
  }

  @Test
  public void testCreateCommandMissingStartDateTime() {
    String command = "create event TeamMeeting from";
    testCommandInBothModes(mode, command);
    assertEquals("Error: Missing start date and time.", view.getLastDisplayedMessage());
  }

  @Test
  public void testCreateCommandMissingLocationValue() {
    String command = "create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T10:30 -location";
    testCommandInBothModes(mode, command);
    assertEquals("Error: Missing location value.", view.getLastDisplayedMessage());
  }

  @Test
  public void testCreateCommandMissingDescriptionValue() {
    String command = "create event TeamMeeting from 2024-03-20T10:00 "
            + "to 2024-03-20T10:30 -description";
    testCommandInBothModes(mode, command);
    assertEquals("Error: Missing description value.", view.getLastDisplayedMessage());
  }

  @Test
  public void testCreateCommandSuccessful() {
    String command = "create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T10:30";
    testCommandInBothModes(mode, command);
    assertEquals("Event created successfully.", view.getLastDisplayedMessage());
  }

  @Test
  public void testCreateInstance_ValidInputs() {
    ICalendarModel model = ICalendarModel.createInstance("listbased");
    IView view = IView.createInstance("consoleView");

    ICalendarController controller = ICalendarController.createInstance(model, view);

    assertNotNull(controller);
    assertEquals("class controller.CalendarController", controller.getClass().toString());
  }

  //checks for quoted text
  @Test
  public void testReadQuotedValue_SingleQuotedString() {
    testCommandInBothModes(mode, "create event 'Hello World' from "
            + "2024-03-20T10:00 to 2024-03-20T11:00");
    assertEquals("Hello World", model.lastAddedEvent.getEventName());
  }

  @Test
  public void testReadQuotedValue_DoubleQuotedString() {
    testCommandInBothModes(mode, "create event \"Hello World\" "
            + "from 2024-03-20T10:00 to 2024-03-20T11:00");
    assertEquals("Hello World", model.lastAddedEvent.getEventName());
  }

  @Test
  public void testReadQuotedValue_UnquotedString() {
    testCommandInBothModes(mode, "create event HelloWorld from "
            + "2024-03-20T10:00 to 2024-03-20T11:00");
    assertEquals("HelloWorld", model.lastAddedEvent.getEventName());
  }

  @Test
  public void testReadQuotedValue_EmptyString() {
    testCommandInBothModes(mode, "create event '' from "
            + "2024-03-20T10:00 to 2024-03-20T11:00");
    assertTrue(view.getLastDisplayedMessage().contains("Error"));
  }

  @Test
  public void testReadQuotedValue_MissingClosingQuote() {
    testCommandInBothModes("interactive", "create event "
            + "\"Hello World from 2024-03-20T10:00 to 2024-03-20T11:00");
    assertTrue(view.getLastDisplayedMessage().contains("Error"));
  }

  @Test
  public void testReadQuotedValue_SpaceWithinQuotes() {
    testCommandInBothModes("interactive", "create event"
            + " \"Hello   World\" from 2024-03-20T10:00 to 2024-03-20T11:00");
    assertEquals("Hello World", model.lastAddedEvent.getEventName());
  }

  @Test
  public void testReadQuotedValue_NoInput_ShouldReturnEmptyString() {
    testCommandInBothModes("interactive", "create "
            + "event from 2024-03-20T10:00 to 2024-03-20T11:00");
    assertTrue(view.getLastDisplayedMessage().contains("Error"));
  }

  private class TestCalendarModel implements ICalendarModel {
    ICalendarEventDTO lastAddedEvent;
    // Fields for edit event
    String lastEditEventProperty;
    String lastEditEventName;
    LocalDateTime lastEditEventStartDateTime;
    LocalDateTime lastEditEventEndDateTime;
    String lastEditEventNewValue;

    // Fields for edit events
    String lastEditEventsProperty;
    String lastEditEventsName;
    LocalDateTime lastEditEventsStartDateTime;
    String lastEditEventsNewValue;

    LocalDate lastPrintDate;
    String lastExportFilename;
    LocalDateTime lastShowStatusDateTime;
    LocalDateTime lastPrintStartDateTime;
    LocalDateTime lastPrintEndDateTime;

    @Override
    public boolean createCalendar(String calName, String timezone) {
      return false;
    }

    @Override
    public boolean addEvent(String calendarName, ICalendarEventDTO event) {
      return false;
    }

    @Override
    public boolean editEvents(String calendarName, String property, String eventName, LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue, boolean editAll) {
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

    @Override
    public List<CalendarEvent> getEventsInRange(String calendarName, LocalDateTime fromDateTime, LocalDateTime toDateTime) {
      return List.of();
    }

    @Override
    public boolean copyEvents(String sourceCalendarName, LocalDateTime sourceStart, LocalDateTime sourceEnd, String targetCalendarName, LocalDateTime targetStart) {
      return false;
    }
  }

  private class MockView implements IView {
    private final List<String> displayedMessages = new ArrayList<>();

    @Override
    public void display(String message) {
      displayedMessages.add(message);
    }

    public String getLastDisplayedMessage() {
      return displayedMessages.get(displayedMessages.size() - 1);
    }

    public List<String> getDisplayedMessages() {
      return displayedMessages;
    }

    public void clearMessages() {
      displayedMessages.clear();
    }
  }
}


