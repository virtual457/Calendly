package controller;

import controller.command.CommandInvoker;
import model.ICalendarModel;
import view.IView;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class AbstractController {

  protected List<String> tokenizeCommand(String input) {
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

  protected void runScanner(Scanner scanner, boolean displayMessage,
                            IView view, CommandInvoker invoker, ICalendarModel model) {
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine().trim();
      if (line.equalsIgnoreCase("exit")) {
        break;
      }

      List<String> tokens = tokenizeCommand(line);
      if (tokens.isEmpty()) {
        continue;
      }

      String action = tokens.get(0);
      String subAction = tokens.size() > 1 ? tokens.get(1) : "";
      String commandKey = (action + " " + subAction).toLowerCase();
      List<String> args = tokens.subList(2, tokens.size());

      String response = invoker.executeCommand(commandKey, args, model);
      if (displayMessage) {
        view.display(response);
      }
    }
  }
}
