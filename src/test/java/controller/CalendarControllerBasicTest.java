package controller;


import model.ICalendarEventDTO;
import model.ICalendarModel;

import org.junit.Before;
import org.junit.Test;

import view.IView;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CalendarControllerBasicTest {

  private ICalendarModel model;
  private MockView view;
  private CalendarControllerBasic controller;

  @Before
  public void setUp() {
    model = new TestCalendarModelBasic();
    view = new MockView();
    controller = new CalendarControllerBasic(model, view);
  }

  @Test
  public void testRunWithValidInput() {
    String input = "create event Meeting from 2025-01-01T10:00 to 2025-01-01T10:30\n" +
          "edit event name Meeting from 2025-01-01T10:00 to 2025-01-01T11:00 with" +
          " \"Team Sync\"\n" +
          "print events on 2025-01-01\n" +
          "show status on 2025-01-01T10:30\n" +
          "export cal calendar.csv\n";

    controller.run(new StringReader(input));

    List<String> outputs = view.getMessages();
    assertFalse(outputs.isEmpty());
    assertTrue(outputs.get(0).contains("Welcome to the Calendar App!"));
    assertTrue(outputs.stream().anyMatch(msg -> msg.contains("Available")));
    assertTrue(outputs.stream().anyMatch(msg -> msg.toLowerCase().contains("exported")));
  }

  @Test
  public void testRunWithIValidInput() {
    String input = "create Meeting from 2025-01-01T10:00 to 2025-01-01T10:30\n";

    controller.run(new StringReader(input));

    List<String> outputs = view.getMessages();
    assertFalse(outputs.isEmpty());
    assertTrue(outputs.get(0).contains("Welcome to the Calendar App!"));
    assertTrue(outputs.stream().anyMatch(msg -> msg.contains("Error: Unknown command.")));
  }

  @Test
  public void testRunWithIValidInput2() {
    String input = "create event from 2025-01-01T10:00 to 2025-01-01T10:30\n";

    controller.run(new StringReader(input));

    List<String> outputs = view.getMessages();
    assertFalse(outputs.isEmpty());
    assertTrue(outputs.get(0).contains("Welcome to the Calendar App!"));
    assertTrue(outputs.stream().anyMatch(msg -> msg.contains("Error Executing command: Expected 'from' or 'on' after event name")));
  }

  @Test
  public void testRunWithIValidInput3() {
    String input = "create event from 2025-01-01T10:00 to 2025-01-01T10:30\n";

    controller.run(new StringReader(input));

    List<String> outputs = view.getMessages();
    assertFalse(outputs.isEmpty());
    assertTrue(outputs.get(0).contains("Welcome to the Calendar App!"));
    assertTrue(outputs.stream().anyMatch(msg -> !msg.contains("Please use somme calendar")));
  }


  @Test
  public void testRunWithValidInputForEdit() {
    String input =
          "edit event name Meeting from 2025-01-01T10:00 to 2025-01-01T11:00 with" +
                " \"Team Sync\"\n";

    controller.run(new StringReader(input));

    List<String> outputs = view.getMessages();
    assertFalse(outputs.isEmpty());
    assertTrue(outputs.get(0).contains("Welcome to the Calendar App!"));
    assertTrue(outputs.stream().anyMatch(msg -> msg.contains("Event(s) edited successfully.")));
  }

  @Test
  public void testRunWithValidInputForCreate() {
    String input = "create event Meeting from 2025-01-01T10:00 to 2025-01-01T10:30\n";

    controller.run(new StringReader(input));

    List<String> outputs = view.getMessages();
    assertFalse(outputs.isEmpty());
    assertTrue(outputs.get(0).contains("Welcome to the Calendar App!"));
    assertTrue(outputs.stream().anyMatch(msg -> msg.contains("Event created successfully.")));
  }

  @Test
  public void testRunInit() {
    String input = "create event Meeting from 2025-01-01T10:00 to 2025-01-01T10:30\n";

    controller.run(new StringReader(input));

    TestCalendarModelBasic tempModel = (TestCalendarModelBasic) this.model;
    assertEquals("Default", tempModel.getLastCreatedCalendar());
    assertEquals("Default", tempModel.getLastUsedCalendar());
  }

  @Test
  public void testRunPrintCommand() {
    String input = "print events on 2024-06-20\n";

    controller.run(new StringReader(input));
    List<String> outputs = view.getMessages();
    assertTrue(outputs.get(0).contains("Welcome to the Calendar App!"));
    assertTrue(outputs.stream().anyMatch(msg -> msg.contains("No events found.")));

  }




  @Test
  public void testRunWithValidInputForEditEvents() {
    String input = "edit events location Meeting from 2025-04-22T09:00 with \"Room B\"\n";

    controller.run(new StringReader(input));

    List<String> outputs = view.getMessages();
    assertFalse(outputs.isEmpty());
    assertTrue(outputs.get(0).contains("Welcome to the Calendar App!"));
    assertTrue(outputs.stream().anyMatch(msg -> msg.contains("Events updated successfully.")));
  }

  @Test
  public void testRunWithValidInputForBasicVersion() {
    String input = "create calendar --name default --timezone America/New_York\n";

    controller.run(new StringReader(input));

    List<String> outputs = view.getMessages();
    assertFalse(outputs.isEmpty());
    assertTrue(outputs.get(0).contains("Welcome to the Calendar App!"));
    assertTrue(outputs.stream().anyMatch(msg -> msg.contains("Error: Unknown command.")));
  }

  @Test
  public void testRunWithValidInputForBasicVersion1() {
    String input = "use calendar --name default\n";

    controller.run(new StringReader(input));

    List<String> outputs = view.getMessages();
    assertFalse(outputs.isEmpty());
    assertTrue(outputs.get(0).contains("Welcome to the Calendar App!"));
    assertTrue(outputs.stream().anyMatch(msg -> msg.contains("Error: Unknown command.")));
  }

  @Test
  public void testRunWithInvalidCommand() {
    String input = "invalid command here\n";
    controller.run(new StringReader(input));
    assertTrue(view.getMessages().stream().anyMatch(msg -> msg.contains("Unknown command")));
  }

  private static class TestCalendarModelBasic extends CalendarControllerTest.TestCalendarModel implements ICalendarModel {
    @Override
    public boolean addEvent(String calendarName, ICalendarEventDTO event) {
      if(!this.lastCreatedCalendarName.equals("Default")) {
        return false;
      }
      this.lastAddedEvent = event;
      return true;
    }

    public String getLastUsedCalendar(){
      return this.lastUsedCalendarName;
    }

    public String getLastCreatedCalendar(){
      return this.lastCreatedCalendarName;
    }
  }


  /**
   * Fake/mock view to capture messages for verification.
   */
  private static class MockView implements IView {
    private final List<String> messages = new ArrayList<>();

    @Override
    public void display(String message) {
      messages.add(message);
    }

    public List<String> getMessages() {
      return messages;
    }
  }


}
