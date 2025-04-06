package view;

import java.util.Objects;
import java.util.Scanner;

import controller.ICommandExecutor;

public class InteractiveConsoleView implements IView {
  @Override
  public void display(String message) {
    if(!Objects.isNull(message)) {
      System.out.println(message);
    }
  }

  @Override
  public void start(ICommandExecutor commandExecutor) {
    try (Scanner scanner = new Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();
        if (line.equalsIgnoreCase("exit")) {
          break;
        }
        commandExecutor.executeCommand(line);

      }
    }
  }
}


