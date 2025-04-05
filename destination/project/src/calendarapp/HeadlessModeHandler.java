package calendarapp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Handles the headless mode of the calendar application.
 * <p>
 * This class processes a sequence of pre-defined commands without user interaction,
 * typically for automated testing or batch processing scenarios.
 * </p>
 */

public class HeadlessModeHandler implements ModeHandler {
  private final String filePath;

  public HeadlessModeHandler(String filePath) {
    this.filePath = filePath;
  }

  @Override
  public Readable getReadable() throws IOException {

    BufferedReader reader = new BufferedReader(new FileReader(filePath));

    String lastLine = null;
    String line;
    while ((line = reader.readLine()) != null) {
      lastLine = line;
    }

    if (lastLine == null || !lastLine.trim().equalsIgnoreCase("exit")) {
      throw new IllegalStateException("File must end with 'exit' in headless mode.");
    }


    return new BufferedReader(new FileReader(filePath));

  }
}
