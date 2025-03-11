package controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;

import model.CalendarEventDTO;
import model.ICalendarModel;

import static org.junit.Assert.*;

public class CalendarControllerTest {
  private CalendarController controller;
  private TestCalendarModel model;
  private ByteArrayOutputStream outputStream;
  private PrintStream originalOut;

  @Before
  public void setUp() {
    model = new TestCalendarModel();
    controller = new CalendarController(model);
    outputStream = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(outputStream));
  }

  @After
  public void restoreStreams() {
    System.setOut(originalOut);
  }

  @Test
  public void testCreateEventCallsModelWithCorrectParams() {
    controller.processCommand("create event TeamMeeting from 2024-03-20T10:00 to 2024-03-20T11:00");
    assertNotNull(model.lastAddedEvent);
    assertEquals("TeamMeeting", model.lastAddedEvent.getEventName());
    assertEquals(LocalDateTime.parse("2024-03-20T10:00"), model.lastAddedEvent.getStartDateTime());
    assertEquals(LocalDateTime.parse("2024-03-20T11:00"), model.lastAddedEvent.getEndDateTime());
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

  private static class TestCalendarModel implements ICalendarModel {
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
}
