package controller;

import model.ICalendarModel;

public interface ICalendarController {
  static ICalendarController createInstance(ICalendarModel model) {
    return new CalendarController(model);
  }
  void processCommand(String command);
}
