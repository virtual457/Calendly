package calendarapp;

import controller.ICalendarController;
import model.ICalendarModel;
import view.IView;


/**
 * Entry point for the Calendar application.
 */
public class CalendarApp {

  /**
   * The main entry point of the calendar application.
   *
   * @param args the command-line arguments used to
   *             determine the mode of execution (interactive or headless).
   */

  public static void main(String[] args) {

    try {
      ICalendarModel model = createModel();
      IView view = createView(args);
      ICalendarController controller = createController(model, view);
      controller.start();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private static ICalendarModel createModel() {
    return ICalendarModel.createInstance("listBased");
  }

  private static IView createView(String[] args) {
    return IView.createInstance(parseViewType(args), args);
  }

  private static ICalendarController createController(ICalendarModel model, IView view) {
    return ICalendarController.createInstance("Advanced", model, view);
  }

  private static String parseViewType(String[] args) {
    if (args.length < 2 || !args[0].equalsIgnoreCase("--mode")) {
      throw new IllegalArgumentException(
            "Usage: --mode <interactive|headless|gui> [filePath]");
    }

    String mode = args[1].toLowerCase();
    if (mode.equals("interactive") || mode.equals("headless") || mode.equals("gui")) {
      return mode;
    } else {
      throw new IllegalArgumentException("Unsupported mode: " + mode);
    }
  }
}
