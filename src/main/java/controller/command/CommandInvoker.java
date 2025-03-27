package controller.command;

import model.ICalendarModel;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CommandInvoker is responsible for executing registered commands dynamically.
 * It maps command names to their respective command classes and instantiates them when needed.
 */
public class CommandInvoker {
  private final Map<String, Class<? extends ICommand>> commandRegistry = new HashMap<>();
  private String currentCalendar; // Stores the currently selected calendar

  public CommandInvoker(String currentCalendar) {
    this.currentCalendar = currentCalendar;
  }

  /**
   * Registers a new command with its corresponding class.
   *
   * @param commandName The name of the command (e.g., "create", "use", "copy").
   * @param commandClass The class that implements the command.
   */
  public void registerCommand(String commandName, Class<? extends ICommand> commandClass) {
    commandRegistry.put(commandName, commandClass);
  }

  public void deregisterCommand(String commandName) {
    commandRegistry.remove(commandName);
  }

  /**
   * Executes a registered command.
   *
   * @param commandName The command to execute.
   * @param parts The Scanner object containing the rest of the user input.
   * @param model The Calendar model instance.
   * @return The response message after executing the command.
   */
  public String executeCommand(String commandName, List<String> parts, ICalendarModel model) {
    Class<? extends ICommand> commandClass = commandRegistry.get(commandName);
    if (commandClass != null) {
      try {

        if((!(commandName.equalsIgnoreCase("use Calendar") || commandName.equalsIgnoreCase("create calendar") || commandName.equalsIgnoreCase("edit calendar"))) && Objects.isNull(currentCalendar)){
          throw new IllegalArgumentException("Please use somme calendar");
        }
        // Instantiate the command dynamically and pass parameters
        ICommand command = commandClass.getDeclaredConstructor(List.class, ICalendarModel.class, String.class)
                .newInstance(parts, model,currentCalendar);


        String result = command.execute();

        // If it's a "use" command, update the current calendar
        if (command instanceof UseCalendarCommand && !result.contains("Error")) {
          this.currentCalendar = ((UseCalendarCommand) command).getCalendarName();
        }

        return result;
      } catch (InvocationTargetException e) {
        Throwable cause = e.getCause();
        return "Error Executing command: " + (cause != null ? cause.getMessage() : e.getMessage());
      } catch (Exception e) {
        return "Error Executing command: " + e.getMessage();
      }
    }
    return "Error: Unknown command.";
  }

  /**
   * Gets the currently active calendar name.
   *
   * @return The current calendar name.
   */
  public String getCurrentCalendar() {
    return currentCalendar;
  }
}
