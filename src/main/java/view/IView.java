package view;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Objects;

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
   * If the given {@code type} corresponds to a known view implementation (such as
   * "console" or "gui"), this method returns an appropriate {@code IView} instance.
   * Otherwise, it may throw an exception or return a default implementation.
   * </p>
   *
   * @param type a string that indicates which view implementation to create
   * @return a new {@code IView} instance corresponding to the requested type
   * @throws IllegalArgumentException if the given {@code type} is invalid or unsupported
   */

  static IView createInstance(String type, String[] args, IReadOnlyCalendarModel model) throws FileNotFoundException {
    switch (type) {
      case "interactive":
        return new InteractiveConsoleView();
      case "headless":
        if (args.length < 3) {
          throw new IllegalArgumentException("Missing filepath for headless mode");
        }
        return new HeadlessConsoleView(args[2]);
      case "gui":
        return new GuiView(model);
      default:
        throw new IllegalArgumentException("Unknown view type: " + type);
    }
  }
  /**
   * Displays a message to the user.
   */
  void display(String message);

  /**
   * Starts the view's input/interaction loop.
   * This only has access to command execution.
   */
  void start(ICommandExecutor commandExecutor);
}