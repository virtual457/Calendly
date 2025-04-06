package controller;


// Command executor interface - restricted interface for views
public interface ICommandExecutor {
  /**
   * Executes a command programmatically.
   */
  void executeCommand(String command);
}