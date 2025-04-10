package controller;

import org.junit.Before;
import org.junit.Test;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.ICalendarEventDTO;
import model.ICalendarModel;
import view.IView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A set of parameterized JUnit tests for verifying the behavior of the
 * CalendarController.
 * <p>
 * This test class is run with the JUnit {@code Parameterized} runner, allowing
 * multiple parameter sets to be tested against the same test methods. It helps
 * cover a variety of input scenarios more comprehensively.
 * </p>
 */
public class CalendarControllerTest {
  private ICalendarController controller;
  private TestCalendarModel model;
  private ByteArrayOutputStream outputStream;
  private PrintStream originalOut;
  private MockView view;
  private String mode;
  String[] options = {"--location Office", "--description Quarterlymeeting", "--private"};
  String[] spacedOptions = {"--location \"Off ice\"", "--description \"Quarterly " +
      "meeting\"", "--private"};
  String[] spacedOptions2 = {"--location Office", "--description \"Quarterly meeting\""
      , "--private"};
  String[] spacedOptions3 = {"--location \"Off ice\"", "--description Quarterlymeeting"
      , "--private"};

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


  private void testCommandInBothModes(String mode, String command) {
    resetModels();

    // Create the complete command string
    String allCommands = "create calendar --name default --timezone America/New_York\n" +
          "use calendar --name default\n" +
          command;

    // Split the string by newlines
    String[] commandArray = allCommands.split("\n");

    // Execute each command sequentially
    for (String cmd : commandArray) {
      if (!cmd.trim().isEmpty()) {
        controller.executeCommand(cmd);
      }
    }
  }


  @Test
  public void testCreateEventVariations() {
    String baseCommand = "create event TeamMeeting from 2024-03-20T10:00 to " +
        "2024-03-20T11:00";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" from 2024-03-20T10:00 to " +
        "2024-03-20T11:00";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" from 2024-03-20T10:00 to " +
        "2024-03-20T11:00";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" from 2024-03-20T10:00 to " +
        "2024-03-20T11:00";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions2);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" from 2024-03-20T10:00 to " +
        "2024-03-20T11:00";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions3);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode, command);
      assertNotNull(model.lastAddedEvent);
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" from 2024-03-20T10:00 "
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
    String baseCommand = "create event \"Team Meeting\" from 2024-03-20T10:00 to"
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
    String baseCommand = "create event \"Team Meeting\" from 2024-03-20T10:00 to 2"
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
    String baseCommand = "create event \"Team Meeting\" from 2024-03-20T10:00 "
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
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" from 2024-03-20T10:00 to "
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
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" from 2024-03-20T10:00 to "
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
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" from 2024-03-20T10:00 to "
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
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" from 2024-03-20T10:00 "
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
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" on 2024-03-20";
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
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" on 2024-03-20";
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
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" on 2024-03-20";
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
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" on 2024-03-20";
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
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" on 2024-03-20 repeats MRU for 5" +
        " times";
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
    String baseCommand = "create event \"Team Meeting\" on 2024-03-20 repeats MRU for 5" +
        " times";
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
    String baseCommand = "create event \"Team Meeting\" on 2024-03-20 repeats MRU for 5" +
        " times";
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
    String baseCommand = "create event \"Team Meeting\" on 2024-03-20 repeats MRU for 5" +
        " times";
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
    String baseCommand = "create event TeamMeeting on 2024-03-20 repeats MRU until " +
        "2024-04-20";
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
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" on 2024-03-20 repeats MRU until" +
        " 2024-04-20";
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
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" on 2024-03-20 repeats MRU until" +
        " 2024-04-20";
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
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" on 2024-03-20 repeats MRU until" +
        " 2024-04-20";
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
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
    String baseCommand = "create event \"Team Meeting\" on 2024-03-20 repeats MRU until" +
        " 2024-04-20";
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
      assertNull(model.lastAddedEvent.getRecurrenceCount());
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
  public void testEditEventName() {
    String command = "edit event name TeamMeeting from 2024-03-20T10:00 to " +
        "2024-03-20T10:30"
        + " with UpdatedMeeting";
    testCommandInBothModes(mode, command);
    assertEquals("name", model.lastEditEventProperty);
    assertEquals("UpdatedMeeting", model.lastEditEventNewValue);
    assertEquals("TeamMeeting", model.lastEditEventName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
        model.lastEditEventStartDateTime);
    assertEquals(LocalDateTime.parse("2024-03-20T10:30"), model.lastEditEventEndDateTime);
  }

  @Test
  public void testEditEventDescription() {
    String command = "edit event description TeamMeeting from 2024-03-20T10:00 to "
        + "2024-03-20T10:30 with \"Updated Description\"";

    testCommandInBothModes(mode, command);
    assertEquals("description", model.lastEditEventProperty);
    assertEquals("Updated Description", model.lastEditEventNewValue);
    assertEquals("TeamMeeting", model.lastEditEventName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
        model.lastEditEventStartDateTime);
    assertEquals(LocalDateTime.parse("2024-03-20T10:30"), model.lastEditEventEndDateTime);
  }

  @Test
  public void testEditEventLocation() {
    String command = "edit event location TeamMeeting from 2024-03-20T10:00 to"
        + " 2024-03-20T10:30 with \"New Location\"";
    testCommandInBothModes(mode, command);
    assertEquals("location", model.lastEditEventProperty);
    assertEquals("New Location", model.lastEditEventNewValue);
    assertEquals("TeamMeeting", model.lastEditEventName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
        model.lastEditEventStartDateTime);
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
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
        model.lastEditEventStartDateTime);
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
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
        model.lastEditEventStartDateTime);
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
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
        model.lastEditEventStartDateTime);
    assertEquals(LocalDateTime.parse("2024-03-20T10:30"), model.lastEditEventEndDateTime);
  }

  //Edit Events Scenarrio
  @Test
  public void testEditEventsCallsModelWithCorrectParams() {
    testCommandInBothModes(mode, "edit events name TeamMeeting "
        + "from 2024-03-20T10:00 with UpdatedMeeting");
    assertEquals("name", model.lastEditEventsProperty);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
        model.lastEditEventsStartDateTime);
    assertEquals("UpdatedMeeting", model.lastEditEventsNewValue);
  }

  @Test
  public void testEditEventsName() {
    String command = "edit events name TeamMeeting from 2024-03-20T10:00 with " +
        "UpdatedMeeting";
    testCommandInBothModes(mode, command);
    assertEquals("name", model.lastEditEventsProperty);
    assertEquals("UpdatedMeeting", model.lastEditEventsNewValue);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
        model.lastEditEventsStartDateTime);
  }

  @Test
  public void testEditEventsDescription() {
    String command = "edit events description TeamMeeting from "
        + "2024-03-20T10:00 with \"Updated Description\"";
    testCommandInBothModes(mode, command);
    assertEquals("description", model.lastEditEventsProperty);
    assertEquals("Updated Description", model.lastEditEventsNewValue);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
        model.lastEditEventsStartDateTime);
  }

  @Test
  public void testEditEventsLocation() {
    String command = "edit events location TeamMeeting from 2024-03-20T10:00 with \"New" +
        " Location\"";
    testCommandInBothModes(mode, command);
    assertEquals("location", model.lastEditEventsProperty);
    assertEquals("New Location", model.lastEditEventsNewValue);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
        model.lastEditEventsStartDateTime);
  }

  @Test
  public void testEditEventsStart() {
    String command = "edit events start TeamMeeting from 2024-03-20T10:00 with " +
        "2024-03-20T10:35";
    testCommandInBothModes(mode, command);
    assertEquals("start", model.lastEditEventsProperty);
    assertEquals("2024-03-20T10:35", model.lastEditEventsNewValue);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
        model.lastEditEventsStartDateTime);
  }

  @Test
  public void testEditEventsEnd() {
    String command = "edit events end TeamMeeting from 2024-03-20T10:00 with " +
        "2024-03-20T10:35";
    testCommandInBothModes(mode, command);
    assertEquals("end", model.lastEditEventsProperty);
    assertEquals("2024-03-20T10:35", model.lastEditEventsNewValue);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
        model.lastEditEventsStartDateTime);
  }

  @Test
  public void testEditEventsIsPublic() {
    String command = "edit events isPublic TeamMeeting from 2024-03-20T10:00 with false";
    testCommandInBothModes(mode, command);
    assertEquals("isPublic", model.lastEditEventsProperty);
    assertEquals("false", model.lastEditEventsNewValue);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
        model.lastEditEventsStartDateTime);
  }

  @Test
  public void testEditEventsAtSpecificStartTime() throws IOException {
    String command = "edit events name TeamMeeting from 2024-03-20T10:00 with " +
        "UpdatedMeeting";
    testCommandInBothModes(mode, command);
    assertEquals("name", model.lastEditEventsProperty);
    assertEquals("UpdatedMeeting", model.lastEditEventsNewValue);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"),
        model.lastEditEventsStartDateTime);
  }

  @Test
  public void testEditAllMatchingEvents() throws IOException {
    String command = "edit events description TeamMeeting \"Updated Description\"";
    testCommandInBothModes(mode, command);
    assertEquals("description", model.lastEditEventsProperty);
    assertEquals("Updated Description", model.lastEditEventsNewValue);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.MIN, model.lastEditEventsStartDateTime);
  }

  // End of edit events function
  //Prrint command tests
  @Test
  public void testPrintEventsOnDate_CallsModelWithFullDayRange() {
    String command = "print events on 2024-03-20";
    testCommandInBothModes(mode, command);
    assertEquals("DefaultCalendarName", model.lastGetEventsRangeCalendar);
    assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastGetEventsRangeStart);
    assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastGetEventsRangeEnd);
  }

  @Test
  public void testPrintEventsInRange_CallsModelWithCorrectStartAndEnd() {
    String command = "print events from 2024-03-20T10:00 to 2024-03-20T12:00";
    testCommandInBothModes(mode, command);

    assertEquals("DefaultCalendarName", model.lastGetEventsRangeCalendar);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastGetEventsRangeStart);
    assertEquals(LocalDateTime.parse("2024-03-20T12:00"), model.lastGetEventsRangeEnd);
  }

  @Test
  public void testPrintEvents_MissingArguments() {
    String command = "print events";
    testCommandInBothModes("interactive", command);
    assertEquals("Error Executing command: Print Command too short need more details",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testPrintEvents_InvalidKeyword() {
    String command = "print events something 2024-03-20";
    testCommandInBothModes("interactive", command);
    assertEquals("Error Executing command: Expected 'on' or 'from' at start of print " +
            "events command.",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testPrintEvents_OnWithExtraArguments() {
    String command = "print events on 2024-03-20 extra";
    testCommandInBothModes("interactive", command);
    assertEquals("Error Executing command: Invalid format. Expected: print events on " +
            "<date>",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testPrintEvents_FromMissingToKeyword() {
    String command = "print events from 2024-03-20T10:00 2024-03-20T12:00";
    testCommandInBothModes("interactive", command);
    assertEquals("Error Executing command: Invalid format. Expected: print events from " +
            "<datetime> to <datetime>",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testPrintEvents_FromIncompleteArguments() {
    String command = "print events from 2024-03-20T10:00 to";
    testCommandInBothModes("interactive", command);
    assertEquals("Error Executing command: Invalid format. Expected: print events from " +
            "<datetime> to <datetime>",
        view.getLastDisplayedMessage());
  }

  //show status commands
  @Test
  public void testShowStatusWhenNoEvents() {
    String command = "show status on 2024-03-20T14:00";
    testCommandInBothModes(mode, command);
    assertEquals(LocalDateTime.parse("2024-03-20T14:00"),
        model.lastGetEventsSpecificDateTime);
    assertEquals("Available", view.getLastDisplayedMessage());
  }

  @Test
  public void testShowStatus_MissingOnKeyword() {
    String command = "show status 2024-03-20T10:00";
    testCommandInBothModes(mode, command);
    assertTrue(view.getLastDisplayedMessage().toLowerCase().contains("error"));
  }

  @Test
  public void testShowStatus_MissingDateTime() {
    String command = "show status on";
    testCommandInBothModes("interactive", command);
    assertTrue(view.getLastDisplayedMessage().toLowerCase().contains("error"));
  }

  @Test
  public void testShowStatus_InvalidDateTimeFormat() {
    String command = "show status on 03-20-2024 10:00AM";
    testCommandInBothModes("interactive", command);
    assertTrue(view.getLastDisplayedMessage().toLowerCase().contains("error"));
  }

  @Test
  public void testShowStatus_RandomGarbage() {
    String command = "show status on banana";
    testCommandInBothModes("interactive", command);
    assertTrue(view.getLastDisplayedMessage().toLowerCase().contains("error"));
  }


  //Exporrt tests forr controller

  @Test
  public void testExportEvents_ValidCommand_CallsModelGetEventsInRange() {
    String command = "export cal myCalendar.csv";
    testCommandInBothModes(mode, command);

    assertEquals("DefaultCalendarName", model.lastGetEventsRangeCalendar);
    assertEquals(LocalDateTime.MIN, model.lastGetEventsRangeStart);
    assertEquals(LocalDateTime.MAX, model.lastGetEventsRangeEnd);
    assertTrue(view.getLastDisplayedMessage().contains("Events exported successfully")
        || view.getLastDisplayedMessage().contains("Error exporting events"));
  }

  @Test
  public void testCopyEvent_MissingTargetKeyword() {
    String command = "copy event TeamMeeting on 2024-03-20T10:00 to 2024-03-22T09:00";
    testCommandInBothModes(mode, command);
    assertEquals("Error Executing command: Insufficient arguments. Expected: event_name on datetime --target calendar_name to datetime",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testExportEvents_MissingFilename() {
    String command = "export cal";
    testCommandInBothModes(mode, command);
    assertEquals("Error Executing command: Missing filename for export.",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testExportEvents_TooManyArguments() {
    String command = "export cal calendar.csv extraArg";
    testCommandInBothModes(mode, command);
    assertEquals("Error Executing command: Too many arguments for export command.",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testExportEvents_MissingCal() {
    String command = "export calendar.csv extraArg";
    testCommandInBothModes(mode, command);
    assertEquals("Error: Unknown command.", view.getLastDisplayedMessage());
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
    String baseCommand = "create event TeamMeeting from 2024-03-20T10:00 to " +
        "2024-03-20T10:30";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("create", "Error: Unknown command.");
    errorCases.put("event", "Error: Unknown command.");
    errorCases.put("TeamMeeting", "Error Executing command: Expected 'from' or 'on' " +
        "after event name");
    errorCases.put("from", "Error Executing command: Expected 'from' or 'on' after " +
        "event name");
    errorCases.put("2024-03-20T10:00", "Error Executing command: Text 'to' could not be" +
        " parsed at index 0");
    errorCases.put("to", "Error Executing command: Expected 'to' after start time");
    errorCases.put("2024-03-20T10:30", "Error Executing command: Missing end datetime " +
        "after 'to'");

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replace(entry.getKey(), "")
          .replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
  }

  @Test
  public void testCreateRecurringEventUntilMissingWords() {
    String baseCommand = "create event TeamMeeting from 2024-03-20T10:00 to " +
        "2024-03-20T10:30 "
        + "repeats MTWRFSU until 2024-04-20T10:30";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("create", "Error: Unknown command.");
    errorCases.put("event", "Error: Unknown command.");
    errorCases.put("TeamMeeting", "Error Executing command: Expected 'from' or 'on' " +
        "after event name");
    errorCases.put("from", "Error Executing command: Expected 'from' or 'on' after " +
        "event name");
    errorCases.put("2024-03-20T10:00", "Error Executing command: Text 'to' could not be" +
        " parsed at index 0");
    errorCases.put("to", "Error Executing command: Expected 'to' after start time");
    errorCases.put("2024-03-20T10:30", "Error Executing command: Text 'repeats' could " +
        "not be parsed at index 0");
    errorCases.put("repeats", "Error Executing command: Unrecognized extra argument: " +
        "MTWRFSU");
    errorCases.put("MTWRFSU", "Error Executing command: Invalid weekday character: u");
    errorCases.put("until", "Error Executing command: Unrecognized extra argument: " +
        "2024-04-20T10:30");
    errorCases.put("2024-04-20T10:30", "Error Executing command: Missing end date after" +
        " 'until'");

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
    errorCases.put("create", "Error: Unknown command.");
    errorCases.put("event", "Error: Unknown command.");
    errorCases.put("TeamMeeting", "Error Executing command: Expected 'from' or 'on' " +
        "after event name");
    errorCases.put("from", "Error Executing command: Expected 'from' or 'on' after " +
        "event name");
    errorCases.put("2024-03-20T10:00", "Error Executing command: Text 'to' could not be" +
        " parsed at index 0");
    errorCases.put("to", "Error Executing command: Expected 'to' after start time");
    errorCases.put("2024-03-20T10:30", "Error Executing command: Text 'repeats' could " +
        "not be parsed at index 0");
    errorCases.put("repeats", "Error Executing command: Unrecognized extra argument: " +
        "MTWR");
    errorCases.put("MTWR", "Error Executing command: Invalid weekday character: f");
    errorCases.put("for", "Error Executing command: Unrecognized extra argument: 5");
    errorCases.put("5", "Error Executing command: For input string: \"times\"");
    errorCases.put("times", "Error Executing command: Missing keyword 'times'");

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
    errorCases.put("create", "Error: Unknown command.");
    errorCases.put("event", "Error: Unknown command.");
    errorCases.put("TeamMeeting", "Error Executing command: Expected 'from' or 'on' " +
        "after event name");
    errorCases.put("on", "Error Executing command: Expected 'from' or 'on' after event " +
        "name");
    errorCases.put("2024-03-20T10:00", "Error Executing command: Missing date after " +
        "'on'");

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replaceFirst("\\b" + entry.getKey()
          + "\\b", "").replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
  }

  @Test
  public void testCreateRecurringAllDayEventForNTimesMissingWords() {
    String baseCommand = "create event TeamMeeting on 2024-03-20 repeats MTWR for 5 " +
        "times";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("create", "Error: Unknown command.");
    errorCases.put("event", "Error: Unknown command.");
    errorCases.put("TeamMeeting", "Error Executing command: Expected 'from' or 'on' " +
        "after event name");
    errorCases.put("on", "Error Executing command: Expected 'from' or 'on' after event " +
        "name");
    errorCases.put("2024-03-20", "Error Executing command: Text 'repeats' could not be " +
        "parsed at index 0");
    errorCases.put("repeats", "Error Executing command: Unrecognized extra argument: " +
        "MTWR");
    errorCases.put("MTWR", "Error Executing command: Invalid weekday character: f");
    errorCases.put("for", "Error Executing command: Unrecognized extra argument: 5");
    errorCases.put("5", "Error Executing command: For input string: \"times\"");
    errorCases.put("times", "Error Executing command: Missing keyword 'times'");

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replaceFirst("\\b" + entry.getKey() + "\\b",
              "")
          .replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
  }


  @Test
  public void testEditEventMissingWords2() {
    String baseCommand = "edit event name TeamMeeting from 2024-03-20T10:00 to "
        + "2024-03-20T10:30 with UpdatedMeeting";

    String errorString  = "Error Executing command: Insufficient arguments for edit event " +
          "command. Expected:edit event property eventName from startDateTime to e" +
          "ndDateTime with newValue";
    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("edit", "Error: Unknown command.");
    errorCases.put("event", "Error: Unknown command.");
    errorCases.put("name", errorString);
    errorCases.put("TeamMeeting", errorString);
    errorCases.put("from", errorString);
    errorCases.put("2024-03-20T10:00", errorString);
    errorCases.put("to",errorString);
    errorCases.put("2024-03-20T10:30", errorString);
    errorCases.put("with", errorString);
    errorCases.put("UpdatedMeeting", errorString);

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replaceFirst("\\b" + entry.getKey() +
          "\\b", "").replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
  }

  @Test
  public void testCreateRecurringAllDayEventUntilMissingWords() {
    String baseCommand = "create event TeamMeeting on 2024-03-20 repeats MTWR until " +
        "2024-04-20";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("create", "Error: Unknown command.");
    errorCases.put("event", "Error: Unknown command.");
    errorCases.put("TeamMeeting", "Error Executing command: Expected 'from' or 'on' " +
        "after event name");
    errorCases.put("on", "Error Executing command: Expected 'from' or 'on' after event " +
        "name");
    errorCases.put("2024-03-20", "Error Executing command: Text 'repeats' could not be " +
        "parsed at index 0");
    errorCases.put("repeats", "Error Executing command: Unrecognized extra argument: " +
        "MTWR");
    errorCases.put("MTWR", "Error Executing command: Invalid weekday character: u");
    errorCases.put("until", "Error Executing command: Unrecognized extra argument: " +
        "2024-04-20");
    errorCases.put("2024-04-20", "Error Executing command: Missing end date after " +
        "'until'");

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
    String baseCommand = "edit events name TeamMeeting from 2024-03-20T10:00 with " +
        "UpdatedMeeting";

    Map<String, String> errorCases = new LinkedHashMap<>();
    errorCases.put("edit", "Error: Unknown command.");
    errorCases.put("events", "Error: Unknown command.");
    errorCases.put("name", "Events updated successfully.");
    errorCases.put("TeamMeeting", "Events updated successfully.");
    errorCases.put("from", "Events updated successfully.");
    errorCases.put("2024-03-20T10:00", "Error Executing command: Invalid datetime format after 'from': with");
    errorCases.put("with", "Error Executing command: Missing 'with' keyword after " +
        "datetime");
    errorCases.put("UpdatedMeeting", "Error Executing command: Missing new property " +
        "value after 'with'");

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
    errorCases.put("edit", "Error: Unknown command.");
    errorCases.put("events", "Error: Unknown command.");
    errorCases.put("name", "Error Executing command: Missing new property value");
    errorCases.put("TeamMeeting", "Error Executing command: Missing new property value");
    errorCases.put("UpdatedMeeting", "Error Executing command: Missing new property value");

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
    errorCases.put("print", "Error: Unknown command.");
    errorCases.put("events", "Error: Unknown command.");
    errorCases.put("on", "Error Executing command: Expected 'on' or 'from' at start of " +
        "print events command.");
    errorCases.put("2024-03-20", "Error Executing command: Invalid format. Expected: " +
        "print events on <date>");

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
    errorCases.put("print", "Error: Unknown command.");
    errorCases.put("events", "Error: Unknown command.");
    errorCases.put("from", "Error Executing command: Expected 'on' or 'from' at start " +
        "of print events command.");
    errorCases.put("2024-03-20T10:00", "Error Executing command: Invalid format. " +
        "Expected: print events from <datetime> to <datetime>");
    errorCases.put("to", "Error Executing command: Invalid format. Expected: print " +
        "events from <datetime> to <datetime>");
    errorCases.put("2024-03-20T12:00", "Error Executing command: Invalid format. " +
        "Expected: print events from <datetime> to <datetime>");

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
    errorCases.put("export", "Error: Unknown command.");
    errorCases.put("cal", "Error: Unknown command.");
    errorCases.put("events.csv", "Error Executing command: Missing filename for export.");

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
    errorCases.put("show", "Error: Unknown command.");
    errorCases.put("status", "Error: Unknown command.");
    errorCases.put("on", "Error Executing command: Invalid syntax. Expected: show " +
        "status on <datetime>");
    errorCases.put("2024-03-20T14:00", "Error Executing command: Invalid syntax. " +
        "Expected: show status on <datetime>");

    for (Map.Entry<String, String> entry : errorCases.entrySet()) {
      String modifiedCommand = baseCommand.replaceFirst("\\b" + entry.getKey()
          + "\\b", "").replaceAll("\\s+", " ").trim();
      testCommandInBothModes(mode, modifiedCommand);
      assertEquals(entry.getValue(), view.getLastDisplayedMessage());
    }
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

    String[] wordsToRemove = {"create", "event", "TeamMeeting", "from", "2024-03-20T10" +
        ":00",
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
    String baseCommand = "create event --autoDecline TeamMeeting from 2024-03-20T10:00 " +
        "to "
        + "2024-03-20T10:30 repeats MTWRF until 2024-04-20T10:30";

    String[] wordsToRemove = {"create", "event", "TeamMeeting", "from", "2024-03-20T10" +
        ":00",
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
    String baseCommand = "create event TeamMeeting on 2024-03-20 repeats MTWRF for 5 " +
        "times";

    String[] wordsToRemove = {"create", "event", "TeamMeeting", "on", "2024-03-20",
        "repeats",
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
    String baseCommand = "create event TeamMeeting on 2024-03-20 repeats MTWRF until " +
        "2024-04-20";

    String[] wordsToRemove = {"create", "event", "TeamMeeting", "on", "2024-03-20",
        "repeats",
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
    String baseCommand = "edit event name TeamMeeting from 2024-03-20T10:00 to " +
        "2024-03-20T10:30"
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
    var a = view.getLastDisplayedMessage();
    assertTrue(view.getLastDisplayedMessage().contains("Using calendar: default"));
  }


  //Tests to validate the mode of the controlleer run


  @Test
  public void testDuplicateLocationOption() {
    String command = "create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T10:30"
        + " --location Office --location Home";
    testCommandInBothModes(mode, command);
    assertEquals("Error Executing command: Duplicate --location flag",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testDuplicateDescriptionOption() {
    String command = "create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T10:30 "
        + "--description Meeting --description Important";
    testCommandInBothModes(mode, command);
    assertEquals("Error Executing command: Duplicate --description flag",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testDuplicatePrivateOption() {
    String command = "create event TeamMeeting from 2024-03-20T10:00 to " +
        "2024-03-20T10:30 " +
        "--private --private";
    testCommandInBothModes(mode, command);
    assertEquals("Error Executing command: Duplicate --private flag",
        view.getLastDisplayedMessage());
  }

  //Edit Invalid scenarios
  @Test
  public void testEditCommandMissingEventType() {
    String command = "edit";
    testCommandInBothModes(mode, command);
    assertEquals("Error: Enter at-least two tokens",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testEditCommandMissingProperty() {
    String command = "edit event";
    testCommandInBothModes(mode, command);
    assertEquals("Error Executing command: Insufficient arguments for edit " +
                "event command. Expected:edit event property eventName from " +
                "startDateTime to endDateTime with newValue",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testEditCommandMissingEventName() {
    String command = "edit event name";
    testCommandInBothModes(mode, command);
    assertEquals("Error Executing command: Insufficient arguments for edit " +
                "event command. Expected:edit event property eventName from " +
                "startDateTime to endDateTime with newValue",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testEditCommandMissingStartDateTime() {
    String command = "edit event name TeamMeeting from";
    testCommandInBothModes(mode, command);
    assertEquals("Error Executing command: Insufficient arguments for edit event command. Expected:edit event property eventName from startDateTime to endDateTime with newValue",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testEditCommandMissingEndDateTime() {
    String command = "edit event name TeamMeeting from 2024-03-20T10:00 to";
    testCommandInBothModes(mode, command);
    assertEquals("Error Executing command: Insufficient arguments for" +
                " edit event command. Expected:edit event property eventName " +
                "from startDateTime to endDateTime with newValue",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testEditCommandSuccessful() {
    String command = "edit event name TeamMeeting from 2024-03-20T10:00 to"
        + " 2024-03-20T10:30 with UpdatedMeeting";
    testCommandInBothModes(mode, command);
    assertEquals("Event(s) edited successfully.", view.getLastDisplayedMessage());
  }

  @Test
  public void testEditEventsMissingStartDateTime() {
    String command = "edit events name TeamMeeting from";
    testCommandInBothModes(mode, command);
    assertEquals("Error Executing command: Missing datetime value at position 4",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testEditEventsSuccessful() {
    String command = "edit events name TeamMeeting from 2024-03-20T10:00 with " +
        "UpdatedMeeting";
    testCommandInBothModes(mode, command);
    assertEquals("Events updated successfully.", view.getLastDisplayedMessage());
  }

  @Test
  public void testPrintEventsMissingStartDateTime() {
    String command = "print events from";
    testCommandInBothModes(mode, command);
    assertEquals("Error Executing command: Invalid format. Expected: print events from " +
        "<datetime> to <datetime>", view.getLastDisplayedMessage());
  }

  @Test
  public void testCreateCommandMissingEventName() {
    String command = "create event";
    testCommandInBothModes(mode, command);
    assertEquals("Error Executing command: Missing event name.",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testCreateCommandMissingStartDateTime() {
    String command = "create event TeamMeeting from";
    testCommandInBothModes(mode, command);
    assertEquals("Error Executing command: Missing start datetime after 'from'",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testCreateCommandMissingLocationValue() {
    String command = "create event TeamMeeting from 2024-03-20T10:00 to " +
        "2024-03-20T10:30 -location";
    testCommandInBothModes(mode, command);
    assertEquals("Error Executing command: Unrecognized extra argument: -location",
        view.getLastDisplayedMessage());
  }

  @Test
  public void testCreateCommandMissingDescriptionValue() {
    String command = "create event TeamMeeting from 2024-03-20T10:00 "
        + "to 2024-03-20T10:30 --description";
    testCommandInBothModes(mode, command);
    assertEquals("Error Executing command: Missing value for --description",
        view.getLastDisplayedMessage());
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
    //IView view = IView.createInstance("consoleView", );

    ICalendarController controller = ICalendarController.createInstance("Advanced", model,
        view);

    assertNotNull(controller);
    assertEquals("class controller.CalendarController", controller.getClass().toString());
  }


  @Test
  public void testCreateInstance_ValidInputs3() {
    ICalendarModel model = ICalendarModel.createInstance("listbased");
    //IView view = IView.createInstance("consoleView");
    try {
      ICalendarController.createInstance("Random", model,
          view);
      fail("Exception expected for random type of controller");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Unsupported version:"));
    }

  }


  //checks for quoted text
  @Test
  public void testReadQuotedValue_SingleQuotedString() {
    testCommandInBothModes(mode, "create event \"Hello World\" from "
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
    testCommandInBothModes(mode, "create event \"Blast Frooti\" from "
        + "2024-03-20T10:00 to 2024-03-20T11:00");
    assertTrue(view.getLastDisplayedMessage().contains("Event created successfully"));
  }

  @Test
  public void testReadQuotedValue_MissingClosingQuote() {
    testCommandInBothModes(mode, "create event "
        + "\"Hello World from 2024-03-20T10:00 to 2024-03-20T11:00");
    assertTrue(view.getLastDisplayedMessage().contains("Error"));
  }

  @Test
  public void testReadQuotedValue_SpaceWithinQuotes() {
    testCommandInBothModes(mode, "create event"
        + " \"Hello   World\" from 2024-03-20T10:00 to 2024-03-20T11:00");
    assertEquals("Hello   World", model.lastAddedEvent.getEventName());
  }

  @Test
  public void testReadQuotedValue_NoInput_ShouldReturnEmptyString() {
    testCommandInBothModes(mode, "create "
        + "event from 2024-03-20T10:00 to 2024-03-20T11:00");
    assertTrue(view.getLastDisplayedMessage().contains("Error"));
  }

  //Copy event tests
  @Test
  public void testCopySingleEvent() {
    String command = "copy event TeamMeeting on 2024-03-20T10:00 --target WorkCalendar " +
        "to 2024-03-22T09:00";
    testCommandInBothModes(mode, command);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastCopyEventSourceStart);
    assertEquals("TeamMeeting", model.lastCopyEventName);
    assertEquals("WorkCalendar", model.lastCopyTargetCalendar);
    assertEquals(LocalDateTime.parse("2024-03-22T09:00"), model.lastCopyEventTargetStart);
  }

  @Test
  public void testCopySingleEventWithQuotedName() {
    String command = "copy event \"Team Meeting\" on 2024-03-20T10:00 --target " +
        "WorkCalendar to 2024-03-22T09:00";
    testCommandInBothModes(mode, command);

    assertEquals("Team Meeting", model.lastCopyEventName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastCopyEventSourceStart);
    assertEquals("WorkCalendar", model.lastCopyTargetCalendar);
    assertEquals(LocalDateTime.parse("2024-03-22T09:00"), model.lastCopyEventTargetStart);
  }

  @Test
  public void testCopyEventWithExtraSpaces() {
    String command = "copy    event   TeamMeeting   on   2024-03-20T10:00   --target   " +
        "WorkCalendar   to   2024-03-22T09:00";
    testCommandInBothModes(mode, command);

    assertEquals("TeamMeeting", model.lastCopyEventName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastCopyEventSourceStart);
    assertEquals("WorkCalendar", model.lastCopyTargetCalendar);
    assertEquals(LocalDateTime.parse("2024-03-22T09:00"), model.lastCopyEventTargetStart);
  }

  @Test
  public void testCopyEventWithDifferentDateFormat() {
    String command = "copy event TeamMeeting on 2024-03-20T10:00:00 --target " +
        "WorkCalendar to 2024-03-22T09:00:00";
    testCommandInBothModes(mode, command);

    assertEquals("TeamMeeting", model.lastCopyEventName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00:00"),
        model.lastCopyEventSourceStart);
    assertEquals("WorkCalendar", model.lastCopyTargetCalendar);
    assertEquals(LocalDateTime.parse("2024-03-22T09:00:00"),
        model.lastCopyEventTargetStart);
  }

  @Test
  public void testCopyEventWithLowercaseKeywords() {
    String command = "copy event TeamMeeting on 2024-03-20T10:00 --target WorkCalendar " +
        "to 2024-03-22T09:00";
    testCommandInBothModes(mode, command);

    assertEquals("TeamMeeting", model.lastCopyEventName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastCopyEventSourceStart);
    assertEquals("WorkCalendar", model.lastCopyTargetCalendar);
    assertEquals(LocalDateTime.parse("2024-03-22T09:00"), model.lastCopyEventTargetStart);
  }

  @Test
  public void testCopyEvent_MissingOnKeyword() {
    String command = "copy event TeamMeeting 2024-03-20T10:00 --target WorkCalendar to " +
        "2024-03-22T09:00";
    testCommandInBothModes(mode, command);
    assertTrue(view.getLastDisplayedMessage().contains("Error Executing command: " +
        "Expected 'on' after event name."));
  }

  @Test
  public void testCopyEvent_MissingTargetFlag() {
    String command = "copy event TeamMeeting on 2024-03-20T10:00 WorkCalendar to " +
        "2024-03-22T09:00";
    testCommandInBothModes(mode, command);
    assertTrue(view.getLastDisplayedMessage().contains("Error Executing command: " +
        "Expected '--target' after source date and time."));
  }

  @Test
  public void testCopyEvent_MissingToKeyword() {
    String command = "copy event TeamMeeting on 2024-03-20T10:00 --target WorkCalendar " +
        "2024-03-22T09:00";
    testCommandInBothModes(mode, command);
    assertTrue(view.getLastDisplayedMessage().contains("Error Executing command: " +
        "Expected 'to' after target calendar name."));
  }

  @Test
  public void testCopyEvent_MissingTargetDateTime() {
    String command = "copy event TeamMeeting on 2024-03-20T10:00 --target WorkCalendar " +
        "to";
    testCommandInBothModes(mode, command);
    assertTrue(view.getLastDisplayedMessage().contains("Error Executing command: " +
        "Missing target datetime."));
  }

  @Test
  public void testCopyEvent_InvalidSourceDateTime() {
    String command = "copy event TeamMeeting on 2024-03-20Txx:00 --target WorkCalendar " +
        "to 2024-03-22T09:00";
    testCommandInBothModes(mode, command);
    assertTrue(view.getLastDisplayedMessage().contains("Error Executing command: Invalid source date and time format: 2024-03-20Txx:00"));
  }

  @Test
  public void testCopyEvent_QuotedEventName() {
    String command = "copy event \"Team Meeting\" on 2024-03-20T10:00 --target " +
        "WorkCalendar to 2024-03-22T09:00";
    testCommandInBothModes(mode, command);
    assertEquals("Team Meeting", model.lastCopyEventName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastCopyEventSourceStart);
    assertEquals("WorkCalendar", model.lastCopyTargetCalendar);
    assertEquals(LocalDateTime.parse("2024-03-22T09:00"), model.lastCopyEventTargetStart);
  }

  @Test
  public void testCopyEvent_EventNameWithMultipleSpaces() {
    String command = "copy event   \"  Team   Meeting   \" on 2024-03-20T10:00 --target" +
        " WorkCalendar to 2024-03-22T09:00";
    testCommandInBothModes(mode, command);
    assertEquals("  Team   Meeting   ", model.lastCopyEventName);
  }

  @Test
  public void testCopyEvent_MissingEventName() {
    String command = "copy event on 2024-03-20T10:00 --target WorkCalendar to " +
        "2024-03-22T09:00";
    testCommandInBothModes(mode, command);
    assertTrue(view.getLastDisplayedMessage().toLowerCase().contains("error"));
  }

  @Test
  public void testCopyEvent_InvalidTargetDateTime() {
    String command = "copy event TeamMeeting on 2024-03-20T10:00 --target WorkCalendar " +
        "to 22-03-2024T09:00";
    testCommandInBothModes(mode, command);
    assertTrue(view.getLastDisplayedMessage().toLowerCase().contains("invalid target " +
        "date and time format"));
  }

  @Test
  public void testCopyEvent_MissingTargetCalendar() {
    String command = "copy event TeamMeeting on 2024-03-20T10:00 --target to " +
        "2024-03-22T09:00";
    testCommandInBothModes(mode, command);
    assertTrue(view.getLastDisplayedMessage().toLowerCase().contains("error"));
  }

  @Test
  public void testCopyEvent_MissingEventKeyword() {
    String command = "copy TeamMeeting on 2024-03-20T10:00 --target WorkCalendar to " +
        "2024-03-22T09:00";
    testCommandInBothModes(mode, command);
    assertTrue(view.getLastDisplayedMessage().contains("Error: Unknown command."));
  }


  protected static class TestCalendarModel implements ICalendarModel {
    ICalendarEventDTO lastAddedEvent;
    // Fields for edit event
    public String lastEditEventProperty;
    String lastEditEventName;
    LocalDateTime lastEditEventStartDateTime;
    LocalDateTime lastEditEventEndDateTime;
    String lastEditEventNewValue;
    String lastUsedCalendarName;
    String lastCreatedCalendarName;

    // Fields for edit events
    String lastEditEventsProperty;
    String lastEditEventsName;
    LocalDateTime lastEditEventsStartDateTime = null;
    String lastEditEventsNewValue;

    LocalDate lastPrintDate;
    String lastExportFilename;
    LocalDateTime lastShowStatusDateTime;
    LocalDateTime lastPrintStartDateTime;
    LocalDateTime lastPrintEndDateTime;


    String lastCopySourceCalendar;
    LocalDateTime lastCopyEventSourceStart;
    String lastCopyEventName;
    String lastCopyTargetCalendar;
    LocalDateTime lastCopyEventTargetStart;

    LocalDateTime lastCopyEventsSourceStart;
    LocalDateTime lastCopyEventsSourceEnd;
    LocalDate lastCopyEventsTargetStart;

    // Get events in range
    String lastGetEventsRangeCalendar;
    LocalDateTime lastGetEventsRangeStart;
    LocalDateTime lastGetEventsRangeEnd;

    // Get events at specific time
    String lastGetEventsSpecificCalendar;
    LocalDateTime lastGetEventsSpecificDateTime;
    String lastCalendarPresentName;
    String lastEditCalendarName;

    String lastEditCalendarProperty;
    String lastEditCalendarNewValue;
    LocalDate lastIsCalendarAvailabledate;
    String lastIsCalendarAvailableName;


    @Override
    public boolean createCalendar(String calName, String timezone) {
      this.lastCreatedCalendarName = calName;
      return true;
    }

    @Override
    public boolean addEvent(String calendarName, ICalendarEventDTO event) {
      this.lastAddedEvent = event;
      return true;
    }

    @Override
    public boolean editEvents(String calendarName, String property, String eventName,
                              LocalDateTime fromDateTime, String newValue, boolean editAll) {
      this.lastEditEventsProperty = property;
      this.lastEditEventsName = eventName;
      this.lastEditEventsStartDateTime = fromDateTime;
      this.lastEditEventsNewValue = newValue;
      return true;
    }

    @Override
    public boolean editEvent(String calendarName, String property, String eventName,
                             LocalDateTime fromDateTime, LocalDateTime toDateTime,
                             String newValue) {
      this.lastEditEventProperty = property;
      this.lastEditEventName = eventName;
      this.lastEditEventStartDateTime = fromDateTime;
      this.lastEditEventEndDateTime = toDateTime;
      this.lastEditEventNewValue = newValue;
      return true;
    }

    @Override
    public boolean isCalendarAvailable(String calName, LocalDate date) {
      this.lastIsCalendarAvailabledate = date;
      this.lastIsCalendarAvailableName = calName;
      return false;
    }

    @Override
    public List<String> getCalendarNames() {
      return List.of();
    }

    @Override
    public String getCalendarTimeZone(String calendarName) {
      return "";
    }

    @Override
    public boolean deleteCalendar(String calName) {
      return false;
    }

    @Override
    public List<ICalendarEventDTO> getEventsInRange(String calendarName,
                                                    LocalDateTime fromDateTime,
                                                    LocalDateTime toDateTime) {
      this.lastGetEventsRangeCalendar = "DefaultCalendarName";
      this.lastGetEventsRangeStart = fromDateTime;
      this.lastGetEventsRangeEnd = toDateTime;
      return List.of();
    }

    @Override
    public List<ICalendarEventDTO> getEventsInSpecificDateTime(String calendarName,
                                                               LocalDateTime dateTime) {
      this.lastGetEventsSpecificCalendar = calendarName;
      this.lastGetEventsSpecificDateTime = dateTime;
      return List.of();
    }

    @Override
    public boolean copyEvents(String sourceCalendarName, LocalDateTime sourceStart,
                              LocalDateTime sourceEnd, String targetCalendarName,
                              LocalDate targetStart) {
      this.lastCopySourceCalendar = sourceCalendarName;
      this.lastCopyEventsSourceStart = sourceStart;
      this.lastCopyEventsSourceEnd = sourceEnd;
      this.lastCopyTargetCalendar = targetCalendarName;
      this.lastCopyEventsTargetStart = targetStart;
      return true;
    }


    @Override
    public boolean copyEvent(String sourceCalendarName, LocalDateTime sourceStart,
                             String eventName,
                             String targetCalendarName,
                             LocalDateTime targetStart) {
      this.lastCopySourceCalendar = sourceCalendarName;
      this.lastCopyEventSourceStart = sourceStart;
      this.lastCopyEventName = eventName;
      this.lastCopyTargetCalendar = targetCalendarName;
      this.lastCopyEventTargetStart = targetStart;
      return true;
    }

    @Override
    public boolean isCalendarPresent(String calName) {
      this.lastUsedCalendarName = calName;
      this.lastCalendarPresentName = calName;
      return true;
    }

    @Override
    public boolean editCalendar(String calendarName, String property, String newValue) {
      this.lastEditCalendarName = calendarName;
      this.lastEditCalendarProperty = property;
      this.lastEditCalendarNewValue = newValue;
      return true;
    }

    @Override
    public boolean addEvents(String calendarName, List<ICalendarEventDTO> events, String timezone) {
      return false;
    }
  }

  private class MockView implements IView {
    private final List<String> displayedMessages = new ArrayList<>();

    @Override
    public void display(String message) {
      displayedMessages.add(message);
    }

    @Override
    public void start(ICommandExecutor commandExecutor) {

    }

    @Override
    public void stop() {

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


