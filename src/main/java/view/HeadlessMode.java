package view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import controller.ICalendarController;

class HeadlessMode implements IMode {
  public void run(ICalendarController controller) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader("commands.txt"));
    String command;
    while ((command = reader.readLine()) != null) {
      if (command.equals("exit")) {
        break;
      }
      controller.processCommand(command);
    }
    reader.close();
  }
}
