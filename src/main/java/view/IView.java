package view;

import model.CalendarModel;
import model.ICalendarModel;

public interface IView {

  static IView createInstance(String type) {
    if (type.equalsIgnoreCase("consoleView")) {
      return new ConsoleView();
    }
    else{
      throw new IllegalArgumentException("Unknown view type: " + type);
    }
  }

  void display(String message);
}