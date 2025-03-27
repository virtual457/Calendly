package controller;

import java.io.StringReader;

import java.util.Scanner;


import controller.command.BasicCreateEventCommand;
import controller.command.CommandInvoker;

import controller.command.CreateCalendarCommand;

import controller.command.EditEventCommand;
import controller.command.EditEventsCalendarCommand;
import controller.command.ExportEventsCommand;
import controller.command.PrintEventsCommand;
import controller.command.ShowStatusCommand;
import controller.command.UseCalendarCommand;
import model.ICalendarModel;
import view.IView;

/**
 * Basic controller which works for the basic version of the application
 * supportring create edit print show and export.
 */
public class CalendarControllerBasic extends AbstractController implements ICalendarController {
  private final ICalendarModel model;
  private final IView view;
  private final CommandInvoker invoker;

  public CalendarControllerBasic(ICalendarModel model, IView view) {
    this.model = model;
    this.view = view;

    this.invoker = new CommandInvoker("Default");

    // Register commands

    invoker.registerCommand("create event", BasicCreateEventCommand.class);
    invoker.registerCommand("export cal", ExportEventsCommand.class);
    invoker.registerCommand("edit event", EditEventCommand.class);
    invoker.registerCommand("edit events", EditEventsCalendarCommand.class);
    invoker.registerCommand("show status", ShowStatusCommand.class);
    invoker.registerCommand("print events", PrintEventsCommand.class);
  }


  @Override
  public void run(Readable input) {
    Scanner scanner = new Scanner(input);
      view.display("Welcome to the Calendar App!");
      initBasicMode();
      runScanner(scanner, true, view, invoker, model);

  }


  private void initBasicMode() {
    invoker.registerCommand("create calendar", CreateCalendarCommand.class);
    invoker.registerCommand("use calendar", UseCalendarCommand.class);
    String preLines =
        "create calendar --name Default --timezone America/New_York\n" +
            "use calendar --name Default\n";

    // Combine prepended lines with the original input
    Readable combinedInput = new StringReader(preLines);
    runScanner(new Scanner(combinedInput), false, view, invoker, model);
    invoker.deregisterCommand("use calendar");
    invoker.deregisterCommand("create calendar");
  }


}
