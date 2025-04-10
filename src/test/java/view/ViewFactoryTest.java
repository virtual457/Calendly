package view;

import model.IReadOnlyCalendarModel;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.*;

public class ViewFactoryTest {

  private IReadOnlyCalendarModel mockModel;
  private String[] validHeadlessArgs;
  private String[] insufficientHeadlessArgs;

  @Before
  public void setUp() {
    // Create a simple manual mock of IReadOnlyCalendarModel
    mockModel = new TestReadOnlyCalendarModel();

    validHeadlessArgs = new String[]{"--mode", "headless", "commands.txt"};
    insufficientHeadlessArgs = new String[]{"--mode", "headless"};
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