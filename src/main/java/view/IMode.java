package view;

import java.io.IOException;

import controller.ICalendarController;


public interface IMode {
  static IMode createInstance(String type) {
    if (type.equalsIgnoreCase("interactive")) {
      return new InteractiveMode();
    } else if (type.equalsIgnoreCase("headless")) {
      return new HeadlessMode();
    } else {
      throw new IllegalArgumentException("Invalid Mode type.");
    }
  }
  void run(ICalendarController controller) throws IOException;
}
