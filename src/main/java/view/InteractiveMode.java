package view;

import java.util.Scanner;

import controller.ICalendarController;

class InteractiveMode implements IMode {
  public void run(ICalendarController controller) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Welcome to the Calendar App!");
    while (true) {
      System.out.print("Enter command: ");
      String command = scanner.nextLine();
      if (command.equals("exit")) {
        break;
      }
      controller.processCommand(command);
    }
    scanner.close();
  }
}

