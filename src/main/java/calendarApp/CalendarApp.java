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

    ICalendarModel model = createModel();
    IView view = createView();
    ICalendarController controller = createController(model,view);
    controller.run(mode, filePath);
  }

  protected static ICalendarModel createModel() {
    return ICalendarModel.createInstance("listBased");
  }

  protected static IView createView() {
    return IView.createInstance("consoleView");
  }

  protected static ICalendarController createController(ICalendarModel model, IView view) {
    return ICalendarController.createInstance(model, view);
  }
}