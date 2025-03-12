package calendarApp;

import java.io.IOException;

import controller.ICalendarController;
import model.ICalendarModel;
import view.IView;

public class CalendarApp {
  public static void main(String[] args) throws IOException {
    if (args.length < 2 || !args[0].equalsIgnoreCase("--mode")) {
      System.out.println("Usage: java CalendarApp --mode <interactive|headless> [filePath]");
      return;
    }

    String mode = args[1];
    String filePath = (args.length > 2) ? args[2] : "";

    ICalendarModel model = ICalendarModel.createInstance("listBased");
    IView view = IView.createInstance("consoleView");
    ICalendarController controller = ICalendarController.createInstance(model, view);
    controller.run(mode, filePath);
  }
}