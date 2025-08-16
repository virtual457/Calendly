package view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


import controller.ICommandExecutor;

public class HeadlessConsoleView implements IView {
  private final String filePath;
  private ICommandExecutor commandExecutor =null;

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
    if(message.toLowerCase().contains("error") && System.getProperty("run_mode").equalsIgnoreCase(
          "false")) {
      commandExecutor.executeCommand("exit");
      System.exit(0);
    }
  }

  @Override
  public void start(ICommandExecutor commandExecutor) {
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      this.commandExecutor =commandExecutor;
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) {
          continue;
        }

        // Parse the command
        commandExecutor.executeCommand(line);
      }
    } catch (IOException e) {
      display("Error: " + e.getMessage());
    }
  }

  @Override
  public void stop() {
    System.out.println("Good Night..Sayonara");
  }
}