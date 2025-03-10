package controller;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import java.io.*;
import java.time.*;
import java.util.*;
import model.*;
import controller.*;

public class CalendarControllerTest {
  private CalendarController controller;
  private ICalendarModel model;
  private ByteArrayOutputStream outputStream;
  private PrintStream originalOut;

  @Before
  public void setUp() {
    model = mock(ICalendarModel.class); // Using Mockito to mock model behavior
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
    verify(model, times(1)).addEvent(argThat(event ->
            event.getEventName().equals("TeamMeeting") &&
                    event.getStartDateTime().equals(LocalDateTime.parse("2024-03-20T10:00")) &&
                    event.getEndDateTime().equals(LocalDateTime.parse("2024-03-20T11:00"))
    ));
  }

  @Test
  public void testEditEventCallsModelWithCorrectParams() {
    controller.processCommand("edit event name TeamMeeting from 2024-03-20T10:00 with UpdatedMeeting");
    verify(model, times(1)).editEvents(eq("name"), eq("TeamMeeting"), isNull(), eq("UpdatedMeeting"));
  }

  @Test
  public void testPrintEventsOnDateCallsModelWithCorrectParams() {
    controller.processCommand("print events on 2024-03-20");
    verify(model, times(1)).printEventsOnSpecificDate(eq(LocalDate.parse("2024-03-20")));
  }

  @Test
  public void testExportCalendarCallsModelWithCorrectParams() {
    controller.processCommand("export cal events.csv");
    verify(model, times(1)).exportEvents(eq("events.csv"));
  }

  @Test
  public void testShowStatusCallsModelWithCorrectParams() {
    controller.processCommand("show status on 2024-03-20T10:30");
    verify(model, times(1)).showStatus(eq(LocalDateTime.parse("2024-03-20T10:30")));
  }

  @Test
  public void testInvalidCommandDoesNotCallModel() {
    controller.processCommand("invalid command format");
    verifyNoInteractions(model);
  }
}
