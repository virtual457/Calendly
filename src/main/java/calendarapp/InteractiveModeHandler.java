package calendarapp;

import java.io.InputStreamReader;

public class InteractiveModeHandler implements ModeHandler {
  @Override
  public Readable getReadable() {
    return new InputStreamReader(System.in);
  }
}
