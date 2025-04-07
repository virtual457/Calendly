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

public interface ICalendarController extends ICommandExecutor{

  /**
   * Creates an instance of {@code ICalendarController} based on the specified version.
   *
   * @param version the version of the controller to instantiate (e.g., "basic", "interactive")
   * @param model   the calendar model to associate with the controller
   * @return an instance of {@code ICalendarController} corresponding to the given version
   * @throws IllegalArgumentException if the version is unsupported or null
   */

  static ICalendarController createInstance(String version, ICalendarModel model,
                                            IView view) {
    if (version.equalsIgnoreCase("basic")) {
      return new CalendarControllerBasic(model, view);
    } else if (version.equalsIgnoreCase("advanced")) {
      return new CalendarController(model, view);
    } else {
      throw new IllegalArgumentException("Unsupported version: " + version);
    }
  }

  /**
   * Starts the controller.
   */
  void start(Readable readable);
}
