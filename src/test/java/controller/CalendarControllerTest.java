package controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.CalendarEventDTO;
import model.ICalendarModel;
import view.IView;

import static org.junit.Assert.*;

public class CalendarControllerTest {
  private CalendarController controller;
  private TestCalendarModel model;
  private ByteArrayOutputStream outputStream;
  private PrintStream originalOut;
  private IView view;

  @Before
  public void setUp() {
    model = new TestCalendarModel();
    view = new MockView();
    controller = new CalendarController(model,view);
    outputStream = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(outputStream));
  }

  @After
  public void restoreStreams() {
    System.setOut(originalOut);
  }

  @Test
  public void testCreateEventWithLocationDescriptionPrivate() {
    controller.processCommand("create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T11:00 -location Office -description Quarterly -private");
    assertNotNull(model.lastAddedEvent);
    assertEquals("TeamMeeting", model.lastAddedEvent.getEventName());
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
    assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
    assertEquals("Office", model.lastAddedEvent.getEventLocation());
    assertEquals("Quarterly", model.lastAddedEvent.getEventDescription());
    assertTrue(model.lastAddedEvent.isPrivate());
  }

  @Test
  public void testCreateEventWithMultiLocationDescriptionPrivate() {
    controller.processCommand("create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T11:00 -location 'Office 227' -description Quarterly -private");
    assertNotNull(model.lastAddedEvent);
    assertEquals("TeamMeeting", model.lastAddedEvent.getEventName());
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
    assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
    assertEquals("Office 227", model.lastAddedEvent.getEventLocation());
    assertEquals("Quarterly", model.lastAddedEvent.getEventDescription());
    assertTrue(model.lastAddedEvent.isPrivate());
  }

  @Test
  public void testCreateEventWithLocationMultiDescriptionPrivate() {
    controller.processCommand("create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T11:00 -location Office -description 'Quarterly meeting' -private");
    assertNotNull(model.lastAddedEvent);
    assertEquals("TeamMeeting", model.lastAddedEvent.getEventName());
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
    assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
    assertEquals("Office", model.lastAddedEvent.getEventLocation());
    assertEquals("Quarterly meeting", model.lastAddedEvent.getEventDescription());
    assertTrue(model.lastAddedEvent.isPrivate());
  }

  @Test
  public void testCreateEventWithLocationDescriptionPrivateWithMultiEventName() {
    controller.processCommand("create event 'Team Meeting' from 2024-03-20T10:00 to 2024-03-20T11:00 -location Office -description 'Quarterly meeting' -private");
    assertNotNull(model.lastAddedEvent);
    assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
    assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
    assertEquals("Office", model.lastAddedEvent.getEventLocation());
    assertEquals("Quarterly meeting", model.lastAddedEvent.getEventDescription());
    assertTrue(model.lastAddedEvent.isPrivate());
  }

  @Test
  public void testCreateEventWithoutLocationDescriptionPrivate() {
    controller.processCommand("create event 'Team Meeting' from 2024-03-20T10:00 to 2024-03-20T11:00");
    assertNotNull(model.lastAddedEvent);
    assertEquals("Team Meeting", model.lastAddedEvent.getEventName());
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
    assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
    assertEquals("", model.lastAddedEvent.getEventLocation());
    assertEquals("", model.lastAddedEvent.getEventDescription());
    assertNull(model.lastAddedEvent.isPrivate());
  }

  @Test
  public void testCreateSingleEvent() {
    controller.processCommand("create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T11:00");
    assertNotNull(model.lastAddedEvent);
    assertEquals("TeamMeeting", model.lastAddedEvent.getEventName());
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
    assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
  }

  @Test
  public void testCreateRecurringEventForNTimes() {
    controller.processCommand("create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T11:00 repeats MRU for 5 times");
    assertNotNull(model.lastAddedEvent);
    assertTrue(model.lastAddedEvent.isRecurring());
    assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY), model.lastAddedEvent.getRecurrenceDays());
    assertEquals(5, model.lastAddedEvent.getRecurrenceCount());
  }

  @Test
  public void testCreateRecurringEventUntilDate() {
    controller.processCommand("create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T11:00 repeats MRU until 2024-04-20T10:00");
    assertNotNull(model.lastAddedEvent);
    assertTrue(model.lastAddedEvent.isRecurring());
    assertEquals(LocalDateTime.parse("2024-04-20T10:00"), model.lastAddedEvent.getRecurrenceEndDate());
  }

  @Test
  public void testCreateAllDayEvent() {
    controller.processCommand("create event TeamMeeting on 2024-03-20");
    assertNotNull(model.lastAddedEvent);
    assertEquals(LocalDateTime.parse("2024-03-20T00:00"), model.lastAddedEvent.getStartDateTime());
    assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), model.lastAddedEvent.getEndDateTime());
  }

  @Test
  public void testCreateRecurringAllDayEventForNTimes() {
    var result = controller.processCommand("create event TeamMeeting on 2024-03-20 repeats MRU for 5 times");
    assertNotNull(model.lastAddedEvent);
    assertTrue(model.lastAddedEvent.isRecurring());
    assertEquals(5, model.lastAddedEvent.getRecurrenceCount());
  }

  @Test
  public void testCreateRecurringAllDayEventUntilDate() {
    var result = controller.processCommand("create event TeamMeeting on 2024-03-20 repeats MRU until 2024-04-20");
    assertNotNull(model.lastAddedEvent);
    assertTrue(model.lastAddedEvent.isRecurring());
    assertEquals(LocalDateTime.parse("2024-04-20T00:00"), model.lastAddedEvent.getRecurrenceEndDate());
  }

  @Test
  public void testCreateEventVariations() {
    String baseCommand = "create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T11:00";
    String[] options = {"-location Office", "-description 'Quarterly meeting'", "-private"};

    List<String> commands = generateCommandCombinations(baseCommand, options);

    for (String command : commands) {
      model.lastAddedEvent = null;
      controller.processCommand(command);
      assertNotNull(model.lastAddedEvent);
      assertEquals("TeamMeeting", model.lastAddedEvent.getEventName());

      if (command.contains("-location")) {
        assertEquals("Office", model.lastAddedEvent.getEventLocation());
      }
      if (command.contains("-description")) {
        assertEquals("Quarterly meeting", model.lastAddedEvent.getEventDescription());
      }
      if (command.contains("-private")) {
        assertTrue(model.lastAddedEvent.isPrivate());
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
    }
    return commands;
  }

  @Test
  public void testEditEventCallsModelWithCorrectParams() {
    controller.processCommand("edit event name TeamMeeting from 2024-03-20T10:00 to 2024-03-20T10:30 with UpdatedMeeting");
    assertEquals("name", model.lastEditEventProperty);
    assertEquals("TeamMeeting", model.lastEditEventName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventStartDateTime);
    assertEquals(LocalDateTime.parse("2024-03-20T10:30"), model.lastEditEventEndDateTime);
    assertEquals("UpdatedMeeting", model.lastEditEventNewValue);
  }

  @Test
  public void testEditEventsCallsModelWithCorrectParams() {
    controller.processCommand("edit events name TeamMeeting from 2024-03-20T10:00 with UpdatedMeeting");
    assertEquals("name", model.lastEditEventsProperty);
    assertEquals("TeamMeeting", model.lastEditEventsName);
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastEditEventsStartDateTime);
    assertEquals("UpdatedMeeting", model.lastEditEventsNewValue);
  }

  @Test
  public void testPrintEventsOnDateCallsModelWithCorrectParams() {
    controller.processCommand("print events on 2024-03-20");
    assertEquals(LocalDate.parse("2024-03-20"), model.lastPrintDate);
  }

  @Test
  public void testExportCalendarCallsModelWithCorrectParams() {
    controller.processCommand("export cal events.csv");
    assertEquals("events.csv", model.lastExportFilename);
  }

  @Test
  public void testShowStatusCallsModelWithCorrectParams() {
    controller.processCommand("show status on 2024-03-20T10:30");
    assertEquals(LocalDateTime.parse("2024-03-20T10:30"), model.lastShowStatusDateTime);
  }

  @Test
  public void testPrintEventsInSpecificRangeCallsModelWithCorrectParams() {
    controller.processCommand("print events from 2024-03-20T10:00 to 2024-03-20T12:00");
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastPrintStartDateTime);
    assertEquals(LocalDateTime.parse("2024-03-20T12:00"), model.lastPrintEndDateTime);
  }

  @Test
  public void testInvalidCommandDoesNotCallModel() {
    controller.processCommand("invalid command format");
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


