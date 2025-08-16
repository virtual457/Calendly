package view;

import java.io.FileNotFoundException;
import controller.ICommandExecutor;
import model.IReadOnlyCalendarModel;

/**
 * Represents a generic view interface for the Calendar application.
 * <p>
 * Implementations of this interface handle user interaction,
 * including displaying data and collecting user input. They may
 * work in different environments (e.g., console, GUI) but must
 * adhere to the same core view responsibilities.
 * </p>
 */
public interface IView {

  /**
   * A factory method that creates a specific implementation of {@code IView}
   * based on the provided type string.
   * <p>
   * Delegates to the ViewFactory to create the appropriate view implementation.
   * </p>
   *
   * @param type a string that indicates which view implementation to create
   * @param args command-line arguments that may be needed for view creation
   * @param model the model that the view will use for displaying data
   * @return a new {@code IView} instance corresponding to the requested type
   * @throws IllegalArgumentException if the given {@code type} is invalid or unsupported
   * @throws FileNotFoundException if a file-based view cannot access its required file
   */
  static IView createInstance(String type, String[] args, IReadOnlyCalendarModel model)
        throws FileNotFoundException {
    return ViewFactory.createView(type, args, model);
  }

  /**
   * Displays a message to the user.
   *
   * @param message the message to display
   */
  void display(String message);

  /**
   * Starts the view's input/interaction loop.
   * This only has access to command execution.
   *
   * @param commandExecutor the executor to process commands
   */
  void start(ICommandExecutor commandExecutor);

  void stop();
}