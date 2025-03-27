package controller.command;

/**
 * ICommand is the interface for all commands in the Calendar application.
 * It defines the structure for executing commands and handling user input.
 */
public interface ICommand {
  /**
   * Executes the command logic.
   *
   * @return A message indicating the result of the command execution.
   */
  String execute();
}