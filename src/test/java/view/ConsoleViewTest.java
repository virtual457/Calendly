package view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import model.IReadOnlyCalendarModel;
import model.ReadOnlyCalendarModel;

/**
 * Unit tests for the ConsoleView class.
 * Verifies console interactions such as displaying messages,
 * reading user input, and formatting outputs.
 */

public class ConsoleViewTest {
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
  public void testDisplay() {
    InteractiveConsoleView consoleView = new InteractiveConsoleView();
    String message = "Hello, World!";
    consoleView.display(message);
    assertEquals(message + System.lineSeparator(), outContent.toString());
  }

  @Test
  public void testDisplayEmptyMessage() {
    InteractiveConsoleView consoleView = new InteractiveConsoleView();
    String message = "";
    consoleView.display(message);
    assertEquals(System.lineSeparator(), outContent.toString());
  }

  @Test
  public void testDisplayNullMessage() {
    InteractiveConsoleView consoleView = new InteractiveConsoleView();
    consoleView.display(null);
    assertNotNull(outContent.toString());
    assertTrue(outContent.toString().trim().isEmpty());
  }

  /*
  @Test
  public void testCreateInstance_ValidType() {
    String[] args = {"--mode", "interactive"};
    IView view =IView.createInstance("interactive", args);

    assertNotNull(view);

    assertEquals("class view.InteractiveConsoleView", view.getClass().toString());
  }

  @Test
  public void testCreateInstance_InvalidType() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      String[] args = {"--mode", "interactive"};
      IView.createInstance("invalidView", args);
    });

    String expectedMessage = "Unknown view type: invalidView";
    assertEquals(expectedMessage, exception.getMessage());
  }
  */

}