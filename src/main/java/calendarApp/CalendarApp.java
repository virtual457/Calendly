package calendarApp;

import java.io.*;


import controller.ICalendarController;

import model.ICalendarModel;

import view.IMode;


/**
 * Main application entry point.
 */
public class CalendarApp {
  public static void main(String[] args) throws IOException {
    ICalendarModel model = ICalendarModel.createInstance("listBased");
    ICalendarController controller = ICalendarController.createInstance(model);

    if (args.length > 0 && args[0].equalsIgnoreCase("--mode")) {
      IMode mode;
      if (args[1].equalsIgnoreCase("interactive")) {
        mode = IMode.createInstance("interactive");
      } else if (args[1].equalsIgnoreCase("headless")) {
        mode = IMode.createInstance("headless");
      } else {
        System.out.println("Invalid mode. Use --mode interactive or --mode headless");
        return;
      }
      mode.run(controller);
    } else {
      System.out.println("Usage: java CalendarApp --mode <interactive|headless>");
    }
  }
}
