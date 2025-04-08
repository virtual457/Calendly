package controller;


import java.util.Scanner;


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
class CalendarController extends AbstractController implements ICalendarController {
  private final ICalendarModel model;
  private final IView view;
  private final CommandInvoker invoker;

  public CalendarController(ICalendarModel model, IView view) {
    this.model = model;
    this.view = view;
    this.invoker = new CommandInvoker(null);

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
  public void start(Readable readable) {
    // Display welcome message
    view.display("Welcome to the Calendar App!");

    // Create default calendar if needed
    //initializeDefaultCalendar();

    // Start the view - it will handle its own input processing
    // We pass "this" as an ICommandExecutor (restricted interface)
    view.start(new CommandExecutorAdaptor(this), readable);

  }


  @Override
  public void executeCommand(String command) {
    System.out.println(command);
    runScanner(new Scanner(command), true, view, invoker, model);
  }


}
