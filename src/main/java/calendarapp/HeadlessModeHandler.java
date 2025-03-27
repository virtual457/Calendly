package calendarapp;

import java.io.*;

public class HeadlessModeHandler implements ModeHandler {
  private final String filePath;

  public HeadlessModeHandler(String filePath) {
    this.filePath = filePath;
  }

  @Override
  public Readable getReadable() throws IOException {

    BufferedReader reader = new BufferedReader(new FileReader(filePath));

    // Optional: check that file ends with "exit"
    String lastLine = null;
    String line;
    while ((line = reader.readLine()) != null) {
      lastLine = line;
    }

    if (lastLine == null || !lastLine.trim().equalsIgnoreCase("exit")) {
      throw new IllegalStateException("File must end with 'exit' in headless mode.");
    }

    // reopen reader for actual use
    return new BufferedReader(new FileReader(filePath));

  }
}
