package calendarapp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public interface ModeHandler {
  Readable getReadable() throws IOException;

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

  // Static inner class to hold the mode map
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
