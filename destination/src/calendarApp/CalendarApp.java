package calendarApp;

import java.io.IOException;

import controller.ICalendarController;
import model.ICalendarModel;
import view.IView;

/**
 * The main entry point for the Calendar application.
 * <p>
 * This class sets up the Calendar model and controller, and then runs the application
 * in either interactive or headless mode based on the user’s command line arguments.
 * </p>
 */

public class CalendarApp {

  /**
   * The main method and entry point for the Calendar application.
   * <p>
   * This method parses command line arguments to determine whether to run
   * the application in interactive or headless mode. It then instantiates the
   * model, controller, and appropriate mode (interactive or headless) and starts
   * the application flow.
   * </p>
   *
   * @param args command line arguments that specify the application mode
   * @throws IOException if there is an error reading or writing files
   */

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

  private static ICalendarModel createModel() {
    return ICalendarModel.createInstance("listBased");
  }

  private static IView createView() {
    return IView.createInstance("consoleView");
  }

  private static ICalendarController createController(ICalendarModel model, IView view) {
    return ICalendarController.createInstance(model, view);
  }
}