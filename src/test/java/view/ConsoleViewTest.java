package view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    assertNotNull(outContent.toString()); // Ensures output is not null
    assertTrue(outContent.toString().trim().isEmpty()); // Ensures null doesn't print unexpected text
  }
}