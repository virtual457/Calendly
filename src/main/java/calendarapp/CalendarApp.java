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
      IView view = createView();
      ICalendarController controller = createController(model, view);

      ModeHandler handler = ModeHandler.fromArgs(args); // handles mode + file logic
      controller.run(handler.getReadable());  // pass just the input stream
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private static ICalendarModel createModel() {
    return ICalendarModel.createInstance("listBased");
  }

  private static IView createView() {
    return IView.createInstance("consoleView");
  }

  private static ICalendarController createController(ICalendarModel model, IView view) {
    return ICalendarController.createInstance("Advanced", model, view);
  }
}
