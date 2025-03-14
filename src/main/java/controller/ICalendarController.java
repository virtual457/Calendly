package controller;

import model.ICalendarModel;
import view.IView;

/**
 * Represents a controller interface for the Calendar application.
 * <p>
 * An implementing class of this interface coordinates the communication between
 * the calendar model (containing event data) and a view (for input/output).
 * It handles user commands and invokes the appropriate model methods to perform
 * actions such as adding events, editing events, printing, exporting, and more.
 * </p>
 */

public interface ICalendarController {
  static ICalendarController createInstance(ICalendarModel model, IView view) {
    return new CalendarController(model, view);
  }

  void run(String mode, String fileName);
}
