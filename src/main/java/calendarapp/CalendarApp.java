package calendarapp;

import controller.ICalendarController;
import model.ICalendarModel;
import view.IView;


/**
 * Entry point for the Calendar application.
 */
public class CalendarApp {
  public static void main(String[] args) {
    ICalendarModel model = createModel();
    IView view = createView();
    ICalendarController controller = createController(model, view);
    try {
      ModeHandler handler = ModeHandler.fromArgs(args); // handles mode + file logic
      controller.run(handler.getReadable());  // pass just the input stream
    } catch (Exception e) {
      view.display(e.getMessage());
    }
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
