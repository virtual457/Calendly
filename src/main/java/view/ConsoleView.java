package view;

/**
 * A console-based view implementation for the Calendar application.
 * <p>
 * This class handles input and output using standard streams
 * (e.g., System.in and System.out). It displays information to
 * the user and passes user commands back to the controller.
 * Implementations of this view typically prompt for commands,
 * display results, and show error messages when needed.
 * </p>
 */

public class ConsoleView implements IView {
  @Override
  public void display(String message) {
    System.out.println(message == null ? "" : message);
  }
}