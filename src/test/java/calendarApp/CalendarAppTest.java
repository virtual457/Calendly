package calendarApp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import static org.junit.Assert.assertTrue;

public class CalendarAppTest {
  private ByteArrayOutputStream outputStream;
  private PrintStream out;

  @Before
  public void setUp() {
    out = System.out;
    outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));
  }

  @After
  public void setDown() {
    System.setOut(out);
  }

  @Test
  public void testInteractiveModeExecution() throws IOException {
    String simulatedUserInput = "exit\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(simulatedUserInput.getBytes());
    System.setIn(inputStream);

    String[] args = {"--mode", "interactive"};
    CalendarApp.main(args);
    String output = outputStream.toString();
    assertTrue(output.contains("Welcome to the Calendar App!"));
  }

}
