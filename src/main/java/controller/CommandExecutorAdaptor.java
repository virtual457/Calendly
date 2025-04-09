package controller;

import java.util.List;

import model.ICalendarModel;
import view.IView;

/**
 * CommandExecutorAdapter - adapts an ICalendarController to the ICommandExecutor interface.
 * This is an example of the object adapter pattern.
 */
public class CommandExecutorAdaptor implements ICommandExecutor {
  private final ICalendarController controller;
  private ICalendarCommandAdapter commandAdapter;

  public CommandExecutorAdaptor(ICalendarController controller) {
    this.controller = controller;
  }

  @Override
  public void executeCommand(String commandName) {
     controller.executeCommand(commandName);
  }

  @Override
  public ICalendarCommandAdapter getCommandAdapter() {
    // Lazy initialization of the adapter Done when and only when required
    if (commandAdapter == null) {
      commandAdapter = new ObjectToCommandAdapter(this);
    }
    return commandAdapter;
  }
}