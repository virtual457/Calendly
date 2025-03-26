package calendarapp;

import controller.ICalendarController;
import model.ICalendarModel;
import view.IView;


import java.io.IOException;

/**
 * Entry point for the Calendar application.
 */
public class CalendarApp {
  public static void main(String[] args) {
    try {
      ModeHandler handler = ModeHandler.fromArgs(args); // handles mode + file logic

      ICalendarModel model = createModel();
      IView view = createView();
      ICalendarController controller = createController(model, view);

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
    return ICalendarController.createInstance(model, view);
  }
}
