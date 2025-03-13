package controller;

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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import model.CalendarEventDTO;
import model.ICalendarModel;
import view.IView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class CalendarControllerTest {
  private ICalendarController controller;
  private TestCalendarModel model;
  private ByteArrayOutputStream outputStream;
  private PrintStream originalOut;
  private IView view;
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
    controller = new CalendarController(model,view);
    outputStream = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(outputStream));
  }

  private void resetModels(){
    model = new TestCalendarModel();
    view = new MockView();
    controller = new CalendarController(model,view);
    outputStream = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(outputStream));
  }

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
      } else if (mode.equalsIgnoreCase("headless")) {
      resetModels();
        File tempFile = File.createTempFile("commands", ".txt");
        try (FileWriter writer = new FileWriter(tempFile)) {
          writer.write(command + "\nexit\n");
        }
        controller.run("headless", tempFile.getAbsolutePath());
      }
    }
    catch (IOException e) {
      fail("IOException thrown: "+e.getMessage());
    }
  }

  @Test
  public void testCreateEventVariations() {
    String baseCommand = "create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T11:00";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(0,model.lastAddedEvent.getRecurrenceCount());
      assertTrue(model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
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
      if(command.contains("--autoDecline")) {
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
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(0,model.lastAddedEvent.getRecurrenceCount());
      assertTrue(model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
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
      if(command.contains("--autoDecline")) {
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
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(0,model.lastAddedEvent.getRecurrenceCount());
      assertTrue(model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
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
      if(command.contains("--autoDecline")) {
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
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(0,model.lastAddedEvent.getRecurrenceCount());
      assertTrue(model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
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
      if(command.contains("--autoDecline")) {
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
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(0,model.lastAddedEvent.getRecurrenceCount());
      assertTrue(model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertNull(model.lastAddedEvent.getRecurrenceEndDate());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());

      if (command.contains("-location")) {
        assertEquals("Off ice", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if(command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  //TODO tests for quotes processing
  //TODO tests for create event case sensitive tests
  //TODO tests fail for controller processing for headless mode


  //TODO tests for wrong MRU
  //TODO test for wong number
  //TODO tests forr timesssss or timmmesss

  //tests forr crrerate reccurring events repeates for times
  @Test
  public void testCreateRecurringEventForNTimes() {
    testCommandInBothModes(mode,"create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T11:00 repeats MRU for 5 times");
    assertNotNull(model.lastAddedEvent);
    assertTrue(model.lastAddedEvent.isRecurring());
    assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
    assertEquals(5, model.lastAddedEvent.getRecurrenceCount());
  }

  @Test
  public void testCreateRecurringEventForNTimesVariations() {
    String baseCommand = "create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T11:00 repeats MRU for 5 times";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
      assertEquals("TeamMeeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(5, model.lastAddedEvent.getRecurrenceCount());
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
      if(command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringEventForNTimesVariationsSpacedOptions() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 to 2024-03-20T11:00 repeats MRU for 5 times";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(5, model.lastAddedEvent.getRecurrenceCount());
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
      if(command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringEventForNTimesVariationsSpacedOptions1() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 to 2024-03-20T11:00 repeats MRU for 5 times";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(5, model.lastAddedEvent.getRecurrenceCount());
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
      if(command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringEventForNTimesVariationsSpacedOptions2() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 to 2024-03-20T11:00 repeats MRU for 5 times";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions2);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(5, model.lastAddedEvent.getRecurrenceCount());
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
      if(command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringEventForNTimesVariationsSpacedOptions3() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 to 2024-03-20T11:00 repeats MRU for 5 times";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions3);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(5, model.lastAddedEvent.getRecurrenceCount());
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
      if(command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  //TODO create a fuzzy test for N times

  //tests create a recurring date until a date
  @Test
  public void testCreateRecurringEventUntilDate() {
    testCommandInBothModes(mode,"create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T11:00 repeats MRU until 2024-04-20T10:00");
    assertNotNull(model.lastAddedEvent);
    assertTrue(model.lastAddedEvent.isRecurring());
    assertEquals(LocalDateTime.parse("2024-04-20T10:00"), model.lastAddedEvent.getRecurrenceEndDate());
  }

  @Test
  public void testCreateRecurringEventUntilDateVariations() {
    String baseCommand = "create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T11:00 repeats MRU until 2024-04-20T10:00";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
      assertEquals("TeamMeeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(0, model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T10:00"),model.lastAddedEvent.getRecurrenceEndDate());
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
      if(command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringEventUntilDateVariationsWithSpacedOptions() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 to 2024-03-20T11:00 repeats MRU until 2024-04-20T10:00";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(0, model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T10:00"),model.lastAddedEvent.getRecurrenceEndDate());
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
      if(command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringEventUntilDateVariationsWithSpacedOptions1() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 to 2024-03-20T11:00 repeats MRU until 2024-04-20T10:00";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(0, model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T10:00"),model.lastAddedEvent.getRecurrenceEndDate());
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
      if(command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringEventUntilDateVariationsWithSpacedOptions2() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 to 2024-03-20T11:00 repeats MRU until 2024-04-20T10:00";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions2);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(0, model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T10:00"),model.lastAddedEvent.getRecurrenceEndDate());
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
      if(command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringEventUntilDateVariationsWithSpacedOptions3() {
    String baseCommand = "create event 'Team Meeting' from 2024-03-20T10:00 to 2024-03-20T11:00 repeats MRU until 2024-04-20T10:00";
    List<String> commands = generateCommandCombinations(baseCommand, spacedOptions3);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(0, model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T10:00"),model.lastAddedEvent.getRecurrenceEndDate());
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
      if(command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }


  //TODO test for recurrence date before startDate
  //TODO Define how create events works in interface for differrent DTO

  //Tests for creating all day event

  @Test
  public void testCreateAllDayEvent() {
    testCommandInBothModes(mode,"create event TeamMeeting on 2024-03-20");
    assertNotNull(model.lastAddedEvent);
    assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastAddedEvent.getStartDateTime());
    assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastAddedEvent.getEndDateTime());
  }

  @Test
  public void testCreateAllDayEventVariations() {
    String baseCommand = "create event TeamMeeting on 2024-03-20";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastAddedEvent.getEndDateTime());
      assertEquals("TeamMeeting", model.lastAddedEvent.getEventName());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertTrue( model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertEquals(0, model.lastAddedEvent.getRecurrenceCount());
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
      if(command.contains("--autoDecline")) {
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
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertTrue( model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertEquals(0, model.lastAddedEvent.getRecurrenceCount());
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
      if(command.contains("--autoDecline")) {
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
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertTrue( model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertEquals(0, model.lastAddedEvent.getRecurrenceCount());
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
      if(command.contains("--autoDecline")) {
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
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertTrue( model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertEquals(0, model.lastAddedEvent.getRecurrenceCount());
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
      if(command.contains("--autoDecline")) {
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
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertFalse(model.lastAddedEvent.isRecurring());
      assertTrue( model.lastAddedEvent.getRecurrenceDays().isEmpty());
      assertEquals(0, model.lastAddedEvent.getRecurrenceCount());
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
      if(command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  //TODO Tests for event on datetime string for date string
  //TODO write testes to validate week of the day

  @Test
  public void testCreateRecurringAllDayEventForNTimes() {
    testCommandInBothModes(mode,"create event TeamMeeting on 2024-03-20 repeats MRU for 5 times");
    assertNotNull(model.lastAddedEvent);
    assertTrue(model.lastAddedEvent.isRecurring());
    assertEquals(5, model.lastAddedEvent.getRecurrenceCount());
  }

  @Test
  public void testCreateRecurringAllDayEventForNTimesVariations() {
    String baseCommand = "create event TeamMeeting on 2024-03-20 repeats MRU for 5 times";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastAddedEvent.getEndDateTime());
      assertEquals("TeamMeeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(5, model.lastAddedEvent.getRecurrenceCount());
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
      if(command.contains("--autoDecline")) {
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
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(5, model.lastAddedEvent.getRecurrenceCount());
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
      if(command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringAllDayEventForNTimesVariationsWithSpacedOptions1(){
    String baseCommand = "create event 'Team Meeting' on 2024-03-20 repeats MRU for 5 times";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(5, model.lastAddedEvent.getRecurrenceCount());
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
      if(command.contains("--autoDecline")) {
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
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(5, model.lastAddedEvent.getRecurrenceCount());
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
      if(command.contains("--autoDecline")) {
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
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(5, model.lastAddedEvent.getRecurrenceCount());
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
      if(command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringAllDayEventUntilDate() {
    testCommandInBothModes(mode,"create event TeamMeeting on 2024-03-20 repeats MRU until 2024-04-20");
    assertNotNull(model.lastAddedEvent);
    assertTrue(model.lastAddedEvent.isRecurring());
    assertEquals(LocalDateTime.parse("2024-04-20T23:59:59"), model.lastAddedEvent.getRecurrenceEndDate());
  }

  @Test
  public void testCreateRecurringAllDayEventForEventUntilDateVariations(){
    String baseCommand = "create event TeamMeeting on 2024-03-20 repeats MRU until 2024-04-20";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastAddedEvent.getEndDateTime());
      assertEquals("TeamMeeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(0, model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T23:59:59"), model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if(command.contains("--autoDecline")) {
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
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(0, model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T23:59:59"), model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Off ice", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterly meeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if(command.contains("--autoDecline")) {
        assertTrue(model.lastAddedEvent.isAutoDecline());
      }
    }
  }

  @Test
  public void testCreateRecurringAllDayEventForEventUntilDateVariationsWithSpacedOptions1(){
    String baseCommand = "create event 'Team Meeting' on 2024-03-20 repeats MRU until 2024-04-20";
    List<String> commands = generateCommandCombinations(baseCommand, options);
    for (String command : commands) {
      model.lastAddedEvent = null;
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(0, model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T23:59:59"), model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if(command.contains("--autoDecline")) {
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
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(0, model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T23:59:59"), model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterly meeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if(command.contains("--autoDecline")) {
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
      testCommandInBothModes(mode,command);
      assertNotNull(model.lastAddedEvent);
      assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastAddedEvent.getStartDateTime());
      assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastAddedEvent.getEndDateTime());
      assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
      assertTrue(model.lastAddedEvent.isRecurring());
      assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
      assertEquals(0, model.lastAddedEvent.getRecurrenceCount());
      assertEquals(LocalDateTime.parse("2024-04-20T23:59:59"), model.lastAddedEvent.getRecurrenceEndDate());

      if (command.contains("-location")) {
        assertEquals("Off ice", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterlymeeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
      }
      if(command.contains("--autoDecline")) {
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
    testCommandInBothModes(mode,"edit event name TeamMeeting from 2024-03-20T10:00 to 2024-03-20T10:30 with UpdatedMeeting");
    assertEquals("name", model.lastEditEventProperty);
    assertEquals("TeamMeeting", model.lastEditEventName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventStartDateTime);
    assertEquals(LocalDateTime.parse("2024-03-20T10:30"), model.lastEditEventEndDateTime);
    assertEquals("UpdatedMeeting", model.lastEditEventNewValue);
  }

  @Test
  public void testEditEventsCallsModelWithCorrectParams() {
    testCommandInBothModes(mode,"edit events name TeamMeeting from 2024-03-20T10:00 with UpdatedMeeting");
    assertEquals("name", model.lastEditEventsProperty);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventsStartDateTime);
    assertEquals("UpdatedMeeting", model.lastEditEventsNewValue);
  }

  @Test
  public void testPrintEventsOnDateCallsModelWithCorrectParams() {
    testCommandInBothModes(mode,"print events on 2024-03-20");
    assertEquals(LocalDate.parse("2024-03-20"), model.lastPrintDate);
  }

  @Test
  public void testExportCalendarCallsModelWithCorrectParams() {
    testCommandInBothModes(mode,"export cal events.csv");
    assertEquals("events.csv", model.lastExportFilename);
  }

  @Test
  public void testShowStatusCallsModelWithCorrectParams() {
    testCommandInBothModes(mode,"show status on 2024-03-20T10:30");
    assertEquals(LocalDateTime.parse("2024-03-20T10:30"), model.lastShowStatusDateTime);
  }

  @Test
  public void testPrintEventsInSpecificRangeCallsModelWithCorrectParams() {
    testCommandInBothModes(mode,"print events from 2024-03-20T10:00 to 2024-03-20T12:00");
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastPrintStartDateTime);
    assertEquals(LocalDateTime.parse("2024-03-20T12:00"), model.lastPrintEndDateTime);
  }

  @Test
  public void testInvalidCommandDoesNotCallModel() {
    testCommandInBothModes(mode,"invalid command format");
    assertNull(model.lastAddedEvent);
    assertNull(model.lastEditEventName);
    assertNull(model.lastEditEventsName);
  }

  private class TestCalendarModel implements ICalendarModel {
    CalendarEventDTO lastAddedEvent;
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
    public boolean addEvent(CalendarEventDTO event) {
      this.lastAddedEvent = event;
      return true;
    }

    @Override
    public boolean editEvent(String property, String eventName, LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue) {
      this.lastEditEventProperty = property;
      this.lastEditEventName = eventName;
      this.lastEditEventStartDateTime = fromDateTime;
      this.lastEditEventEndDateTime = toDateTime;
      this.lastEditEventNewValue = newValue;
      return true;
    }

    @Override
    public boolean editEvents(String property, String eventName, LocalDateTime fromDateTime, String newValue) {
      this.lastEditEventsProperty = property;
      this.lastEditEventsName = eventName;
      this.lastEditEventsStartDateTime = fromDateTime;
      this.lastEditEventsNewValue = newValue;
      return true;
    }

    @Override
    public String printEventsOnSpecificDate(LocalDate date) {
      this.lastPrintDate = date;
      return "Printed events.";
    }

    @Override
    public String printEventsInSpecificRange(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
      this.lastPrintStartDateTime = fromDateTime;
      this.lastPrintEndDateTime = toDateTime;
      return "Printed events in range.";
    }


    @Override
    public String exportEvents(String filename) {
      this.lastExportFilename = filename;
      return "Exported events.";
    }

    @Override
    public String showStatus(LocalDateTime dateTime) {
      this.lastShowStatusDateTime = dateTime;
      return "Status checked.";
    }
  }
  private class MockView implements IView {
    private final List<String> displayedMessages = new ArrayList<>();

    @Override
    public void display(String message) {
      displayedMessages.add(message);
    }

    public List<String> getDisplayedMessages() {
      return displayedMessages;
    }

    public void clearMessages() {
      displayedMessages.clear();
    }
  }
}


