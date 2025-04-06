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

  public CommandExecutorAdaptor(ICalendarController controller) {
    this.controller = controller;
  }

  @Override
  public void executeCommand(String commandName) {
     controller.executeCommand(commandName);
  }
}