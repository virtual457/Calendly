package calendarapp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents a handler for different operational modes of the calendar application.
 * Implementations of this interface define how commands are processed in a given mode
 * (e.g., interactive, batch).
 */


public interface ModeHandler {
  Readable getReadable() throws IOException;

  /**
   * Creates and returns the appropriate {@link ModeHandler} implementation
   * based on the provided command-line arguments.
   *
   * @param args the command-line arguments passed to the application
   * @return the corresponding ModeHandler for the selected mode
   * @throws IllegalArgumentException if the arguments are invalid or unsupported
   */

  static ModeHandler fromArgs(String[] args) {
    if (args.length < 2 || !args[0].equalsIgnoreCase("--mode")) {
      throw new IllegalArgumentException("Usage: --mode <interactive|headless> [filePath]");
    }

    String mode = args[1];
    Function<String[], ModeHandler> constructor = Registry.modeMap.get(mode.toLowerCase());

    if (constructor == null) {
      throw new IllegalArgumentException("Unsupported mode: " + mode);
    }

    return constructor.apply(args);
  }

  /**
   * The {@code Registry} class serves as a centralized location for storing and retrieving
   * components or services used across the application. It supports dependency injection
   * and lookup of shared instances like the controller, model, or view.
   */


  class Registry {
    static final Map<String, Function<String[], ModeHandler>> modeMap = new HashMap<>();

    static {
      modeMap.put("interactive", args -> new InteractiveModeHandler());
      modeMap.put("headless", args -> {
        if (args.length < 3) {
          throw new IllegalArgumentException("Missing filepath for headless mode.");
        }
        return new HeadlessModeHandler(args[2]);
      });
    }
  }
}
