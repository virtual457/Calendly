package calendarapp;

import java.io.InputStreamReader;

/**
 * Implements the interactive mode for the calendar application.
 * <p>
 * This handler allows users to interact with the calendar system through
 * the console using typed commands. It continuously reads input, processes
 * commands, and provides feedback until the user exits.
 * </p>
 */

public class InteractiveModeHandler implements ModeHandler {
  @Override
  public Readable getReadable() {
    return new InputStreamReader(System.in);
  }
}
