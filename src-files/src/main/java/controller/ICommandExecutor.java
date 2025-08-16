package controller;

/**
 * Interface for executing commands programmatically.
 */
public interface ICommandExecutor {
  /**
   * Executes a command programmatically.
   */
  void executeCommand(String command);

  /**
   * Gets a calendar command adapter for higher-level operations.
   * @return An adapter that provides object-oriented access to calendar operations
   */
  ICalendarCommandAdapter getCommandAdapter();
}