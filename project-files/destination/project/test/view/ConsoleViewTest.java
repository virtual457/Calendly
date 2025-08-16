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
    ConsoleView consoleView = new ConsoleView();
    String message = "Hello, World!";
    consoleView.display(message);
    assertEquals(message + System.lineSeparator(), outContent.toString());
  }

  @Test
  public void testDisplayEmptyMessage() {
    ConsoleView consoleView = new ConsoleView();
    String message = "";
    consoleView.display(message);
    assertEquals(System.lineSeparator(), outContent.toString());
  }

  @Test
  public void testDisplayNullMessage() {
    ConsoleView consoleView = new ConsoleView();
    consoleView.display(null);
    assertNotNull(outContent.toString());
    assertTrue(outContent.toString().trim().isEmpty());
  }

  @Test
  public void testCreateInstance_ValidType() {
    IView view = IView.createInstance("consoleView");

    assertNotNull(view);

    assertEquals("class view.ConsoleView", view.getClass().toString());
  }

  @Test
  public void testCreateInstance_InvalidType() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      IView.createInstance("invalidView");
    });

    String expectedMessage = "Unknown view type: invalidView";
    assertEquals(expectedMessage, exception.getMessage());
  }


}