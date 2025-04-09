package view;

import java.io.FileNotFoundException;
import model.IReadOnlyCalendarModel;

/**
 * Factory class responsible for creating different view implementations.
 * Follows the Factory Method pattern to encapsulate view instantiation logic.
 */
public class ViewFactory {

  /**
   * Creates a view instance based on the specified type.
   *
   * @param type the type of view to create (interactive, headless, gui)
   * @param args command-line arguments that may be required for view creation
   * @param model the read-only model to be used by the view
   * @return an appropriate IView implementation
   * @throws FileNotFoundException if a file-based view cannot find its required file
   * @throws IllegalArgumentException if the view type is unsupported or arguments are invalid
   */
  public static IView createView(String type, String[] args, IReadOnlyCalendarModel model)
        throws FileNotFoundException {

    if (type == null) {
      throw new IllegalArgumentException("View type cannot be null");
    }

    switch (type.toLowerCase()) {
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
}