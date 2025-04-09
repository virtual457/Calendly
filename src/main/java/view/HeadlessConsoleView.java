package view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


import controller.ICommandExecutor;

public class HeadlessConsoleView implements IView {
  private final String filePath;

  public HeadlessConsoleView(String filePath) {
    this.filePath = filePath;
    validateFile(filePath);
  }

  private void validateFile(String filePath) {
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      // Check that the file ends with "exit"
      String lastLine = null;
      String line;

      while ((line = reader.readLine()) != null) {
        if (!line.trim().isEmpty()) {
          lastLine = line.trim();
        }
      }

      if (lastLine == null || !lastLine.equalsIgnoreCase("exit")) {
        throw new IllegalArgumentException("File must end with 'exit' in headless mode.");
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Error reading file: " + e.getMessage());
    }
  }

  @Override
  public void display(String message) {
    System.out.println(message);
    if(message.toLowerCase().contains("error")) {
      //System.exit(1);
    }
  }

  @Override
  public void start(ICommandExecutor commandExecutor) {
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) {
          continue;
        }

        if (line.equalsIgnoreCase("exit")) {
          break;
        }

        // Parse the command
        commandExecutor.executeCommand(line);
      }
    } catch (IOException e) {
      display("Error: " + e.getMessage());
    }
  }
}