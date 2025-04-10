package view;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Scanner;

import controller.ICommandExecutor;

public class InteractiveConsoleView implements IView {
  private final Readable readable;
  public InteractiveConsoleView() {

    readable = new InputStreamReader(System.in);
  }

  @Override
  public void display(String message) {
    if(!Objects.isNull(message)) {
      System.out.println(message);
    }
  }

  @Override
  public void start(ICommandExecutor commandExecutor) {
    try (Scanner scanner = new Scanner(readable)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();
        commandExecutor.executeCommand(line);

      }
    }
  }

  @Override
  public void stop() {
    System.out.println("Good Night..Sayonara");
  }
}


