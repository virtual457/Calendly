package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import controller.command.CommandInvoker;
import controller.command.CopyEventCommand;
import controller.command.CopyEventsCommand;
import controller.command.CreateCalendarCommand;
import controller.command.CreateEventCommand;
import controller.command.EditCalendarCommand;
import controller.command.EditEventCommand;
import controller.command.EditEventsCalendarCommand;
import controller.command.ExportEventsCommand;
import controller.command.PrintEventsCommand;
import controller.command.ShowStatusCommand;
import controller.command.UseCalendarCommand;
import model.ICalendarModel;
import view.IView;

/**
 * A controller implementation for managing multiple calendars.
 * It interacts with the model and ensures commands are executed
 * within a selected calendar context.
 */
class CalendarController implements ICalendarController {
  private final ICalendarModel model;
  private final IView view;
  private final CommandInvoker invoker;

  public CalendarController(ICalendarModel model, IView view) {
    this.model = model;
    this.view = view;
    this.invoker = new CommandInvoker();

    // Register commands
    invoker.registerCommand("create calendar", CreateCalendarCommand.class);
    invoker.registerCommand("create event", CreateEventCommand.class);
    invoker.registerCommand("use calendar", UseCalendarCommand.class);
    invoker.registerCommand("copy event", CopyEventCommand.class);
    invoker.registerCommand("copy events", CopyEventsCommand.class);
    invoker.registerCommand("export cal", ExportEventsCommand.class);
    invoker.registerCommand("edit event", EditEventCommand.class);
    invoker.registerCommand("edit events", EditEventsCalendarCommand.class);
    invoker.registerCommand("show status", ShowStatusCommand.class);
    invoker.registerCommand("print events", PrintEventsCommand.class);
    invoker.registerCommand("edit calendar", EditCalendarCommand.class);
  }


  @Override
  public void run(Readable input) {
    try (Scanner scanner = new Scanner(input)) {
      view.display("Welcome to the Calendar App!");

      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();
        if (line.equalsIgnoreCase("exit")) break;

        List<String> tokens = tokenizeCommand(line);
        if (tokens.isEmpty()) continue;

        String action = tokens.get(0);
        String subAction = tokens.size() > 1 ? tokens.get(1) : "";
        String commandKey = (action + " " + subAction).toLowerCase();
        List<String> args = tokens.subList(2, tokens.size());

        String response = invoker.executeCommand(commandKey, args, model);
        view.display(response);
      }
    } catch (Exception e) {
      view.display("Error: " + e.getMessage());
    }
  }

  private List<String> tokenizeCommand(String input) {
    List<String> tokens = new ArrayList<>();
    Matcher matcher = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(input);
    while (matcher.find()) {
      if (matcher.group(1) != null) {
        tokens.add(matcher.group(1)); // Quoted
      } else {
        tokens.add(matcher.group(2)); // Unquoted
      }
    }
    return tokens;
  }
}
