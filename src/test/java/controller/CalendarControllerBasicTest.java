package controller;


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
    model = new CalendarControllerTest.TestCalendarModel();
    view = new MockView();
    controller = new CalendarControllerBasic(model, view);
  }

  @Test
  public void testRunWithValidInput() {
    String input =
        // The initBasicMode will already create and use the "Default" calendar
        "create event Meeting from 2025-01-01T10:00 to 2025-01-01T10:30\n" +
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
  public void testRunWithInvalidCommand() {
    String input = "invalid command here\n";
    controller.run(new StringReader(input));
    assertTrue(view.getMessages().stream().anyMatch(msg -> msg.contains("Unknown command")));
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
