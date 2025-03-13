package calendarApp;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import controller.ICalendarController;
import model.ICalendarEventDTO;
import model.ICalendarModel;
import view.IView;
/*
public class CalendarAppTest {
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  @Before
  public void setUpStreams() {
    System.setOut(new PrintStream(outContent));
  }

  @After
  public void restoreStreams() {
    System.setOut(originalOut);
  }

  @Test
  public void testInvalidArguments() throws IOException {
    CalendarApp.main(new String[]{});
    assertTrue(outContent.toString().contains("Usage: java CalendarApp --mode <interactive|headless> [filePath]"));
  }

  @Test
  public void testValidInteractiveMode() throws IOException {
    ICalendarModel model = MockCalendarModel.createInstance("listBased");
    IView view = MockView.createInstance("consoleView");
    ICalendarController controller = MockCalendarController.createInstance(model, view);

    CalendarApp.main(new String[]{"--mode", "interactive"});
    assertTrue(outContent.toString().contains("Running in interactive mode"));
  }

  @Test
  public void testValidHeadlessMode() throws IOException {
    ICalendarModel model = MockCalendarModel.createInstance("listBased");
    IView view = MockView.createInstance("consoleView");
    ICalendarController controller = MockCalendarController.createInstance(model, view);

    CalendarApp.main(new String[]{"--mode", "headless", "testfile.txt"});
    var a = outContent.toString();
    assertTrue(outContent.toString().contains("Running in headless mode with file: testfile.txt"));
  }
}



class MockCalendarModel implements ICalendarModel {
  public static ICalendarModel createInstance(String type) {
    return new MockCalendarModel();
  }

  @Override
  public boolean addEvent(ICalendarEventDTO event) {
    return false;
  }

  @Override
  public boolean editEvent(String property, String eventName, LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue) {
    return false;
  }

  @Override
  public boolean editEvents(String property, String eventName, LocalDateTime fromDateTime, String newValue) {
    return false;
  }

  @Override
  public String printEventsOnSpecificDate(LocalDate date) {
    return "";
  }

  @Override
  public String printEventsInSpecificRange(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
    return "";
  }

  @Override
  public String exportEvents(String filename) {
    return "";
  }

  @Override
  public String showStatus(LocalDateTime dateTime) {
    return "";
  }
}

class MockView implements IView {
  public static IView createInstance(String type) {
    return new MockView();
  }

  @Override
  public void display(String message) {
    System.out.println(message);
  }
}

class MockCalendarController implements ICalendarController {
  private final ICalendarModel model;
  private final IView view;

  public MockCalendarController(ICalendarModel model, IView view) {
    this.model = model;
    this.view = view;
  }

  public static ICalendarController createInstance(ICalendarModel model, IView view) {
    return new MockCalendarController(model, view);
  }

  @Override
  public void run(String mode, String filePath) {
    if ("interactive".equalsIgnoreCase(mode)) {
      view.display("Running in interactive mode (mocked, no infinite loop) with file: " + filePath);
      return;
    }
    if ("headless".equalsIgnoreCase(mode)) {
      view.display("Running in headless mode (mocked, no infinite loop) with file: " + filePath);
      return;
    }
    view.display("Running in " + mode + " mode with file: " + filePath);
  }
}
*/