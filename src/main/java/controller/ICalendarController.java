package controller;

import model.ICalendarModel;
import view.IView;

public interface ICalendarController {
  static ICalendarController createInstance(ICalendarModel model, IView view) {
    return new CalendarController(model, view);
  }

  void run(String mode, String fileName);
}
