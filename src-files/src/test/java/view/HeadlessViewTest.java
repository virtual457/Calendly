package view;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import controller.ICalendarCommandAdapter;
import controller.ICommandExecutor;

public class HeadlessViewTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private TestCommandExecutor commandExecutor;
  private HeadlessConsoleView view;
  private File validScriptFile;
  private File invalidScriptFile;
  private File emptyScriptFile;
  private PrintStream originalOut;
  private ByteArrayOutputStream outContent;
  private SecurityManager originalSecurityManager;

  private static class TestCommandExecutor implements ICommandExecutor {
    private List<String> executedCommands = new ArrayList<>();
    private boolean exitCalled = false;

    @Override
    public void executeCommand(String command) {
      executedCommands.add(command);
      if (command.equals("exit")) {
        exitCalled = true;
      }
    }

    @Override
    public ICalendarCommandAdapter getCommandAdapter() {
      return null;
    }

    public List<String> getExecutedCommands() {
      return executedCommands;
    }

    public boolean wasExitCalled() {
      return exitCalled;
    }
  }

  private static class NoExitSecurityManager extends SecurityManager {
    @Override
    public void checkExit(int status) {
      throw new SecurityException("System.exit called with status: " + status);
    }

    @Override
    public void checkPermission(java.security.Permission perm) {
    }
  }

  @Before
  public void setUp() throws IOException {
    validScriptFile = tempFolder.newFile("validScript.txt");
    try (FileWriter writer = new FileWriter(validScriptFile)) {
      writer.write("command1\ncommand2\nexit");
    }

    invalidScriptFile = tempFolder.newFile("invalidScript.txt");
    try (FileWriter writer = new FileWriter(invalidScriptFile)) {
      writer.write("command1\ncommand2\nnotExit");
    }

    emptyScriptFile = tempFolder.newFile("emptyScript.txt");

    commandExecutor = new TestCommandExecutor();

    originalOut = System.out;
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    originalSecurityManager = System.getSecurityManager();
    System.setSecurityManager(new NoExitSecurityManager());
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    System.clearProperty("run_mode");
    System.setSecurityManager(originalSecurityManager);
  }

  @Test
  public void testValidateFileSuccess() {
    view = new HeadlessConsoleView(validScriptFile.getAbsolutePath());
    assertNotNull(view);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateFileFailureNoExit() {
    view = new HeadlessConsoleView(invalidScriptFile.getAbsolutePath());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateFileFailureEmpty() {
    view = new HeadlessConsoleView(emptyScriptFile.getAbsolutePath());
  }

  @Test
  public void testDisplayNormalMessage() {
    view = new HeadlessConsoleView(validScriptFile.getAbsolutePath());
    view.display("Normal message");

    assertEquals("Normal message" + System.lineSeparator(), outContent.toString());
  }

  @Test(expected = SecurityException.class)
  public void testDisplayErrorMessageWithRunModeFalse() {
    System.setProperty("run_mode", "false");

    view = new HeadlessConsoleView(validScriptFile.getAbsolutePath());
    view.start(commandExecutor);

    view.display("Error: Something went wrong");

    assertTrue(commandExecutor.wasExitCalled());
  }

  @Test
  public void testDisplayErrorMessageWithRunModeTrue() {
    System.setProperty("run_mode", "true");

    view = new HeadlessConsoleView(validScriptFile.getAbsolutePath());
    view.start(commandExecutor);

    view.display("Error: Something went wrong");

    assertEquals("Error: Something went wrong" + System.lineSeparator(), outContent.toString());
  }

  @Test
  public void testStartExecutesAllCommands() {
    view = new HeadlessConsoleView(validScriptFile.getAbsolutePath());
    view.start(commandExecutor);

    List<String> executedCommands = commandExecutor.getExecutedCommands();
    assertEquals(3, executedCommands.size());
    assertEquals("command1", executedCommands.get(0));
    assertEquals("command2", executedCommands.get(1));
    assertEquals("exit", executedCommands.get(2));
  }

  @Test
  public void testStartSkipsEmptyLines() throws IOException {
    File fileWithEmptyLines = tempFolder.newFile("emptyLines.txt");
    try (FileWriter writer = new FileWriter(fileWithEmptyLines)) {
      writer.write("\ncommand1\n\n  \ncommand2\nexit");
    }

    view = new HeadlessConsoleView(fileWithEmptyLines.getAbsolutePath());
    view.start(commandExecutor);

    List<String> executedCommands = commandExecutor.getExecutedCommands();
    assertEquals(3, executedCommands.size());
    assertEquals("command1", executedCommands.get(0));
    assertEquals("command2", executedCommands.get(1));
    assertEquals("exit", executedCommands.get(2));
  }


  @Test
  public void testStop() {
    view = new HeadlessConsoleView(validScriptFile.getAbsolutePath());
    view.stop();

    assertEquals("Good Night..Sayonara" + System.lineSeparator(), outContent.toString());
  }
}